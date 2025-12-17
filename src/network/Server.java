package network;

import foundation.Deletable;
import foundation.MainPanel;
import level.Level;
import level.tile.Tile;
import level.tile.TileData;
import render.anim.unit.AnimTilePath;
import unit.UnitData;
import unit.Unit;
import unit.UnitTeam;
import unit.action.Action;
import unit.type.UnitType;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Server implements Deletable {
    public static final int TCP_PORT = 37001;
    public Level level;
    public ConcurrentHashMap<UnitTeam, Integer> teamClientIDs = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    public ConcurrentHashMap<InetAddress, ClientHandler> clientsByAddress = new ConcurrentHashMap<>();
    private final ServerSocket serverSocket;

    public Server(Level level) {
        teamClientIDs.put(UnitTeam.ORDERED_TEAMS[0], 0);
        this.level = level;
        try {
            serverSocket = new ServerSocket(TCP_PORT);
            if (MainPanel.CREATE_SERVER_AND_CLIENT_CONNECTIONS) {
                Socket socket = new Socket("127.0.0.1", TCP_PORT);
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            try {
                while (true) {
                    int id = clientIDCounter++;
                    ClientHandler client = new ClientHandler(serverSocket.accept(), this, id).start();
                    clients.put(id, client);
                    clientsByAddress.put(client.socket.getInetAddress(), client);
                }
            } catch (IOException _) {
            }
        }).start();
    }

    public void closeClient(ClientHandler client) {
        client.closeClient();
        removeClient(client);
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client.clientID);
        clientsByAddress.remove(client.socket.getInetAddress());
        for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
            if (teamClientIDs.containsKey(team) && teamClientIDs.get(team) == client.clientID)
                teamClientIDs.remove(team);
        }
        client.delete();
        sendTeamsAvailablePacket();
    }

    public void sendTeamsAvailablePacket() {
        clients.forEach((_, c) -> c.queueTeamsAvailablePacket());
    }

    public void sendTurnUpdatePacket() {
        clients.forEach((_, c) -> c.queueTurnUpdatePacket(true));
        sendEnergyUpdatePacket();
    }

    public void sendUnitUpdatePacket() {
        clients.forEach((_, c) -> c.queueUnitUpdatePacket());
    }

    public void sendTileTypePacket(Tile tile) {
        TileData data = tile.getTileData();
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.TILE_TYPE_UPDATE, w -> {
            PacketWriter.writePoint(tile.pos, w);
            data.write(w);
        })));
    }

    public void sendUnitMovePacket(AnimTilePath path, Point illegalTile, Point pos, Unit unit) {
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_MOVE_UNIT, w -> {
            path.write(w);
            w.writeBoolean(illegalTile != null);
            if (illegalTile != null) {
                PacketWriter.writePoint(illegalTile, w);
            }
            PacketWriter.writePoint(pos, w);
            unit.data.type.write(w);
            PacketWriter.writeEnum(unit.data.team, w);
        })));
    }

    public void sendUnitShootPacket(Unit from, Unit to) {
        UnitData fromData = from.data.copy();
        UnitData toData = to.data.copy();
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_SHOOT_UNIT, w -> {
            fromData.write(w);
            toData.write(w);
        })));
    }

    public void sendUnitMinePacket(Unit unit) {
        UnitData data = unit.data.copy();
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_MINE, data::write)));
    }

    public void sendUnitRepairPacket(Unit from, Unit to) {
        UnitData fromData = from.data.copy();
        UnitData toData = to.data.copy();
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_REPAIR, w -> {
            fromData.write(w);
            toData.write(w);
        })));
    }

    public void sendStructureRepairPacket(Unit u, float amount) {
        UnitData data = u.data.copy();
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_STRUCTURE_REPAIR, w -> {
            data.write(w);
            w.writeFloat(amount);
        })));
    }

    public void sendUnitResupplyPacket(Unit from, Unit to) {
        UnitData fromData = from.data.copy();
        UnitData toData = to.data.copy();
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_RESUPPLY, w -> {
            fromData.write(w);
            toData.write(w);
        })));
    }

    public void sendStructureResupplyPacket(Unit u) {
        UnitData data = u.data.copy();
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_STRUCTURE_RESUPPLY, data::write)));
    }

    public void sendUnitCapturePacket(Unit unit, boolean action) {
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_CAPTURE_UNIT, w -> {
            PacketWriter.writePoint(unit.data.pos, w);
            w.writeBoolean(action);
            w.writeInt(unit.getCaptureProgress());
        })));
    }

    public void sendEnergyUpdatePacket() {
        clients.forEach((_, c) -> c.queueEnergyUpdatePacket());
    }

    public void sendUnitShieldRegenPacket(Unit unit) {
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_SHIELD_REGEN, w -> {
            PacketWriter.writePoint(unit.data.pos, w);
            w.writeFloat(unit.data.shieldRenderHP);
        })));
    }

    public void sendUnitStealthPacket(Unit unit) {
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_STEALTH_UNIT, w -> {
            PacketWriter.writePoint(unit.data.pos, w);
            w.writeBoolean(unit.data.stealthMode);
        })));
    }

    public void sendStructurePacket(Tile tile, boolean cameraTo) {
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_STRUCTURE_UPDATE, w -> {
            PacketWriter.writePoint(tile.pos, w);
            boolean hasStructure = tile.hasStructure();
            w.writeBoolean(hasStructure);
            w.writeBoolean(cameraTo);
            if (hasStructure) {
                tile.structure.write(w);
            }
        })));
    }

    public void sendStructureDestroy(Tile tile, boolean cameraTo) {
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_DESTROY_STRUCTURE, w -> {
            PacketWriter.writePoint(tile.pos, w);
            w.writeBoolean(cameraTo);
        })));
    }

    public void sendBotSelectTile(Point pos) {
        clients.forEach((_, c) -> c.queuePacket(new PacketWriter(PacketType.BOT_SELECT_TILE, w -> {
            PacketWriter.writePoint(pos, w);
        })));
    }

    public UnitTeam getClientTeam(int clientID) {
        for (Map.Entry<UnitTeam, Integer> entry : teamClientIDs.entrySet()) {
            if (entry.getValue() == clientID) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static int clientIDCounter = 1;

    @Override
    public void delete() {
        level = null;
        HashSet<ClientHandler> clientHandlers = new HashSet<>(clients.values());
        clientHandlers.forEach(ClientHandler::closeClient);
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ClientHandler implements Deletable {
        public final Socket socket;
        private final DataInputStream reader;
        private final DataOutputStream writer;
        private final Vector<PacketWriter> packetQueue = new Vector<>();
        public Server server;

        public final int clientID;

        public ClientHandler(Socket socket, Server server, int clientID) {
            this.socket = socket;
            this.server = server;
            this.clientID = clientID;
            try {
                socket.setSoTimeout(10000);
                reader = new DataInputStream(socket.getInputStream());
                writer = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            queuePacket(new PacketWriter(PacketType.CLIENT_INIT, w -> {
                w.writeInt(clientID);
                w.writeInt(server.level.initialPlayerCount());
            }));
            queueTeamsAvailablePacket();
            queueTurnUpdatePacket(false);
        }

        public void queueTeamsAvailablePacket() {
            queuePacket(new PacketWriter(PacketType.TEAMS_AVAILABLE, w -> {
                PacketWriter.writeMap(server.teamClientIDs, k -> PacketWriter.writeEnum(k, w), w::writeInt, w);
                HashMap<UnitTeam, Boolean> bots = new HashMap<>();
                server.level.teamData.forEach((team, data) -> {
                    bots.put(team, data.bot);
                });
                PacketWriter.writeMap(bots, k -> PacketWriter.writeEnum(k, w), w::writeBoolean, w);
            }));
        }

        public void queueTurnUpdatePacket(boolean updateUnits) {
            queuePacket(new PacketWriter(PacketType.LEVEL_TURN_UPDATE, w -> {
                w.writeInt(server.level.getTurn());
                PacketWriter.writeEnum(server.level.getActiveTeam(), w);
            }));
            if (updateUnits)
                queueUnitUpdatePacket();
        }

        public void queueUnitUpdatePacket() {
            queuePacket(new PacketWriter(PacketType.UNIT_UPDATE, w -> {
                PacketWriter.writeCollection(server.level.unitSet, u -> u.data.write(w), w);
            }));
            queueEnergyUpdatePacket();
        }

        public void queueEnergyUpdatePacket() {
            queuePacket(new PacketWriter(PacketType.ENERGY_UPDATE, w -> {
                server.level.levelRenderer.energyManager.write(w);
            }));
        }

        public void queueTeamDataPacket() {
            queuePacket(new PacketWriter(PacketType.TEAM_DATA_UPDATE, this::writeTeamData));
        }

        private void writeTeamData(DataOutputStream w) throws IOException {
            PacketWriter.writeMap(server.level.teamData, k -> PacketWriter.writeEnum(k, w), v -> v.write(w), w);
        }

        public synchronized void queuePacket(PacketWriter packet) {
            packetQueue.add(packet);
        }

        public ClientHandler start() {
            new Thread(this::runReader).start();
            new Thread(this::runWriter).start();
            return this;
        }

        public void readStream(DataInputStream stream) throws IOException {
            switch (PacketType.values()[stream.readInt()]) {
                case PING -> {
                    queuePacket(new PacketWriter(PacketType.PING, w -> {
                    }));
                }
                case CLIENT_REQUEST_LEVEL_DATA -> {
                    queueTurnUpdatePacket(true);
                    MainPanel.addTaskAfterAnimBlock(() -> queueTurnUpdatePacket(true));
                }
                case CLIENT_END_TURN -> {
                    UnitTeam endTeam = server.getClientTeam(clientID);
                    MainPanel.addTask((() -> {
                        if (server.level.getActiveTeam() == endTeam) {
                            server.level.preEndTurn();
                        }
                    }));
                }
                case JOIN_REQUEST -> {
                    UnitTeam requestedTeam = PacketReceiver.readEnum(UnitTeam.class, reader);
                    MainPanel.addTask(() -> {
                        if (server.teamClientIDs.containsKey(requestedTeam) || server.level.teamData.get(requestedTeam).bot) {
                            queueTeamsAvailablePacket();
                        } else {
                            server.teamClientIDs.put(requestedTeam, clientID);
                            server.sendTeamsAvailablePacket();
                            queuePacket(new PacketWriter(PacketType.JOIN_REQUEST_ACCEPTED, w -> {
                                writeTeamData(w);
                                w.writeLong(server.level.seed);
                                w.writeFloat(server.level.botDifficulty);
                                w.writeInt(server.level.tilesX);
                                w.writeInt(server.level.tilesY);
                                server.level.gameplaySettings.write(w);
                                PacketWriter.writeEnum(requestedTeam, w);
                                for (int x = 0; x < server.level.tilesX; x++) {
                                    for (int y = 0; y < server.level.tilesY; y++) {
                                        server.level.getTile(x, y).write(w);
                                    }
                                }
                            }));
                        }
                    });
                }
                case CLIENT_REQUEST_MOVE_UNIT -> {
                    AnimTilePath path = new AnimTilePath(reader);
                    boolean hasIllegalTile = reader.readBoolean();
                    Point illegalTile;
                    if (hasIllegalTile) {
                        illegalTile = PacketReceiver.readPoint(reader);
                    } else {
                        illegalTile = null;
                    }
                    Point unitPos = PacketReceiver.readPoint(reader);
                    UnitType type = UnitType.read(reader);
                    UnitTeam team = PacketReceiver.readEnum(UnitTeam.class, reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit unit = server.level.getUnit(unitPos);
                        if (unit == null || unit.data.type != type || unit.data.team != team || unit.data.hasPerformedAction(Action.MOVE) || !server.level.levelRenderer.energyManager.canAfford(unit.data.team, path.getEnergyCost(unit, server.level), true)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        server.sendUnitMovePacket(path, illegalTile, unit.data.pos, unit);
                        unit.startMove(path, illegalTile);
                    });
                }
                case CLIENT_REQUEST_SHOOT_UNIT -> {
                    Point from = PacketReceiver.readPoint(reader), to = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit fromUnit = server.level.getUnit(from), toUnit = server.level.getUnit(to);
                        if (fromUnit == null || toUnit == null || server.teamClientIDs.get(fromUnit.data.team) != clientID || fromUnit.data.hasPerformedAction(Action.FIRE) || !server.level.levelRenderer.energyManager.canAfford(fromUnit, Action.FIRE, true)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        fromUnit.clientAttack(toUnit);
                    });
                }
                case CLIENT_REQUEST_MINE -> {
                    Point pos = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit u = server.level.getUnit(pos);
                        if (u == null || u.data.hasPerformedAction(Action.MINE)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        u.performMiningAction();
                    });
                }
                case CLIENT_REQUEST_REPAIR -> {
                    Point from = PacketReceiver.readPoint(reader), to = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit fromUnit = server.level.getUnit(from), toUnit = server.level.getUnit(to);
                        if (fromUnit == null || toUnit == null || server.teamClientIDs.get(fromUnit.data.team) != clientID || fromUnit.data.hasPerformedAction(Action.REPAIR) || !server.level.levelRenderer.energyManager.canAfford(fromUnit, Action.REPAIR, true)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        fromUnit.repair(toUnit);
                    });
                }
                case CLIENT_REQUEST_RESUPPLY -> {
                    Point from = PacketReceiver.readPoint(reader), to = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit fromUnit = server.level.getUnit(from), toUnit = server.level.getUnit(to);
                        if (fromUnit == null || toUnit == null || server.teamClientIDs.get(fromUnit.data.team) != clientID || fromUnit.data.hasPerformedAction(Action.RESUPPLY) || !server.level.levelRenderer.energyManager.canAfford(fromUnit, Action.RESUPPLY, true)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        fromUnit.resupply(toUnit);
                    });
                }
                case CLIENT_REQUEST_CAPTURE -> {
                    Point pos = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit u = server.level.getUnit(pos);
                        if (u == null || u.data.hasPerformedAction(Action.CAPTURE) || !u.canCapture() || !server.level.levelRenderer.energyManager.canAfford(u, Action.CAPTURE, true)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        u.captureAction();
                        server.sendUnitCapturePacket(u, true);
                    });
                }
                case CLIENT_REQUEST_SHIELD_REGEN -> {
                    Point pos = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit u = server.level.getUnit(pos);
                        if (u == null || u.data.hasPerformedAction(Action.SHIELD_REGEN) || !server.level.levelRenderer.energyManager.canAfford(u, Action.SHIELD_REGEN, true)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        u.regenShield();
                    });
                }
                case CLIENT_REQUEST_STEALTH -> {
                    Point pos = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit u = server.level.getUnit(pos);
                        if (u == null || u.data.hasPerformedAction(Action.STEALTH) || !server.level.levelRenderer.energyManager.canAfford(u, Action.STEALTH, true)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        u.data.addPerformedAction(Action.STEALTH);
                        u.setStealthMode(!u.data.stealthMode);
                        server.sendUnitStealthPacket(u);
                    });
                }
            }
        }

        public void runReader() {
            while (!closed) {
                try {
                    readStream(reader);
                } catch (EOFException | SocketException e) {
                    break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            server.closeClient(this);
        }

        public void runWriter() {
            try {
                while (!closed) {
                    TimeUnit.MILLISECONDS.sleep(5);
                    synchronized (this) {
                        packetQueue.forEach(p -> {
                            try {
                                PacketWriter.writeEnum(p.type(), writer);
                                p.writer().accept(writer);
                            } catch (SocketException e) {
                                closeClient();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        packetQueue.clear();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean closed = false;

        public void closeClient() {
            closed = true;
            try {
                socket.close();
            } catch (IOException _) {
            }
        }

        @Override
        public void delete() {
            server = null;
        }
    }
}

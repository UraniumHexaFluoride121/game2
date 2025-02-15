package network;

import foundation.Deletable;
import foundation.MainPanel;
import level.Level;
import level.tile.Tile;
import render.anim.AnimTilePath;
import unit.Unit;
import unit.UnitData;
import unit.UnitTeam;
import unit.type.UnitType;
import unit.action.Action;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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

    public Server(Level level) {
        teamClientIDs.put(UnitTeam.ORDERED_TEAMS[0], 0);
        this.level = level;
        ServerSocket serverSocket;
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
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        clients.forEach((id, c) -> c.queueTeamsAvailablePacket());
    }

    public void sendTurnUpdatePacket() {
        clients.forEach((id, c) -> c.queueTurnUpdatePacket());
    }

    public void sendUnitMovePacket(AnimTilePath path, Point illegalTile, Unit unit) {
        clients.forEach((id, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_MOVE_UNIT, w -> {
            path.write(w);
            w.writeBoolean(illegalTile != null);
            if (illegalTile != null) {
                PacketWriter.writePoint(illegalTile, w);
            }
            PacketWriter.writePoint(unit.pos, w);
            unit.type.write(w);
            PacketWriter.writeEnum(unit.team, w);
        })));
    }

    public void sendUnitShootPacket(Unit from, Unit to) {
        clients.forEach((id, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_SHOOT_UNIT, w -> {
            new UnitData(from).write(w);
            new UnitData(to).write(w);
        })));
    }

    public void sendUnitCapturePacket(Unit unit) {
        clients.forEach((id, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_CAPTURE_UNIT, w -> {
            PacketWriter.writePoint(unit.pos, w);
            w.writeInt(unit.getCaptureProgress());
        })));
    }

    public void sendUnitShieldRegenPacket(Unit unit) {
        clients.forEach((id, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_SHIELD_REGEN, w -> {
            PacketWriter.writePoint(unit.pos, w);
            w.writeFloat(unit.shieldHP);
        })));
    }

    public void sendStructurePacket(Tile tile) {
        clients.forEach((id, c) -> c.queuePacket(new PacketWriter(PacketType.SERVER_STRUCTURE_UPDATE, w -> {
            PacketWriter.writePoint(tile.pos, w);
            boolean hasStructure = tile.hasStructure();
            w.writeBoolean(hasStructure);
            if (hasStructure) {
                tile.structure.write(w);
            }
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
                w.writeInt(server.level.playerCount());
            }));
            queueTeamsAvailablePacket();
            queueTurnUpdatePacket();
        }

        public void queueTeamsAvailablePacket() {
            queuePacket(new PacketWriter(PacketType.TEAMS_AVAILABLE, w -> {
                PacketWriter.writeMap(server.teamClientIDs, k -> PacketWriter.writeEnum(k, w), w::writeInt, w);
            }));
        }

        public void queueTurnUpdatePacket() {
            queuePacket(new PacketWriter(PacketType.LEVEL_TURN_UPDATE, w -> {
                w.writeInt(server.level.getTurn());
                PacketWriter.writeEnum(server.level.getActiveTeam(), w);
            }));
        }

        public void queueUnitUpdatePacket() {
            queuePacket(new PacketWriter(PacketType.UNIT_UPDATE, w -> {
                PacketWriter.writeCollection(server.level.unitSet, u -> new UnitData(u).write(w), w);
            }));
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
                    queueTurnUpdatePacket();
                    queueUnitUpdatePacket();
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        queueTurnUpdatePacket();
                        queueUnitUpdatePacket();
                    });
                }
                case CLIENT_END_TURN -> {
                    UnitTeam endTeam = server.getClientTeam(clientID);
                    MainPanel.addTask((() -> {
                        if (server.level.getActiveTeam() == endTeam) {
                            server.level.endTurn();
                        }
                    }));
                }
                case JOIN_REQUEST -> {
                    UnitTeam requestedTeam = PacketReceiver.readEnum(UnitTeam.class, reader);
                    MainPanel.addTask(() -> {
                        if (server.teamClientIDs.containsKey(requestedTeam)) {
                            queueTeamsAvailablePacket();
                        } else {
                            server.teamClientIDs.put(requestedTeam, clientID);
                            server.sendTeamsAvailablePacket();
                            queuePacket(new PacketWriter(PacketType.JOIN_REQUEST_ACCEPTED, w -> {
                                PacketWriter.writeMap(server.level.playerTeam, k -> PacketWriter.writeEnum(k, w), v -> PacketWriter.writeEnum(v, w), w);
                                w.writeLong(server.level.seed);
                                w.writeInt(server.level.tilesX);
                                w.writeInt(server.level.tilesY);
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
                        if (unit == null || unit.type != type || unit.team != team || unit.hasPerformedAction(Action.MOVE)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        server.sendUnitMovePacket(path, illegalTile, unit);
                        unit.startMove(path, illegalTile);
                    });
                }
                case CLIENT_REQUEST_SHOOT_UNIT -> {
                    Point from = PacketReceiver.readPoint(reader), to = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit fromUnit = server.level.getUnit(from), toUnit = server.level.getUnit(to);
                        if (fromUnit == null || toUnit == null || server.teamClientIDs.get(fromUnit.team) != clientID || fromUnit.hasPerformedAction(Action.FIRE)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        fromUnit.clientAttack(toUnit);
                    });
                }
                case CLIENT_REQUEST_CAPTURE_UNIT -> {
                    Point pos = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit u = server.level.getUnit(pos);
                        if (u == null || u.hasPerformedAction(Action.CAPTURE) || !u.canCapture()) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        u.incrementCapture();
                        server.sendUnitCapturePacket(u);
                    });
                }
                case CLIENT_REQUEST_SHIELD_REGEN -> {
                    Point pos = PacketReceiver.readPoint(reader);
                    MainPanel.addTaskAfterAnimBlock(() -> {
                        Unit u = server.level.getUnit(pos);
                        if (u == null || u.hasPerformedAction(Action.SHIELD_REGEN)) {
                            queueUnitUpdatePacket();
                            return;
                        }
                        u.addPerformedAction(Action.SHIELD_REGEN);
                        u.setShieldHP(u.shieldHP + u.type.shieldRegen);
                        server.sendUnitShieldRegenPacket(u);
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
                                closed = true;
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
        }

        @Override
        public void delete() {
            server = null;
        }
    }
}

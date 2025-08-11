package network;

import foundation.Deletable;
import foundation.MainPanel;
import level.GameplaySettings;
import level.Level;
import level.PlayerTeam;
import level.structure.Structure;
import level.structure.StructureType;
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
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static network.Server.*;

public class Client implements Deletable {
    public ConcurrentHashMap<UnitTeam, Integer> teamClientIDs = new ConcurrentHashMap<>();
    public HashMap<UnitTeam, Boolean> bots = new HashMap<>();
    public final String address;
    public Socket socket;
    private DataInputStream reader;
    private DataOutputStream writer;
    private final Vector<PacketWriter> packetQueue = new Vector<>();
    public int clientID = -1, playerCount = 0;

    public boolean failed = false, connected = false, levelCreated = false;

    public Client(String s) {
        int port;
        if (s.indexOf(':') != -1 && s.indexOf(':') == s.lastIndexOf(':')) {
            address = s.substring(0, s.indexOf(':'));
            port = Integer.parseInt(s.substring(s.indexOf(':') + 1));
        } else if (s.indexOf('/') != -1 && s.indexOf('/') == s.lastIndexOf('/')) {
            address = s.substring(0, s.indexOf('/'));
            port = Integer.parseInt(s.substring(s.indexOf('/') + 1));
        } else {
            address = s;
            port = TCP_PORT;
        }
        try {
            if (MainPanel.CREATE_SERVER_AND_CLIENT_CONNECTIONS) {
                ServerSocket serverSocket = new ServerSocket(port);
                serverSocket.close();
            }
            socket = new Socket(address, port);
            socket.setSoTimeout(10000);
            reader = new DataInputStream(socket.getInputStream());
            writer = new DataOutputStream(socket.getOutputStream());
            new Thread(this::runReader).start();
            new Thread(this::runWriter).start();
            connected = true;
        } catch (ConnectException e) {
            failed = true;
        } catch (IOException e) {
            failed = true;
            System.out.println("[WARNING] Client port already in use!");
        }
    }

    public synchronized void queuePacket(PacketWriter packet) {
        packetQueue.add(packet);
    }

    public void readStream(DataInputStream stream) throws IOException {
        switch (PacketType.values()[stream.readInt()]) {
            case PING -> {
            }
            case LEVEL_TURN_UPDATE -> {
                int turn = reader.readInt();
                UnitTeam activeTeam = PacketReceiver.readEnum(UnitTeam.class, reader);
                if (!levelCreated)
                    return;
                MainPanel.addTask(() -> {
                    if (MainPanel.getActiveLevel() != null)
                        MainPanel.getActiveLevel().setTurn(activeTeam, turn, true);
                });
            }
            case UNIT_UPDATE -> {
                HashSet<UnitData> data = new HashSet<>();
                PacketReceiver.readCollection(data, () -> new UnitData(reader), reader);
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    HashSet<Unit> unitsUnaccountedFor = new HashSet<>(l.unitSet);
                    for (UnitData d : data) {
                        Unit u = null;
                        for (Unit unit : unitsUnaccountedFor) {
                            if (unit.data.pos.equals(d.pos)) {
                                u = unit;
                            }
                        }
                        if (u != null && u.data.team == d.team && u.data.type == d.type) {
                            unitsUnaccountedFor.remove(u);
                        } else {
                            u = new Unit(d.type, d.team, d.pos, l);
                            l.forceAddUnit(u);
                        }
                        u.updateFromData(d);
                    }
                    unitsUnaccountedFor.forEach(l::qRemoveUnit);
                    l.setCaptureProgressBars();
                    l.updateSelectedUnit();
                });
            }
            case CLIENT_INIT -> {
                clientID = reader.readInt();
                playerCount = reader.readInt();
                MainPanel.titleScreen.updateColourSelectorVisibility();
            }
            case TEAMS_AVAILABLE -> {
                teamClientIDs = PacketReceiver.readMap(new ConcurrentHashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), reader::readInt, reader);
                bots = PacketReceiver.readMap(new HashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), reader::readBoolean, reader);
                MainPanel.titleScreen.updateColourSelectorVisibility();
            }
            case JOIN_REQUEST_ACCEPTED -> {
                HashMap<UnitTeam, PlayerTeam> playerTeams = PacketReceiver.readMap(new HashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), () -> PacketReceiver.readEnum(PlayerTeam.class, reader), reader);
                HashMap<UnitTeam, PlayerTeam> initialPlayerTeams = PacketReceiver.readMap(new HashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), () -> PacketReceiver.readEnum(PlayerTeam.class, reader), reader);
                HashMap<UnitTeam, Float> destroyedUnitsDamage = PacketReceiver.readMap(new HashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), reader::readFloat, reader);
                HashMap<UnitTeam, Integer> destroyedUnitsByTeam = PacketReceiver.readMap(new HashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), reader::readInt, reader);
                long seed = reader.readLong();
                float botDifficulty = reader.readFloat();
                int width = reader.readInt(), height = reader.readInt();
                GameplaySettings gameplaySettings = new GameplaySettings(reader);
                UnitTeam team = PacketReceiver.readEnum(UnitTeam.class, reader);
                TileData[][] data = new TileData[width][];
                Structure[][] structures = new Structure[width][];
                for (int x = 0; x < width; x++) {
                    data[x] = new TileData[height];
                    structures[x] = new Structure[height];
                    for (int y = 0; y < height; y++) {
                        TileData d = Tile.read(reader);
                        data[x][y] = d;
                        if (d.hasStructure()) {
                            structures[x][y] = new Structure(reader);
                        } else {
                            structures[x][y] = null;
                        }
                    }
                }
                MainPanel.addTask(() -> {
                    MainPanel.startNewLevel(() -> {
                        Level l = new Level(playerTeams, seed, width, height, bots, gameplaySettings, NetworkState.CLIENT, botDifficulty);
                        l.initialPlayerTeams = initialPlayerTeams;
                        l.destroyedUnitsDamage = destroyedUnitsDamage;
                        l.destroyedUnitsByTeam = destroyedUnitsByTeam;
                        l.clientSetThisTeam(team);
                        HashMap<UnitTeam, Point> basePositions = new HashMap<>();
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                Tile tile = l.getTile(x, y);
                                tile.setTileType(data[x][y]);
                                Structure structure = structures[x][y];
                                if (structure != null) {
                                    if (structure.type == StructureType.BASE) {
                                        basePositions.put(structure.team, structure.pos);
                                    } else {
                                        tile.setStructure(structure);
                                    }
                                } else
                                    tile.removeStructure();
                            }
                        }
                        l.setBasePositions(basePositions);
                        return l;
                    }, l -> {
                        levelCreated = true;
                        requestLevelData();
                    });
                });
            }
            case TILE_TYPE_UPDATE -> {
                Point pos = PacketReceiver.readPoint(reader);
                TileData data = Tile.read(reader);
                if (!levelCreated)
                    return;
                MainPanel.addTask(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Tile t = l.getTile(pos);
                    t.setTileType(data);
                    if (l.tileSelector.getSelectedTile() == t)
                        l.levelRenderer.tileInfo.setTile(t);
                });
            }
            case SERVER_MOVE_UNIT -> {
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
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Unit unit = MainPanel.getActiveLevel().getUnit(unitPos);
                    if (unit == null || unit.data.type != type || unit.data.team != team || unit.data.hasPerformedAction(Action.MOVE)) {
                        requestLevelData();
                        return;
                    }
                    unit.startMove(path, illegalTile);
                });
            }
            case SERVER_SHOOT_UNIT -> {
                UnitData from = new UnitData(reader), to = new UnitData(reader);
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Unit fromUnit = from.getUnit(l, true), toUnit = to.getUnit(l, true);
                    fromUnit.attack(toUnit);
                    fromUnit.data.addPerformedAction(Action.FIRE);
                });
            }
            case SERVER_MINE -> {
                UnitData data = new UnitData(reader);
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Unit unit = data.getUnit(l, true);
                    unit.performMiningAction(unit.data.mining);
                });
            }
            case SERVER_REPAIR -> {
                UnitData from = new UnitData(reader), to = new UnitData(reader);
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Unit fromUnit = from.getUnit(l, true), toUnit = to.getUnit(l, true);
                    fromUnit.repair(toUnit);
                });
            }
            case SERVER_RESUPPLY -> {
                UnitData from = new UnitData(reader), to = new UnitData(reader);
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Unit fromUnit = from.getUnit(l, true), toUnit = to.getUnit(l, true);
                    fromUnit.resupply(toUnit);
                });
            }
            case SERVER_CAPTURE_UNIT -> {
                Point pos = PacketReceiver.readPoint(reader);
                boolean action = reader.readBoolean();
                int progress = reader.readInt();
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Unit u = l.getUnit(pos);
                    if (u == null) {
                        requestLevelData();
                        return;
                    }
                    u.capture(progress, action);
                });
            }
            case SERVER_STRUCTURE_UPDATE -> {
                Point pos = PacketReceiver.readPoint(reader);
                boolean hasStructure = reader.readBoolean();
                boolean cameraTo = reader.readBoolean();
                Structure structure;
                if (hasStructure)
                    structure = new Structure(reader);
                else
                    structure = null;
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Tile tile = l.getTile(pos);
                    if (tile.hasStructure()) {
                        if (hasStructure) {
                            if (!structure.equals(tile.structure)) {
                                tile.setStructure(structure);
                                if (cameraTo)
                                    l.levelRenderer.setCameraInterpBlockPos(tile.renderPosCentered);
                            }
                        } else {
                            if (tile.structure.type == StructureType.BASE)
                                l.removePlayer(tile.structure.team);
                            tile.explodeStructure(MainPanel.getActiveLevel());
                            if (cameraTo)
                                l.levelRenderer.setCameraInterpBlockPos(tile.renderPosCentered);
                        }
                    } else if (hasStructure) {
                        if (cameraTo)
                            l.levelRenderer.setCameraInterpBlockPos(tile.renderPosCentered);
                        tile.setStructure(structure);
                    }
                    Unit u = l.getUnit(pos);
                    if (u != null && !u.canCapture())
                        u.stopCapture();
                    l.levelRenderer.energyManager.recalculateIncome();
                });
            }
            case SERVER_DESTROY_STRUCTURE -> {
                Point pos = PacketReceiver.readPoint(reader);
                boolean cameraTo = reader.readBoolean();
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Tile tile = l.getTile(pos);
                    if (!tile.hasStructure()) {
                        requestLevelData();
                        return;
                    }
                    tile.explodeStructure(MainPanel.getActiveLevel());
                    if (cameraTo)
                        l.levelRenderer.setCameraInterpBlockPos(tile.renderPosCentered);
                });
            }
            case SERVER_SHIELD_REGEN -> {
                Point pos = PacketReceiver.readPoint(reader);
                float shieldHP = reader.readFloat();
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Unit u = l.getUnit(pos);
                    if (u == null) {
                        requestLevelData();
                        return;
                    }
                    u.regenShield(shieldHP);
                });
            }
            case SERVER_STEALTH_UNIT -> {
                Point pos = PacketReceiver.readPoint(reader);
                boolean stealth = reader.readBoolean();
                if (!levelCreated)
                    return;
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.getActiveLevel();
                    Unit u = l.getUnit(pos);
                    if (u == null) {
                        requestLevelData();
                        return;
                    }
                    u.data.addPerformedAction(Action.STEALTH);
                    u.setStealthMode(stealth);
                });
            }
            case ENERGY_UPDATE -> {
                HashMap<UnitTeam, Integer> availableMap = PacketReceiver.readMap(new HashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), reader::readInt, reader);
                HashMap<UnitTeam, Integer> incomeMap = PacketReceiver.readMap(new HashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), reader::readInt, reader);
                if (!levelCreated)
                    return;
                MainPanel.addTask(() -> {
                    Level l = MainPanel.getActiveLevel();
                    l.levelRenderer.energyManager.updateFromRead(availableMap, incomeMap);
                });
            }
            case BOT_SELECT_TILE -> {
                Point pos = PacketReceiver.readPoint(reader);
                if (!levelCreated)
                    return;
                MainPanel.addTask(() -> {
                    Level l = MainPanel.getActiveLevel();
                    l.botHandlerMap.get(l.getActiveTeam()).selectTileClient(pos);
                });
            }
        }
    }

    public void runReader() {
        while (true) {
            try {
                readStream(reader);
            } catch (EOFException | SocketException | SocketTimeoutException e) {
                break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (MainPanel.getActiveLevel() != null)
            MainPanel.clientDisconnect();
    }

    public void sendJoinRequest(UnitTeam team) {
        queuePacket(new PacketWriter(PacketType.JOIN_REQUEST, w -> {
            PacketWriter.writeEnum(team, w);
        }));
    }

    public void sendEndTurn() {
        queuePacket(new PacketWriter(PacketType.CLIENT_END_TURN, w -> {
        }));
    }

    public void requestLevelData() {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_LEVEL_DATA, w -> {
        }));
    }

    public void sendUnitMoveRequest(AnimTilePath path, Point illegalTile, Unit unit) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_MOVE_UNIT, w -> {
            path.write(w);
            w.writeBoolean(illegalTile != null);
            if (illegalTile != null) {
                PacketWriter.writePoint(illegalTile, w);
            }
            PacketWriter.writePoint(unit.data.pos, w);
            unit.data.type.write(w);
            PacketWriter.writeEnum(unit.data.team, w);
        }));
    }

    public void sendUnitShootRequest(Unit from, Unit to) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_SHOOT_UNIT, w -> {
            PacketWriter.writePoint(from.data.pos, w);
            PacketWriter.writePoint(to.data.pos, w);
        }));
    }

    public void sendUnitMineRequest(Unit unit) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_MINE, w -> {
            PacketWriter.writePoint(unit.data.pos, w);
        }));
    }

    public void sendUnitRepairRequest(Unit from, Unit to) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_REPAIR, w -> {
            PacketWriter.writePoint(from.data.pos, w);
            PacketWriter.writePoint(to.data.pos, w);
        }));
    }

    public void sendUnitResupplyRequest(Unit from, Unit to) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_RESUPPLY, w -> {
            PacketWriter.writePoint(from.data.pos, w);
            PacketWriter.writePoint(to.data.pos, w);
        }));
    }

    public void sendUnitCaptureRequest(Unit unit) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_CAPTURE, w -> {
            PacketWriter.writePoint(unit.data.pos, w);
        }));
    }

    public void sendUnitShieldRegenRequest(Unit unit) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_SHIELD_REGEN, w -> {
            PacketWriter.writePoint(unit.data.pos, w);
        }));
    }

    public void sendUnitStealthRequest(Unit unit) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_STEALTH, w -> {
            PacketWriter.writePoint(unit.data.pos, w);
        }));
    }

    public AtomicBoolean close = new AtomicBoolean(false);

    private long lastPingTime = System.currentTimeMillis();

    public void runWriter() {
        try {
            while (!close.get()) {
                TimeUnit.MILLISECONDS.sleep(5);
                synchronized (this) {
                    if (System.currentTimeMillis() - lastPingTime > 2500) {
                        lastPingTime = System.currentTimeMillis();
                        queuePacket(new PacketWriter(PacketType.PING, w -> {
                        }));
                    }
                    packetQueue.forEach(p -> {
                        try {
                            PacketWriter.writeEnum(p.type(), writer);
                            p.writer().accept(writer);
                        } catch (SocketException e) {
                            close.set(true);
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

    @Override
    public void delete() {
        close.set(true);
        try {
            socket.close();
        } catch (IOException _) {
        }
    }
}

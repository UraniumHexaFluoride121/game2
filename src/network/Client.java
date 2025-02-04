package network;

import foundation.Deletable;
import foundation.MainPanel;
import level.Level;
import level.PlayerTeam;
import level.Tile;
import level.TileData;
import render.anim.AnimTilePath;
import unit.Unit;
import unit.UnitData;
import unit.UnitTeam;
import unit.UnitType;
import unit.action.Action;

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
    public final String address;
    public Socket socket;
    private DataInputStream reader;
    private DataOutputStream writer;
    private final Vector<PacketWriter> packetQueue = new Vector<>();
    public int clientID = -1, playerCount = 0;

    public boolean failed = false, connected = false;

    public Client(String address) {
        this.address = address;
        try {
            if (MainPanel.CREATE_SERVER_AND_CLIENT_CONNECTIONS) {
                ServerSocket serverSocket = new ServerSocket(TCP_PORT);
                serverSocket.close();
            }
            socket = new Socket(address, TCP_PORT);
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
                MainPanel.addTask(() -> {
                    if (MainPanel.activeLevel != null)
                        MainPanel.activeLevel.setTurn(activeTeam, turn);
                });
            }
            case UNIT_UPDATE -> {
                HashSet<UnitData> data = new HashSet<>();
                PacketReceiver.readCollection(data, () -> new UnitData(reader), reader);
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.activeLevel;
                    HashSet<Unit> unitsUnaccountedFor = new HashSet<>(l.unitSet);
                    for (UnitData d : data) {
                        Unit u = null;
                        for (Unit unit : unitsUnaccountedFor) {
                            if (unit.pos.equals(d.pos)) {
                                u = unit;
                            }
                        }
                        if (u != null && u.team == d.team && u.type == d.type) {
                            unitsUnaccountedFor.remove(u);
                        } else {
                            u = new Unit(d.type, d.team, d.pos, l);
                            l.forceAddUnit(u);
                        }
                        u.performedActions.clear();
                        u.performedActions.addAll(d.performedActions);
                        u.hitPoints = d.hitPoints;
                        u.firingTempHP = d.hitPoints;
                        for (int i = 0; i < d.weaponAmmo.size(); i++) {
                            u.weapons.get(i).ammo = d.weaponAmmo.get(i);
                        }
                    }
                    unitsUnaccountedFor.forEach(l::qRemoveUnit);
                    if (!l.levelRenderer.clientHasInitialCameraPos) {
                        l.levelRenderer.useLastCameraPos(l.thisTeam);
                        l.levelRenderer.clientHasInitialCameraPos = true;
                    }
                });
            }
            case CLIENT_INIT -> {
                clientID = reader.readInt();
                playerCount = reader.readInt();
                MainPanel.titleScreen.updateColourSelectorVisibility();
            }
            case TEAMS_AVAILABLE -> {
                teamClientIDs = PacketReceiver.readMap(new ConcurrentHashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), reader::readInt, reader);
                MainPanel.titleScreen.updateColourSelectorVisibility();
            }
            case JOIN_REQUEST_ACCEPTED -> {
                HashMap<UnitTeam, PlayerTeam> playerTeams = PacketReceiver.readMap(new HashMap<>(), () -> PacketReceiver.readEnum(UnitTeam.class, reader), () -> PacketReceiver.readEnum(PlayerTeam.class, reader), reader);
                long seed = reader.readLong();
                int width = reader.readInt(), height = reader.readInt();
                UnitTeam team = PacketReceiver.readEnum(UnitTeam.class, reader);
                TileData[][] data = new TileData[width][];
                for (int x = 0; x < width; x++) {
                    data[x] = new TileData[height];
                    for (int y = 0; y < height; y++) {
                        data[x][y] = Tile.read(reader);
                    }
                }
                MainPanel.addTask(() -> {
                    MainPanel.startNewLevel(() -> {
                        Level l = new Level(playerTeams, seed, width, height, NetworkState.CLIENT);
                        l.setThisTeam(team);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                l.getTile(x, y).setTileType(data[x][y]);
                            }
                        }
                        return l;
                    }, this::requestLevelData);
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
                UnitType type = PacketReceiver.readEnum(UnitType.class, reader);
                UnitTeam team = PacketReceiver.readEnum(UnitTeam.class, reader);
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Unit unit = MainPanel.activeLevel.getUnit(unitPos);
                    if (unit == null || unit.type != type || unit.team != team || unit.performedActions.contains(Action.MOVE)) {
                        requestLevelData();
                        return;
                    }
                    unit.startMove(path, illegalTile);
                });
            }
            case SERVER_SHOOT_UNIT -> {
                UnitData from = new UnitData(reader), to = new UnitData(reader);
                MainPanel.addTaskAfterAnimBlock(() -> {
                    Level l = MainPanel.activeLevel;
                    Unit fromUnit = from.getUnit(l), toUnit = to.getUnit(l);
                    fromUnit.attack(toUnit);
                    fromUnit.performedActions.add(Action.FIRE);
                    fromUnit.performedActions.add(Action.MOVE);
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

    public void sendMoveUnitRequest(AnimTilePath path, Point illegalTile, Unit unit) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_MOVE_UNIT, w -> {
            path.write(w);
            w.writeBoolean(illegalTile != null);
            if (illegalTile != null) {
                PacketWriter.writePoint(illegalTile, w);
            }
            PacketWriter.writePoint(unit.pos, w);
            PacketWriter.writeEnum(unit.type, w);
            PacketWriter.writeEnum(unit.team, w);
        }));
    }

    public void sendShootUnitRequest(Unit from, Unit to) {
        queuePacket(new PacketWriter(PacketType.CLIENT_REQUEST_SHOOT_UNIT, w -> {
            PacketWriter.writePoint(from.pos, w);
            PacketWriter.writePoint(to.pos, w);
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

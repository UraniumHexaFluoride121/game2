package level;

import foundation.Deletable;
import foundation.MainPanel;
import foundation.input.ButtonRegister;
import foundation.math.ObjPos;
import foundation.math.RandomHandler;
import foundation.tick.RegisteredTickable;
import foundation.tick.TickOrder;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileSelector;
import level.tile.TileType;
import network.NetworkState;
import network.Server;
import render.Renderable;
import render.renderables.HexagonBorder;
import unit.Unit;
import unit.UnitTeam;
import unit.UnitType;
import unit.action.Action;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Level implements Renderable, Deletable, RegisteredTickable {
    public Server server;
    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    public final long seed;
    public final RandomHandler random;
    public final int tilesX, tilesY;
    public final ObjPos tileBound;

    public final TileSelector tileSelector;
    public Unit selectedUnit;

    public HashMap<UnitTeam, Point> basePositions = new HashMap<>();

    private UnitTeam activeTeam = null;
    public UnitTeam thisTeam = null;

    public ButtonRegister buttonRegister = new ButtonRegister();

    private Action activeAction = null;

    private int turn = 1;

    public final Tile[][] tiles;
    private Unit[][] unitGrid;
    public final HashSet<Unit> unitSet = new HashSet<>();
    public final HashMap<UnitTeam, PlayerTeam> playerTeam;
    public LevelRenderer levelRenderer;

    public final NetworkState networkState;

    public Level(HashMap<UnitTeam, PlayerTeam> playerTeam, long seed, int width, int height, NetworkState networkState) {
        this.networkState = networkState;
        if (networkState == NetworkState.SERVER) {
            server = new Server(this);
            thisTeam = UnitTeam.GREEN;
        }
        tilesX = width;
        tilesY = height;
        tiles = new Tile[tilesX][];
        unitGrid = new Unit[tilesX][];
        tileBound = Tile.getTilesBound(tilesX, tilesY);
        this.playerTeam = playerTeam;
        this.seed = seed;
        random = new RandomHandler(seed);
        activeTeam = getFirstTeam();
        for (int x = 0; x < tilesX; x++) {
            tiles[x] = new Tile[tilesY];
            for (int y = 0; y < tilesY; y++) {
                tiles[x][y] = new Tile(x, y, TileType.EMPTY, this);
            }
        }
        tileSelector = new TileSelector(tiles, this);
        buttonRegister.register(tileSelector);
        for (int x = 0; x < tilesX; x++) {
            unitGrid[x] = new Unit[tilesY];
            for (int y = 0; y < tilesY; y++) {
                unitGrid[x][y] = null;
            }
        }
        levelRenderer = new LevelRenderer(this);
    }

    public Level generateDefaultTerrain(TeamSpawner spawner) {
        while (true) {
            HashSet<Point> nebula = tileSelector.pointTerrain(40, 3, TileType.EMPTY, r -> switch (r) {
                case 0 -> .7f;
                case 1 -> .3f;
                case 2 -> .1f;
                default -> 0f;
            });
            for (Point p : nebula) {
                getTile(p).setTileType(TileType.NEBULA, this);
            }
            HashSet<Point> denseNebula = tileSelector.pointTerrain(10, 1, TileType.NEBULA, r -> switch (r) {
                case 0 -> .1f;
                default -> 0f;
            });
            for (Point p : denseNebula) {
                getTile(p).setTileType(TileType.DENSE_NEBULA, this);
            }
            HashSet<Point> asteroids = tileSelector.pointTerrain(18, 2, TileType.EMPTY, r -> switch (r) {
                case 0 -> .25f;
                case 1 -> .12f;
                default -> 0f;
            });
            for (Point p : asteroids) {
                getTile(p).setTileType(TileType.ASTEROIDS, this);
            }

            if (spawner.generateTeams(this))
                break;
            tileSelector.tileSet.forEach(t -> t.setTileType(TileType.EMPTY, this));
        }
        return this;
    }

    public void init() {
        registerTickable();
    }

    public void setBasePositions(HashMap<UnitTeam, Point> basePositions) {
        this.basePositions.values().forEach(p -> getTile(p).removeStructure());
        this.basePositions = basePositions;
        basePositions.forEach(((team, p) -> {
            Tile tile = getTile(p);
            tile.setStructure(StructureType.BASE, team);
            levelRenderer.lastCameraPos.putIfAbsent(team, tile.renderPosCentered);
        }));
        if (basePositions.containsKey(getThisTeam()))
            levelRenderer.setCameraInterpBlockPos(getTile(basePositions.get(getThisTeam())).renderPosCentered);
    }

    @Override
    public void tick(float deltaTime) {
        levelRenderer.tick(deltaTime);
        unitSet.forEach(u -> u.tick(deltaTime));
        removeUnits();
    }

    @Override
    public TickOrder getTickOrder() {
        return TickOrder.LEVEL;
    }

    public void addUnit(Unit unit) {
        if (unitGrid[unit.pos.x][unit.pos.y] == null) {
            unitSet.add(unit);
            unitGrid[unit.pos.x][unit.pos.y] = unit;
        } else
            unit.delete();
        updateFoW();
    }

    public void forceAddUnit(Unit unit) {
        Unit prevUnit = unitGrid[unit.pos.x][unit.pos.y];
        unitSet.add(unit);
        unitGrid[unit.pos.x][unit.pos.y] = unit;
        levelRenderer.lastCameraPos.putIfAbsent(unit.team, Tile.getCenteredRenderPos(unit.pos));
        if (prevUnit != null)
            qRemoveUnit(prevUnit);
        updateFoW();
    }

    private final HashSet<Unit> qRemoveUnit = new HashSet<>();

    public void qRemoveUnit(Unit unit) {
        qRemoveUnit.add(unit);
    }

    private void removeUnits() {
        qRemoveUnit.forEach(unit -> {
            unitSet.remove(unit);
            Unit unitAtPos = unitGrid[unit.pos.x][unit.pos.y];
            if (unitAtPos == unit)
                unitGrid[unit.pos.x][unit.pos.y] = null;
            if (selectedUnit == unit)
                selectedUnit = null;
            unit.delete();
        });
        qRemoveUnit.clear();
        updateFoW();
    }

    public boolean canUnitBeMoved(Point newPos) {
        return getUnit(newPos) == null;
    }

    public void moveUnit(Unit unit, Point newPos, boolean selectNewTile) {
        if (!canUnitBeMoved(newPos))
            throw new RuntimeException("Unit could not be moved to tile " + newPos);
        unitGrid[unit.pos.x][unit.pos.y] = null;
        unitGrid[newPos.x][newPos.y] = unit;
        unit.updateLocation(newPos);
        if (selectNewTile)
            tileSelector.select(getTile(newPos));
        updateFoW();
        unit.updateActionUI();
    }

    public Tile getTile(Point pos) {
        return tiles[pos.x][pos.y];
    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    public Unit getUnit(Point pos) {
        return unitGrid[pos.x][pos.y];
    }

    public Unit getUnit(int x, int y) {
        return unitGrid[x][y];
    }

    public void updateSelectedUnit() {
        if (tileSelector.getSelectedTile() == null) {
            selectedUnit = null;
            return;
        }
        selectedUnit = getUnit(tileSelector.getSelectedTile().pos);
        if (selectedUnit != null)
            selectedUnit.updateActionUI();
    }

    public UnitTeam getActiveTeam() {
        return activeTeam;
    }

    public boolean hasActiveAction() {
        return activeAction != null;
    }

    public void endAction() {
        activeAction = null;
        if (levelRenderer.highlightTileRenderer != null) {
            levelRenderer.highlightTileRenderer.close();
        }
        levelRenderer.unitTileBorderRenderer = null;
        unitSet.forEach(Unit::updateActionUI);
    }

    public void endTurn() {
        levelRenderer.confirm.makeInvisible();
        if (networkState == NetworkState.CLIENT) {
            MainPanel.client.sendEndTurn();
            return;
        }
        if (networkState == NetworkState.LOCAL) {
            tileSelector.deselect();
        }
        UnitTeam team = activeTeam;
        activeTeam = getNextTeam(activeTeam);
        endAction();
        for (Unit unit : unitSet) {
            unit.turnEnded();
        }
        if (activeTeam == getFirstTeam())
            turn++;
        levelRenderer.onNextTurn.start("Turn " + turn, activeTeam);
        levelRenderer.turnBox.setNewTurn();
        updateFoW();
        levelRenderer.useLastCameraPos(team, activeTeam);
        levelRenderer.endTurn.setGrayedOut(getThisTeam() != getActiveTeam());
        if (networkState == NetworkState.SERVER)
            server.sendTurnUpdatePacket();
    }

    public void setTurn(UnitTeam activeTeam, int turn) {
        if (this.activeTeam != activeTeam || this.turn != turn) {
            levelRenderer.onNextTurn.start("Turn " + turn, activeTeam);
            for (Unit unit : unitSet) {
                unit.turnEnded();
            }
        }
        this.activeTeam = activeTeam;
        this.turn = turn;
        levelRenderer.turnBox.setNewTurn();
        levelRenderer.endTurn.setGrayedOut(getThisTeam() != getActiveTeam());
    }

    public Action getActiveAction() {
        return activeAction;
    }

    public void setActiveAction(Action activeAction) {
        this.activeAction = activeAction;
    }

    public static HashMap<UnitTeam, PlayerTeam> allSeparate() {
        HashMap<UnitTeam, PlayerTeam> teamIndex = new HashMap<>();
        for (int i = 0; i < UnitTeam.values().length; i++) {
            teamIndex.put(UnitTeam.values()[i], PlayerTeam.values()[i]);
        }
        return teamIndex;
    }

    public boolean samePlayerTeam(Unit a, Unit b) {
        return Objects.equals(playerTeam.get(a.team), playerTeam.get(b.team));
    }

    public UnitTeam getNextTeam(UnitTeam from) {
        for (int i = 1; i <= UnitTeam.values().length; i++) {
            UnitTeam next = UnitTeam.ORDERED_TEAMS[(from.order + i) % UnitTeam.ORDERED_TEAMS.length];
            if (playerTeam.containsKey(next))
                return next;
        }
        return null;
    }

    public UnitTeam getThisTeam() {
        if (networkState == NetworkState.LOCAL) {
            return activeTeam;
        }
        return thisTeam;
    }

    public UnitTeam getFirstTeam() {
        return getNextTeam(UnitTeam.ORDERED_TEAMS[UnitTeam.ORDERED_TEAMS.length - 1]);
    }

    public int playerCount() {
        return playerTeam.size();
    }

    private static final Color FOW_TILE_BORDER_COLOUR = new Color(67, 67, 67, 255);

    public void updateFoW() {
        tileSelector.tileSet.forEach(t -> t.isFoW = true);
        HashSet<Point> visible = new HashSet<>();
        UnitTeam team = getThisTeam();
        unitSet.forEach(u -> {
            if (u.team == team)
                visible.addAll(u.getVisibleTiles());
        });
        visible.forEach(p -> {
            getTile(p).isFoW = false;
        });
        levelRenderer.fowTileBorder = new HexagonBorder(visible, FOW_TILE_BORDER_COLOUR);
    }

    public void setThisTeam(UnitTeam team) {
        thisTeam = team;
        levelRenderer.useLastCameraPos(team);
    }

    public boolean rendered = false;

    @Override
    public void render(Graphics2D g) {
        levelRenderer.render(g);
        rendered = true;
    }

    @Override
    public void delete() {
        unitSet.forEach(Unit::delete);
        unitSet.clear();
        unitGrid = null;
        tileSelector.delete();
        qRemoveUnit.clear();
        removeTickable();
        levelRenderer.delete();
        buttonRegister.delete();
        levelRenderer = null;
        buttonRegister = null;
    }

    public int getTurn() {
        return turn;
    }
}

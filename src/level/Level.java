package level;

import foundation.Deletable;
import foundation.MainPanel;
import foundation.input.ButtonRegister;
import foundation.math.HexagonalDirection;
import foundation.math.ObjPos;
import foundation.math.RandomHandler;
import foundation.tick.RegisteredTickable;
import foundation.tick.TickOrder;
import level.structure.Structure;
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
import unit.action.Action;
import unit.bot.BotHandler;
import unit.bot.VisibilityData;

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

    private UnitTeam activeTeam = null, thisTeam = null, lastActiveNonBot = null;

    public ButtonRegister buttonRegister = new ButtonRegister();

    private Action activeAction = null;

    private int turn = 1;

    public final Tile[][] tiles;
    private Unit[][] unitGrid;
    public final HashSet<Unit> unitSet = new HashSet<>();
    public final HashMap<UnitTeam, PlayerTeam> playerTeam;
    public final HashMap<UnitTeam, Boolean> bots;
    public final HashMap<UnitTeam, BotHandler> botHandlerMap = new HashMap<>();
    public LevelRenderer levelRenderer;

    public final NetworkState networkState;
    //Higher values are easier
    public final float botDifficulty;

    public Level(HashMap<UnitTeam, PlayerTeam> playerTeam, long seed, int width, int height, HashMap<UnitTeam, Boolean> bots, NetworkState networkState, float botDifficulty) {
        this.networkState = networkState;
        this.botDifficulty = botDifficulty;
        if (networkState == NetworkState.SERVER) {
            server = new Server(this);
            thisTeam = UnitTeam.ORDERED_TEAMS[0];
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
        lastActiveNonBot = activeTeam;
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
        this.bots = bots;
        bots.forEach((team, isBot) -> {
            if (isBot) {
                botHandlerMap.put(team, new BotHandler(this, team));
            }
        });
        if (networkState != NetworkState.CLIENT && bots.get(getActiveTeam()))
            botHandlerMap.get(getActiveTeam()).startTurn();
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
        levelRenderer.energyManager.recalculateIncome();
        levelRenderer.energyManager.incrementTurn(getThisTeam());
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
        if (!qRemoveUnit.isEmpty())
            removeUnits();
        if (networkState != NetworkState.CLIENT && bots.get(getActiveTeam()))
            botHandlerMap.get(getActiveTeam()).tick(deltaTime);
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
        HashSet<UnitTeam> removeTeams = new HashSet<>();
        for (UnitTeam team : playerTeam.keySet()) {
            boolean hasUnit = false;
            for (Unit unit : unitSet) {
                if (unit.team == team) {
                    hasUnit = true;
                    break;
                }
            }
            if (!hasUnit)
                removeTeams.add(team);
        }
        for (UnitTeam removeTeam : removeTeams) {
            removePlayer(removeTeam);
        }
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
        unit.stopCapture();
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
        setActiveAction(null);
        closeBorderRenderer();
        unitSet.forEach(Unit::updateActionUI);
    }

    public void closeBorderRenderer() {
        if (levelRenderer.highlightTileRenderer != null) {
            levelRenderer.highlightTileRenderer.close();
        }
        levelRenderer.unitTileBorderRenderer = null;
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
        if (!bots.get(activeTeam))
            lastActiveNonBot = activeTeam;
        endAction();
        for (Unit unit : unitSet) {
            unit.turnEnded();
        }
        if (activeTeam == getFirstTeam())
            turn++;
        levelRenderer.onNextTurn.start("Turn " + turn, activeTeam);
        levelRenderer.turnBox.setNewTurn();
        updateFoW();
        if (!bots.get(activeTeam))
            levelRenderer.useLastCameraPos(team, activeTeam);
        else
            levelRenderer.setLastCameraPos(team);
        levelRenderer.endTurn.setGrayedOut(getThisTeam() != getActiveTeam());
        levelRenderer.energyManager.incrementTurn(activeTeam);
        structureEndTurnUpdate();
        if (bots.get(getActiveTeam()))
            botHandlerMap.get(getActiveTeam()).startTurn();
        if (networkState == NetworkState.SERVER)
            server.sendTurnUpdatePacket();
    }

    public void setTurn(UnitTeam activeTeam, int turn, boolean endIfDifferent) {
        if (this.activeTeam != activeTeam || this.turn != turn) {
            endAction();
            levelRenderer.onNextTurn.start("Turn " + turn, activeTeam);
            if (endIfDifferent)
                for (Unit unit : unitSet) {
                    unit.turnEnded();
                }
        }
        this.activeTeam = activeTeam;
        if (!bots.get(activeTeam))
            lastActiveNonBot = activeTeam;
        this.turn = turn;
        levelRenderer.turnBox.setNewTurn();
        levelRenderer.endTurn.setGrayedOut(getThisTeam() != getActiveTeam());
    }

    public void structureEndTurnUpdate() {
        tileSelector.tileSet.forEach(t -> {
            if (!t.hasStructure())
                return;
            Unit u = getUnit(t.pos);
            Structure s = t.structure;
            if (u == null || u.team != activeTeam || s.team != u.team)
                return;
            if (s.type.resupply)
                u.resupply();
            u.regenerateHP(s.type.unitRegen);
        });
    }

    public Action getActiveAction() {
        return activeAction;
    }

    public void setActiveAction(Action activeAction) {
        if (activeAction == null)
            levelRenderer.exitActionButton.setEnabled(false);
        this.activeAction = activeAction;
    }

    public boolean samePlayerTeam(Unit a, Unit b) {
        return Objects.equals(playerTeam.get(a.team), playerTeam.get(b.team));
    }

    public boolean samePlayerTeam(UnitTeam a, UnitTeam b) {
        return Objects.equals(playerTeam.get(a), playerTeam.get(b));
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
            return lastActiveNonBot;
        }
        return thisTeam;
    }

    public UnitTeam getFirstTeam() {
        return getNextTeam(UnitTeam.ORDERED_TEAMS[UnitTeam.ORDERED_TEAMS.length - 1]);
    }

    public int playerCount() {
        return playerTeam.size();
    }

    public void removePlayer(UnitTeam team) {
        if (activeTeam == team && networkState != NetworkState.CLIENT)
            endTurn();
        if (!playerTeam.containsKey(team))
            return;
        levelRenderer.onTeamEliminated.start(team.getName() + " Eliminated!", team);
        Tile baseTile = getTile(basePositions.get(team));
        if (baseTile.hasStructure())
            baseTile.explodeStructure();
        basePositions.remove(team);
        playerTeam.remove(team);
        unitSet.forEach(u -> {
            if (u.team == team) {
                u.onDestroyed(null);
            }
        });
        updateFoW();
        if (!isThisPlayerAlive())
            levelRenderer.endTurn.setGrayedOut(true);
    }

    private static final Color FOW_TILE_BORDER_COLOUR = new Color(67, 67, 67, 255);
    public VisibilityData currentVisibility = null;

    public void updateFoW() {
        if (!isThisPlayerAlive()) {
            tileSelector.tileSet.forEach(t -> t.isFoW = false);
            unitSet.forEach(u -> u.visibleInStealthMode = true);
            levelRenderer.fowTileBorder = null;
            HashSet<Point> points = new HashSet<>();
            for (Tile t : tileSelector.tileSet) {
                points.add(t.pos);
            }
            currentVisibility = new VisibilityData(points, new HashSet<>(unitSet));
            return;
        }
        currentVisibility = getVisibilityData(getThisTeam());
        tileSelector.tileSet.forEach(t -> {
            t.isFoW = !currentVisibility.visibleTiles().contains(t.pos);
        });
        levelRenderer.fowTileBorder = new HexagonBorder(currentVisibility.visibleTiles(), FOW_TILE_BORDER_COLOUR);
        unitSet.forEach(u -> u.visibleInStealthMode = currentVisibility.stealthVisibleUnit().contains(u));
    }

    public VisibilityData getVisibilityData(UnitTeam team) {
        HashSet<Point> visible = new HashSet<>();
        HashSet<Unit> stealthVisible = new HashSet<>();
        unitSet.forEach(u -> {
            if (samePlayerTeam(u.team, team)) {
                visible.addAll(u.getVisibleTiles());
                stealthVisible.add(u);
            } else {
                Point pos = u.renderTile().pos;
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point point = d.offset(pos);
                    if (!tileSelector.validCoordinate(point))
                        continue;
                    Unit other = getUnit(point);
                    if (other != null && samePlayerTeam(other.team, team)) {
                        stealthVisible.add(u);
                        break;
                    }
                }
            }
        });
        tileSelector.tileSet.forEach(t -> {
            if (t.hasStructure() && samePlayerTeam(t.structure.team, team))
                visible.add(t.pos);
        });
        return new VisibilityData(visible, stealthVisible);
    }

    public void setCaptureProgressBars() {
        unitSet.forEach(u -> {
            u.setCaptureProgress(u.getCaptureProgress());
        });
    }

    public void setThisTeam(UnitTeam team) {
        thisTeam = team;
        levelRenderer.useLastCameraPos(team);
    }

    public boolean isThisPlayerAlive() {
        return true;
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
        if (server != null) {
            server.delete();
            server = null;
        }
        currentVisibility = null;
    }

    public int getTurn() {
        return turn;
    }
}

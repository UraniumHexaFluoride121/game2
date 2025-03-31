package level;

import foundation.MainPanel;
import foundation.math.HexagonalDirection;
import foundation.tick.TickOrder;
import level.structure.Structure;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileSelector;
import level.tile.TileType;
import level.tutorial.TutorialLevel;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventTurnStart;
import network.NetworkState;
import network.Server;
import render.level.tile.HexagonBorder;
import unit.Unit;
import unit.UnitMapDataConsumer;
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

public class Level extends AbstractLevel<LevelRenderer, TileSelector> {
    public static final int MIN_WIDTH = 8, MAX_WIDTH = 45, MIN_HEIGHT = 6, MAX_HEIGHT = 25;
    public Server server;
    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public Unit selectedUnit;
    public HashMap<UnitTeam, Point> basePositions = new HashMap<>();
    private UnitTeam activeTeam = null, thisTeam = null, lastActiveNonBot = null;

    private Action activeAction = null;

    private int turn = 1;

    private Unit[][] unitGrid;
    public final HashSet<Unit> unitSet = new HashSet<>();
    public final HashMap<UnitTeam, PlayerTeam> playerTeam;
    public HashMap<UnitTeam, PlayerTeam> initialPlayerTeams;
    public final HashMap<UnitTeam, Boolean> bots;
    public final HashMap<UnitTeam, BotHandler> botHandlerMap = new HashMap<>();
    public final GameplaySettings gameplaySettings;

    public final NetworkState networkState;
    //Higher values are easier
    public final float botDifficulty;

    public Level(HashMap<UnitTeam, PlayerTeam> playerTeam, long seed, int width, int height, HashMap<UnitTeam, Boolean> bots, GameplaySettings gameplaySettings, NetworkState networkState, float botDifficulty) {
        super(width, height, seed);
        this.gameplaySettings = gameplaySettings;
        this.networkState = networkState;
        this.botDifficulty = botDifficulty;
        if (networkState == NetworkState.SERVER) {
            server = new Server(this);
            thisTeam = UnitTeam.ORDERED_TEAMS[0];
        }
        unitGrid = new Unit[tilesX][];
        this.playerTeam = playerTeam;
        initialPlayerTeams = new HashMap<>(playerTeam);
        activeTeam = getFirstTeam();
        lastActiveNonBot = activeTeam;
        tileSelector = new TileSelector(this);
        buttonRegister.register(tileSelector);
        for (int x = 0; x < tilesX; x++) {
            unitGrid[x] = new Unit[tilesY];
            for (int y = 0; y < tilesY; y++) {
                unitGrid[x][y] = null;
            }
        }
        levelRenderer = new LevelRenderer(this);
        levelRenderer.createRenderers();
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
        do {
            generateTiles();
        } while (!spawner.generateTeams(this));
        levelRenderer.energyManager.recalculateIncome();
        levelRenderer.energyManager.incrementTurn(getThisTeam());
        return this;
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
    public void forEachUnitMapData(UnitMapDataConsumer action) {
        unitSet.forEach(u -> action.accept(u.getRenderPos(), u.team, u.renderVisible()));
    }

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
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
        TutorialManager.acceptEvent(new EventTurnStart(this, getActiveTeam()));
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
        return Objects.equals(initialPlayerTeams.get(a.team), initialPlayerTeams.get(b.team));
    }

    public boolean samePlayerTeam(UnitTeam a, UnitTeam b) {
        return Objects.equals(initialPlayerTeams.get(a), initialPlayerTeams.get(b));
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
        if (gameEnded())
            MainPanel.addTaskAfterAnimBlock(() -> {
                levelRenderer.endScreen.setEnabled(true);
            });
    }

    public boolean gameEnded() {
        return new HashSet<>(playerTeam.values()).size() <= 1;
    }

    public PlayerTeam survivingPlayerTeam() {
        HashSet<PlayerTeam> teams = new HashSet<>(playerTeam.values());
        if (teams.size() != 1)
            throw new RuntimeException("More than one surviving PlayerTeam");
        for (PlayerTeam team : teams) {
            return team;
        }
        return null;
    }

    public static final Color FOW_TILE_BORDER_COLOUR = new Color(67, 67, 67, 255);
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
            if (!gameplaySettings.isFoWEnabled || (t.hasStructure() && samePlayerTeam(t.structure.team, team)))
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
        return playerTeam.containsKey(getThisTeam());
    }

    @Override
    public void delete() {
        unitSet.forEach(Unit::delete);
        unitSet.clear();
        unitGrid = null;
        qRemoveUnit.clear();
        levelRenderer.delete();
        super.delete();
        buttonRegister = null;
        if (server != null) {
            server.delete();
            server = null;
        }
        currentVisibility = null;
        TutorialManager.deleteSequence();
    }

    public int getTurn() {
        return turn;
    }
}

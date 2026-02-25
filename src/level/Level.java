package level;

import foundation.MainPanel;
import foundation.math.HexagonalDirection;
import foundation.tick.TickOrder;
import level.structure.Structure;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileSelector;
import level.tile.TileSet;
import level.tile.TileType;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventTilesFoW;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Level extends AbstractLevel<LevelRenderer, TileSelector> {
    public static final int MIN_WIDTH = 8, MAX_WIDTH = 45, MIN_HEIGHT = 6, MAX_HEIGHT = 25;
    public Server server;
    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public Unit selectedUnit;
    private UnitTeam activeTeam = null, thisTeam = null, lastActiveNonBot = null;

    private Action activeAction = null;

    private int turn = 1;

    private Unit[][] unitGrid;
    public final HashSet<Unit> unitSet = new HashSet<>();
    private HashMap<UnitTeam, TeamData> teamData = new HashMap<>();
    public final GameplaySettings gameplaySettings;

    public final NetworkState networkState;
    //Higher values are easier
    public final float botDifficulty;

    public Level(HashMap<UnitTeam, TeamData> teamData, long seed, int width, int height, GameplaySettings gameplaySettings, NetworkState networkState, float botDifficulty) {
        super(width, height, seed);
        this.gameplaySettings = gameplaySettings;
        this.networkState = networkState;
        this.botDifficulty = botDifficulty;
        if (networkState == NetworkState.SERVER) {
            server = new Server(this);
            thisTeam = UnitTeam.ORDERED_TEAMS[0];
        }
        unitGrid = new Unit[tilesX][];
        if (!MainPanel.isSingleplayer())
            this.teamData = teamData;
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
        getTeamData().forEach((team, data) -> {
            if (data.bot) {
                data.botHandler = new BotHandler(this, team);
            }
        });
        if (networkState != NetworkState.CLIENT && getActiveTeamData().bot)
            getActiveTeamData().botHandler.startTurn();
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
        basePositions.forEach(((team, p) -> {
            getTeamData().get(team).basePos = p;
        }));
        getTeamData().values().forEach(data -> getTile(data.basePos).removeStructure());
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
        unitSet.forEach(u -> action.accept(u.getRenderPos(), u.data.team, u.renderVisible()));
    }

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        unitSet.forEach(u -> u.tick(deltaTime));
        if (!qRemoveUnit.isEmpty())
            removeUnits();
        if (networkState != NetworkState.CLIENT && getActiveTeamData().bot)
            getActiveTeamData().botHandler.tick(deltaTime);
    }

    @Override
    public TickOrder getTickOrder() {
        return TickOrder.LEVEL;
    }

    public void addUnit(Unit unit, boolean addToInitialCount) {
        if (unitGrid[unit.data.pos.x][unit.data.pos.y] == null) {
            unitSet.add(unit);
            unitGrid[unit.data.pos.x][unit.data.pos.y] = unit;
            if (addToInitialCount)
                getTeamData().get(unit.data.team).unitCount++;
        } else
            unit.delete();
        updateFoW();
        updateSelectedUnit();
    }

    public void forceAddUnit(Unit unit) {
        Unit prevUnit = unitGrid[unit.data.pos.x][unit.data.pos.y];
        unitSet.add(unit);
        unitGrid[unit.data.pos.x][unit.data.pos.y] = unit;
        levelRenderer.lastCameraPos.putIfAbsent(unit.data.team, Tile.getCenteredRenderPos(unit.data.pos));
        if (prevUnit != null)
            qRemoveUnit(prevUnit);
        updateFoW();
    }

    private final HashSet<Unit> qRemoveUnit = new HashSet<>();

    public void qRemoveUnit(Unit unit) {
        qRemoveUnit.add(unit);
    }

    private void removeUnits() {
        if (!qRemoveUnit.isEmpty()) {
            qRemoveUnit.forEach(unit -> {
                unitSet.remove(unit);
                Unit unitAtPos = unitGrid[unit.data.pos.x][unit.data.pos.y];
                if (unitAtPos == unit)
                    unitGrid[unit.data.pos.x][unit.data.pos.y] = null;
                if (selectedUnit == unit)
                    selectedUnit = null;
                unit.delete();
            });
            qRemoveUnit.clear();
            updateSelectedUnit();
        }
        HashSet<UnitTeam> removeTeams = new HashSet<>();
        for (UnitTeam team : getTeamData().keySet()) {
            if (!getTeamData().get(team).alive)
                continue;
            boolean hasUnit = false;
            for (Unit unit : unitSet) {
                if (unit.data.team == team) {
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
        unitGrid[unit.data.pos.x][unit.data.pos.y] = null;
        unitGrid[newPos.x][newPos.y] = unit;
        unit.stopCapture();
        unit.updateLocation(newPos);
        if (selectNewTile)
            tileSelector.select(getTile(newPos));
        updateFoW();
        unit.updateActionUI();
        updateSelectedUnit();
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

    public TeamData getActiveTeamData() {
        return getTeamData().get(activeTeam);
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

    public void preEndTurn() {
        levelRenderer.confirm.makeInvisible();
        if (networkState == NetworkState.CLIENT) {
            MainPanel.client.sendEndTurn();
            return;
        }
        if (networkState == NetworkState.LOCAL) {
            tileSelector.deselect();
            if (showNextPlayerScreen()) {
                UnitTeam nextTeam = getNextTeam(activeTeam);
                levelRenderer.nextPlayerScreen.enable(nextTeam);
                if (!getTeamData().get(nextTeam).bot)
                    return;
            }
        }
        endTurn();
    }

    public void endTurn() {
        UnitTeam prevTeam = activeTeam;
        activeTeam = getNextTeam(activeTeam);
        if (!getActiveTeamData().bot)
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
        if (networkState == NetworkState.LOCAL) {
            levelRenderer.setLastCameraPos(prevTeam);
            if (!getActiveTeamData().bot)
                levelRenderer.useLastCameraPos(activeTeam, showNextPlayerScreen());
        }
        levelRenderer.endTurn.setGrayedOut(getThisTeam() != getActiveTeam());
        levelRenderer.energyManager.incrementTurn(activeTeam);
        structureStartTurnUpdate();
        unitStartTurnUpdate();
        if (getActiveTeamData().bot)
            getActiveTeamData().botHandler.startTurn();
        if (isServer())
            server.sendTurnUpdatePacket();
        TutorialManager.acceptEvent(new EventTurnStart(this, getActiveTeam()));
    }

    public boolean showNextPlayerScreen() {
        return networkState == NetworkState.LOCAL && realPlayerCount() > 1 && gameplaySettings.isFoWEnabled;
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
        if (!getActiveTeamData().bot)
            lastActiveNonBot = activeTeam;
        this.turn = turn;
        levelRenderer.turnBox.setNewTurn();
        levelRenderer.endTurn.setGrayedOut(getThisTeam() != getActiveTeam());
    }

    public void structureStartTurnUpdate() {
        tileSelector.tileSet.forEach(t -> {
            if (!t.hasStructure())
                return;
            Unit u = getUnit(t.pos);
            Structure s = t.structure;
            if (u == null || u.data.team != getActiveTeam() || !samePlayerTeam(s.team, u.data.team))
                return;
            if (s.stats.resupply(this) && u.stats.consumesAmmo() && u.data.ammo != u.stats.ammoCapacity()) {
                u.resupply(true);
                if (isServer()) {
                    server.sendStructureResupplyPacket(u);
                }
            }
            if (s.stats.unitRegen(this) != 0 && u.data.hitPoints < u.stats.maxHP()) {
                u.regenerateHP(s.stats.unitRegen(this), true);
                if (isServer()) {
                    server.sendStructureRepairPacket(u, s.stats.unitRegen(this));
                }
            }
        });
    }

    private boolean isServer() {
        return networkState == NetworkState.SERVER;
    }

    public void unitStartTurnUpdate() {
        tileSelector.tileSet.forEach(t -> {
            Unit u = getUnit(t.pos);
            if (u == null)
                return;
            boolean unitUpdate = false;
            if (u.data.team == getActiveTeam()) {
                if (u.isCapturing()) {
                    u.incrementCapture(false);
                    if (isServer())
                        server.sendUnitCapturePacket(u, false);
                }
                if (u.data.mining) {
                    t.miningBarFill--;
                    if (t.miningBarFill == 0) {
                        t.setTileType(TileType.EMPTY, this);
                        u.setMining(false);
                        unitUpdate = true;
                    }
                    if (t == tileSelector.getSelectedTile()) {
                        levelRenderer.tileInfo.setTile(t);
                    }
                    if (isServer())
                        server.sendTileTypePacket(t);
                }
            }
            updateSelectedUnit();
            if (unitUpdate && networkState == NetworkState.SERVER)
                server.sendUnitUpdatePacket();
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
        return Objects.equals(getTeamData().get(a.data.team).playerTeam, getTeamData().get(b.data.team).playerTeam);
    }

    public boolean samePlayerTeam(UnitTeam a, UnitTeam b) {
        if (a == null || b == null)
            return a == b;
        if (!getTeamData().containsKey(a) || !getTeamData().containsKey(b))
            return false;
        return Objects.equals(getTeamData().get(a).playerTeam, getTeamData().get(b).playerTeam);
    }

    public UnitTeam getNextTeam(UnitTeam from) {
        for (int i = 1; i <= UnitTeam.values().length; i++) {
            UnitTeam next = UnitTeam.ORDERED_TEAMS[(from.order + i) % UnitTeam.ORDERED_TEAMS.length];
            if (getTeamData().containsKey(next))
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
        return Math.toIntExact(getTeamData().values().stream().filter(data -> data.alive).count());
    }

    public int realPlayerCount() {
        int count = 0;
        for (TeamData data : getTeamData().values()) {
            if (!data.bot && data.alive)
                count++;
        }
        return count;
    }

    public int initialPlayerCount() {
        return getTeamData().size();
    }

    public void removePlayer(UnitTeam team) {
        if (activeTeam == team && networkState != NetworkState.CLIENT)
            preEndTurn();
        TeamData data = getTeamData().get(team);
        if (!data.alive)
            return;
        levelRenderer.onTeamEliminated.start(team.getName() + " Eliminated!", team);
        Tile baseTile = getTile(data.basePos);
        if (baseTile.hasStructure())
            baseTile.explodeStructure(this);
        data.alive = false;
        unitSet.forEach(u -> {
            if (u.data.team == team) {
                u.onDestroyed(null);
            }
        });
        tileSelector.tileSet.forEach(t -> {
            if (t.hasStructure() && t.structure.team == team) {
                if (t.structure.type.destroyedOnCapture) {
                    t.explodeStructure(this);
                    if (isServer())
                        server.sendStructureDestroy(t, false);
                } else {
                    t.structure.setTeam(null);
                    if (isServer())
                        server.sendStructurePacket(t, false);
                }
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
        return playerCount() <= 1;
    }

    public PlayerTeam survivingPlayerTeam() {
        HashSet<PlayerTeam> teams = new HashSet<>(getTeamData().values().stream().filter(data -> data.alive).map(data -> data.playerTeam).toList());
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
            unitSet.forEach(u -> u.data.visibleInStealthMode = true);
            levelRenderer.fowTileBorder = null;
            TileSet points = new TileSet(this);
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
        unitSet.forEach(u -> u.data.visibleInStealthMode = currentVisibility.stealthVisibleUnit().contains(u));
        TutorialManager.acceptEvent(new EventTilesFoW(this));
    }

    public VisibilityData getVisibilityData(UnitTeam team) {
        TileSet visible = new TileSet(this);
        HashSet<Unit> stealthVisible = new HashSet<>();
        unitSet.forEach(u -> {
            if (samePlayerTeam(u.data.team, team)) {
                visible.addAll(u.getVisibleTiles());
                stealthVisible.add(u);
            } else {
                Point pos = u.renderTile().pos;
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point point = d.offset(pos);
                    if (!tileSelector.validCoordinate(point))
                        continue;
                    Unit other = getUnit(point);
                    if (other != null && samePlayerTeam(other.data.team, team)) {
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

    public void clientSetThisTeam(UnitTeam team) {
        thisTeam = team;
    }

    public boolean isThisPlayerAlive() {
        return getTeamData().get(getThisTeam()).alive;
    }

    public static final int VICTORY_SCORE = 100, DAMAGE_SCORE_MAX = 30, TURN_SCORE_MAX = 30, UNITS_DESTROYED_SCORE_MAX = 30;

    public float getDamageScore(UnitTeam team) {
        float percentage = getTotalRemainingHP(team) / getTotalMaxHP(team);
        return Math.clamp((float) (1.2f * Math.pow(percentage, 0.8f) * DAMAGE_SCORE_MAX), 0, DAMAGE_SCORE_MAX);
    }

    public float getTotalRemainingHP(UnitTeam team) {
        return unitSet.stream().filter(u -> u.data.team == team).map(u -> u.data.lowestHP).reduce(Float::sum).get();
    }

    public float getTotalMaxHP(UnitTeam team) {
        return getTeamData().get(team).destroyedUnitsHP + unitSet.stream().filter(u -> u.data.team == team).map(u -> u.stats.maxHP()).reduce(Float::sum).get();
    }

    public float getTurnScore(UnitTeam team) {
        float res = Math.clamp((float)
                        (4 * (1 - Math.pow((turn - Math.sqrt(tilesX + tilesY) + 4) / 20f, 0.1f * Math.sqrt(initialPlayerCount()))) *
                                Math.log10(getUnitsPerTeam() + 2) * TURN_SCORE_MAX),
                0, TURN_SCORE_MAX
        );
        if (!Float.isFinite(res))
            return TURN_SCORE_MAX;
        return res;
    }

    public int getUnitsDestroyedByTeam(UnitTeam team) {
        return getTeamData().get(team).destroyedEnemiesCount;
    }

    public int getTotalEnemies(UnitTeam team) {
        AtomicInteger count = new AtomicInteger();
        getTeamData().forEach((otherTeam, data) -> {
            if (!samePlayerTeam(team, otherTeam))
                count.addAndGet(data.unitCount);
        });
        return count.get();
    }

    public float getUnitsPerTeam() {
        AtomicInteger count = new AtomicInteger();
        getTeamData().forEach((team, data) -> count.addAndGet(data.unitCount));
        return (float) count.get() / getTeamData().values().stream().map(data -> data.playerTeam).collect(Collectors.toSet()).size();
    }

    public float getUnitsDestroyedScore(UnitTeam team) {
        return Math.clamp((float) (getUnitsDestroyedByTeam(team) / getTotalEnemies(team)) * UNITS_DESTROYED_SCORE_MAX * 1.1f, 0, UNITS_DESTROYED_SCORE_MAX);
    }

    public float getPlayerTeamScore(Function<UnitTeam, ? extends Number> scoreFunction, PlayerTeam pTeam, boolean total) {
        AtomicReference<Double> score = new AtomicReference<>(0d);
        AtomicInteger count = new AtomicInteger();
        getTeamData().forEach((t, d) -> {
            if (d.playerTeam != pTeam)
                return;
            score.updateAndGet(v -> v + scoreFunction.apply(t).doubleValue());
            count.getAndIncrement();
        });
        return (float) (total ? score.get() : score.get() / count.get());
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

    public HashMap<UnitTeam, TeamData> getTeamData() {
        return MainPanel.isSingleplayer() ? MainPanel.spState.teamData : teamData;
    }
}

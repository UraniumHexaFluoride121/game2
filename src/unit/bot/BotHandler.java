package unit.bot;

import foundation.Deletable;
import foundation.MainPanel;
import foundation.TimedTaskQueue;
import foundation.tick.Tickable;
import level.Level;
import level.tile.Tile;
import level.tutorial.TutorialManager;
import network.NetworkState;
import render.anim.AnimTilePath;
import render.anim.PowAnimation;
import render.level.FiringRenderer;
import render.level.tile.TilePath;
import unit.ShipClass;
import unit.Unit;
import unit.UnitData;
import unit.UnitTeam;
import unit.action.Action;
import unit.weapon.FiringData;
import unit.weapon.WeaponInstance;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static unit.bot.BotTileDataType.*;

public class BotHandler implements Deletable, Tickable {
    public Level level;
    public final UnitTeam team;
    private HashSet<Unit> thisTeamUnits;
    private final HashMap<Unit, BotTileData> unitMoveData = new HashMap<>();
    public BotTileData unitDebugData = null;
    public HashMap<BotTileDataType, BotTileData> tileData = new HashMap<>();
    private final AtomicBoolean executingMove = new AtomicBoolean(false), runningTurn = new AtomicBoolean(false);
    private final TimedTaskQueue anim = new TimedTaskQueue();
    private int enemiesDestroyed = 0;

    public BotHandler(Level level, UnitTeam team) {
        this.level = level;
        this.team = team;
        for (BotTileDataType type : values()) {
            tileData.put(type, new BotTileData(level));
        }
    }

    public void startTurn() {
        runningTurn.set(true);
        updateData();
    }

    public boolean findBestMove() {
        if (TutorialManager.isTutorial() && !TutorialManager.forcedBotActions.isEmpty()) {
            BotActionData data = TutorialManager.forcedBotActions.pollFirst().get();
            if (data == null)
                return false;
            else
                return executeMove(data);
        }
        VisibilityData visibility = level.getVisibilityData(team);
        int energyAvailable = level.levelRenderer.energyManager.availableMap.get(team);
        AtomicReference<Action> action = new AtomicReference<>(), secondaryAction = new AtomicReference<>();
        AtomicReference<Point> target = new AtomicReference<>(), secondaryTarget = new AtomicReference<>();
        AtomicReference<Unit> unit = new AtomicReference<>();
        AtomicReference<Float> value = new AtomicReference<>((float) Integer.MIN_VALUE);
        unitMoveData.forEach((u, d) -> {
            if (u.hasPerformedAction(Action.MOVE) || !u.type.canPerformAction(Action.MOVE) || u.getCaptureProgress() > 0)
                return;
            HashMap<Point, Float> costMap = u.tilesInMoveRangeCostMap(visibility);
            float current = d.get(u.pos);
            costMap.forEach((p, cost) -> {
                int energyCost = TilePath.getEnergyCost(u.type, costMap, p);
                Tile t = level.getTile(p);
                if (!u.canUnitAppearToBeMoved(t, visibility) || energyCost > energyAvailable)
                    return;

                float v = (d.get(p) - current) / (energyCost + 2);
                if (t.hasStructure() && t.structure.canBeCaptured && !u.type.canPerformAction(Action.CAPTURE))
                    v -= 12;
                v *= noise();
                if (v > value.get()) {
                    unit.set(u);
                    action.set(Action.MOVE);
                    secondaryAction.set(null);
                    target.set(p);
                    value.set(v);
                    unitDebugData = d;
                }
                if (u.hasPerformedAction(Action.FIRE) || !u.type.canPerformAction(Action.FIRE) || u.stealthMode)
                    return;
                if (energyCost + u.getActionCost(Action.FIRE).get() > energyAvailable)
                    return;
                UnitData data = new UnitData(u);
                data.pos = p;
                HashSet<Point> firingTiles = u.tilesInFiringRange(visibility, data, true);
                for (Point firingTile : firingTiles) {
                    Unit other = level.getUnit(firingTile);
                    FiringData firingData = u.getCurrentFiringData(other)
                            .setThisPos(p);
                    WeaponInstance thisWeapon = Unit.getBestWeaponAgainst(firingData, true);
                    float damageDealt = thisWeapon.getDamageAgainst(firingData);
                    if (firingData.otherData().hitPoints > 0 && thisWeapon.template.counterattack) {
                        FiringData reverse = FiringData.reverse(firingData, other.weapons);
                        WeaponInstance otherWeapon = Unit.getBestWeaponAgainst(reverse, true);
                        if (otherWeapon != null)
                            damageDealt -= otherWeapon.getDamageAgainst(reverse);
                    }
                    v = noise() * (d.get(p) - current + damageDealt * 30) / (energyCost + 2 + u.getActionCost(Action.FIRE).orElse(0));
                    if (t.hasStructure() && level.samePlayerTeam(team, t.structure.team) && other.canCapture())
                        v += other.getCaptureProgress() > 0 ? 10 : 2;
                    if (v > value.get()) {
                        unit.set(u);
                        action.set(Action.MOVE);
                        secondaryAction.set(Action.FIRE);
                        target.set(p);
                        secondaryTarget.set(firingTile);
                        value.set(v);
                        unitDebugData = d;
                    }
                }
            });
        });
        thisTeamUnits.forEach(u -> {
            if (u.hasPerformedAction(Action.FIRE) || !u.type.canPerformAction(Action.FIRE) || u.getActionCost(Action.FIRE).orElse(Integer.MAX_VALUE) > energyAvailable || u.stealthMode)
                return;
            HashSet<Point> firingTiles = u.tilesInFiringRange(visibility, new UnitData(u), true);
            for (Point firingTile : firingTiles) {
                Unit other = level.getUnit(firingTile);
                FiringData firingData = u.getCurrentFiringData(other);
                WeaponInstance thisWeapon = Unit.getBestWeaponAgainst(firingData, true);
                float damageDealt = thisWeapon.getDamageAgainst(firingData);
                if (firingData.otherData().hitPoints > 0 && thisWeapon.template.counterattack) {
                    FiringData reverse = FiringData.reverse(firingData, other.weapons);
                    WeaponInstance otherWeapon = Unit.getBestWeaponAgainst(reverse, true);
                    if (otherWeapon != null)
                        damageDealt -= otherWeapon.getDamageAgainst(reverse);
                }
                Tile t = level.getTile(firingTile);
                float v = (damageDealt * 30) / (u.getActionCost(Action.FIRE).orElse(0));
                if (t.hasStructure() && level.samePlayerTeam(team, t.structure.team) && other.canCapture())
                    v += other.getCaptureProgress() > 0 ? 10 : 2;
                v *= noise();
                if (v > value.get() && !u.hasPerformedAction(Action.FIRE)) {
                    unit.set(u);
                    action.set(Action.FIRE);
                    secondaryAction.set(null);
                    target.set(firingTile);
                    value.set(v);
                }
            }
        });
        thisTeamUnits.forEach(u -> {
            if (u.hasPerformedAction(Action.SHIELD_REGEN) || !u.type.canPerformAction(Action.SHIELD_REGEN))
                return;
            int cost = u.getActionCost(Action.SHIELD_REGEN).get();
            if (cost > energyAvailable)
                return;
            float regen = Math.min(u.type.shieldRegen, u.type.shieldHP - u.shieldHP);
            float v = noise() * 15 * regen / cost;
            if (regen > 0 && v > value.get()) {
                unit.set(u);
                action.set(Action.SHIELD_REGEN);
                value.set(v);
            }
        });
        thisTeamUnits.forEach(u -> {
            if (u.hasPerformedAction(Action.CAPTURE) || !u.type.canPerformAction(Action.CAPTURE) || u.getActionCost(Action.CAPTURE).orElse(Integer.MAX_VALUE) > energyAvailable)
                return;
            Tile t = level.getTile(u.pos);
            if (t.hasStructure() && t.structure.canBeCaptured && !level.samePlayerTeam(team, t.structure.team)) {
                HashSet<Point> surrounding = level.tileSelector.tilesInRadius(u.pos, 2);
                boolean hasUnit = false;
                for (Point p : surrounding) {
                    Unit other = level.getUnit(p);
                    if (other != null && !level.samePlayerTeam(u, other) && other.visible(visibility)) {
                        hasUnit = true;
                        break;
                    }
                }
                boolean finalStep = t.structure.type.captureSteps == u.getCaptureProgress() + 1;
                float v = noise() * (u.getCaptureProgress() <= 0 ? 5f : finalStep ? 20 : 10);
                if ((!hasUnit || finalStep) && v > value.get()) {
                    unit.set(u);
                    action.set(Action.CAPTURE);
                    value.set(v);
                }
            }
        });
        thisTeamUnits.forEach(u -> {
            if (u.hasPerformedAction(Action.STEALTH) || !u.type.canPerformAction(Action.STEALTH) || u.getActionCost(Action.STEALTH).orElse(Integer.MAX_VALUE) > energyAvailable)
                return;
            int enemyCount = 0;
            for (Point p : level.tileSelector.tilesInRadius(u.pos, 6)) {
                Unit other = level.getUnit(p);
                if (other != null && other.visible(visibility) && !level.samePlayerTeam(u, other))
                    enemyCount++;
            }
            float v;
            if (u.stealthMode) {
                v = noise() * 0.3f * (3 * enemyCount + 10 * (float) Math.log((float) (enemiesDestroyed + 1) / (level.playerTeam.size() - 1))) / (u.getActionCost(Action.STEALTH).orElse(0) + 5) * (float) Math.log(u.getPerTurnActionCost(Action.STEALTH).orElse(1) + 1);
            } else
                v = noise() * (28 - 4 * enemyCount - 5 * (float) Math.log((float) (enemiesDestroyed + 1) / (level.playerTeam.size() - 1))) / (u.getActionCost(Action.STEALTH).orElse(0) + 5 * u.getPerTurnActionCost(Action.STEALTH).orElse(0) + 1);
            if (v > value.get() && v > 0.4f) {
                unit.set(u);
                action.set(Action.STEALTH);
                value.set(v);
            }
        });
        return executeMove(new BotActionData(action.get(), secondaryAction.get(), target.get(), secondaryTarget.get(), unit.get(), value.get()));
    }

    public boolean executeMove(BotActionData d) {
        if (d.value < 0)
            return false;
        if (d.action == null)
            return false;
        VisibilityData visibility = level.getVisibilityData(team);
        if (d.action == Action.MOVE) {
            TilePath path = new TilePath(d.unit.type, d.unit.tilesInMoveRange(visibility), d.unit.pos, level.tileSelector);
            path.setShortestPath(d.target, level);
            AnimTilePath animPath = path.getAnimPath(p -> d.unit.isIllegalTile(p, visibility));
            selectUnitTile(d.unit);
            anim.addTask(0.8f, () -> {
                animPath.startTimer();
                anim.addTask(((PowAnimation) animPath.getTimer()).getTime() + 0.2f, () -> {
                    if (d.secondaryAction == Action.FIRE) {
                        Unit other = level.getUnit(d.secondaryTarget);
                        selectUnitTile(other);
                        anim.addTask(0.6f, () -> {
                            anim.addTask(FiringRenderer.estimatedAnimationTime(), () -> {
                            });
                            level.levelRenderer.energyManager.canAfford(d.unit, Action.FIRE, true);
                            if (level.networkState == NetworkState.SERVER) {
                                level.server.sendUnitShootPacket(d.unit, other);
                            }
                            d.unit.addPerformedAction(Action.FIRE);
                            d.unit.attack(other);
                        });
                    }
                });
                level.levelRenderer.energyManager.canAfford(d.unit.team, animPath.getEnergyCost(d.unit, level), true);
                Point illegalTile = animPath.illegalTile(path);
                if (level.networkState == NetworkState.SERVER) {
                    level.server.sendUnitMovePacket(animPath, illegalTile, d.unit.pos, d.unit);
                }
                d.unit.startMove(animPath, illegalTile);
            });
            return true;
        }
        if (d.action == Action.FIRE) {
            Unit other = level.getUnit(d.target);
            selectUnitTile(other);
            anim.addTask(0.8f, () -> {
                anim.addTask(FiringRenderer.estimatedAnimationTime(), () -> {
                });
                level.levelRenderer.energyManager.canAfford(d.unit, Action.FIRE, true);
                if (level.networkState == NetworkState.SERVER) {
                    level.server.sendUnitShootPacket(d.unit, other);
                }
                d.unit.attack(other);
                d.unit.addPerformedAction(Action.FIRE);
            });
            return true;
        }
        if (d.action == Action.SHIELD_REGEN) {
            Unit u = d.unit;
            selectUnitTile(u);
            anim.addTask(0.8f, () -> {
                level.levelRenderer.energyManager.canAfford(u, Action.SHIELD_REGEN, true);
                u.addPerformedAction(Action.SHIELD_REGEN);
                u.setShieldHP(u.shieldHP + u.type.shieldRegen);
                if (level.networkState == NetworkState.SERVER)
                    level.server.sendUnitShieldRegenPacket(u);
            });
            return true;
        }
        if (d.action == Action.CAPTURE) {
            Unit u = d.unit;
            selectUnitTile(u);
            anim.addTask(0.8f, () -> {
                level.levelRenderer.energyManager.canAfford(u, Action.CAPTURE, true);
                u.incrementCapture();
                anim.addTask(1.5f, () -> {
                });
                if (level.networkState == NetworkState.SERVER)
                    level.server.sendUnitCapturePacket(u);
            });
            return true;
        }
        if (d.action == Action.STEALTH) {
            Unit u = d.unit;
            selectUnitTile(u);
            anim.addTask(1, () -> {
                level.levelRenderer.energyManager.canAfford(u, Action.STEALTH, true);
                u.addPerformedAction(Action.STEALTH);
                u.setStealthMode(!u.stealthMode, false);
                if (level.networkState == NetworkState.SERVER)
                    level.server.sendUnitStealthPacket(u);
                selectUnitTile(u);
                anim.addTask(1, () -> {
                });
            });
            return true;
        }
        return false;
    }

    public void updateData() {
        VisibilityData visibility = level.getVisibilityData(team);
        clearUnitData();
        for (BotTileDataType type : values()) {
            data(type).resetTiles();
        }
        thisTeamUnits = new HashSet<>();
        level.unitSet.forEach(u -> {
            if (u.team == team) {
                thisTeamUnits.add(u);
                data(ALLIED_UNITS).addValue(u.pos, 3, r -> expDecay(10, .5f, r));
            }
        });

        level.basePositions.forEach((t, pos) -> {
            if (!level.samePlayerTeam(t, team)) {
                data(ENEMY_UNIT_POSITIONS)
                        .addValue(pos, 25, r -> (float) Math.pow(100f - 4 * r, 0.7f) * 3);
                data(ENEMY_UNIT_POSITIONS)
                        .addPointValue(pos, 5);
            } else {
                Unit u = level.getUnit(pos);
                if (u != null && !level.samePlayerTeam(team, u.team) && u.canCapture()) {
                    data(ALLIED_UNITS_NEEDED).addValue(pos, 6, r -> 150 - 20f * r);
                }
            }
        });
        level.unitSet.forEach(u -> {
            if (!level.samePlayerTeam(team, u.team) && u.visible(visibility)) {
                data(ENEMY_UNIT_POSITIONS).addValue(u.pos, 4, r -> 10 - r * 2f);
                data(SCOUT_NEEDED).addValue(u.pos, 1, r -> -120f);
                for (ShipClass shipClass : ShipClass.values()) {
                    data(BotTileDataType.enemyDamageTypeFromClass(shipClass))
                            .addValue(u.pos, 4, r -> (TutorialManager.isTutorial() ? 0f : 1) * (10 - r * 2f) * u.getDamageAgainstType(shipClass.getDamageType()));
                }
            } else {
                data(SCOUT_NEEDED).addValue(u.pos, 1, r -> -30f);
            }
        });

        data(ALLIED_UNITS_NEEDED).add(data(ENEMY_UNIT_POSITIONS), 1);
        data(ALLIED_UNITS_NEEDED).add(data(ALLIED_UNITS), 0.2f);
        for (Unit unit : thisTeamUnits) {
            if (unit.hasPerformedAction(Action.MOVE))
                continue;
            BotTileData d = new BotTileData(level);

            d.add(data(ALLIED_UNITS_NEEDED), 1);
            if (unit.stealthMode)
                d.add(data(SCOUT_NEEDED), 1);
            else
                d.add(data(BotTileDataType.enemyDamageTypeFromClass(unit.type.shipClass)), -1f);
            unitMoveData.put(unit, d);
        }
    }

    private void clearUnitData() {
        unitDebugData = null;
        unitMoveData.forEach((u, d) -> d.delete());
        unitMoveData.clear();
    }

    private float expDecay(float c, float l, float x) {
        return (float) (c * Math.exp(-l * x));
    }

    private BotTileData data(BotTileDataType type) {
        return tileData.get(type);
    }

    private void selectUnitTile(Unit u) {
        if (u.renderVisible()) {
            Tile t = level.getTile(u.pos);
            level.tileSelector.select(t);
            level.levelRenderer.setCameraInterpBlockPos(t.renderPosCentered);
        } else {
            level.tileSelector.deselect();
        }
        if (level.networkState == NetworkState.SERVER)
            level.server.sendBotSelectTile(u.pos);
    }

    public void selectTileClient(Point pos) {
        Unit u = level.getUnit(pos);
        if (u == null)
            return;
        if (u.renderVisible()) {
            Tile t = level.getTile(u.pos);
            level.tileSelector.select(t);
            level.levelRenderer.setCameraInterpBlockPos(t.renderPosCentered);
        } else {
            level.tileSelector.deselect();
        }
    }

    public void incrementDestroyedUnits() {
        enemiesDestroyed++;
    }

    public int getDestroyedUnits() {
        return enemiesDestroyed;
    }

    public void loadDestroyedUnits(int count) {
        enemiesDestroyed = count;
    }

    private float noise() {
        return (float) (2 * Math.pow(Math.random(), level.botDifficulty) - 1);
    }

    @Override
    public void delete() {
        level = null;
        thisTeamUnits.clear();
        for (BotTileDataType type : values()) {
            tileData.get(type).delete();
        }
        tileData.clear();
        clearUnitData();
        anim.delete();
    }

    @Override
    public void tick(float deltaTime) {
        anim.tick(deltaTime);
        if (runningTurn.get() && !executingMove.get() && !anim.hasIncompleteTask() && !level.levelRenderer.runningAnim()) {
            executingMove.set(true);
            MainPanel.addTask(() -> {
                updateData();
                if (!findBestMove()) {
                    runningTurn.set(false);
                    MainPanel.addTaskAfterAnimBlock(level::endTurn);
                }
                executingMove.set(false);
            });
        }
    }
}

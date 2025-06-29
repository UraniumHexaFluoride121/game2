package unit.bot;

import foundation.Deletable;
import foundation.MainPanel;
import foundation.TimedTaskQueue;
import foundation.math.MathUtil;
import foundation.tick.Tickable;
import level.Level;
import level.tile.Tile;
import level.tile.TileModifier;
import level.tile.TileSet;
import level.tile.TileType;
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
        float m = Math.max(MathUtil.lerp(15, 0, level.unitSet.stream().filter(u -> u.team == team).count() / (level.levelRenderer.energyManager.incomeMap.get(team) / 12f)), 0);
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
                int energyCost = TilePath.getEnergyCost(u.stats, costMap, p);
                Tile t = level.getTile(p);
                if (!u.canUnitAppearToBeMoved(t, visibility) || energyCost > energyAvailable)
                    return;

                float v = (d.get(p) - current) / (energyCost + 2 + costModifier(m, u)) * (float) Math.pow(u.hitPoints / u.type.hitPoints, 0.5f);
                if (t.hasStructure() && t.structure.canBeCaptured && !u.type.canPerformAction(Action.CAPTURE))
                    v -= 12;
                if (u.getCaptureProgress() > 0)
                    v -= 5;
                if (u.mining)
                    v -= 15;
                v *= noise();
                if (v > value.get()) {
                    unit.set(u);
                    action.set(Action.MOVE);
                    secondaryAction.set(null);
                    target.set(p);
                    value.set(v);
                    unitDebugData = d;
                }
                UnitData data = new UnitData(u);
                data.pos = p;
                if (!u.hasPerformedAction(Action.FIRE) && u.type.canPerformAction(Action.FIRE) && !u.stealthMode
                        && energyCost + u.stats.getActionCost(Action.FIRE).get() <= energyAvailable) {
                    TileSet firingTiles = u.stats.tilesInFiringRange(visibility, data, true);
                    for (Point firingTile : firingTiles) {
                        Unit other = level.getUnit(firingTile);
                        FiringData firingData = u.getCurrentFiringData(other)
                                .setThisPos(p);
                        WeaponInstance thisWeapon = firingData.getBestWeaponAgainst(true);
                        float damageDealt = thisWeapon.getDamageAgainst(firingData);
                        if (firingData.otherData.hitPoints > 0 && thisWeapon.template.counterattack) {
                            FiringData reverse = FiringData.reverse(firingData);
                            WeaponInstance otherWeapon = reverse.getBestWeaponAgainst(true);
                            if (otherWeapon != null)
                                damageDealt -= otherWeapon.getDamageAgainst(reverse);
                        }
                        v = noise() * (d.get(p) - current + damageDealt * 30) / (energyCost + 2 + costModifier(m, u) + u.stats.getActionCost(Action.FIRE).orElse(0));
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
                }
                if (!u.hasPerformedAction(Action.REPAIR) && u.type.canPerformAction(Action.REPAIR)
                        && energyCost + u.stats.getActionCost(Action.REPAIR).get() <= energyAvailable) {
                    for (Point tile : u.stats.getRepairTiles(p)) {
                        Unit other = level.getUnit(tile);
                        float repairAmount = Math.min(u.stats.repair(), other.type.hitPoints - other.hitPoints);
                        v = noise() * (d.get(p) - current + repairAmount * 30 * (10 / other.type.hitPoints)) / (energyCost + 2 + costModifier(m, u) + u.stats.getActionCost(Action.REPAIR).orElse(0));
                        if (v > value.get()) {
                            unit.set(u);
                            action.set(Action.MOVE);
                            secondaryAction.set(Action.REPAIR);
                            target.set(p);
                            secondaryTarget.set(tile);
                            value.set(v);
                            unitDebugData = d;
                        }
                    }
                }
                if (!u.hasPerformedAction(Action.RESUPPLY) && u.type.canPerformAction(Action.RESUPPLY)
                        && energyCost + u.stats.getActionCost(Action.RESUPPLY).get() <= energyAvailable) {
                    for (Point tile : u.stats.getResupplyTiles(p)) {
                        v = noise() * (d.get(p) - current + (float) Math.pow(1 - u.ammo / (float) u.stats.ammoCapacity(), 1.3f) * 40) / (energyCost + 2 + costModifier(m, u) + u.stats.getActionCost(Action.RESUPPLY).orElse(0));
                        if (v > value.get()) {
                            unit.set(u);
                            action.set(Action.MOVE);
                            secondaryAction.set(Action.RESUPPLY);
                            target.set(p);
                            secondaryTarget.set(tile);
                            value.set(v);
                            unitDebugData = d;
                        }
                    }
                }
            });
        });
        thisTeamUnits.forEach(u -> {
            if (u.hasPerformedAction(Action.FIRE) || !u.type.canPerformAction(Action.FIRE) || u.stats.getActionCost(Action.FIRE).orElse(Integer.MAX_VALUE) > energyAvailable || u.stealthMode)
                return;
            TileSet firingTiles = u.stats.tilesInFiringRange(visibility, new UnitData(u), true);
            for (Point firingTile : firingTiles) {
                Unit other = level.getUnit(firingTile);
                FiringData firingData = u.getCurrentFiringData(other);
                WeaponInstance thisWeapon = firingData.getBestWeaponAgainst(true);
                float damageDealt = thisWeapon.getDamageAgainst(firingData);
                if (firingData.otherData.hitPoints > 0 && thisWeapon.template.counterattack) {
                    FiringData reverse = FiringData.reverse(firingData);
                    WeaponInstance otherWeapon = reverse.getBestWeaponAgainst(true);
                    if (otherWeapon != null)
                        damageDealt -= otherWeapon.getDamageAgainst(reverse);
                }
                Tile t = level.getTile(firingTile);
                float v = (damageDealt * 30) / (u.stats.getActionCost(Action.FIRE).orElse(0) + costModifier(m, u));
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
            if (u.type.canPerformAction(Action.MINE) && !u.mining && !u.hasPerformedAction(Action.MINE) && level.getTile(u.pos).type == TileType.ASTEROIDS) {
                float v = (noise() + 1) * 5;
                if (v > value.get()) {
                    unit.set(u);
                    action.set(Action.MINE);
                    secondaryAction.set(null);
                    value.set(v);
                }
            }
        });
        thisTeamUnits.forEach(u -> {
            if (!u.hasPerformedAction(Action.REPAIR) && u.type.canPerformAction(Action.REPAIR)
                    && u.stats.getActionCost(Action.REPAIR).get() <= energyAvailable) {
                for (Point tile : u.stats.getRepairTiles(u.pos)) {
                    Unit other = level.getUnit(tile);
                    float repairAmount = Math.min(u.stats.repair(), other.type.hitPoints - other.hitPoints);
                    float v = noise() * (repairAmount * 30 * (10 / other.type.hitPoints)) / (u.stats.getActionCost(Action.REPAIR).orElse(0) + costModifier(m, u));
                    if (v > value.get()) {
                        unit.set(u);
                        action.set(Action.REPAIR);
                        secondaryAction.set(null);
                        target.set(tile);
                        value.set(v);
                    }
                }
            }
        });
        thisTeamUnits.forEach(u -> {
            if (!u.hasPerformedAction(Action.RESUPPLY) && u.type.canPerformAction(Action.RESUPPLY)
                    && u.stats.getActionCost(Action.RESUPPLY).get() <= energyAvailable) {
                for (Point tile : u.stats.getResupplyTiles(u.pos)) {
                    float v = noise() * ((float) Math.pow(1 - u.ammo / (float) u.stats.ammoCapacity(), 1.3f) * 40) / (u.stats.getActionCost(Action.RESUPPLY).orElse(0) + costModifier(m, u));
                    if (v > value.get()) {
                        unit.set(u);
                        action.set(Action.RESUPPLY);
                        secondaryAction.set(null);
                        target.set(tile);
                        value.set(v);
                    }
                }
            }
        });
        thisTeamUnits.forEach(u -> {
            if (u.hasPerformedAction(Action.SHIELD_REGEN) || !u.type.canPerformAction(Action.SHIELD_REGEN))
                return;
            int cost = u.stats.getActionCost(Action.SHIELD_REGEN).get();
            if (cost > energyAvailable)
                return;
            float regen = Math.min(u.stats.shieldRegen(), u.stats.maxShieldHP() - u.shieldHP);
            float v = noise() * 15 * regen / (cost + costModifier(m, u));
            if (regen > 0 && v > value.get()) {
                unit.set(u);
                action.set(Action.SHIELD_REGEN);
                secondaryAction.set(null);
                value.set(v);
            }
        });
        thisTeamUnits.forEach(u -> {
            if (u.hasPerformedAction(Action.CAPTURE) || !u.type.canPerformAction(Action.CAPTURE) || u.stats.getActionCost(Action.CAPTURE).orElse(Integer.MAX_VALUE) > energyAvailable)
                return;
            Tile t = level.getTile(u.pos);
            if (t.hasStructure() && t.structure.canBeCaptured && !level.samePlayerTeam(team, t.structure.team)) {
                TileSet surrounding = TileSet.tilesInRadius(u.pos, 2, level);
                boolean hasUnit = false;
                for (Point p : surrounding) {
                    Unit other = level.getUnit(p);
                    if (other != null && !level.samePlayerTeam(u, other) && other.visible(visibility) && other.type.canPerformAction(Action.FIRE)) {
                        hasUnit = true;
                        break;
                    }
                }
                boolean finalStep = t.structure.type.captureSteps == u.getCaptureProgress() + 1;
                float v = noise() * (u.getCaptureProgress() <= 0 ? 5f : finalStep ? 20 : 10);
                if ((!hasUnit || finalStep) && v > value.get()) {
                    unit.set(u);
                    action.set(Action.CAPTURE);
                    secondaryAction.set(null);
                    value.set(v);
                }
            }
        });
        thisTeamUnits.forEach(u -> {
            if (u.hasPerformedAction(Action.STEALTH) || !u.type.canPerformAction(Action.STEALTH) || u.stats.getActionCost(Action.STEALTH).orElse(Integer.MAX_VALUE) > energyAvailable)
                return;
            int enemyCount = 0;
            for (Point p : TileSet.tilesInRadius(u.pos, 6, level)) {
                Unit other = level.getUnit(p);
                if (other != null && other.visible(visibility) && !level.samePlayerTeam(u, other))
                    enemyCount++;
            }
            float v;
            if (u.stealthMode) {
                v = noise() * 0.3f * (3 * enemyCount + 10 * (float) Math.log((float) (enemiesDestroyed + 1) / (level.playerTeam.size() - 1))) / (u.stats.getActionCost(Action.STEALTH).orElse(0) + 5) * (float) Math.log(u.stats.getPerTurnActionCost(Action.STEALTH).orElse(1) + 1 + costModifier(m, u));
            } else
                v = noise() * (28 - 4 * enemyCount - 5 * (float) Math.log((float) (enemiesDestroyed + 1) / (level.playerTeam.size() - 1))) / (u.stats.getActionCost(Action.STEALTH).orElse(0) + 5 * u.stats.getPerTurnActionCost(Action.STEALTH).orElse(0) + 1 + costModifier(m, u));
            if (v > value.get() && v > 0.4f) {
                unit.set(u);
                action.set(Action.STEALTH);
                secondaryAction.set(null);
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
            TilePath path = new TilePath(d.unit.stats.tilesInMoveRange(visibility), d.unit.pos, level.tileSelector);
            path.setShortestPath(d.target, d.unit.stats, level);
            AnimTilePath animPath = path.getAnimPath(p -> d.unit.isIllegalTile(p, visibility));
            selectUnitTile(d.unit);
            anim.addTask(0.8f, () -> {
                animPath.startTimer();
                Point illegalTile = animPath.illegalTile(path);
                anim.addTask(((PowAnimation) animPath.getTimer()).getTime() + 0.2f, () -> {
                    if (d.secondaryAction == Action.FIRE && illegalTile == null) {
                        Unit other = level.getUnit(d.secondaryTarget);
                        anim.addTask(0.6f, () -> {
                            anim.addTask(FiringRenderer.estimatedAnimationTime(level.gameplaySettings.showFiringAnim), () -> {
                            });
                            level.levelRenderer.energyManager.canAfford(d.unit, Action.FIRE, true);
                            if (level.networkState == NetworkState.SERVER) {
                                level.server.sendUnitShootPacket(d.unit, other);
                            }
                            d.unit.addPerformedAction(Action.FIRE);
                            d.unit.attack(other);
                        });
                    }
                    if (d.secondaryAction == Action.REPAIR && illegalTile == null) {
                        Unit other = level.getUnit(d.secondaryTarget);
                        anim.addTask(0.6f, () -> {
                            level.levelRenderer.energyManager.canAfford(d.unit, Action.REPAIR, true);
                            d.unit.repair(other);
                        });
                    }
                    if (d.secondaryAction == Action.RESUPPLY && illegalTile == null) {
                        Unit other = level.getUnit(d.secondaryTarget);
                        anim.addTask(0.6f, () -> {
                            level.levelRenderer.energyManager.canAfford(d.unit, Action.RESUPPLY, true);
                            d.unit.resupply(other);
                        });
                    }
                });
                level.levelRenderer.energyManager.canAfford(d.unit.team, animPath.getEnergyCost(d.unit, level), true);
                if (level.networkState == NetworkState.SERVER) {
                    level.server.sendUnitMovePacket(animPath, illegalTile, d.unit.pos, d.unit);
                }
                d.unit.startMove(animPath, illegalTile);
            });
            return true;
        }
        if (d.action == Action.FIRE) {
            Unit other = level.getUnit(d.target);
            anim.addTask(0.8f, () -> {
                anim.addTask(FiringRenderer.estimatedAnimationTime(level.gameplaySettings.showFiringAnim), () -> {
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
        if (d.action == Action.MINE) {
            anim.addTask(0.8f, d.unit::performMiningAction);
            return true;
        }
        if (d.action == Action.REPAIR) {
            Unit u = d.unit, other = level.getUnit(d.target);
            anim.addTask(0.8f, () -> {
                level.levelRenderer.energyManager.canAfford(u, Action.REPAIR, true);
                u.repair(other);
            });
            return true;
        }
        if (d.action == Action.RESUPPLY) {
            Unit u = d.unit, other = level.getUnit(d.target);
            anim.addTask(0.8f, () -> {
                level.levelRenderer.energyManager.canAfford(u, Action.RESUPPLY, true);
                u.resupply(other);
            });
            return true;
        }
        if (d.action == Action.SHIELD_REGEN) {
            Unit u = d.unit;
            anim.addTask(0.8f, () -> {
                level.levelRenderer.energyManager.canAfford(u, Action.SHIELD_REGEN, true);
                u.regenShield();
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
                u.setStealthMode(!u.stealthMode);
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
        level.tileSelector.tileSet.forEach(t -> {
            if (t.type == TileType.ASTEROIDS) {
                data(ASTEROID_FIELDS).addValue(t.pos, 10, r -> (float) Math.pow(50f - 4 * r, 0.7f) / 15);
                data(ASTEROID_FIELDS).addPointValue(t.pos, 25);
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
        level.tileSelector.tileSet.forEach(t -> {
            if (!t.hasStructure())
                return;
            if (!level.samePlayerTeam(t.structure.team, team) && TileSet.tilesInRadius(t.pos, 2, level).m(level, m -> m.unitFilter(TileModifier.withVisibleEnemies(team, level).and(u -> u.type.canPerformAction(Action.FIRE)))).isEmpty()) {
                data(ENEMY_UNIT_POSITIONS)
                        .addValue(t.pos, 6, r -> (float) Math.pow(26f - 4 * r, 0.7f) * 0.75f);
                data(ENEMY_UNIT_POSITIONS)
                        .addPointValue(t.pos, 3);
            }
        });
        level.unitSet.forEach(u -> {
            if (!level.samePlayerTeam(team, u.team) && u.visible(visibility)) {
                data(ENEMY_UNIT_POSITIONS).addValue(u.pos, 4, r -> 10 - r * 2f);
                data(SCOUT_NEEDED).addValue(u.pos, 1, r -> -120f);
                for (ShipClass shipClass : ShipClass.values()) {
                    data(enemyDamageTypeFromClass(shipClass))
                            .addValue(u.pos, 4, r -> (TutorialManager.isTutorial() ? 0f : 1) * (10 - r * 2f) * MathUtil.map(0.5f, 1.5f, 0, 5, u.getDamageAgainstClass(shipClass)));
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

            if (unit.type.canPerformAction(Action.FIRE))
                d.add(data(ALLIED_UNITS_NEEDED), 1);
            else if (unit.type.canPerformAction(Action.MINE)) {
                d.add(data(ASTEROID_FIELDS), 1f);
                d.add(data(ENEMY_UNIT_POSITIONS), -0.1f);
                d.add(data(enemyDamageTypeFromClass(unit.type.shipClass)), -2f);
            } else {
                d.add(data(ALLIED_UNITS), 2);
                d.add(data(enemyDamageTypeFromClass(unit.type.shipClass)), -0.5f);
            }
            if (unit.stealthMode)
                d.add(data(SCOUT_NEEDED), 1);
            else
                d.add(data(enemyDamageTypeFromClass(unit.type.shipClass)), -1f);
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

    private float costModifier(float value, Unit u) {
        return u.type.canPerformAction(Action.FIRE) || u.type.canPerformAction(Action.REPAIR) ? value : 0;
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
                    MainPanel.addTaskAfterAnimBlock(level::preEndTurn);
                }
                executingMove.set(false);
            });
        }
    }
}

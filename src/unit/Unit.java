package unit;

import foundation.Deletable;
import foundation.MainPanel;
import foundation.math.HexagonalDirection;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import level.Level;
import level.structure.StructureType;
import level.tile.*;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventActionComplete;
import level.tutorial.sequence.event.EventActionPerform;
import level.tutorial.sequence.event.EventActionSelect;
import network.NetworkState;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;
import render.anim.AnimTilePath;
import render.anim.ImageSequenceAnim;
import render.anim.LerpAnimation;
import render.anim.SineAnimation;
import render.level.tile.HexagonBorder;
import render.level.tile.HighlightTileRenderer;
import render.level.tile.TileFlash;
import render.level.tile.TilePath;
import render.level.ui.UnitDamageNumberUI;
import render.level.ui.UnitTextUI;
import render.texture.ImageSequenceGroup;
import render.types.text.DynamicTextRenderer;
import unit.action.Action;
import unit.action.ActionSelector;
import unit.action.ActionShapes;
import unit.action.UnitFiringExplosion;
import unit.bot.VisibilityData;
import unit.stats.Modifier;
import unit.stats.ModifierCategory;
import unit.stats.StatManager;
import unit.stats.modifiers.WeaponDamageModifier;
import unit.type.UnitType;
import unit.weapon.FiringData;
import unit.weapon.WeaponInstance;
import unit.weapon.WeaponTemplate;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static level.tile.Tile.*;
import static unit.action.Action.*;

public class Unit implements Deletable, Tickable {
    public static final Color HP_BACKGROUND_COLOUR = new Color(67, 67, 67);
    public static final Color SHIELD_HP_BACKGROUND_COLOUR = new Color(79, 115, 140);
    public final UnitType type;
    public final UnitTeam team;
    public StatManager stats;
    public Point pos;
    private int captureProgress = -1;
    public boolean stealthMode = false, visibleInStealthMode = true, mining = false;

    private ObjPos renderPos;

    private final SineAnimation idleAnimX, idleAnimY, moveAnimX, moveAnimY;
    private final LerpAnimation stealthTransparencyAnim = new LerpAnimation(1).finish();

    private ActionSelector actionUI = new ActionSelector(() -> {
        if (getLevel().getThisTeam() != getLevel().getActiveTeam() || getLevel().getThisTeam() != getTeam())
            return false;
        if (getLevel().getActiveAction() != null || getLevel().levelRenderer.uiUnitInfo.showFiringRange() || getLevel().levelRenderer.uiUnitInfo.showEffectiveness())
            return false;
        Tile selectedTile = getLevel().tileSelector.getSelectedTile();
        return selectedTile != null && selectedTile.pos.equals(getPos());
    }, this);

    public int ammo;
    public final ArrayList<WeaponInstance> weapons = new ArrayList<>();
    private final HashSet<Action> performedActions = new HashSet<>();

    public float hitPoints, firingTempHP;
    public float shieldHP, shieldFiringTempHP;
    public float lowestHP;
    private final ArrayList<UnitTextUI> damageUIs = new ArrayList<>();
    private final DynamicTextRenderer hitPointText = new DynamicTextRenderer(() -> MathUtil.floatToString((float) Math.ceil(hitPoints), 0), 0.7f, Color.WHITE)
            .setTextAlign(HorizontalAlign.RIGHT)
            .setBold(true)
            .setRenderBorder(0.1f, 0.3f, HP_BACKGROUND_COLOUR);
    private final DynamicTextRenderer shieldHitPointText = new DynamicTextRenderer(() -> MathUtil.floatToString((float) Math.ceil(shieldHP), 0), 0.7f, Color.WHITE)
            .setTextAlign(HorizontalAlign.LEFT)
            .setBold(true)
            .setRenderBorder(0.1f, 0.3f, SHIELD_HP_BACKGROUND_COLOUR);

    private Level level;

    public Unit(UnitType type, UnitTeam team, Point pos, Level level) {
        this.type = type;
        this.team = team;
        this.pos = pos;
        this.level = level;
        stats = new StatManager(this);
        selectableTiles = new TileSet(level.tilesX, level.tilesY);
        hitPoints = stats.maxHP();
        firingTempHP = hitPoints;
        lowestHP = hitPoints;
        shieldHP = stats.maxShieldHP();
        ammo = stats.ammoCapacity();
        shieldFiringTempHP = shieldHP;
        level.buttonRegister.register(actionUI);
        renderPos = Tile.getRenderPos(pos);
        actionUI.setActions(type.actions, this);
        for (WeaponTemplate template : type.weapons) {
            weapons.add(new WeaponInstance(template));
        }
        idleAnimX = new SineAnimation((5f + (float) Math.random()) * type.getBobbingRate(), (float) Math.random() * 360);
        idleAnimY = new SineAnimation((7f + (float) Math.random()) * type.getBobbingRate(), (float) Math.random() * 360);
        moveAnimX = new SineAnimation((1f + (float) Math.random() * .3f) * type.getBobbingRate(), (float) Math.random() * 360);
        moveAnimY = new SineAnimation((1.5f + (float) Math.random() * .3f) * type.getBobbingRate(), (float) Math.random() * 360);
    }

    public void renderTile(Graphics2D g) {
        if (level == null || explosion != null) return;
        Composite c = null;
        if (!stealthTransparencyAnim.finished()) {
            c = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, stealthTransparencyAnim.normalisedProgress()));
        } else if (stealthMode && !isRenderStealthVisible()) return;
        GameRenderer.renderOffset(getShipRenderPos(), g, () -> {
            g.translate(-TILE_SIZE / 2, 0);
            type.tileRenderer(team, getPose()).render(g);
            if (shieldHP > 0)
                type.shieldRenderer.render(g, TILE_SIZE);
        });
        if (path == null) {
            GameRenderer.renderOffset(getRenderPos(), g, () -> {
                g.translate(TILE_SIZE / 4.5f, TILE_SIZE / 15);
                GameRenderer.renderTransformed(g, () -> {
                    hitPointText.render(g);
                });
                if (stats.maxShieldHP() != 0) {
                    g.translate(-TILE_SIZE * 0.45f, 0);
                    shieldHitPointText.render(g);
                }
            });
            if (stealthMode) {
                GameRenderer.renderOffsetScaled(renderPos, TILE_SIZE * 0.3f, g, () -> {
                    g.translate(-1, -0.1);
                    g.setColor(ICON_COLOUR);
                    g.fill(ActionShapes.STEALTH);
                });
            }
            if (mining) {
                GameRenderer.renderOffsetScaled(renderPos, TILE_SIZE * 0.25f, g, () -> {
                    g.translate(-1.2, 0);
                    g.setColor(ICON_COLOUR);
                    g.fill(ActionShapes.ENERGY);
                });
            }
        }
        if (c != null)
            g.setComposite(c);
    }

    public void renderActions(Graphics2D g) {
        if (level == null)
            return;
        if (level.levelRenderer.runningAnim())
            return;
        GameRenderer.renderOffset(getRenderPos(), g, () -> {
            actionUI.render(g);
        });
    }

    public void renderActionBelowUnits(Graphics2D g) {
        if (level == null)
            return;
        if (level.selectedUnit == this) {
            if (movePath != null) {
                if (selector().mouseOverTile != null)
                    movePath.setEnd(selector().mouseOverTile.pos, stats, level);
                if (!level.hasActiveAction())
                    movePath = null;
                else {
                    level.levelRenderer.movementModifiers.show(movePath, this, g);
                    movePath.render(g);
                }
            }
        }
    }

    public static final BasicStroke AIM_TARGET_STROKE = new BasicStroke(0.03f * Renderable.SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    private final SineAnimation targetAnim = new SineAnimation(2, 90);

    private ImageSequenceAnim explosion = null;
    private UnitFiringExplosion firingExplosion = null;

    public synchronized void renderActionAboveUnits(Graphics2D g) {
        if (level == null || (stealthMode && !visibleInStealthMode)) return;
        damageUIs.removeIf(e -> e.render(g));
        if (level.selectedUnit == this) {
            if (level.getActiveAction() == FIRE) {
                Tile tile = selector().mouseOverTile;
                if (tile != null && selectableTiles.contains(tile.pos)) {
                    GameRenderer.renderOffsetScaled(Tile.getCenteredRenderPos(tile.pos), TILE_SIZE / Renderable.SCALING, g, () -> {
                        g.setStroke(AIM_TARGET_STROKE);
                        g.setColor(ATTACK_TILE_FLASH);
                        float anim = MathUtil.map(-1, 1, 1f, 1.2f, targetAnim.normalisedProgress());

                        g.drawOval((int) (-0.4f * Renderable.SCALING * anim), (int) (-0.4f * Renderable.SCALING * anim), (int) (0.8f * Renderable.SCALING * anim), (int) (0.8f * Renderable.SCALING * anim));

                        g.drawLine((int) (-0.53f * Renderable.SCALING * anim), 0, (int) (-0.27f * Renderable.SCALING * anim), 0);
                        g.drawLine((int) (0.53f * Renderable.SCALING * anim), 0, (int) (0.27f * Renderable.SCALING * anim), 0);
                        g.drawLine(0, (int) (-0.53f * Renderable.SCALING * anim), 0, (int) (-0.27f * Renderable.SCALING * anim));
                        g.drawLine(0, (int) (0.53f * Renderable.SCALING * anim), 0, (int) (0.27f * Renderable.SCALING * anim));
                    });
                    if (!level.levelRenderer.isFiring()) {
                        Unit unit = level.getUnit(tile.pos);
                        level.levelRenderer.damageUI.show(this, unit);
                        level.levelRenderer.damageModifiers.show(tile.pos, this, unit, g);
                    }
                } else
                    targetAnim.startTimer();
            }
        }
        if (firingExplosion != null) {
            firingExplosion.render(g);
            if (firingExplosion.timer().normalisedProgress() > 0.8f) {
                level.levelRenderer.removeAnimBlock(firingExplosion);
            }
            if (firingExplosion.finished()) {
                firingExplosion.delete();
                firingExplosion = null;
            }
        }
        if (explosion != null) {
            GameRenderer.renderOffset(getRenderPos().copy().add(0, TILE_SIZE * SIN_60_DEG / 2), g, () -> {
                explosion.render(g);
            });
            if (explosion.timer.normalisedProgress() > 0.5f)
                level.levelRenderer.disableCameraShake();
            if (explosion.timer.finished()) {
                level.levelRenderer.removeAnimBlock(explosion);
                level.qRemoveUnit(this);
            }
        }
    }

    public synchronized void renderEnergyCostIndicator(Graphics2D g) {
        if (level == null) return;
        if (level.selectedUnit == this && movePath != null)
            movePath.renderEnergyCost(g, stats, level);
    }

    public static final Color MOVE_TILE_BORDER_COLOUR = new Color(107, 184, 248, 192),
            FIRE_TILE_BORDER_COLOUR = new Color(243, 103, 103, 192),
            REPAIR_TILE_BORDER_COLOUR = new Color(116, 250, 69, 205),
            RESUPPLY_TILE_BORDER_COLOUR = new Color(124, 86, 45, 205);

    public static final Color ATTACK_TILE_FLASH = new Color(221, 68, 68),
            SHIELD_REGEN_TILE_FLASH = new Color(69, 235, 250),
            MINE_TILE_FLASH = new Color(156, 69, 250),
            REPAIR_TILE_FLASH = new Color(116, 250, 69),
            RESUPPLY_TILE_FLASH = new Color(124, 86, 45);

    public synchronized void onActionSelect(Action action) {
        if (!TutorialManager.actionEnabled(action))
            return;
        if (action == MOVE && TutorialManager.actionTileNotSelectable(pos, MOVE_FROM))
            return;
        TutorialManager.acceptEvent(new EventActionSelect(level, action));
        if (action == MOVE) {
            setSelectableTiles(action, MOVE_TILE_BORDER_COLOUR, stats.tilesInMoveRange(level.currentVisibility));
            movePath = new TilePath(selectableTiles, pos, selector());
            level.levelRenderer.exitActionButton.setEnabled(true);
        } else if (action == FIRE) {
            setSelectableTiles(action, FIRE_TILE_BORDER_COLOUR, stats.tilesInFiringRange(level.currentVisibility, new UnitData(this), true));
            level.levelRenderer.exitActionButton.setEnabled(true);
        } else if (action == CAPTURE) {
            if (!level.levelRenderer.energyManager.canAfford(this, CAPTURE, true)) return;
            endAndDeselectAction();
            if (level.networkState == NetworkState.CLIENT) {
                MainPanel.client.sendUnitCaptureRequest(this);
                return;
            }
            captureAction();
            if (level.networkState == NetworkState.SERVER)
                level.server.sendUnitCapturePacket(this, true);
            TutorialManager.acceptEvent(new EventActionPerform(level, CAPTURE));
            return;
        } else if (action == SHIELD_REGEN) {
            if (!level.levelRenderer.energyManager.canAfford(this, SHIELD_REGEN, true)) return;
            endAction();
            regenShield();
            TutorialManager.acceptEvent(new EventActionPerform(level, SHIELD_REGEN));
        } else if (action == STEALTH) {
            if (!level.levelRenderer.energyManager.canAfford(this, STEALTH, true)) return;
            endAction();
            stealth();
            TutorialManager.acceptEvent(new EventActionPerform(level, STEALTH));
        } else if (action == MINE) {
            endAndDeselectAction();
            if (level.networkState == NetworkState.CLIENT) {
                MainPanel.client.sendUnitMineRequest(this);
                return;
            }
            performMiningAction();
            TutorialManager.acceptEvent(new EventActionPerform(level, MINE));
        } else if (action == REPAIR) {
            setSelectableTiles(action, REPAIR_TILE_BORDER_COLOUR, stats.getRepairTiles(pos));
            level.levelRenderer.exitActionButton.setEnabled(true);
        } else if (action == RESUPPLY) {
            setSelectableTiles(action, RESUPPLY_TILE_BORDER_COLOUR, stats.getResupplyTiles(pos));
            level.levelRenderer.exitActionButton.setEnabled(true);
        } else {
            selectableTiles.clear();
        }
        level.setActiveAction(action);
    }

    //Called when there is an active action and this unit is selected
    public synchronized void onTileClicked(Tile tile, Action action) {
        if (tile == null || level == null || TutorialManager.actionTileNotSelectable(tile.pos, action)) return;
        if (level.networkState == NetworkState.CLIENT) {
            onTileClickedAsClient(tile, action);
            return;
        }
        if (selectableTiles.contains(tile.pos)) {
            if (action == MOVE) {
                if (movePath.isRangeValid() && canUnitAppearToBeMoved(tile, level.currentVisibility) && stats.canAffordMove(movePath)) {
                    AnimTilePath path = movePath.getAnimPath(p -> isIllegalTile(p, level.currentVisibility));
                    level.levelRenderer.energyManager.canAfford(team, path.getEnergyCost(this, level), true);
                    startMove(path, path.illegalTile(movePath));
                    endAndDeselectAction();
                    if (level.networkState == NetworkState.SERVER)
                        level.server.sendUnitMovePacket(path, illegalTile, pos, this);
                    TutorialManager.acceptEvent(new EventActionPerform(level, MOVE));
                }
            } else if (action == FIRE) {
                if (!level.levelRenderer.energyManager.canAfford(this, FIRE, true)) return;
                Unit other = level.getUnit(tile.pos);
                if (level.networkState == NetworkState.SERVER)
                    level.server.sendUnitShootPacket(this, other);
                attack(other);
                addPerformedAction(FIRE);
                endAndDeselectAction();
                TutorialManager.acceptEvent(new EventActionPerform(level, FIRE));
            } else if (action == REPAIR) {
                if (!level.levelRenderer.energyManager.canAfford(this, REPAIR, true)) return;
                repair(level.getUnit(tile.pos));
                TutorialManager.acceptEvent(new EventActionPerform(level, REPAIR));
                endAndDeselectAction();
            } else if (action == RESUPPLY) {
                if (!level.levelRenderer.energyManager.canAfford(this, RESUPPLY, true)) return;
                resupply(level.getUnit(tile.pos));
                endAndDeselectAction();
                TutorialManager.acceptEvent(new EventActionPerform(level, RESUPPLY));
            }
        }
    }

    public synchronized void onTileClickedAsClient(Tile tile, Action action) {
        if (selectableTiles.contains(tile.pos)) {
            if (action == MOVE) {
                if (movePath.isRangeValid() && canUnitAppearToBeMoved(tile, level.currentVisibility) && stats.canAffordMove(movePath)) {
                    AnimTilePath path = movePath.getAnimPath(p -> isIllegalTile(p, level.currentVisibility));
                    level.levelRenderer.energyManager.canAfford(team, path.getEnergyCost(this, level), true);
                    MainPanel.client.sendUnitMoveRequest(path, path.illegalTile(movePath), this);
                    endAndDeselectAction();
                }
            } else if (action == FIRE) {
                if (!level.levelRenderer.energyManager.canAfford(this, FIRE, true)) return;
                MainPanel.client.sendUnitShootRequest(this, level.getUnit(tile.pos));
                endAndDeselectAction();
            } else if (action == REPAIR) {
                if (!level.levelRenderer.energyManager.canAfford(this, REPAIR, true)) return;
                MainPanel.client.sendUnitRepairRequest(this, level.getUnit(tile.pos));
                endAndDeselectAction();
            } else if (action == RESUPPLY) {
                if (!level.levelRenderer.energyManager.canAfford(this, RESUPPLY, true)) return;
                MainPanel.client.sendUnitResupplyRequest(this, level.getUnit(tile.pos));
                endAndDeselectAction();
            }
        }
    }

    public boolean isIllegalTile(Point t, VisibilityData visibility) {
        Unit u = level.getUnit(t);
        if (u == null)
            return false;
        return !u.visible(visibility) && !level.samePlayerTeam(u, this);
    }

    private void stealth() {
        if (level.networkState == NetworkState.CLIENT) {
            MainPanel.client.sendUnitStealthRequest(this);
            return;
        }
        setStealthMode(!stealthMode);
        addPerformedAction(STEALTH);
        level.updateSelectedUnit();
        if (level.networkState == NetworkState.SERVER)
            level.server.sendUnitStealthPacket(this);
    }

    public void setStealthMode(boolean stealth) {
        if (stealthMode != stealth) {
            cameraToIgnoreStealth();
            if (!visibleInStealthMode) {
                stealthTransparencyAnim.setReversed(stealth);
                stealthTransparencyAnim.startTimer();
            }
        }
        stealthMode = stealth;
        level.levelRenderer.energyManager.recalculateIncome();
        level.updateSelectedUnit();
    }

    public void setMining(boolean mining) {
        if (mining == this.mining)
            return;
        this.mining = mining;
        level.levelRenderer.energyManager.recalculateIncome();
    }

    public void performMiningAction(boolean mining) {
        setMining(mining);
        tileFlash(MINE_TILE_FLASH);
        addPerformedAction(MINE);
        cameraTo();
        level.updateSelectedUnit();
        if (level.networkState == NetworkState.SERVER)
            level.server.sendUnitMinePacket(this);
    }

    public void performMiningAction() {
        performMiningAction(!mining);
    }

    public void regenShield() {
        if (level.networkState == NetworkState.CLIENT) {
            MainPanel.client.sendUnitShieldRegenRequest(this);
            return;
        }
        regenShield(shieldHP + stats.shieldRegen());
        if (level.networkState == NetworkState.SERVER)
            level.server.sendUnitShieldRegenPacket(this);
    }

    public void regenShield(float newHP) {
        addPerformedAction(Action.SHIELD_REGEN);
        setShieldHP(newHP);
        cameraTo();
        level.updateSelectedUnit();
        tileFlash(SHIELD_REGEN_TILE_FLASH);
    }

    public void repair(Unit other) {
        other.cameraTo(team);
        if (level.networkState == NetworkState.SERVER) {
            level.server.sendUnitRepairPacket(this, other);
        }
        other.setHP(other.hitPoints + stats.repair());
        addPerformedAction(REPAIR);
        other.tileFlash(REPAIR_TILE_FLASH);
    }

    public void resupply(Unit other) {
        other.cameraTo(team);
        if (level.networkState == NetworkState.SERVER) {
            level.server.sendUnitResupplyPacket(this, other);
        }
        other.resupply();
        other.showResupply();
        addPerformedAction(RESUPPLY);
    }

    public void resupply() {
        ammo = stats.ammoCapacity();
    }

    public void showResupply() {
        if (renderVisible()) {
            tileFlash(RESUPPLY_TILE_FLASH);
            ObjPos pos = getRenderPos();
            damageUIs.add(new UnitTextUI("RESUPPLY", pos.x, pos.y, 0.5f, 1, UnitTextUI.RESUPPLY_COLOR));
        }
    }

    public void setShieldHP(float newHP) {
        newHP = Math.clamp(newHP, 0, stats.maxShieldHP());
        addDamageUI(newHP - shieldHP, true);
        shieldHP = newHP;
        shieldFiringTempHP = shieldHP;
    }

    public void setHP(float newHP) {
        newHP = Math.clamp(newHP, 0, stats.maxHP());
        addDamageUI(newHP - hitPoints, false);
        hitPoints = newHP;
        firingTempHP = hitPoints;
        lowestHP = Math.min(lowestHP, hitPoints);
    }

    public void addDamageUI(float value, boolean shield) {
        if (value == 0 || !visible(level.currentVisibility))
            return;
        ObjPos pos = getRenderPos();
        damageUIs.add(new UnitDamageNumberUI(value, pos.x + (shield ? -TILE_SIZE * 0.2f : TILE_SIZE * 0.2f), pos.y, value > 0 ? 1 : -0.7f, shield));
    }

    public void capture(int captureProgress, boolean action) {
        Tile tile = level.getTile(pos);
        if (this.captureProgress == -1 && captureProgress != -1)
            tile.startCapturing(team.uiColour);
        if (captureProgress != -1) {
            tile.setProgress(captureProgress, level, () -> {
                finishCapture(tile);
            });
        }
        if (action) {
            cameraTo();
            addPerformedAction(CAPTURE);
        }
        this.captureProgress = captureProgress;
    }

    private void finishCapture(Tile tile) {
        if (level.networkState == NetworkState.CLIENT)
            return;
        if (captureProgress >= tile.structure.type.captureSteps) {
            level.levelRenderer.setCameraInterpBlockPos(tile.renderPosCentered);
            if (tile.structure.type == StructureType.BASE) {
                level.removePlayer(tile.structure.team);
            }
            if (tile.structure.type.destroyedOnCapture) {
                tile.explodeStructure();
            } else {
                tile.structure.setTeam(team);
            }
            stopCapture();
            level.levelRenderer.energyManager.recalculateIncome();
            if (level.networkState == NetworkState.SERVER) {
                level.server.sendStructurePacket(tile, true);
            }
        }
    }

    public void captureAction() {
        if (isCapturing()) {
            stopCapture();
            addPerformedAction(CAPTURE);
        } else
            incrementCapture(true);
    }

    public void incrementCapture(boolean action) {
        int newCaptureProgress = captureProgress;
        if (newCaptureProgress == -1)
            newCaptureProgress = 1;
        else
            newCaptureProgress++;
        capture(newCaptureProgress, action);
    }

    public void decrementCapture() {
        int newCaptureProgress = captureProgress;
        if (newCaptureProgress <= 0)
            return;
        newCaptureProgress--;
        capture(newCaptureProgress, false);
    }

    public boolean isCapturing() {
        return captureProgress != -1;
    }

    public void setCaptureProgress(int captureProgress) {
        capture(captureProgress, false);
    }

    public boolean canCapture() {
        Tile tile = level.getTile(pos);
        return type.canCapture && tile.hasStructure() && !level.samePlayerTeam(tile.structure.team, team) && tile.structure.canBeCaptured;
    }

    public void stopCapture() {
        captureProgress = -1;
    }

    public int getCaptureProgress() {
        return captureProgress;
    }

    public void startMove(AnimTilePath path, Point illegalTile) {
        if (mining)
            setMining(false);
        this.path = path;
        this.illegalTile = illegalTile;
        addPerformedAction(MOVE);
        level.levelRenderer.registerAnimBlock(path);
    }

    public boolean canFireAt(Unit other) {
        if (other == null)
            return false;
        return !level.samePlayerTeam(this, other);
    }

    public void clientAttack(Unit other) {
        if (level.networkState == NetworkState.SERVER) {
            level.server.sendUnitShootPacket(this, other);
        }
        attack(other);
        addPerformedAction(FIRE);
    }

    public void attack(Unit other) {
        FiringData firingData = getCurrentFiringData(other);
        WeaponInstance thisWeapon = firingData.getBestWeaponAgainst(true);
        WeaponInstance otherWeapon = null;
        if (firingData.otherData.hitPoints > 0 && thisWeapon.template.counterattack) {
            otherWeapon = FiringData.reverse(firingData).getBestWeaponAgainst(true);
        }
        firingData.updateTempHP();
        if (renderVisible() && other.renderVisible() && level.gameplaySettings.showFiringAnim) {
            level.levelRenderer.beginFiring(this, other, thisWeapon, otherWeapon);
        } else if (other.renderVisible()) {
            firingExplosion = new UnitFiringExplosion(level.getTile(other.pos).renderPosCentered, this, other);
            level.levelRenderer.registerAnimBlock(firingExplosion);
            other.tileFlash(ATTACK_TILE_FLASH);
        } else {
            postFiring(other, true, false);
            other.postFiring(this, false, false);
        }
        if (other.renderVisible())
            other.cameraTo(team);
    }

    public void postFiring(Unit other, boolean isThisAttacking, boolean firingAnim) {
        if (renderVisible()) {
            setShieldHP(shieldFiringTempHP);
            setHP(firingTempHP);
        } else {
            hitPoints = firingTempHP;
            shieldHP = shieldFiringTempHP;
            lowestHP = Math.min(lowestHP, hitPoints);
        }
        if (hitPoints <= 0)
            onDestroyed(other);
        else {
            decrementCapture();
        }
        if (!isThisAttacking)
            TutorialManager.acceptEvent(new EventActionComplete(level, FIRE));
    }

    public void onDestroyed(Unit destroyedBy) { //destroyedBy is null if not destroyed by a unit
        if (destroyedBy != null) {
            if (level.networkState != NetworkState.CLIENT) {
                for (UnitTeam t : UnitTeam.ORDERED_TEAMS) {
                    if (level.samePlayerTeam(destroyedBy.team, t) && level.bots.get(t))
                        level.botHandlerMap.get(t).incrementDestroyedUnits();
                }
            }
            level.destroyedUnitsByTeam.put(destroyedBy.team, level.destroyedUnitsByTeam.get(destroyedBy.team) + 1);
        }
        if (renderVisible()) {
            explosion = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE * 2f, 0.5f);
            level.levelRenderer.registerAnimBlock(explosion);
            level.levelRenderer.enableCameraShake(new ObjPos(0.2f, 0.2f));
        } else {
            level.qRemoveUnit(this);
        }
        level.destroyedUnitsDamage.put(team, level.destroyedUnitsDamage.get(team) + stats.maxHP());
    }

    public Color getWeaponEffectivenessAgainst(Unit other) {
        float damage = -1;
        ArrayList<Modifier> m = new ArrayList<>();
        for (WeaponInstance weapon : weapons) {
            m = weapon.template.getModifiers(other.type);
            damage = Math.max(damage, Modifier.multiplicativeEffect(ModifierCategory.DAMAGE, m));
        }
        return WeaponDamageModifier.getDamageModifierColour(m.toArray(new Modifier[0]));
    }

    public WeaponInstance.FireAnimState getFireAnimState(WeaponInstance currentlyFiring) {
        WeaponInstance animWeapon = null;
        for (WeaponInstance weapon : weapons) {
            if (weapon.template.runsAnim)
                animWeapon = weapon;
        }
        if (animWeapon == currentlyFiring)
            return WeaponInstance.FireAnimState.FIRE;
        if (animWeapon == null || (stats.consumesAmmo() && ammo == 0))
            return WeaponInstance.FireAnimState.EMPTY;
        return WeaponInstance.FireAnimState.HOLD;
    }

    private TileSet selectableTiles;
    private TilePath movePath = null;
    private AnimTilePath path = null;
    public Point illegalTile = null;

    public TileSet selectableTiles() {
        return selectableTiles;
    }

    public UnitPose getPose() {
        if (path != null)
            return path.getDirection().getPose();
        return UnitPose.FORWARD;
    }

    private void updateRenderPos() {
        if (path != null) {
            if (path.finished()) {
                renderPos = path.getEnd();
                if (!renderTile().pos.equals(pos))
                    level.moveUnit(this, path.getLastTile(), level.getThisTeam() == level.getActiveTeam());
                level.levelRenderer.removeAnimBlock(path);
                path = null;
                if (illegalTile != null) {
                    Tile tile = level.getTile(illegalTile);
                    if (renderVisible() && level.getUnit(illegalTile).visible(level.currentVisibility)) {
                        tile.setIllegalTile();
                    }
                }
                if (level.getThisTeam() == team) {
                    selector().select(level.getTile(pos));
                }
                TutorialManager.acceptEvent(new EventActionComplete(level, MOVE));
            } else {
                renderPos = path.getPos();
                Tile renderTile = renderTile();
                if (!renderTile.pos.equals(pos)) {
                    Unit other = level.getUnit(renderTile.pos);
                    if (other == null) {
                        level.moveUnit(this, renderTile.pos, false);
                    } else
                        level.updateFoW();
                }
                if (!isRenderFoW() && (!stealthMode || isRenderStealthVisible())) {
                    level.levelRenderer.registerAnimBlock(path);
                    level.levelRenderer.setCameraInterpBlockPos(renderPos.copy().addY(TILE_SIZE / 2 * SIN_60_DEG));
                } else {
                    level.levelRenderer.removeAnimBlock(path);
                }
            }
        }
    }

    public ObjPos getRenderPos() {
        if (path != null) {
            if (path.finished())
                renderPos = path.getEnd();
            else
                renderPos = path.getPos();
        }
        return renderPos;
    }

    public boolean isRenderFoW() {
        if (level.getThisTeam() == team)
            return false;
        if (path == null) {
            return level.getTile(pos).isFoW;
        }
        Tile renderTile = renderTile();
        if (renderTile == null)
            return true;
        return renderTile.isFoW;
    }

    public boolean isRenderStealthVisible() {
        if (path == null) {
            return visibleInStealthMode;
        }
        if (!level.isThisPlayerAlive() || level.samePlayerTeam(team, level.getThisTeam()))
            return true;
        Tile renderTile = renderTile();
        if (renderTile == null)
            return false;
        for (HexagonalDirection d : HexagonalDirection.values()) {
            Point point = d.offset(renderTile.pos);
            if (!selector().validCoordinate(point))
                continue;
            Unit other = level.getUnit(point);
            if (other != null && level.samePlayerTeam(other.team, level.getThisTeam()))
                return true;
        }
        return false;
    }

    private ObjPos getShipRenderPos() {
        if (path != null)
            return getRenderPos().copy().add(moveAnimX.normalisedProgress() / 6 * type.getBobbingAmount(), moveAnimY.normalisedProgress() / 4 * type.getBobbingAmount());
        return getRenderPos().copy().add(idleAnimX.normalisedProgress() / 6 * type.getBobbingAmount(), idleAnimY.normalisedProgress() / 4 * type.getBobbingAmount());
    }

    public TileSet getVisibleTiles() {
        Point renderTilePos = renderTile().pos;
        return TileSet.all(level).m(level, t -> t
                .tilesInRange(renderTilePos, stats::viewRange, stats.maxViewRange())
                .add(TileSet.tilesInRadius(renderTilePos, 1, level)));
    }

    public Point getPos() {
        return pos;
    }

    public Level getLevel() {
        return level;
    }

    public UnitTeam getTeam() {
        return team;
    }

    public void updateLocation(Point pos) {
        this.pos = pos;
    }

    public void updateActionUI() {
        actionUI.updateActions(this);
    }

    public void turnEnded() {
        clearPerformedActions();
        updateActionUI();
    }

    public TileSelector selector() {
        return level.tileSelector;
    }

    public void regenerateHP(float amount) {
        hitPoints = Math.clamp(hitPoints + amount, 0, stats.maxHP());
        firingTempHP = hitPoints;
    }

    public void updateFromData(UnitData d) {
        clearPerformedActions();
        d.performedActions.forEach(this::addPerformedAction);
        hitPoints = d.hitPoints;
        firingTempHP = d.hitPoints;
        shieldHP = d.shieldHP;
        lowestHP = d.lowestHP;
        shieldFiringTempHP = d.shieldHP;
        if (captureProgress != d.captureProgress) {
            setCaptureProgress(d.captureProgress);
        }
        ammo = d.weaponAmmo;
        stealthMode = d.stealthMode;
        setMining(d.mining);
    }

    public boolean hasPerformedAction(Action action) {
        return performedActions.contains(action);
    }

    public void addPerformedAction(Action action) {
        performedActions.add(action);
    }

    public HashSet<Action> getPerformedActions() {
        return performedActions;
    }

    public void clearPerformedActions() {
        performedActions.clear();
    }

    public void tileFlash(Color color) {
        if (renderVisible())
            new TileFlash(color, 1, TileSet.of(level, pos), level);
    }

    public void cameraTo() {
        if (visible(level.currentVisibility) && level.getThisTeam() != team) {
            level.levelRenderer.setCameraInterpBlockPos(level.getTile(pos).renderPosCentered);
        }
    }

    public void cameraTo(UnitTeam actionPerformingTeam) {
        if (visible(level.currentVisibility) && level.getThisTeam() != actionPerformingTeam) {
            level.levelRenderer.setCameraInterpBlockPos(level.getTile(pos).renderPosCentered);
        }
    }

    private void cameraToIgnoreStealth() {
        if (!isRenderFoW() && level.getThisTeam() != team) {
            level.levelRenderer.setCameraInterpBlockPos(level.getTile(pos).renderPosCentered);
        }
    }

    public boolean canUnitAppearToBeMoved(Tile tile, VisibilityData visibility) {
        Unit u = level.getUnit(tile.pos);
        return u == null || !u.visible(visibility);
    }

    @Override
    public void delete() {
        if (explosion != null) {
            level.levelRenderer.removeAnimBlock(explosion);
            level.levelRenderer.disableCameraShake();
        }
        level.buttonRegister.remove(actionUI);
        level = null;
        actionUI.delete();
        actionUI = null;
        movePath = null;
        hitPointText.delete();
        stats = null;
    }

    @Override
    public void tick(float deltaTime) {
        updateRenderPos();
    }

    private void endAndDeselectAction() {
        MainPanel.addTask(() -> {
            level.endAction();
            level.tileSelector.deselect();
        });
    }

    private void endAction() {
        MainPanel.addTask(() -> {
            level.endAction();
        });
    }

    private void setSelectableTiles(Action action, Color border, TileSet tiles) {
        selectableTiles = tiles;
        level.levelRenderer.highlightTileRenderer = new HighlightTileRenderer(action.tileColour, selectableTiles, level);
        level.levelRenderer.unitTileBorderRenderer = new HexagonBorder(selectableTiles, border);
    }

    public Tile renderTile() {
        return selector().tileAtPos(getRenderPos().copy().addY(0.5f * SIN_60_DEG * TILE_SIZE));
    }

    public boolean renderVisible() {
        return !isRenderFoW() && (!stealthMode || isRenderStealthVisible());
    }

    public boolean visible(VisibilityData visibility) {
        return visibility.visibleTiles().contains(pos) && (!stealthMode || visibility.stealthVisibleUnit().contains(this));
    }

    public HashMap<Point, Float> tilesInMoveRangeCostMap(VisibilityData visibility) {
        return selector().tilesInRangeCostMap(pos, TileSet.all(level).m(level, t -> t
                .unitFilter(TileModifier.withoutVisibleEnemies(team, level, visibility))
        ), stats::moveCost, stats.maxMovement());
    }

    public float getDamageAgainstClass(ShipClass shipClass) {
        if (stats.consumesAmmo() && ammo == 0)
            return 0;
        final float[] v = {0};
        weapons.forEach(w -> {
            v[0] = Math.max(v[0], stats.baseDamage() * Modifier.multiplicativeEffect(ModifierCategory.DAMAGE, w.template.getModifiers(shipClass)));
        });
        return v[0];
    }

    public FiringData getCurrentFiringData(Unit otherUnit) {
        return new FiringData(this, otherUnit, level);
    }

    public String getActionCostText(Action a) {
        if (a == MOVE) {
            return stats.moveEnergyCost(stats.moveCost(TileType.EMPTY)) + " - " + stats.moveEnergyCost(stats.maxMovement());
        }
        return String.valueOf(stats.getActionCost(a).orElse(0));
    }

    public String getPerTurnActionCostText(Action a) {
        int v = stats.getPerTurnActionCost(a).orElse(0);
        return String.valueOf(v < 0 ? "+" + v : v);
    }
}

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
import render.Renderable;
import render.anim.timer.LerpAnimation;
import render.anim.timer.SineAnimation;
import render.anim.unit.*;
import render.level.tile.HexagonBorder;
import render.level.tile.HighlightTileRenderer;
import render.level.tile.TileFlash;
import render.level.tile.TilePath;
import render.level.ui.UnitDamageNumberUI;
import render.level.ui.UnitTextUI;
import render.types.text.DynamicTextRenderer;
import unit.action.Action;
import unit.action.ActionSelector;
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

import static level.tile.Tile.*;
import static unit.action.Action.*;

public class Unit extends UnitLike implements Deletable, Tickable {
    public UnitData data;
    public StatManager stats;

    private ObjPos renderPos;

    private final SineAnimation idleAnimX, idleAnimY, moveAnimX, moveAnimY;

    private ActionSelector actionUI = new ActionSelector(() -> {
        if (getLevel().getThisTeam() != getLevel().getActiveTeam() || getLevel().getThisTeam() != getTeam())
            return false;
        if (getLevel().getActiveAction() != null || getLevel().levelRenderer.uiUnitInfo.showFiringRange() || getLevel().levelRenderer.uiUnitInfo.showEffectiveness())
            return false;
        Tile selectedTile = getLevel().tileSelector.getSelectedTile();
        return selectedTile != null && selectedTile.pos.equals(data.pos);
    }, this);

    public final ArrayList<WeaponInstance> weapons = new ArrayList<>();

    private final ArrayList<UnitTextUI> damageUIs = new ArrayList<>();

    private AnimHandler animHandler = new AnimHandler();

    private Level level;

    public Unit(UnitType type, UnitTeam team, Point pos, Level level) {
        this.level = level;
        stealthTransparencyAnim = new LerpAnimation(1).finish();
        data = new UnitData(type, team, pos);
        stats = new StatManager(this);
        data.initialise(stats);
        selectableTiles = new TileSet(level.tilesX, level.tilesY);
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
        if (level == null || exploding) return;
        renderUnit(g, getPose(), stats.maxShieldHP() != 0);
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
        animHandler.render(g, AnimType.BELOW_UNIT);
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

    private boolean exploding = false;

    public synchronized void renderActionAboveUnits(Graphics2D g) {
        if (level == null || (data.stealthMode && !data.visibleInStealthMode)) return;
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
        animHandler.render(g, AnimType.ABOVE_UNIT);
    }

    public synchronized void renderEnergyCostIndicator(Graphics2D g) {
        if (level == null) return;
        if (level.selectedUnit == this && movePath != null)
            movePath.renderEnergyCost(g, stats, level);
    }

    public static final Color MOVE_TILE_BORDER_COLOUR = new Color(107, 184, 248, 192), INVALID_MOVE_TILE_BORDER_COLOUR = new Color(246, 75, 75, 192),
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
        if (action == MOVE && TutorialManager.actionTileNotSelectable(data.pos, MOVE_FROM))
            return;
        TutorialManager.acceptEvent(new EventActionSelect(level, action));
        if (action == MOVE) {
            setSelectableTiles(action, MOVE_TILE_BORDER_COLOUR, stats.tilesInMoveRange(level.currentVisibility));
            movePath = new TilePath(selectableTiles, data.pos, selector());
            level.levelRenderer.exitActionButton.setEnabled(true);
        } else if (action == FIRE) {
            setSelectableTiles(action, FIRE_TILE_BORDER_COLOUR, stats.tilesInFiringRange(level.currentVisibility, data, true));
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
            setSelectableTiles(action, REPAIR_TILE_BORDER_COLOUR, stats.getRepairTiles(data.pos));
            level.levelRenderer.exitActionButton.setEnabled(true);
        } else if (action == RESUPPLY) {
            setSelectableTiles(action, RESUPPLY_TILE_BORDER_COLOUR, stats.getResupplyTiles(data.pos));
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
                    level.levelRenderer.energyManager.canAfford(data.team, path.getEnergyCost(this, level), true);
                    startMove(path, path.illegalTile(movePath));
                    endAndDeselectAction();
                    if (level.networkState == NetworkState.SERVER)
                        level.server.sendUnitMovePacket(path, illegalTile, data.pos, this);
                    TutorialManager.acceptEvent(new EventActionPerform(level, MOVE));
                }
            } else if (action == FIRE) {
                if (!level.levelRenderer.energyManager.canAfford(this, FIRE, true)) return;
                Unit other = level.getUnit(tile.pos);
                if (level.networkState == NetworkState.SERVER)
                    level.server.sendUnitShootPacket(this, other);
                attack(other);
                data.addPerformedAction(FIRE);
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
                    level.levelRenderer.energyManager.canAfford(data.team, path.getEnergyCost(this, level), true);
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
        setStealthMode(!data.stealthMode);
        data.addPerformedAction(STEALTH);
        level.updateSelectedUnit();
        if (level.networkState == NetworkState.SERVER)
            level.server.sendUnitStealthPacket(this);
    }

    public void setStealthMode(boolean stealth) {
        if (data.stealthMode != stealth) {
            cameraToIgnoreStealth();
            if (!data.visibleInStealthMode) {
                stealthTransparencyAnim.setReversed(stealth);
                stealthTransparencyAnim.startTimer();
            }
        }
        data.stealthMode = stealth;
        level.levelRenderer.energyManager.recalculateIncome();
        level.updateSelectedUnit();
    }

    public void setMining(boolean mining) {
        if (mining == this.data.mining)
            return;
        this.data.mining = mining;
        level.levelRenderer.energyManager.recalculateIncome();
    }

    public void performMiningAction(boolean mining) {
        setMining(mining);
        tileFlash(MINE_TILE_FLASH);
        data.addPerformedAction(MINE);
        cameraTo();
        level.updateSelectedUnit();
        if (level.networkState == NetworkState.SERVER)
            level.server.sendUnitMinePacket(this);
    }

    public void performMiningAction() {
        performMiningAction(!data.mining);
    }

    public void regenShield() {
        if (level.networkState == NetworkState.CLIENT) {
            MainPanel.client.sendUnitShieldRegenRequest(this);
            return;
        }
        regenShield(data.shieldRenderHP + stats.shieldRegen());
        if (level.networkState == NetworkState.SERVER)
            level.server.sendUnitShieldRegenPacket(this);
    }

    public void regenShield(float newHP) {
        data.addPerformedAction(Action.SHIELD_REGEN);
        data.setShieldHP(newHP, stats.maxShieldHP(), this::addDamageUI);
        cameraTo();
        level.updateSelectedUnit();
        tileFlash(SHIELD_REGEN_TILE_FLASH);
    }

    public void repair(Unit other) {
        other.cameraTo(data.team);
        if (level.networkState == NetworkState.SERVER) {
            level.server.sendUnitRepairPacket(this, other);
        }
        other.data.setHP(other.data.renderHP + stats.repair(), other.stats.maxHP(), other::addDamageUI);
        data.addPerformedAction(REPAIR);
        other.animHandler.registerAnim(new UnitRepair(other.getCenter()));
        other.tileFlash(REPAIR_TILE_FLASH);
    }

    public void resupply(Unit other) {
        other.cameraTo(data.team);
        if (level.networkState == NetworkState.SERVER) {
            level.server.sendUnitResupplyPacket(this, other);
        }
        other.resupply();
        other.showResupply();
        data.addPerformedAction(RESUPPLY);
    }

    public void resupply() {
        data.ammo = stats.ammoCapacity();
    }

    public void showResupply() {
        if (renderVisible()) {
            tileFlash(RESUPPLY_TILE_FLASH);
            ObjPos pos = getRenderPos();
            damageUIs.add(new UnitTextUI("RESUPPLY", pos.x, pos.y, 0.5f, 1, UnitTextUI.RESUPPLY_COLOR));
        }
    }

    public void addDamageUI(float value, boolean shield) {
        if (value == 0 || !visible(level.currentVisibility))
            return;
        ObjPos pos = getRenderPos();
        damageUIs.add(new UnitDamageNumberUI(value, pos.x + (shield ? -TILE_SIZE * 0.2f : TILE_SIZE * 0.2f), pos.y, value > 0 ? 1 : -0.7f, shield));
    }

    public void capture(int captureProgress, boolean action) {
        Tile tile = level.getTile(data.pos);
        if (this.data.captureProgress == -1 && captureProgress != -1)
            tile.startCapturing(data.team.uiColour);
        if (captureProgress != -1) {
            tile.setProgress(captureProgress, level, captureProgress >= tile.structure.type.captureSteps, () -> {
                finishCapture(tile);
            });
        }
        if (action) {
            cameraTo();
            data.addPerformedAction(CAPTURE);
        }
        this.data.captureProgress = captureProgress;
    }

    private void finishCapture(Tile tile) {
        if (level.networkState == NetworkState.CLIENT)
            return;
        level.levelRenderer.setCameraInterpBlockPos(tile.renderPosCentered);
        if (tile.structure.type == StructureType.BASE) {
            level.removePlayer(tile.structure.team);
        }
        if (tile.structure.type.destroyedOnCapture) {
            tile.explodeStructure(level);
        } else {
            tile.structure.setTeam(data.team);
        }
        stopCapture();
        level.levelRenderer.energyManager.recalculateIncome();
        if (level.networkState == NetworkState.SERVER) {
            level.server.sendStructurePacket(tile, true);
        }
    }

    public void captureAction() {
        if (isCapturing()) {
            stopCapture();
            data.addPerformedAction(CAPTURE);
        } else
            incrementCapture(true);
    }

    public void incrementCapture(boolean action) {
        int newCaptureProgress = data.captureProgress;
        if (newCaptureProgress == -1)
            newCaptureProgress = 1;
        else
            newCaptureProgress++;
        capture(newCaptureProgress, action);
    }

    public void decrementCapture() {
        int newCaptureProgress = data.captureProgress;
        if (newCaptureProgress <= 0)
            return;
        newCaptureProgress--;
        capture(newCaptureProgress, false);
    }

    public boolean isCapturing() {
        return data.captureProgress != -1;
    }

    public void setCaptureProgress(int captureProgress) {
        capture(captureProgress, false);
    }

    public boolean canCapture() {
        Tile tile = level.getTile(data.pos);
        return data.type.canCapture && tile.hasStructure() && !level.samePlayerTeam(tile.structure.team, data.team) && tile.structure.canBeCaptured;
    }

    public void stopCapture() {
        data.captureProgress = -1;
    }

    public int getCaptureProgress() {
        return data.captureProgress;
    }

    public void startMove(AnimTilePath path, Point illegalTile) {
        if (data.mining)
            setMining(false);
        this.path = path;
        this.illegalTile = illegalTile;
        data.addPerformedAction(MOVE);
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
        data.addPerformedAction(FIRE);
    }

    public void attack(Unit other) {
        FiringData firingData = getCurrentFiringData(other);
        WeaponInstance thisWeapon = firingData.getBestWeaponAgainst(true);
        WeaponInstance otherWeapon;
        if (firingData.otherData.hitPoints > 0 && thisWeapon.template.counterattack) {
            otherWeapon = FiringData.reverse(firingData).getBestWeaponAgainst(true);
        } else {
            otherWeapon = null;
        }
        firingData.realiseDamage();
        if (renderVisible() && other.renderVisible() && level.gameplaySettings.showFiringAnim) {
            animHandler.registerAnim(new AttackArrow(getCenter(), other.getCenter()), () -> {
                level.levelRenderer.beginFiring(this, other, thisWeapon, otherWeapon);
            });
        } else if (other.renderVisible()) {
            AttackArrow.createAttackArrow(getCenter(), level.getTile(other.data.pos).renderPosCentered,
                    other.data.shieldRenderHP > 0, renderVisible(), animHandler, () -> postFiringOther(other, false));
            animHandler.animBlock(0.7f, level);
            other.tileFlash(ATTACK_TILE_FLASH);
        } else {
            postFiringOther(other, false);
        }
        if (other.renderVisible())
            other.cameraTo(data.team);
    }

    public void postFiringOther(Unit other, boolean firingAnim) {
        postFiring(other, true, firingAnim);
        other.postFiring(this, false, firingAnim);
    }

    public void postFiring(Unit other, boolean isThisAttacking, boolean firingAnim) {
        if (data.renderHP != data.hitPoints) {
            decrementCapture();
        }
        if (renderVisible()) {
            data.setShieldHP(data.shieldHP, stats.maxShieldHP(), this::addDamageUI);
            data.setHP(data.hitPoints, stats.maxHP(), this::addDamageUI);
        } else {
            data.renderHP = data.hitPoints;
            data.shieldRenderHP = data.shieldHP;
            data.lowestHP = Math.min(data.lowestHP, data.renderHP);
        }
        if (data.renderHP <= 0)
            onDestroyed(other);
        if (!isThisAttacking)
            TutorialManager.acceptEvent(new EventActionComplete(level, FIRE));
    }

    public void onDestroyed(Unit destroyedBy) { //destroyedBy is null if not destroyed by a unit
        if (destroyedBy != null) {
            if (level.networkState != NetworkState.CLIENT) {
                for (UnitTeam t : UnitTeam.ORDERED_TEAMS) {
                    if (level.samePlayerTeam(destroyedBy.data.team, t) && level.bots.get(t))
                        level.botHandlerMap.get(t).incrementDestroyedUnits();
                }
            }
            level.destroyedUnitsByTeam.put(destroyedBy.data.team, level.destroyedUnitsByTeam.get(destroyedBy.data.team) + 1);
        }
        if (renderVisible()) {
            UnitExplosion anim = new UnitExplosion(getCenter(), level);
            animHandler.registerAnim(anim);
            exploding = true;
            level.levelRenderer.registerTimerBlock(anim, () -> {
                level.qRemoveUnit(this);
            });
            level.levelRenderer.enableCameraShake(new ObjPos(0.2f, 0.2f));
        } else {
            level.qRemoveUnit(this);
        }
        level.destroyedUnitsDamage.put(data.team, level.destroyedUnitsDamage.get(data.team) + stats.maxHP());
    }

    public Color getWeaponEffectivenessAgainst(Unit other) {
        float damage = -1;
        ArrayList<Modifier> m = new ArrayList<>();
        for (WeaponInstance weapon : weapons) {
            m = weapon.template.getModifiers(other.data.type);
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
        if (animWeapon == null || (stats.consumesAmmo() && data.ammo == 0))
            return WeaponInstance.FireAnimState.EMPTY;
        return WeaponInstance.FireAnimState.HOLD;
    }

    private TileSet selectableTiles;
    private TilePath movePath = null;
    private AnimTilePath path = null;
    public Point illegalTile = null;

    public boolean movePathValid() {
        return movePath != null && movePath.isValid();
    }

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
                if (!renderTile().pos.equals(data.pos))
                    level.moveUnit(this, path.getLastTile(), level.getThisTeam() == level.getActiveTeam());
                level.levelRenderer.removeAnimBlock(path);
                path = null;
                if (illegalTile != null) {
                    Tile tile = level.getTile(illegalTile);
                    if (renderVisible() && level.getUnit(illegalTile).visible(level.currentVisibility)) {
                        tile.setIllegalTile();
                    }
                }
                if (level.getThisTeam() == data.team) {
                    selector().select(level.getTile(data.pos));
                }
                TutorialManager.acceptEvent(new EventActionComplete(level, MOVE));
            } else {
                renderPos = path.getPos();
                Tile renderTile = renderTile();
                if (!renderTile.pos.equals(data.pos)) {
                    Unit other = level.getUnit(renderTile.pos);
                    if (other == null) {
                        level.moveUnit(this, renderTile.pos, false);
                    } else
                        level.updateFoW();
                }
                if (!isRenderFoW() && (!data.stealthMode || isRenderStealthVisible())) {
                    level.levelRenderer.registerAnimBlock(path);
                    level.levelRenderer.setCameraInterpBlockPos(renderPos.copy().addY(TILE_SIZE / 2 * SIN_60_DEG));
                } else {
                    level.levelRenderer.removeAnimBlock(path);
                }
            }
        }
    }

    @Override
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
        if (level.getThisTeam() == data.team)
            return false;
        if (path == null) {
            return level.getTile(data.pos).isFoW;
        }
        Tile renderTile = renderTile();
        if (renderTile == null)
            return true;
        return renderTile.isFoW;
    }

    public boolean isRenderStealthVisible() {
        if (path == null) {
            return data.visibleInStealthMode;
        }
        if (!level.isThisPlayerAlive() || level.samePlayerTeam(data.team, level.getThisTeam()))
            return true;
        Tile renderTile = renderTile();
        if (renderTile == null)
            return false;
        for (HexagonalDirection d : HexagonalDirection.values()) {
            Point point = d.offset(renderTile.pos);
            if (!selector().validCoordinate(point))
                continue;
            Unit other = level.getUnit(point);
            if (other != null && level.samePlayerTeam(other.data.team, level.getThisTeam()))
                return true;
        }
        return false;
    }

    @Override
    public ObjPos getShipRenderOffset() {
        if (path != null)
            return new ObjPos(data.shipBobbingX(moveAnimX), data.shipBobbingY(moveAnimY));
        return new ObjPos(data.shipBobbingX(idleAnimX), data.shipBobbingY(idleAnimY));
    }

    public TileSet getVisibleTiles() {
        Point renderTilePos = renderTile().pos;
        return TileSet.all(level).m(level, t -> t
                .tilesInRange(renderTilePos, stats::viewRange, stats.maxViewRange())
                .add(TileSet.tilesInRadius(renderTilePos, 1, level)));
    }

    public Level getLevel() {
        return level;
    }

    public UnitTeam getTeam() {
        return data.team;
    }

    public void updateLocation(Point pos) {
        this.data.pos = pos;
    }

    public void updateActionUI() {
        actionUI.updateActions(this);
    }

    public void turnEnded() {
        data.clearPerformedActions();
        updateActionUI();
    }

    public TileSelector selector() {
        return level.tileSelector;
    }

    public void regenerateHP(float amount) {
        data.renderHP = Math.clamp(data.renderHP + amount, 0, stats.maxHP());
        data.hitPoints = data.renderHP;
    }

    public void updateFromData(UnitData d) {
        if (data.captureProgress != d.captureProgress) {
            setCaptureProgress(d.captureProgress);
        }
        setMining(d.mining);
        data = d;
    }

    public void tileFlash(Color color) {
        if (renderVisible())
            new TileFlash(color, 1, TileSet.of(level, data.pos), level);
    }

    public void cameraTo() {
        if (visible(level.currentVisibility) && level.getThisTeam() != data.team) {
            level.levelRenderer.setCameraInterpBlockPos(getCenter());
        }
    }

    public void cameraTo(UnitTeam actionPerformingTeam) {
        if (visible(level.currentVisibility) && level.getThisTeam() != actionPerformingTeam) {
            level.levelRenderer.setCameraInterpBlockPos(getCenter());
        }
    }

    private void cameraToIgnoreStealth() {
        if (!isRenderFoW() && level.getThisTeam() != data.team) {
            level.levelRenderer.setCameraInterpBlockPos(getCenter());
        }
    }

    public boolean canUnitAppearToBeMoved(Tile tile, VisibilityData visibility) {
        Unit u = level.getUnit(tile.pos);
        return u == null || !u.visible(visibility);
    }

    private ObjPos getCenter() {
        return level.getTile(data.pos).renderPosCentered;
    }

    @Override
    public void delete() {
        level.buttonRegister.remove(actionUI);
        level = null;
        actionUI.delete();
        actionUI = null;
        movePath = null;
        hitPointText.delete();
        shieldHitPointText.delete();
        stats = null;
        animHandler.delete();
        animHandler = null;
    }

    private final DynamicTextRenderer hitPointText = createHPText();
    private final DynamicTextRenderer shieldHitPointText = createShieldHPText();

    @Override
    public DynamicTextRenderer getHPText() {
        return hitPointText;
    }

    @Override
    public DynamicTextRenderer getShieldHPText() {
        return shieldHitPointText;
    }

    @Override
    public void tick(float deltaTime) {
        updateRenderPos();
        animHandler.tick(deltaTime);
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

    @Override
    public boolean renderVisible() {
        return !isRenderFoW() && (!data.stealthMode || isRenderStealthVisible());
    }

    public boolean visible(VisibilityData visibility) {
        return visibility.visibleTiles().contains(data.pos) && (!data.stealthMode || visibility.stealthVisibleUnit().contains(this));
    }

    public HashMap<Point, Float> tilesInMoveRangeCostMap(VisibilityData visibility) {
        return selector().tilesInRangeCostMap(data.pos, TileSet.all(level).m(level, t -> t
                .unitFilter(TileModifier.withoutVisibleEnemies(data.team, level, visibility))
        ), stats::moveCost, stats.maxMovement());
    }

    public float getDamageAgainstClass(ShipClass shipClass) {
        if (stats.consumesAmmo() && data.ammo == 0)
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

    @Override
    public UnitData getData() {
        return data;
    }
}

package unit;

import foundation.Deletable;
import foundation.MainPanel;
import foundation.math.HexagonalDirection;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import level.Level;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileSelector;
import network.NetworkState;
import render.GameRenderer;
import render.Renderable;
import render.anim.AnimTilePath;
import render.anim.ImageSequenceAnim;
import render.anim.LerpAnimation;
import render.anim.SineAnimation;
import render.level.tile.HexagonBorder;
import render.level.tile.HighlightTileRenderer;
import render.level.tile.TilePath;
import render.types.text.TextAlign;
import render.types.text.TextRenderer;
import render.texture.ImageSequenceGroup;
import render.level.ui.UnitDamageNumberUI;
import unit.action.Action;
import unit.action.ActionSelector;
import unit.action.ActionShapes;
import unit.action.UnitFiringExplosion;
import unit.bot.VisibilityData;
import unit.type.UnitType;
import unit.weapon.DamageType;
import unit.weapon.FiringData;
import unit.weapon.WeaponInstance;
import unit.weapon.WeaponTemplate;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static level.tile.Tile.*;
import static unit.action.Action.*;

public class Unit implements Deletable, Tickable {
    private static final Color HP_BACKGROUND_COLOUR = new Color(67, 67, 67), SHIELD_HP_BACKGROUND_COLOUR = new Color(79, 115, 140);
    public final UnitType type;
    public final UnitTeam team;
    public Point pos;
    private int captureProgress = -1;
    public boolean stealthMode = false, visibleInStealthMode = true;

    private ObjPos renderPos;

    private final SineAnimation idleAnimX, idleAnimY, moveAnimX, moveAnimY;
    private final LerpAnimation stealthTransparencyAnim = new LerpAnimation(1).finish();

    private ActionSelector actionUI = new ActionSelector(() -> {
        if (getLevel().getThisTeam() != getLevel().getActiveTeam() || getLevel().getThisTeam() != getTeam())
            return false;
        if (getLevel().getActiveAction() != null || getLevel().levelRenderer.uiUnitInfo.showFiringRange)
            return false;
        Tile selectedTile = getLevel().tileSelector.getSelectedTile();
        return selectedTile != null && selectedTile.pos.equals(getPos());
    }, this);

    public final ArrayList<WeaponInstance> weapons = new ArrayList<>();
    private final HashSet<Action> performedActions = new HashSet<>();

    public float hitPoints, firingTempHP;
    public float shieldHP, shieldFiringTempHP;
    private ArrayList<UnitDamageNumberUI> damageUIs = new ArrayList<>();
    private final TextRenderer hitPointText = new TextRenderer(() -> MathUtil.floatToString((float) Math.ceil(hitPoints), 0), 0.7f, Color.WHITE)
            .setTextAlign(TextAlign.RIGHT)
            .setBold(true)
            .setRenderBorder(0.1f, 0.3f, HP_BACKGROUND_COLOUR);
    private final TextRenderer shieldHitPointText = new TextRenderer(() -> MathUtil.floatToString((float) Math.ceil(shieldHP), 0), 0.7f, Color.WHITE)
            .setTextAlign(TextAlign.LEFT)
            .setBold(true)
            .setRenderBorder(0.1f, 0.3f, SHIELD_HP_BACKGROUND_COLOUR);

    private Level level;

    public Unit(UnitType type, UnitTeam team, Point pos, Level level) {
        this.type = type;
        this.team = team;
        this.pos = pos;
        this.level = level;
        hitPoints = type.hitPoints;
        firingTempHP = hitPoints;
        shieldHP = type.shieldHP;
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
            GameRenderer.renderOffset(getAndUpdateRenderPos(), g, () -> {
                g.translate(TILE_SIZE / 4.5f, TILE_SIZE / 15);
                GameRenderer.renderTransformed(g, () -> {
                    hitPointText.render(g);
                });
                if (type.shieldHP != 0) {
                    g.translate(-TILE_SIZE * 0.45f, 0);
                    shieldHitPointText.render(g);
                }
            });
            if (stealthMode) {
                GameRenderer.renderOffsetScaled(renderPos, TILE_SIZE * 0.3f, g, () -> {
                    g.translate(-1, -0.1);
                    g.setColor(ICON_COLOUR);
                    ActionShapes.stealthIcon(g);
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
        GameRenderer.renderOffset(getAndUpdateRenderPos(), g, () -> {
            actionUI.render(g);
        });
    }

    public void renderActionBelowUnits(Graphics2D g) {
        if (level == null)
            return;
        if (level.selectedUnit == this) {
            if (movePath != null) {
                if (selector().mouseOverTile != null)
                    movePath.setEnd(selector().mouseOverTile.pos, level);
                if (!level.hasActiveAction())
                    movePath = null;
                else
                    movePath.render(g);
            }
        }
    }

    public static final BasicStroke AIM_TARGET_STROKE = new BasicStroke(0.03f * Renderable.SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color AIM_TARGET_COLOUR = new Color(200, 92, 92);
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
                        g.setColor(AIM_TARGET_COLOUR);
                        float anim = MathUtil.map(-1, 1, 1f, 1.2f, targetAnim.normalisedProgress());

                        g.drawOval((int) (-0.4f * Renderable.SCALING * anim), (int) (-0.4f * Renderable.SCALING * anim), (int) (0.8f * Renderable.SCALING * anim), (int) (0.8f * Renderable.SCALING * anim));

                        g.drawLine((int) (-0.53f * Renderable.SCALING * anim), 0, (int) (-0.27f * Renderable.SCALING * anim), 0);
                        g.drawLine((int) (0.53f * Renderable.SCALING * anim), 0, (int) (0.27f * Renderable.SCALING * anim), 0);
                        g.drawLine(0, (int) (-0.53f * Renderable.SCALING * anim), 0, (int) (-0.27f * Renderable.SCALING * anim));
                        g.drawLine(0, (int) (0.53f * Renderable.SCALING * anim), 0, (int) (0.27f * Renderable.SCALING * anim));
                    });
                    level.levelRenderer.damageUI.show(this, level.getUnit(tile.pos));
                } else
                    targetAnim.startTimer();
            }
        }
        if (firingExplosion != null) {
            firingExplosion.render(g);
            if (firingExplosion.finished()) {
                level.levelRenderer.removeAnimBlock(firingExplosion);
                firingExplosion.delete();
                firingExplosion = null;
            }
        }
        if (explosion != null) {
            GameRenderer.renderOffset(getAndUpdateRenderPos().copy().add(0, TILE_SIZE * SIN_60_DEG / 2), g, () -> {
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
            movePath.renderEnergyCost(g, level);
    }

    public static final Color MOVE_TILE_BORDER_COLOUR = new Color(107, 184, 248, 192),
            FIRE_TILE_BORDER_COLOUR = new Color(243, 103, 103, 118);

    public synchronized void onActionSelect(Action action) {
        if (action == MOVE) {
            selectableTiles = tilesInMoveRange(level.currentVisibility);
            movePath = new TilePath(type, selectableTiles, pos, selector());
            level.levelRenderer.highlightTileRenderer = new HighlightTileRenderer(action.tileColour, selectableTiles, level);
            level.levelRenderer.unitTileBorderRenderer = new HexagonBorder(selectableTiles, MOVE_TILE_BORDER_COLOUR);
            level.levelRenderer.exitActionButton.setEnabled(true);
        } else if (action == FIRE) {
            selectableTiles = tilesInFiringRange(level.currentVisibility, new UnitData(this), true);
            level.levelRenderer.highlightTileRenderer = new HighlightTileRenderer(action.tileColour, selectableTiles, level);
            level.levelRenderer.unitTileBorderRenderer = new HexagonBorder(selectableTiles, FIRE_TILE_BORDER_COLOUR);
            level.levelRenderer.exitActionButton.setEnabled(true);
        } else if (action == CAPTURE) {
            if (!level.levelRenderer.energyManager.canAfford(this, CAPTURE, true)) return;
            endAndDeselectAction();
            if (level.networkState == NetworkState.CLIENT) {
                addPerformedAction(CAPTURE);
                MainPanel.client.sendUnitCaptureRequest(this);
                return;
            }
            incrementCapture();
            if (level.networkState == NetworkState.SERVER)
                level.server.sendUnitCapturePacket(this);
            return;
        } else if (action == SHIELD_REGEN) {
            if (!level.levelRenderer.energyManager.canAfford(this, SHIELD_REGEN, true)) return;
            endAction();
            if (level.networkState == NetworkState.CLIENT) {
                MainPanel.client.sendUnitShieldRegenRequest(this);
                return;
            }
            addPerformedAction(SHIELD_REGEN);
            setShieldHP(shieldHP + type.shieldRegen);
            if (level.networkState == NetworkState.SERVER)
                level.server.sendUnitShieldRegenPacket(this);
        } else if (action == STEALTH) {
            if (!level.levelRenderer.energyManager.canAfford(this, STEALTH, true)) return;
            setStealthMode(!stealthMode, true);
            endAction();
            if (level.networkState == NetworkState.CLIENT) {
                MainPanel.client.sendUnitStealthRequest(this);
                return;
            }
            addPerformedAction(STEALTH);
            if (level.networkState == NetworkState.SERVER)
                level.server.sendUnitStealthPacket(this);
        } else {
            selectableTiles = new HashSet<>();
        }
        level.setActiveAction(action);
    }

    //Called when there is an active action and this unit is selected
    public synchronized void onTileClicked(Tile tile, Action action) {
        if (tile == null || level == null) return;
        if (level.networkState == NetworkState.CLIENT) {
            onTileClickedAsClient(tile, action);
            return;
        }
        if (selectableTiles.contains(tile.pos)) {
            if (action == MOVE) {
                if (canUnitAppearToBeMoved(tile, level.currentVisibility) && level.levelRenderer.energyManager.canAfford(team, movePath.getEnergyCost(level), false)) {
                    path = movePath.getAnimPath(p -> isIllegalTile(p, level.currentVisibility));
                    level.levelRenderer.energyManager.canAfford(team, path.getEnergyCost(this, level), true);
                    illegalTile = path.illegalTile(movePath);
                    addPerformedAction(MOVE);
                    endAndDeselectAction();
                    if (level.networkState == NetworkState.SERVER) {
                        level.server.sendUnitMovePacket(path, illegalTile, pos, this);
                    }
                }
            } else if (action == FIRE) {
                if (!level.levelRenderer.energyManager.canAfford(this, FIRE, true)) return;
                Unit other = level.getUnit(tile.pos);
                if (level.networkState == NetworkState.SERVER) {
                    level.server.sendUnitShootPacket(this, other);
                }
                attack(other);
                addPerformedAction(FIRE);
                endAndDeselectAction();
            }
        }
    }

    public synchronized void onTileClickedAsClient(Tile tile, Action action) {
        if (selectableTiles.contains(tile.pos)) {
            if (action == MOVE) {
                if (canUnitAppearToBeMoved(tile, level.currentVisibility) && level.levelRenderer.energyManager.canAfford(team, movePath.getEnergyCost(level), false)) {
                    AnimTilePath path = movePath.getAnimPath(p -> isIllegalTile(p, level.currentVisibility));
                    level.levelRenderer.energyManager.canAfford(team, path.getEnergyCost(this, level), true);
                    MainPanel.client.sendUnitMoveRequest(path, path.illegalTile(movePath), this);
                    endAndDeselectAction();
                }
            } else if (action == FIRE) {
                if (!level.levelRenderer.energyManager.canAfford(this, FIRE, true)) return;
                MainPanel.client.sendUnitShootRequest(this, level.getUnit(tile.pos));
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

    public void setShieldHP(float newHP) {
        addDamageUI(newHP - shieldHP, true);
        shieldHP = Math.clamp(newHP, 0, type.shieldHP);
        shieldFiringTempHP = shieldHP;
    }

    public void setHP(float newHP) {
        addDamageUI(newHP - hitPoints, false);
        hitPoints = Math.clamp(newHP, 0, type.hitPoints);
        firingTempHP = hitPoints;
    }

    public void addDamageUI(float value, boolean shield) {
        if (value == 0)
            return;
        ObjPos pos = getRenderPos();
        damageUIs.add(new UnitDamageNumberUI(value, pos.x + (shield ? -TILE_SIZE * 0.2f : TILE_SIZE * 0.2f), pos.y, value > 0 ? 1 : -0.7f));
    }

    public void capture(int captureProgress, boolean action) {
        Tile tile = level.getTile(pos);
        if (this.captureProgress == -1 && captureProgress != -1)
            tile.startCapturing(team.uiColour);
        if (captureProgress != -1) {
            if (tile.isFoW) {
                tile.instantSetProgress(captureProgress);
                finishCapture(tile);
            } else {
                tile.setProgress(captureProgress, level, () -> {
                    finishCapture(tile);
                });
            }
        }
        if (action) {
            if (!isRenderFoW())
                level.levelRenderer.setCameraInterpBlockPos(tile.renderPosCentered);
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
            if (level.networkState == NetworkState.SERVER) {
                level.server.sendStructurePacket(tile);
            }
        }
    }

    public void incrementCapture() {
        int newCaptureProgress = captureProgress;
        if (newCaptureProgress == -1)
            newCaptureProgress = 1;
        else
            newCaptureProgress++;
        capture(newCaptureProgress, true);
    }

    public void decrementCapture() {
        int newCaptureProgress = captureProgress;
        if (newCaptureProgress <= 0)
            return;
        newCaptureProgress--;
        capture(newCaptureProgress, false);
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
        WeaponInstance thisWeapon = getBestWeaponAgainst(firingData, true);
        WeaponInstance otherWeapon = null;
        if (firingData.otherData().hitPoints > 0 && thisWeapon.template.counterattack) {
            otherWeapon = getBestWeaponAgainst(FiringData.reverse(firingData, other.weapons), true);
        }
        firingData.updateTempHP(this, other);
        if (renderVisible() && other.renderVisible()) {
            level.levelRenderer.beginFiring(this, other, thisWeapon, otherWeapon);
        } else if (other.renderVisible()) {
            firingExplosion = new UnitFiringExplosion(level.getTile(other.pos).renderPosCentered, this, other);
            level.levelRenderer.registerAnimBlock(firingExplosion);
        } else {
            postFiring(other, true, false);
            other.postFiring(this, false, false);
        }
        if (other.renderVisible() && level.getThisTeam() != level.getActiveTeam())
            level.levelRenderer.setCameraInterpBlockPos(level.getTile(other.pos).renderPosCentered);
    }

    public void postFiring(Unit other, boolean isThisAttacking, boolean firingAnim) {
        if (!firingAnim && renderVisible()) {
            setShieldHP(shieldFiringTempHP);
            setHP(firingTempHP);
        } else {
            hitPoints = firingTempHP;
            shieldHP = shieldFiringTempHP;
        }
        if (hitPoints <= 0)
            onDestroyed(other);
        else if (!isThisAttacking) {
            decrementCapture();
        }
    }

    public void onDestroyed(Unit destroyedBy) { //destroyedBy is null if not destroyed by a unit
        if (destroyedBy != null) {
            if (level.networkState != NetworkState.CLIENT)
                for (UnitTeam t : UnitTeam.ORDERED_TEAMS) {
                    if (level.samePlayerTeam(destroyedBy.team, t) && level.bots.get(t))
                        level.botHandlerMap.get(t).incrementDestroyedUnits();
                }
        }
        if (renderVisible()) {
            explosion = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE * 2f, 0.5f);
            level.levelRenderer.registerAnimBlock(explosion);
            level.levelRenderer.enableCameraShake(new ObjPos(0.2f, 0.2f));
        } else {
            level.qRemoveUnit(this);
        }
    }

    public static WeaponInstance getBestWeaponAgainst(FiringData firingData, boolean fireWeapon) {
        WeaponInstance bestWeapon = null;
        float damage = 0;
        for (WeaponInstance weapon : firingData.weapons()) {
            if (weapon.requiresAmmo && firingData.thisData().weaponAmmo == 0)
                continue;
            if (!weapon.tilesInFiringRange.apply(firingData.thisData(), firingData.level()).contains(firingData.otherData().pos))
                continue;
            float newDamage = weapon.getDamageAgainst(firingData);
            if (newDamage > damage) {
                damage = newDamage;
                bestWeapon = weapon;
            }
        }
        if (bestWeapon != null && fireWeapon) {
            bestWeapon.fire(firingData);
        }
        return bestWeapon;
    }

    public static float getDamageAgainst(FiringData firingData) {
        float damage = 0;
        for (WeaponInstance weapon : firingData.weapons()) {
            if (weapon.requiresAmmo && weapon.ammo == 0)
                continue;
            float newDamage = weapon.getDamageAgainst(firingData);
            if (newDamage > damage) {
                damage = newDamage;
            }
        }
        return damage;
    }

    public WeaponInstance getAmmoWeapon() {
        for (WeaponInstance weapon : weapons) {
            if (weapon.requiresAmmo)
                return weapon;
        }
        return null;
    }

    public WeaponInstance.FireAnimState getFireAnimState(WeaponInstance currentlyFiring) {
        WeaponInstance animWeapon = null;
        for (WeaponInstance weapon : weapons) {
            if (weapon.runsAnim)
                animWeapon = weapon;
        }
        if (animWeapon == currentlyFiring)
            return WeaponInstance.FireAnimState.FIRE;
        if (animWeapon == null || (animWeapon.requiresAmmo && animWeapon.ammo == 0))
            return WeaponInstance.FireAnimState.EMPTY;
        return WeaponInstance.FireAnimState.HOLD;
    }

    public HashSet<Point> tilesInFiringRange(VisibilityData visibility, UnitData data, boolean onlyWithEnemies) {
        HashSet<Point> tiles = new HashSet<>();
        for (WeaponInstance weapon : weapons) {
            if (weapon.requiresAmmo && data.weaponAmmo == 0)
                continue;
            if (onlyWithEnemies) {
                tiles.addAll(selector().withEnemyUnits(weapon.tilesInFiringRange.apply(data, level), this, visibility));
            } else
                tiles.addAll(weapon.tilesInFiringRange.apply(data, level));
        }
        return tiles;
    }

    private HashSet<Point> selectableTiles = new HashSet<>();
    private TilePath movePath = null;
    private AnimTilePath path = null;
    public Point illegalTile = null;

    public HashSet<Point> selectableTiles() {
        return selectableTiles;
    }

    public UnitPose getPose() {
        if (path != null)
            return path.getDirection().getPose();
        return UnitPose.FORWARD;
    }

    private ObjPos getAndUpdateRenderPos() {
        if (path != null) {
            if (path.finished()) {
                renderPos = path.getEnd();
                if (level.getUnit(pos) != this)
                    level.moveUnit(this, path.getLastTile(), level.getThisTeam() == level.getActiveTeam());
                level.levelRenderer.removeAnimBlock(path);
                path = null;
                if (illegalTile != null) {
                    Tile tile = level.getTile(illegalTile);
                    if (renderVisible()) {
                        tile.setIllegalTile();
                    }
                }
                if (level.getThisTeam() == team) {
                    selector().select(level.getTile(pos));
                }
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
        return renderPos;
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
            return getAndUpdateRenderPos().copy().add(moveAnimX.normalisedProgress() / 6 * type.getBobbingAmount(), moveAnimY.normalisedProgress() / 4 * type.getBobbingAmount());
        return getAndUpdateRenderPos().copy().add(idleAnimX.normalisedProgress() / 6 * type.getBobbingAmount(), idleAnimY.normalisedProgress() / 4 * type.getBobbingAmount());
    }

    public HashSet<Point> getVisibleTiles() {
        Point renderTilePos = renderTile().pos;
        HashSet<Point> points = selector().tilesInRange(renderTilePos, selector().allTiles(), type.tileViewRangeCostFunction, type.maxViewRange);
        points.addAll(selector().tilesInRadius(renderTilePos, 1));
        return points;
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

    public void resupply() {
        weapons.forEach(w -> w.ammo = w.ammoCapacity);
    }

    public void regenerateHP(float amount) {
        hitPoints = Math.clamp(hitPoints + amount, 0, type.hitPoints);
        firingTempHP = hitPoints;
    }

    public void updateFromData(UnitData d) {
        clearPerformedActions();
        d.performedActions.forEach(this::addPerformedAction);
        hitPoints = d.hitPoints;
        firingTempHP = d.hitPoints;
        shieldHP = d.shieldHP;
        shieldFiringTempHP = d.shieldHP;
        if (captureProgress != d.captureProgress) {
            setCaptureProgress(d.captureProgress);
        }
        if (d.weaponAmmo != -1)
            getAmmoWeapon().ammo = d.weaponAmmo;
        stealthMode = d.stealthMode;
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

    public void setStealthMode(boolean stealth, boolean cameraTo) {
        if (stealthMode != stealth) {
            if (!isRenderFoW() && cameraTo && level.getThisTeam() != team) {
                level.levelRenderer.setCameraInterpBlockPos(level.getTile(pos).renderPosCentered);
            }
            if (!visibleInStealthMode) {
                stealthTransparencyAnim.setReversed(stealth);
                stealthTransparencyAnim.startTimer();
            }
        }
        stealthMode = stealth;
        level.levelRenderer.energyManager.recalculateIncome();
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
    }

    @Override
    public void tick(float deltaTime) {
        getAndUpdateRenderPos();
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

    public boolean removeActionEnergyCost(Action a) {
        return a == STEALTH && stealthMode;
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

    public HashSet<Point> tilesInMoveRange(VisibilityData visibility) {
        return selector().tilesInRange(pos, selector().tilesWithoutEnemies(selector().allTiles(), team, visibility), type.tileMovementCostFunction, type.maxMovement);
    }

    public HashMap<Point, Float> tilesInMoveRangeCostMap(VisibilityData visibility) {
        return selector().tilesInRangeCostMap(pos, selector().tilesWithoutEnemies(selector().allTiles(), team, visibility), type.tileMovementCostFunction, type.maxMovement);
    }

    public float getDamageAgainstType(DamageType damageType) {
        final float[] v = {0};
        weapons.forEach(w -> {
            if (w.requiresAmmo && w.ammo == 0)
                return;
            v[0] = Math.max(v[0], w.template.damageTypes.get(damageType).fill);
        });
        return v[0];
    }

    public FiringData getCurrentFiringData(Unit otherUnit) {
        return new FiringData(new UnitData(this), new UnitData(otherUnit), weapons, level);
    }

    public Optional<Integer> getActionCost(Action action) {
        return type.getActionCost(action);
    }

    public Optional<Integer> getPerTurnActionCost(Action action) {
        return type.getPerTurnActionCost(action);
    }
}

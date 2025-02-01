package unit;

import foundation.Deletable;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.Level;
import level.Tile;
import level.TileSelector;
import render.GameRenderer;
import render.Renderable;
import render.anim.AnimTilePath;
import render.anim.ImageSequenceAnim;
import render.anim.SineAnimation;
import render.renderables.HighlightTileRenderer;
import render.renderables.TilePath;
import render.renderables.text.TextAlign;
import render.renderables.text.TextRenderer;
import render.texture.ImageSequenceGroup;
import unit.action.Action;
import unit.action.ActionSelector;
import unit.weapon.WeaponInstance;
import unit.weapon.WeaponTemplate;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

import static level.Tile.*;
import static unit.action.Action.*;

public class Unit implements Deletable {
    public static final Color HP_BACKGROUND_COLOUR = new Color(67, 67, 67);
    public final UnitType type;
    public final UnitTeam team;
    public Point pos;

    private ObjPos renderPos;

    private final SineAnimation
            idleAnimX = new SineAnimation(5f + (float) Math.random(), (float) Math.random() * 360),
            idleAnimY = new SineAnimation(7f + (float) Math.random(), (float) Math.random() * 360);
    private final SineAnimation
            moveAnimX = new SineAnimation(1f + (float) Math.random() * .3f, (float) Math.random() * 360),
            moveAnimY = new SineAnimation(1.5f + (float) Math.random() * .3f, (float) Math.random() * 360);

    private ActionSelector actionUI = new ActionSelector(this::getRenderPos, () -> {
        if (getLevel().getActiveTeam() != getTeam())
            return false;
        if (getLevel().getActiveAction() != null)
            return false;
        Tile selectedTile = getLevel().tileSelector.selectedTile;
        return selectedTile != null && selectedTile.pos.equals(getPos());
    });

    public final ArrayList<WeaponInstance> weapons = new ArrayList<>();
    public final HashSet<Action> performedActions = new HashSet<>();

    public float hitPoints, firingTempHP;
    private final TextRenderer hitPointText = new TextRenderer(() -> MathUtil.floatToString((float) Math.ceil(hitPoints), 0), 0.7f, Color.WHITE)
            .setTextAlign(TextAlign.RIGHT)
            .setBold(true)
            .setRenderBorder(0.1f, 0.3f, HP_BACKGROUND_COLOUR);

    private Level level;

    public Unit(UnitType type, UnitTeam team, Point pos, Level level) {
        this.type = type;
        this.team = team;
        this.pos = pos;
        this.level = level;
        hitPoints = type.hitPoints;
        firingTempHP = hitPoints;
        level.buttonRegister.register(actionUI);
        renderPos = Tile.getRenderPos(pos);
        actionUI.setActions(type.actions, this);
        for (WeaponTemplate template : type.weapons) {
            weapons.add(new WeaponInstance(template));
        }
    }

    public void renderTile(Graphics2D g) {
        if (explosion != null)
            return;
        GameRenderer.renderOffset(getShipRenderPos(), g, () -> {
            g.translate(-TILE_SIZE / 2, 0);
            type.tileRenderer(team, getPose()).render(g);
        });
        if (path == null) {
            GameRenderer.renderOffset(getRenderPos(), g, () -> {
                g.translate(TILE_SIZE / 4.5f, TILE_SIZE / 15);
                hitPointText.render(g);
            });
        }
    }

    public void renderActions(Graphics2D g) {
        if (level.levelRenderer.runningAnim())
            return;
        GameRenderer.renderOffset(getRenderPos(), g, () -> {
            actionUI.render(g);
        });
    }

    public void renderActionBelowUnits(Graphics2D g) {
        if (level.selectedUnit == this) {
            if (movePath != null) {
                if (selector().mouseOverTile != null)
                    movePath.setEnd(selector().mouseOverTile.pos, level);
                movePath.render(g);
                if (!level.hasActiveAction())
                    movePath = null;
            }
        }
    }

    public static final BasicStroke AIM_TARGET_STROKE = new BasicStroke(0.03f * Renderable.SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color AIM_TARGET_COLOUR = new Color(200, 92, 92);
    private final SineAnimation targetAnim = new SineAnimation(2, 90);

    private ImageSequenceAnim explosion = null;


    public void renderActionAboveUnits(Graphics2D g) {
        if (level.selectedUnit == this) {
            if (level.getActiveAction() == Action.FIRE) {
                if (selector().mouseOverTile != null) {
                    GameRenderer.renderOffsetScaled(Tile.getCenteredRenderPos(selector().mouseOverTile.pos), Tile.TILE_SIZE / Renderable.SCALING, g, () -> {
                        g.setStroke(AIM_TARGET_STROKE);
                        g.setColor(AIM_TARGET_COLOUR);
                        float anim = MathUtil.map(-1, 1, 1f, 1.2f, targetAnim.normalisedProgress());

                        g.drawOval((int) (-0.4f * Renderable.SCALING * anim), (int) (-0.4f * Renderable.SCALING * anim), (int) (0.8f * Renderable.SCALING * anim), (int) (0.8f * Renderable.SCALING * anim));

                        g.drawLine((int) (-0.53f * Renderable.SCALING * anim), 0, (int) (-0.27f * Renderable.SCALING * anim), 0);
                        g.drawLine((int) (0.53f * Renderable.SCALING * anim), 0, (int) (0.27f * Renderable.SCALING * anim), 0);
                        g.drawLine(0, (int) (-0.53f * Renderable.SCALING * anim), 0, (int) (-0.27f * Renderable.SCALING * anim));
                        g.drawLine(0, (int) (0.53f * Renderable.SCALING * anim), 0, (int) (0.27f * Renderable.SCALING * anim));
                    });
                    level.levelRenderer.damageUI.show(this, level.getUnit(selector().mouseOverTile.pos));
                } else
                    targetAnim.startTimer();
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

    public void onActionSelect(Action action) {
        level.setActiveAction(action);
        if (action == Action.MOVE) {
            selectableTiles = selector().tilesInRange(pos, selector().tilesWithoutEnemies(selector().allTiles(), team), type.tileMovementCostFunction, type.maxMovement);
            movePath = new TilePath(type, selectableTiles, pos, selector());
            level.levelRenderer.highlightTileRenderer = new HighlightTileRenderer(action.tileColour, selectableTiles, level);
        } else if (action == Action.FIRE) {
            selectableTiles = tilesInFiringRange();
            level.levelRenderer.highlightTileRenderer = new HighlightTileRenderer(action.tileColour, selectableTiles, level);
        } else {
            selectableTiles = new HashSet<>();
        }
    }

    //Called when there is an active action and this unit is selected
    public void onTileClicked(Tile tile, Action action) {
        if (tile == null)
            return;
        if (selectableTiles.contains(tile.pos)) {
            if (action == Action.MOVE) {
                if (tile.isFoW || level.canUnitBeMoved(tile.pos)) {
                    path = movePath.getAnimPath(t -> {
                        return level.getTile(t).isFoW && level.getUnit(t) != null && !level.samePlayerTeam(level.getUnit(t), this);
                    });
                    if (path.length() != movePath.length() + 1) {
                        illegalTile = movePath.getTile(path.length() - 1);
                    } else
                        illegalTile = null;
                    performedActions.add(MOVE);
                    level.levelRenderer.registerAnimBlock(path);
                    level.endAction();
                }
            } else if (action == FIRE) {
                attack(level.getUnit(tile.pos));
                performedActions.add(FIRE);
                performedActions.add(MOVE);
                level.endAction();
                selector().deselect();
            }
        }
    }

    public boolean canFireAt(Unit other) {
        if (other == null)
            return false;
        return !level.samePlayerTeam(this, other);
    }

    public void attack(Unit other) {
        WeaponInstance thisWeapon = getBestWeaponAgainst(this, other, true);
        WeaponInstance otherWeapon = null;
        if (other.firingTempHP > 0) {
            otherWeapon = getBestWeaponAgainst(other, this, true);
        }
        level.levelRenderer.beginFiring(this, other, thisWeapon, otherWeapon);
    }

    public void postFiring(Unit other) {
        hitPoints = firingTempHP;
        if (hitPoints <= 0)
            onDestroyed(other);
    }

    private void onDestroyed(Unit destroyedBy) {
        explosion = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE * 2f, 0.5f);
        level.levelRenderer.registerAnimBlock(explosion);
        level.levelRenderer.enableCameraShake(new ObjPos(0.2f, 0.2f));
    }

    public static WeaponInstance getBestWeaponAgainst(Unit thisUnit, Unit other, boolean fireWeapon) {
        WeaponInstance bestWeapon = null;
        float damage = 0;
        for (WeaponInstance weapon : thisUnit.weapons) {
            if (weapon.requiresAmmo && weapon.ammo == 0)
                continue;
            float newDamage = weapon.getDamageAgainst(thisUnit, other);
            if (newDamage > damage) {
                damage = newDamage;
                bestWeapon = weapon;
            }
        }
        if (bestWeapon != null && fireWeapon)
            bestWeapon.fire(thisUnit, other);
        return bestWeapon;
    }

    public static float getDamageAgainst(Unit thisUnit, Unit other) {
        float damage = 0;
        for (WeaponInstance weapon : thisUnit.weapons) {
            if (weapon.requiresAmmo && weapon.ammo == 0)
                continue;
            float newDamage = weapon.getDamageAgainst(thisUnit, other);
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

    public HashSet<Point> tilesInFiringRange() {
        HashSet<Point> tiles = new HashSet<>();
        for (WeaponInstance weapon : weapons) {
            if (weapon.requiresAmmo && weapon.ammo == 0)
                continue;
            tiles.addAll(weapon.tilesInFiringRange.apply(this));
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

    private ObjPos getRenderPos() {
        if (path != null) {
            if (path.finished()) {
                renderPos = path.getEnd();
                level.moveUnit(this, path.getLastTile(), true);
                level.levelRenderer.removeAnimBlock(path);
                path = null;
                if (illegalTile != null)
                    level.getTile(illegalTile).setIllegalTile();
            } else {
                renderPos = path.getPos();
                level.levelRenderer.setCameraInterpBlockPos(renderPos.copy().addY(TILE_SIZE / 2 * SIN_60_DEG));
            }
        }
        return renderPos;
    }

    private ObjPos getShipRenderPos() {
        if (path != null)
            return getRenderPos().copy().add(moveAnimX.normalisedProgress() / 6, moveAnimY.normalisedProgress() / 4);
        return getRenderPos().copy().add(idleAnimX.normalisedProgress() / 6, idleAnimY.normalisedProgress() / 4);
    }

    public float getTileDamageMultiplier() {
        return type.damageReduction.apply(level.getTile(pos).type);
    }

    public HashSet<Point> getVisibleTiles() {
        HashSet<Point> points = selector().tilesInRange(pos, selector().allTiles(), type.tileViewRangeCostFunction, type.maxViewRange);
        points.addAll(selector().tilesInRadius(pos, 1));
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

    public void update() {
        actionUI.updateActions(this);
    }

    public void turnEnded() {
        performedActions.clear();
        update();
    }

    public TileSelector selector() {
        return level.tileSelector;
    }

    @Override
    public void delete() {
        level.buttonRegister.remove(actionUI);
        level = null;
        actionUI.delete();
        actionUI = null;
        movePath = null;
        hitPointText.delete();
    }
}

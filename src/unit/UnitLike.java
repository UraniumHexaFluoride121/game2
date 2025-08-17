package unit;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import render.GameRenderer;
import render.HorizontalAlign;
import render.anim.timer.LerpAnimation;
import render.level.ui.UnitDamageNumberUI;
import render.level.ui.UnitTextUI;
import render.types.text.DynamicTextRenderer;
import unit.action.ActionShapes;
import unit.stats.StatManager;
import unit.weapon.FiringData;
import unit.weapon.WeaponInstance;
import unit.weapon.WeaponTemplate;

import java.awt.*;
import java.util.ArrayList;

import static level.tile.Tile.*;
import static unit.action.Action.*;

public abstract class UnitLike<T extends StatManager<?>> {
    public static final Color HP_BACKGROUND_COLOUR = new Color(67, 67, 67);
    public static final Color SHIELD_HP_BACKGROUND_COLOUR = new Color(79, 115, 140);
    public LerpAnimation stealthTransparencyAnim = null;
    public UnitData data;
    public final ArrayList<WeaponInstance> weapons = new ArrayList<>();
    protected final ArrayList<UnitTextUI> damageUIs = new ArrayList<>();
    public T stats;

    public UnitLike(UnitData data, T stats) {
        this.data = data;
        this.stats = stats;
        for (WeaponTemplate template : data.type.weapons) {
            weapons.add(new WeaponInstance(template));
        }
    }

    public void renderUnit(Graphics2D g, UnitPose pose, boolean displayShieldHP) {
        Composite c = null;
        if (stealthTransparencyAnim != null && !stealthTransparencyAnim.finished()) {
            c = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, stealthTransparencyAnim.normalisedProgress()));
        } else if (!renderVisible()) return;

        ObjPos renderPos = getRenderPos();
        ObjPos offset = getShipRenderOffset();

        GameRenderer.renderOffset(offset.add(renderPos), g, () -> {
            g.translate(-TILE_SIZE / 2, 0);
            renderUnitPose(g, pose, data);
        });
        if (pose == UnitPose.FORWARD) {
            GameRenderer.renderOffset(renderPos, g, () -> {
                GameRenderer.renderTransformed(g, () -> {
                    g.translate(textXOffset(), textYOffset());
                    getHPText().render(g);
                });
                if (displayShieldHP) {
                    g.translate(-textXOffset(), textYOffset());
                    getShieldHPText().render(g);
                }
            });
            if (data.stealthMode) {
                GameRenderer.renderOffsetScaled(renderPos, TILE_SIZE * 0.3f, g, () -> {
                    g.translate(-1, -0.1);
                    g.setColor(ICON_COLOUR);
                    g.fill(ActionShapes.STEALTH);
                });
            }
            if (data.mining) {
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

    public void renderUnitPose(Graphics2D g, UnitPose pose, UnitData data) {
        data.type.tileRenderer(data.team, pose).render(g);
        if (data.shieldRenderHP > 0)
            data.type.shieldRenderer.render(g, TILE_SIZE);
    }

    public abstract ObjPos getShipRenderOffset();

    public abstract ObjPos getRenderPos();

    public DynamicTextRenderer createHPText() {
        return new DynamicTextRenderer(() -> MathUtil.floatToString((float) Math.ceil(data.renderHP), 0), 0.7f, Color.WHITE)
                .setTextAlign(HorizontalAlign.RIGHT)
                .setBold(true)
                .setRenderBorder(0.1f, 0.3f, HP_BACKGROUND_COLOUR);
    }

    public DynamicTextRenderer createShieldHPText() {
        return new DynamicTextRenderer(() -> MathUtil.floatToString((float) Math.ceil(data.shieldRenderHP), 0), 0.7f, Color.WHITE)
                .setTextAlign(HorizontalAlign.LEFT)
                .setBold(true)
                .setRenderBorder(0.1f, 0.3f, SHIELD_HP_BACKGROUND_COLOUR);
    }

    public abstract DynamicTextRenderer getHPText();

    public abstract DynamicTextRenderer getShieldHPText();

    public float textXOffset() {
        return TILE_SIZE / 4.5f;
    }

    public float textYOffset() {
        return TILE_SIZE / 15;
    }

    public boolean renderVisible() {
        return true;
    }

    public abstract FiringData getCurrentFiringData(UnitLike<?> otherUnit);

    public FiringResult getFiringResult(UnitLike<?> other) {
        FiringData firingData = getCurrentFiringData(other);
        WeaponInstance thisWeapon = firingData.getBestWeaponAgainst(true);
        WeaponInstance otherWeapon;
        if (firingData.otherData.hitPoints > 0 && thisWeapon.template.counterattack) {
            otherWeapon = FiringData.reverse(firingData).getBestWeaponAgainst(true);
        } else {
            otherWeapon = null;
        }
        FiringResult result = new FiringResult(firingData, thisWeapon, otherWeapon);
        return result;
    }

    public void postFiringOther(UnitLike<?> other) {
        postFiring(other, true);
        other.postFiring(this, false);
    }

    public void postFiring(UnitLike<?> other, boolean isThisAttacking) {
        if (renderVisible()) {
            data.setShieldHP(data.shieldHP, stats.maxShieldHP(), this::addDamageUI);
            data.setHP(data.hitPoints, stats.maxHP(), this::addDamageUI);
        } else {
            data.renderHP = data.hitPoints;
            data.shieldRenderHP = data.shieldHP;
            data.lowestHP = Math.min(data.lowestHP, data.renderHP);
        }
        if (data.hitPoints <= 0)
            onDestroyed(other);
    }

    public abstract void onDestroyed(UnitLike<?> destroyedBy);

    public void addDamageUI(float value, boolean shield) {
        if (value == 0 || !renderVisible())
            return;
        ObjPos pos = getRenderPos();
        damageUIs.add(new UnitDamageNumberUI(value, pos.x + (shield ? -TILE_SIZE * 0.2f : TILE_SIZE * 0.2f), pos.y, value > 0 ? 1 : -0.7f, shield));
    }

    public void renderDamageUIs(Graphics2D g) {
        damageUIs.removeIf(e -> e.render(g));
    }

    public record FiringResult(FiringData firingData, WeaponInstance thisWeapon, WeaponInstance otherWeapon) {
    }
}

package unit;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import render.GameRenderer;
import render.HorizontalAlign;
import render.anim.timer.LerpAnimation;
import render.types.text.DynamicTextRenderer;
import unit.action.ActionShapes;

import java.awt.*;

import static level.tile.Tile.*;
import static unit.action.Action.*;

public abstract class UnitLike {
    public static final Color HP_BACKGROUND_COLOUR = new Color(67, 67, 67);
    public static final Color SHIELD_HP_BACKGROUND_COLOUR = new Color(79, 115, 140);
    public LerpAnimation stealthTransparencyAnim = null;

    public void renderUnit(Graphics2D g, UnitPose pose, boolean displayShieldHP) {
        Composite c = null;
        if (stealthTransparencyAnim != null && !stealthTransparencyAnim.finished()) {
            c = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, stealthTransparencyAnim.normalisedProgress()));
        } else if (!renderVisible()) return;

        UnitData data = getData();
        ObjPos renderPos = getRenderPos();
        ObjPos offset = getShipRenderOffset();

        GameRenderer.renderOffset(offset.add(renderPos), g, () -> {
            g.translate(-TILE_SIZE / 2, 0);
            data.type.tileRenderer(data.team, pose).render(g);
            if (data.shieldRenderHP > 0)
                data.type.shieldRenderer.render(g, TILE_SIZE);
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

    public abstract UnitData getData();

    public abstract ObjPos getShipRenderOffset();

    public abstract ObjPos getRenderPos();

    public DynamicTextRenderer createHPText() {
        return new DynamicTextRenderer(() -> MathUtil.floatToString((float) Math.ceil(getData().renderHP), 0), 0.7f, Color.WHITE)
                .setTextAlign(HorizontalAlign.RIGHT)
                .setBold(true)
                .setRenderBorder(0.1f, 0.3f, HP_BACKGROUND_COLOUR);
    }

    public DynamicTextRenderer createShieldHPText() {
        return new DynamicTextRenderer(() -> MathUtil.floatToString((float) Math.ceil(getData().shieldRenderHP), 0), 0.7f, Color.WHITE)
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
}

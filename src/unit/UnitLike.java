package unit;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import render.GameRenderer;
import render.HorizontalAlign;
import render.types.text.DynamicTextRenderer;
import unit.action.ActionShapes;

import java.awt.*;

import static level.tile.Tile.*;
import static unit.action.Action.*;

public interface UnitLike {
    Color HP_BACKGROUND_COLOUR = new Color(67, 67, 67);
    Color SHIELD_HP_BACKGROUND_COLOUR = new Color(79, 115, 140);

    default void renderUnit(Graphics2D g, UnitPose pose, boolean displayShieldHP) {
        UnitData data = getData();
        ObjPos renderPos = getRenderPos();
        ObjPos offset = getShipRenderOffset();

        GameRenderer.renderOffset(offset.add(renderPos), g, () -> {
            g.translate(-TILE_SIZE / 2, 0);
            data.type.tileRenderer(data.team, pose).render(g);
            if (data.shieldHP > 0)
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
    }

    UnitData getData();

    ObjPos getShipRenderOffset();

    ObjPos getRenderPos();

    default DynamicTextRenderer createHPText() {
        return new DynamicTextRenderer(() -> MathUtil.floatToString((float) Math.ceil(getData().renderHP), 0), 0.7f, Color.WHITE)
                .setTextAlign(HorizontalAlign.RIGHT)
                .setBold(true)
                .setRenderBorder(0.1f, 0.3f, HP_BACKGROUND_COLOUR);
    }

    default DynamicTextRenderer createShieldHPText() {
        return new DynamicTextRenderer(() -> MathUtil.floatToString((float) Math.ceil(getData().shieldRenderHP), 0), 0.7f, Color.WHITE)
                .setTextAlign(HorizontalAlign.LEFT)
                .setBold(true)
                .setRenderBorder(0.1f, 0.3f, SHIELD_HP_BACKGROUND_COLOUR);
    }

    DynamicTextRenderer getHPText();

    DynamicTextRenderer getShieldHPText();

    default float textXOffset() {
        return TILE_SIZE / 4.5f;
    }

    default float textYOffset() {
        return TILE_SIZE / 15;
    }
}

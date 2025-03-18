package render.ui.implementation;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.tile.Tile;
import render.*;
import render.ui.types.UITextLabel;
import unit.Unit;

import java.awt.*;

public class UIDamage extends AbstractRenderElement {
    private final UITextLabel label = new UITextLabel(7, 1, false);
    private ObjPos offset;

    public UIDamage(RenderRegister<OrderedRenderable> register, RenderOrder order) {
        super(register, order);
        label.updateTextLeft("DAMAGE:");
        label.setTextLeftBold().setTextRightBold();
        label.setBackgroundColour(new Color(198, 79, 79, 190));
        renderable = g -> {
            if (offset == null)
                return;
            GameRenderer.renderOffset(offset.x - 3.8f, offset.y - 1, g, () -> {
                label.render(g);
            });
            offset = null;
        };
    }

    public void show(Unit thisUnit, Unit targetUnit) {
        offset = Tile.getRenderPos(targetUnit.pos);
        label.updateTextRight(MathUtil.floatToString(Unit.getDamageAgainst(thisUnit.getCurrentFiringData(targetUnit)), 1));
    }
}

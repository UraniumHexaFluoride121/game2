package render.types;

import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.Renderable;
import render.level.tile.RenderElement;

import java.awt.*;

public class UIFullScreenColour extends RenderElement {
    public UIFullScreenColour(RenderRegister<OrderedRenderable> register, RenderOrder order, Color color) {
        super(register, order, g -> {
            g.setColor(color);
            g.fillRect(0, 0, (int) (Renderable.right() + 1), (int) (Renderable.top() + 1));
        });
    }
}

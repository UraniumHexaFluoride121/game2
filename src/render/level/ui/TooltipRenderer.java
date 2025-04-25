package render.level.ui;

import foundation.MainPanel;
import render.*;

public class TooltipRenderer extends AbstractRenderElement {
    public TooltipRenderer(RenderRegister<OrderedRenderable> register, RenderOrder order) {
        super(register, order);
        renderable = g -> {
            MainPanel.generatedTooltipRenderers.forEach(r -> r.render(g));
            MainPanel.generatedTooltipRenderers.clear();
        };
    }
}

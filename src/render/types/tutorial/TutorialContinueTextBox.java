package render.types.tutorial;

import foundation.input.ButtonOrder;
import level.Level;
import render.RenderOrder;
import render.UIColourTheme;
import render.types.container.UIContainer;
import render.types.input.button.UIButton;
import render.HorizontalAlign;
import render.types.box.UIDisplayBoxRenderElement;

public class TutorialContinueTextBox extends UIContainer {
    public final float width;

    public TutorialContinueTextBox(Level l, float x, float y, float width, HorizontalAlign textAlign, String text, Runnable onContinue) {
        super(l.levelRenderer.mainRenderer, l.buttonRegister, RenderOrder.TUTORIAL_UI, ButtonOrder.TUTORIAL_UI, x, y);
        this.width = width;
        addRenderables((r, b) -> {
            new UIDisplayBoxRenderElement(r, RenderOrder.TUTORIAL_UI, 0, 0, width, -1, box -> {
                box.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX_DARK);
            }, true).addText(0.7f, textAlign, text).setHorizontalAlign(HorizontalAlign.CENTER);
            new UIButton(r, b, RenderOrder.TUTORIAL_UI, ButtonOrder.TUTORIAL_UI, -2, -2, 4, 1.5f, 0.8f, false)
                    .setBold().setText("Continue").setColourTheme(UIColourTheme.DEEP_GREEN).setOnClick(onContinue);
        });
    }
}

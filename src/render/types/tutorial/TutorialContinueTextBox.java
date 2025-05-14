package render.types.tutorial;

import foundation.input.ButtonOrder;
import level.Level;
import render.RenderOrder;
import render.UIColourTheme;
import render.types.container.UIContainer;
import render.types.input.button.UIButton;
import render.types.text.TextAlign;
import render.types.text.UIMLTextBoxRenderElement;

public class TutorialContinueTextBox extends UIContainer {
    public final float width;

    public TutorialContinueTextBox(Level l, float x, float y, float width, TextAlign textAlign, String text, Runnable onContinue) {
        super(l.levelRenderer.mainRenderer, l.buttonRegister, RenderOrder.TUTORIAL_UI, ButtonOrder.TUTORIAL_UI, x, y);
        this.width = width;
        addRenderables((r, b) -> {
            new UIMLTextBoxRenderElement(r, RenderOrder.TUTORIAL_UI, 0, 0, width, -1, 0.7f, textAlign, box -> {
                box.setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER);
            }, true).setText(text).setBoxAlign(TextAlign.CENTER);
            new UIButton(r, b, RenderOrder.TUTORIAL_UI, ButtonOrder.TUTORIAL_UI, -2, -2, 4, 1.5f, 0.8f, false)
                    .setBold().setText("Continue").setColourTheme(UIColourTheme.DEEP_GREEN).setOnClick(onContinue);
        });
    }
}

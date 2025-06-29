package render.types.tutorial;

import foundation.input.ButtonOrder;
import level.Level;
import render.RenderOrder;
import render.UIColourTheme;
import render.types.container.LevelUIContainer;
import render.types.input.button.UIButton;
import render.HorizontalAlign;
import render.types.box.UIDisplayBoxRenderElement;

public class LevelTutorialContinueTextBox extends LevelUIContainer<Level> {
    public final float width;

    public LevelTutorialContinueTextBox(Level l, float x, float y, float width, HorizontalAlign textAlign, String text, Runnable onContinue) {
        super(l.levelRenderer.levelUIRenderer, l.buttonRegister, RenderOrder.TUTORIAL_LEVEL_UI, ButtonOrder.TUTORIAL_LEVEL_UI, x, y, l);
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

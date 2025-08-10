package render.level.info;

import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import level.Level;
import render.*;
import render.level.tile.RenderElement;
import render.types.UIFullScreenColour;
import render.types.box.UIBox;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.input.button.UIButton;

import java.util.function.UnaryOperator;

public class InfoScreen extends LevelUIContainer<Level> {
    protected final float width, height;
    protected boolean backInCorner;

    public InfoScreen(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, float width, float height, Level level, boolean backInCorner) {
        super(register, buttonRegister, RenderOrder.INFO_SCREEN, Renderable.right() / 2 - width / 2, Renderable.top() / 2 - height / 2, level);
        setEnabled(false);
        this.width = width;
        this.height = height;
        this.backInCorner = backInCorner;
        addRenderables((r, b) -> {
            new UIFullScreenColour(r, RenderOrder.INFO_SCREEN_BACKGROUND, UnitInfoScreen.FULL_SCREEN_MENU_BACKGROUND_COLOUR)
                    .setZOrder(-100).translate(-(Renderable.right() / 2 - width / 2), -(Renderable.top() / 2 - height / 2));
            if (backInCorner)
                new UIButton(r, b, RenderOrder.INFO_SCREEN,
                        3.5f - (Renderable.right() / 2 - width / 2), Renderable.top() - 2.5f - (Renderable.top() / 2 - height / 2), 9, 2, 1.4f, false, this::disable)
                        .setText("Back").setBold().setColourTheme(UIColourTheme.DEEP_RED);
            else
                new UIButton(r, b, RenderOrder.INFO_SCREEN,
                        width / 2 - 4.5f, -2.5f, 9, 2, 1.4f, false, this::disable)
                        .setText("Back").setBold().setColourTheme(UIColourTheme.DEEP_RED);
            new OnButtonInput(b, RenderOrder.INFO_SCREEN, t -> t == InputType.ESCAPE, this::disable);
        });
    }

    public void disable() {
        setEnabled(false);
        if (backInCorner)
            level.levelRenderer.endTurn.setEnabled(true);
    }

    @Override
    public UIContainer setEnabled(boolean enabled) {
        if (enabled && backInCorner)
            level.levelRenderer.endTurn.setEnabled(false);
        return super.setEnabled(enabled);
    }

    protected void box() {
        box(b -> b);
    }

    protected void box(UnaryOperator<UIBox> boxModifier) {
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.INFO_SCREEN_BACKGROUND,
                    boxModifier.apply(new UIBox(width, height).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER))
            ).setZOrder(-50);
        });
    }

    protected void backTopCorner() {
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.INFO_SCREEN_BACKGROUND,
                    new UIBox(width, height).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER)
            ).setZOrder(-50);
        });
    }

    @Override
    public boolean blocking(InputType type) {
        return true;
    }
}

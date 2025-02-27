package render.ui.implementation;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import level.Level;
import render.*;
import render.renderables.RenderElement;
import render.ui.UIColourTheme;
import render.ui.types.LevelUIContainer;
import render.ui.types.UIButton;

import static render.ui.implementation.UnitInfoScreen.*;

public class PauseMenu extends LevelUIContainer {
    private static final float WIDTH = 18, HEIGHT = 2, Y_OFFSET = -5;
    public PauseMenu(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, Level level) {
        super(register, buttonRegister, order, buttonOrder, 0, 0, level);
        addRenderables((r, b) -> {
            new OnButtonInput(b, ButtonOrder.PAUSE_MENU, t -> t == InputType.ESCAPE, () -> setEnabled(false));
            new RenderElement(r, RenderOrder.PAUSE_MENU_BACKGROUND, g -> {
                g.setColor(FULL_SCREEN_MENU_BACKGROUND_COLOUR);
                g.fillRect(0, 0, (int) Math.ceil(Renderable.right()), (int) Math.ceil(Renderable.top()));
            });
            newButton(r, b, 0, WIDTH / 2 - 0.25f, 0)
                    .setColourTheme(UIColourTheme.RED).setText("Exit game").setOnClick(() -> {
                        MainPanel.addTask(MainPanel::toTitleScreen);
                    });
            newButton(r, b, 0, WIDTH / 2 - 0.25f, WIDTH / 2 + 0.25f)
                    .setColourTheme(UIColourTheme.GREEN).setText("Continue game").setOnClick(() -> {
                        setEnabled(false);
                    });
        });
    }

    private UIButton newButton(GameRenderer r, ButtonRegister b, int i, float width, float xOffset) {
        return new UIButton(r, b, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU, Renderable.right() / 2 - WIDTH / 2 + xOffset, Renderable.top() / 2 + i * HEIGHT + Y_OFFSET, width, HEIGHT - 0.25f, 1f, false)
                .setBold();
    }

    private UIButton newButton(GameRenderer r, ButtonRegister b, int i) {
        return newButton(r, b, i, WIDTH, 0);
    }

    @Override
    public boolean blocking(InputType type) {
        return true;
    }
}

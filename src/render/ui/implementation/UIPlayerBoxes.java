package render.ui.implementation;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import render.*;
import render.ui.types.UIBox;
import render.ui.types.UITextDisplayBox;
import unit.UnitTeam;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class UIPlayerBoxes extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private ButtonOrder buttonOrder;
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private GameRenderer renderer = new GameRenderer(new AffineTransform(), null);
    private final float x, y;

    private final ArrayList<PlayerBox> boxes = new ArrayList<>();

    public UIPlayerBoxes(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y) {
        super(register, order);
        this.buttonOrder = buttonOrder;
        this.buttonRegister = buttonRegister;
        this.x = x;
        this.y = y;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        boxes.add(new PlayerBox(renderer, RenderOrder.TITLE_SCREEN_BUTTONS, 0));
        boxes.add(new PlayerBox(renderer, RenderOrder.TITLE_SCREEN_BUTTONS, 1));
        renderable = g -> {
            renderer.render(g);
        };
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return true;
    }

    private boolean blocking = false;

    @Override
    public boolean blocking(InputType type) {
        return blocking;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!enabled)
            return;
        blocking = internal.acceptInput(pos.copy().subtract(x, y), type, true);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!enabled)
            return;
        blocking = internal.acceptInput(pos.copy().subtract(x, y), type, false);
    }

    @Override
    public void delete() {
        super.delete();
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
    }

    private static class PlayerBox extends AbstractRenderElement {
        private final UITextDisplayBox numberBox;
        private final UIBox mainBox;
        private int index;

        public PlayerBox(RenderRegister<OrderedRenderable> register, RenderOrder order, int index) {
            super(register, order);
            numberBox = new UITextDisplayBox(null, RenderOrder.NONE, 0.1f, 1, 2, 2, 1.7f);
            mainBox = new UIBox(13, 3.5f, 0, UIBox.BoxShape.RECTANGLE);
            update(index);
            renderable = g -> {
                GameRenderer.renderOffset(0, index * 4, g, () -> {
                    GameRenderer.renderOffsetScaled(0, 4, 1, -1, g, () -> {
                        numberBox.render(g);
                        GameRenderer.renderOffset(3, 0.25f, g, () -> {
                            mainBox.render(g);
                        });
                    });
                });
            };
        }

        public void update(int index) {
            this.index = index;
            numberBox.setText(String.valueOf(index + 1))
                    .setColourTheme(UnitTeam.ORDERED_TEAMS[index].uiColour);
            mainBox.setColourTheme(UnitTeam.ORDERED_TEAMS[index].uiColour);
        }
    }
}

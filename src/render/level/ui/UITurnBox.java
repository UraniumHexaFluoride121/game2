package render.level.ui;

import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import level.Level;
import render.*;
import render.types.text.FixedTextRenderer;
import render.types.text.TextAlign;
import render.types.box.UIBox;
import render.types.text.UITextLabel;

public class UITurnBox extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private final FixedTextRenderer text = new FixedTextRenderer(null, 1.5f, UITextLabel.TEXT_COLOUR)
            .setTextAlign(TextAlign.CENTER).setBold(true);
    private final StaticHitBox hitBox = StaticHitBox.createFromOriginAndSize(13.5f, Renderable.top() - 2.5f, 10, 2);
    private Level level;
    private final UIBox box = new UIBox(10, 2);

    public UITurnBox(RenderRegister<OrderedRenderable> register, RenderOrder order, Level level) {
        super(register, order);
        this.level = level;
        renderable = g -> {
            GameRenderer.renderOffset(13.5f, Renderable.top() - 2.5f, g, () -> {
                box.render(g);
                g.translate(10 / 2f, 0.5);
                text.render(g);
            });
        };
        setNewTurn();
    }

    public void setNewTurn() {
        box.setColourTheme(level.getActiveTeam().uiColour);
        text.updateText("Turn " + level.getTurn());
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        return hitBox.isPositionInside(level.levelRenderer.transformCameraPosToBlock(pos));
    }

    @Override
    public boolean blocking(InputType type) {
        return type.isMouseInput();
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return ButtonOrder.LEVEL_UI;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {

    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {

    }

    @Override
    public void delete() {
        super.delete();
        level = null;
    }
}

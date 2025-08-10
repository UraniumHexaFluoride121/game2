package render.level.ui;

import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import foundation.math.HitBox;
import level.Level;
import render.*;
import render.types.box.UIBox;
import render.HorizontalAlign;
import render.types.text.TextRenderer;
import render.types.text.UITextLabel;

public class UITurnBox extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private final TextRenderer text = new TextRenderer(null, 1.5f, UITextLabel.TEXT_COLOUR)
            .setTextAlign(HorizontalAlign.CENTER).setBold(true);
    private final HitBox hitBox = HitBox.createFromOriginAndSize(13.5f, Renderable.top() - 2.5f, 10, 2);
    private Level level;
    private final UIBox box = new UIBox(10, 2);

    public UITurnBox(RenderRegister<OrderedRenderable> register, RenderOrder order, Level level) {
        super(register, order);
        this.level = level;
        renderable = g -> {
            if (isEnabled())
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

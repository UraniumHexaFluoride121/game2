package render.ui.implementation;

import foundation.input.ButtonClickHandler;
import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import level.Level;
import render.*;
import render.renderables.text.FixedTextRenderer;
import render.renderables.text.TextAlign;
import render.ui.UIColourTheme;
import render.ui.types.UIBox;
import render.ui.types.UITextLabel;

import java.awt.*;

public class UIEndTurn extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private final FixedTextRenderer text = new FixedTextRenderer("End Turn", 1.4f, UITextLabel.TEXT_COLOUR)
            .setTextAlign(TextAlign.CENTER).setBold(true);
    private final StaticHitBox hitBox = StaticHitBox.createFromOriginAndSize(0.5f, Renderable.top() - 2.5f, 9, 2);
    private Level level;
    private final ButtonClickHandler clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, false, this::onClick);
    private final UIBox box = new UIBox(9, 2).setClickHandler(clickHandler);
    private float textOffset = 0.5f;

    public UIEndTurn(RenderRegister<OrderedRenderable> register, RenderOrder order, Level level) {
        super(register, order);
        this.level = level;
        renderable = g -> {
            GameRenderer.renderOffset(0.5f, Renderable.top() - 2.5f, g, () -> {
                box.render(g);
                g.translate(9 / 2f, textOffset);
                text.render(g);
            });
        };
    }

    @Override
    public void render(Graphics2D g) {
        if (isEnabled())
            super.render(g);
    }

    private boolean grayedOut = false;

    public void setGrayedOut(boolean grayedOut) {
        boolean playerAlive = level.isThisPlayerAlive();
        this.grayedOut = grayedOut || !playerAlive;
        box.setColourTheme(grayedOut ? UIColourTheme.GRAYED_OUT : UIColourTheme.LIGHT_BLUE);
        text.setTextSize(grayedOut ? 1 : 1.4f);
        text.updateText(playerAlive ? grayedOut ? "Waiting for turn..." : "End Turn" : "You've lost!");
        textOffset = grayedOut ? 0.62f : 0.5f;
    }

    private void onClick() {
        level.levelRenderer.confirm.makeVisible("End Turn?", level::endTurn, level.levelRenderer.confirm::makeInvisible);
    }

    @Override
    public boolean posInside(ObjPos pos) {
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
        if (grayedOut || level.levelRenderer.runningAnim())
            return;
        clickHandler.buttonPressed(pos, inside, blocked, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (grayedOut)
            return;
        clickHandler.buttonReleased(pos, inside, blocked, type);
    }

    @Override
    public void delete() {
        super.delete();
        level = null;
        clickHandler.delete();
        box.setClickHandler(null);
    }
}

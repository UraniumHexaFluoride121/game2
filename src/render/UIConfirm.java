package render;

import foundation.Main;
import foundation.input.ButtonClickHandler;
import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import level.AbstractLevel;
import render.types.text.FixedTextRenderer;
import render.types.text.MultiLineTextBox;
import render.types.text.TextAlign;
import render.types.box.UIBox;
import render.types.text.UITextLabel;

import java.awt.*;

public class UIConfirm extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private static final float width = 12, height = 5;
    private final MultiLineTextBox text = new MultiLineTextBox(0, 0, width - 2, 1.5f, TextAlign.CENTER);
    private final FixedTextRenderer
            confirmText = new FixedTextRenderer("Confirm", 0.85f, UITextLabel.TEXT_COLOUR)
                    .setTextAlign(TextAlign.CENTER).setBold(true),
            cancelText = new FixedTextRenderer("Cancel", 0.85f, UITextLabel.TEXT_COLOUR)
                    .setTextAlign(TextAlign.CENTER).setBold(true);
    private final StaticHitBox
            confirmBox = StaticHitBox.createFromOriginAndSize(Renderable.right() / 2 + width / 2f - 1.5f - width / 3, Renderable.top() / 2 - height / 2f + 0.5f,
            width / 3, 1.5f),
            cancelBox = StaticHitBox.createFromOriginAndSize(Renderable.right() / 2 - width / 2f + 1.5f, Renderable.top() / 2 - height / 2f + 0.5f,
                    width / 3, 1.5f);
    private final ButtonClickHandler
            confirmButtonHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, false, null),
            cancelButtonHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, false, null);
    private final UIBox
            box = new UIBox(width, height),
            confirm = new UIBox(width / 3, 1.5f).setClickHandler(confirmButtonHandler).setColourTheme(UIColourTheme.DEEP_GREEN),
            cancel = new UIBox(width / 3, 1.5f).setClickHandler(cancelButtonHandler).setColourTheme(UIColourTheme.DEEP_RED);

    private AbstractLevel<?, ?> level;
    public boolean visible = false;

    public UIConfirm(RenderRegister<OrderedRenderable> register, RenderOrder order, AbstractLevel<?, ?> level) {
        super(register, order);
        this.level = level;
        renderable = g -> {
            if (!visible || !isEnabled())
                return;
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, Main.window.getWidth(), Main.window.getHeight());
            GameRenderer.renderOffset(Renderable.right() / 2 - width / 2f, Renderable.top() / 2 - height / 2f, g, () -> {
                box.render(g);
                g.translate(width / 2f, 4.5f - text.textSize);
                text.render(g);
            });
            GameRenderer.renderOffset(Renderable.right() / 2 + width / 2f - 1.5f - width / 3, Renderable.top() / 2 - height / 2f + 0.5f, g, () -> {
                confirm.render(g);
                g.translate(width / 6, 0.45f);
                confirmText.render(g);
            });
            GameRenderer.renderOffset(Renderable.right() / 2 - width / 2f + 1.5f, Renderable.top() / 2 - height / 2f + 0.5f, g, () -> {
                cancel.render(g);
                g.translate(width / 6 - 0.1f, 0.45f);
                cancelText.render(g);
            });
        };
    }

    public void makeVisible(String text, Runnable onConfirm, Runnable onCancel) {
        visible = true;
        this.text.updateText(text);
        confirmButtonHandler.setOnClick(onConfirm);
        cancelButtonHandler.setOnClick(onCancel);
    }

    public UIConfirm setTextSize(float textSize) {
        text.setTextSize(textSize);
        return this;
    }

    public UIConfirm setTextConfirm(String text) {
        confirmText.updateText(text);
        return this;
    }

    public UIConfirm setTextCancel(String text) {
        cancelText.updateText(text);
        return this;
    }

    public void makeInvisible() {
        visible = false;
        confirmButtonHandler.setOnClick(null);
        cancelButtonHandler.setOnClick(null);
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return visible;
    }

    @Override
    public boolean blocking(InputType type) {
        return true;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return ButtonOrder.CONFIRM_UI;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (blocked)
            return;
        if (type == InputType.ESCAPE) {
            makeInvisible();
            return;
        }
        ObjPos block = level.levelRenderer.transformCameraPosToBlock(pos);
        confirmButtonHandler.buttonPressed(pos, confirmBox.isPositionInside(block), false, type);
        cancelButtonHandler.buttonPressed(pos, cancelBox.isPositionInside(block), false, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        confirmButtonHandler.buttonReleased(pos, inside, blocked, type);
        cancelButtonHandler.buttonReleased(pos, inside, blocked, type);
    }

    @Override
    public void delete() {
        super.delete();
        level = null;
        confirmButtonHandler.delete();
        cancelButtonHandler.delete();
        cancel.setClickHandler(null);
        confirm.setClickHandler(null);
    }
}

package render.ui.types;

import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.*;
import render.renderables.text.FixedTextRenderer;
import render.renderables.text.TextAlign;
import render.ui.UIColourTheme;

public class UIButton extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    protected final FixedTextRenderer text;
    public final float x, y, height, width;
    protected final ButtonClickHandler clickHandler;
    protected final UIBox box;
    protected final StaticHitBox hitBox;
    protected final ButtonOrder buttonOrder;
    protected ButtonRegister buttonRegister;

    protected boolean clickEnabled = true;

    public UIButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float textSize, boolean staySelected) {
        this(register, buttonRegister, order, buttonOrder, x, y, width, height, textSize, staySelected, null);
    }

    public UIButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float textSize, boolean staySelected, Runnable onClick) {
        super(register, order);
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.buttonOrder = buttonOrder;
        this.buttonRegister = buttonRegister;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, staySelected, onClick);
        box = new UIBox(width, height).setClickHandler(clickHandler);
        text = new FixedTextRenderer(null, textSize, UITextLabel.TEXT_COLOUR)
                .setTextAlign(TextAlign.CENTER);
        hitBox = StaticHitBox.createFromOriginAndSize(x, y, width, height);
        renderable = g -> {
            if (!enabled)
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                g.translate(width / 2f - textSize * 0.03f, height / 2 - textSize * 0.75 / 2);
                text.render(g);
            });
        };
    }

    public UIButton setText(String text) {
        this.text.updateIfDifferent(text);
        return this;
    }

    public UIButton setBold() {
        text.setBold(true);
        return this;
    }

    public UIButton noDeselect() {
        clickHandler.noDeselect();
        return this;
    }

    public UIButton deselect() {
        clickHandler.deselect();
        return this;
    }

    public UIButton setColourTheme(UIColourTheme colourTheme) {
        box.setColourTheme(colourTheme);
        return this;
    }

    public UIButton setOnDeselect(Runnable runnable) {
        clickHandler.setOnDeselect(runnable);
        return this;
    }

    public UIButton setOnClick(Runnable runnable) {
        clickHandler.setOnClick(runnable);
        return this;
    }

    public UIButton setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    public UIButton setClickEnabled(boolean clickEnabled) {
        this.clickEnabled = clickEnabled;
        return this;
    }

    public UIButton select() {
        clickHandler.select();
        return this;
    }

    public UIButton setBoxShape(UIBox.BoxShape shape) {
        box.setShape(shape);
        return this;
    }

    public UIButton setBoxCorner(float corner) {
        box.setCorner(corner);
        return this;
    }

    public boolean isSelected() {
        return clickHandler.isSelected();
    }

    public String getText() {
        return text.getText();
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return enabled && hitBox.isPositionInside(pos);
    }

    @Override
    public boolean blocking(InputType type) {
        return type.isMouseInput();
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (enabled && clickEnabled)
            clickHandler.buttonPressed(pos, inside, blocked, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (enabled && clickEnabled)
            clickHandler.buttonReleased(pos, inside, blocked, type);
    }

    @Override
    public void delete() {
        super.delete();
        clickHandler.delete();
        box.setClickHandler(null);
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
    }
}

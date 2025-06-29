package render.types.input.button;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import render.*;
import render.types.box.UIBox;
import render.types.text.TextRenderer;
import render.HorizontalAlign;
import render.types.text.TooltipManager;
import render.types.text.UITextLabel;

import java.util.function.Consumer;

public class UIButton extends AbstractUIButton {
    protected final TextRenderer text;
    public float textSize;

    public UIButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float textSize, boolean staySelected) {
        this(register, buttonRegister, order, buttonOrder, x, y, width, height, textSize, staySelected, null);
    }

    public UIButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float textSize, boolean staySelected, Runnable onClick) {
        this(register, buttonRegister, order, buttonOrder, x, y, width, height, textSize, staySelected, onClick,
                new TextRenderer(null, textSize, UITextLabel.TEXT_COLOUR)
                        .setTextAlign(HorizontalAlign.CENTER));
    }

    protected UIButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float textSize, boolean staySelected, Runnable onClick, TextRenderer text) {
        super(register, buttonRegister, order, buttonOrder, x, y, height, width, staySelected, onClick);
        this.textSize = textSize;
        this.text = text;
        renderable = g -> {
            if (!isEnabled())
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                g.translate(width / 2f, height / 2 - getTextSize() * 0.75 / 2);
                text.render(g);
            });
            tooltip.render(g);
        };
    }

    @Override
    public UIButton tooltip(Consumer<TooltipManager> action) {
        super.tooltip(action);
        return this;
    }

    @Override
    public UIButton noDeselect() {
        super.noDeselect();
        return this;
    }

    @Override
    public UIButton toggleMode() {
        super.toggleMode();
        return this;
    }

    @Override
    public UIButton deselect() {
        super.deselect();
        return this;
    }

    @Override
    public UIButton setColourTheme(UIColourTheme colourTheme) {
        super.setColourTheme(colourTheme);
        return this;
    }

    @Override
    public UIButton setOnDeselect(Runnable runnable) {
        super.setOnDeselect(runnable);
        return this;
    }

    @Override
    public UIButton setOnClick(Runnable runnable) {
        super.setOnClick(runnable);
        return this;
    }

    @Override
    public UIButton setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public UIButton setClickEnabled(boolean clickEnabled) {
        super.setClickEnabled(clickEnabled);
        return this;
    }

    @Override
    public UIButton select() {
        super.select();
        return this;
    }

    @Override
    public UIButton runOnCLick() {
        super.runOnCLick();
        return this;
    }

    @Override
    public UIButton setBoxShape(UIBox.BoxShape shape) {
        super.setBoxShape(shape);
        return this;
    }

    @Override
    public UIButton setBoxCorner(float corner) {
        super.setBoxCorner(corner);
        return this;
    }

    public UIButton setText(String text) {
        this.text.updateText(text);
        return this;
    }

    public UIButton setTextSize(float size) {
        textSize = size;
        text.setTextSize(size);
        return this;
    }

    public UIButton setBold() {
        text.setBold(true);
        return this;
    }

    public String getText() {
        return text.getText();
    }

    private float getTextSize() {
        return textSize;
    }
}

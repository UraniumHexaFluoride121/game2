package render.renderables.text;

import foundation.input.ButtonInputReceiver;
import foundation.input.InputType;
import foundation.math.ObjPos;
import render.GameRenderer;
import render.anim.SineAnimation;

import java.awt.*;
import java.awt.font.TextLayout;

public class EditableTextRenderer extends FixedTextRenderer implements ButtonInputReceiver {
    public final StringBuilder s = new StringBuilder();
    private final int maxLength;
    private int caretPosition = 0;
    private float caretRenderPosition = 0;
    private SineAnimation anim = new SineAnimation(1, 0);
    private static final Color caretColour = new Color(221, 221, 221);

    private boolean caretUpdate = false, selected = false;
    private float caretUpdateToPos = -100;

    public EditableTextRenderer(String text, float textSize, Color main, int maxLength) {
        super(text, textSize, main);
        this.maxLength = maxLength;
    }

    @Override
    public void render(Graphics2D g) {
        super.render(g);
        if (caretUpdateToPos != -100) {
            if (text != null) {
                for (caretPosition = 0; caretPosition <= text.length(); caretPosition++) {
                    updateCaret(g);
                    if (caretRenderPosition * textSize > caretUpdateToPos) {
                        caretPosition--;
                        updateCaret(g);
                        break;
                    }
                }
                updateCaret(g);
            }
            caretUpdateToPos = -100;
        }
        if (caretUpdate) {
            caretUpdate = false;
            updateCaret(g);
        }
        if (selected && anim.normalisedProgress() > -.6)
            GameRenderer.renderScaled(textSize / 10, textSize / 10, g, () -> {
                g.setColor(caretColour);
                g.fillRect((int) (caretRenderPosition * 10), -10, 20, 160);
            });
    }

    @Override
    public void updateText(String text) {
        super.updateText(text);
        caretUpdate = true;
    }

    private void updateCaret(Graphics2D g) {
        if (text == null) {
            caretPosition = 0;
            caretRenderPosition = 0;
            return;
        }
        caretPosition = Math.clamp(caretPosition, 0, text.length());
        String string = text.substring(0, caretPosition);
        if (string.isEmpty()) {
            caretRenderPosition = 0;
        } else {
            Shape textShape = new TextLayout(string, font, g.getFontRenderContext()).getOutline(null);
            caretRenderPosition = (float) (textShape.getBounds2D().getWidth() + textShape.getBounds2D().getX());
        }
    }

    public void addChar(char c) {
        if (s.length() < maxLength) {
            s.insert(caretPosition, c);
            caretPosition++;
            updateText(s.toString());
            anim.startTimer();
        }
    }

    public void removeChar() {
        if (!s.isEmpty() && caretPosition != 0) {
            s.deleteCharAt(caretPosition - 1);
            caretPosition--;
            anim.startTimer();
        }
        if (s.isEmpty()) {
            updateText((String) null);
        } else {
            updateText(s.toString());
        }
    }

    public void caretToEnd() {
        if (text == null)
            return;
        caretPosition = text.length();
        anim.startTimer();
        caretUpdate = true;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!blocked) {
            if (type.isArrowKey()) {
                if (type == InputType.RIGHT_ARROW)
                    caretPosition++;
                else
                    caretPosition--;
                anim.startTimer();
                caretUpdate = true;
            }
            if (inside && type == InputType.MOUSE_LEFT) {
                anim.startTimer();
                caretUpdateToPos = pos.x;
                if (bounds != null) {
                    if (textAlign == TextAlign.CENTER)
                        caretUpdateToPos += bounds.middleX() * textSize;
                    else if (textAlign == TextAlign.RIGHT)
                        caretUpdateToPos += bounds.getRight() * textSize;
                }
            }
        }
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
    }

    public EditableTextRenderer setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public boolean isSelected() {
        return selected;
    }
}

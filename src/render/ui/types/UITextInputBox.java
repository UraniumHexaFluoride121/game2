package render.ui.types;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.ButtonState;
import foundation.input.InputType;
import foundation.math.ObjPos;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;

import java.util.function.Predicate;

public class UITextInputBox extends UIButton {
    public final StringBuilder s = new StringBuilder();
    private final int maxLength;
    private final Predicate<InputType> allowedChars;

    public UITextInputBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float textSize, boolean staySelected, int maxLength, Predicate<InputType> allowedChars) {
        super(register, buttonRegister, order, buttonOrder, x, y, width, height, textSize, staySelected);
        this.maxLength = maxLength;
        this.allowedChars = allowedChars;
        box.setShape(UIBox.BoxShape.RECTANGLE);
    }

    @Override
    public boolean blocking(InputType type) {
        return true;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (clickHandler.state == ButtonState.SELECTED) {
            if (type == InputType.BACKSPACE) {
                if (!s.isEmpty())
                    s.deleteCharAt(s.length() - 1);
                if (s.isEmpty()) {
                    text.updateIfDifferent(null);
                } else {
                    text.updateIfDifferent(s.toString());
                }
                return;
            }
            if (allowedChars.test(type)) {
                if (s.length() < maxLength)
                    s.append(type.c);
                text.updateIfDifferent(s.toString());
                return;
            }
        }
        super.buttonPressed(pos, inside, blocked, type);
    }
}

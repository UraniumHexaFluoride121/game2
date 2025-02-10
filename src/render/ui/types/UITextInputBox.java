package render.ui.types;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.ButtonState;
import foundation.input.InputType;
import foundation.math.ObjPos;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.renderables.text.EditableTextRenderer;
import render.renderables.text.TextAlign;

import java.util.function.Predicate;

public class UITextInputBox extends UIButton {
    private final Predicate<InputType> allowedChars;
    private final EditableTextRenderer editableText;

    public UITextInputBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float textSize, boolean staySelected, int maxLength, Predicate<InputType> allowedChars) {
        super(register, buttonRegister, order, buttonOrder, x, y, width, height, textSize, staySelected, null,
                new EditableTextRenderer(null, textSize, UITextLabel.TEXT_COLOUR, maxLength)
                        .setTextAlign(TextAlign.CENTER));
        editableText = (EditableTextRenderer) text;
        this.allowedChars = allowedChars;
        setOnClick(() -> {
            if (!editableText.isSelected())
                editableText.caretToEnd();
            editableText.setSelected(true);
        });
        setOnDeselect(() -> editableText.setSelected(false));
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
                editableText.removeChar();
                return;
            }
            if (allowedChars.test(type)) {
                editableText.addChar(type.c);
                return;
            }
            editableText.buttonPressed(pos.copy().addX(-width / 2 - x), inside, blocked, type);
        }
        super.buttonPressed(pos, inside, blocked, type);
    }

    public String getText() {
        return editableText.getText();
    }
}

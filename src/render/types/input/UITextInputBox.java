package render.types.input;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.ButtonState;
import foundation.input.InputType;
import foundation.math.ObjPos;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.types.input.button.UIButton;
import render.types.text.EditableTextRenderer;
import render.types.text.TextAlign;
import render.types.box.UIBox;
import render.types.text.UITextLabel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.function.Predicate;

public class UITextInputBox extends UIButton {
    private final Predicate<InputType> allowedChars;
    private final EditableTextRenderer editableText;

    public UITextInputBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float textSize, boolean staySelected, int maxLength, Predicate<InputType> allowedChars) {
        super(register, buttonRegister, order, buttonOrder, x, y, width, height, textSize, staySelected, null,
                new EditableTextRenderer(null, textSize, UITextLabel.TEXT_COLOUR, maxLength == -1 ? 100 : maxLength)
                        .setTextAlign(TextAlign.CENTER));
        editableText = (EditableTextRenderer) text;
        if (maxLength == -1)
            editableText.setMaxTextWidth(width - 1);
        this.allowedChars = allowedChars;
        setOnClick(() -> {
            if (!editableText.isSelected())
                editableText.caretToEnd();
            editableText.setSelected(true);
        });
        setOnDeselect(() -> editableText.setSelected(false));
        box.setShape(UIBox.BoxShape.RECTANGLE);
    }

    public UITextInputBox setOnChanged(Runnable onChanged) {
        editableText.setOnChanged(onChanged);
        return this;
    }

    @Override
    public UITextInputBox setText(String text) {
        super.setText(text);
        return this;
    }

    @Override
    public boolean blocking(InputType type) {
        return type.isMouseInput() || type.isCharInput;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (clickHandler.state == ButtonState.SELECTED) {
            if (type == InputType.PASTE_TEXT) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    try {
                        String text = (String) clipboard.getData(DataFlavor.stringFlavor);
                        for (char c : text.toCharArray()) {
                            if (allowedChars.test(new InputType(true, c))) {
                                editableText.addChar(c);
                            }
                        }
                    } catch (UnsupportedFlavorException | IOException _) {
                    }
                }
            } else if (type == InputType.BACKSPACE) {
                editableText.removeChar();
                return;
            } else if (allowedChars.test(type)) {
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

    @Override
    public void delete() {
        super.delete();
        editableText.delete();
    }
}

package foundation.input;

import foundation.Deletable;
import foundation.math.ObjPos;

public class ButtonClickHandler implements ButtonInputReceiver, Deletable {
    public ButtonState state = ButtonState.DEFAULT;
    public boolean mouseHover = false, pressed = false;
    public final boolean staySelected;
    public boolean noDeselect = false;

    private final InputType clickInput;

    private Runnable onClick, onDeselect;

    public ButtonClickHandler(InputType clickInput, boolean staySelected, Runnable onClick) {
        this.staySelected = staySelected;
        this.clickInput = clickInput;
        this.onClick = onClick;
    }

    public ButtonClickHandler(InputType clickInput, boolean staySelected) {
        this.staySelected = staySelected;
        this.clickInput = clickInput;
        onClick = null;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public ButtonClickHandler setOnDeselect(Runnable onDeselect) {
        this.onDeselect = onDeselect;
        return this;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (noDeselect && state == ButtonState.SELECTED)
            return;
        if (type == InputType.MOUSE_OVER) {
            mouseHover = inside && !blocked;
            if (state != ButtonState.SELECTED)
                state = pressed ? ButtonState.PRESSED : mouseHover ? ButtonState.HOVER : ButtonState.DEFAULT;
        } else if (type == clickInput) {
            pressed = inside && !blocked;
            state = pressed ? ButtonState.PRESSED : ButtonState.DEFAULT;
        }
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (noDeselect && state == ButtonState.SELECTED)
            return;
        if (type == clickInput) {
            if (staySelected && inside && !blocked && pressed) {
                pressed = false;
                if (onClick != null)
                    onClick.run();
                state = ButtonState.SELECTED;
            } else {
                if (state == ButtonState.PRESSED && mouseHover) {
                    if (onClick != null)
                        onClick.run();
                }
                pressed = false;
                state = mouseHover ? ButtonState.HOVER : ButtonState.DEFAULT;
                if (onDeselect != null) {
                    onDeselect.run();
                }
            }
        }
    }

    public void select() {
        if (staySelected)
            state = ButtonState.SELECTED;
        pressed = false;
        if (onClick != null)
            onClick.run();
    }

    public void deselect() {
        state = ButtonState.DEFAULT;
        pressed = false;
        if (onDeselect != null)
            onDeselect.run();
    }

    public ButtonClickHandler noDeselect() {
        noDeselect = true;
        return this;
    }

    public void runOnClick() {
        if (onClick != null)
            onClick.run();
    }

    @Override
    public void delete() {
        onClick = null;
        onDeselect = null;
    }
}

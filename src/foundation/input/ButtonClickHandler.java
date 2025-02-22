package foundation.input;

import foundation.Deletable;
import foundation.math.ObjPos;

public class ButtonClickHandler implements ButtonInputReceiver, Deletable {
    public ButtonState state = ButtonState.DEFAULT;
    public boolean mouseHover = false, pressed = false;
    public final boolean staySelected;
    public boolean noDeselect = false;
    public boolean toggle = false;
    private boolean lastSelected = false;

    private final InputType clickInput;

    private Runnable onClick = () -> lastSelected = true, onDeselect = () -> lastSelected = false;

    public ButtonClickHandler(InputType clickInput, boolean staySelected, Runnable onClick) {
        this(clickInput, staySelected);
        setOnClick(onClick);
    }

    public ButtonClickHandler(InputType clickInput, boolean staySelected) {
        this.staySelected = staySelected;
        this.clickInput = clickInput;
    }

    public void setOnClick(Runnable onClick) {
        if (onClick == null)
            this.onClick = () -> lastSelected = true;
        else {
            this.onClick = () -> {
                onClick.run();
                lastSelected = true;
            };
        }
    }

    public ButtonClickHandler setOnDeselect(Runnable onDeselect) {
        if (onDeselect == null)
            this.onDeselect = () -> lastSelected = false;
        else {
            this.onDeselect = () -> {
                onDeselect.run();
                lastSelected = false;
            };
        }
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
            ButtonState prevState = state;
            state = pressed ? ButtonState.PRESSED : ButtonState.DEFAULT;
            if (prevState == ButtonState.SELECTED && state == ButtonState.DEFAULT)
                onDeselect.run();
        }
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (noDeselect && state == ButtonState.SELECTED)
            return;
        if (type == clickInput) {
            if (staySelected && inside && !blocked && pressed) {
                pressed = false;
                if (toggle && lastSelected) {
                    state = ButtonState.DEFAULT;
                    onDeselect.run();
                } else {
                    state = ButtonState.SELECTED;
                    onClick.run();
                }
            } else {
                if (state == ButtonState.PRESSED && mouseHover) {
                    onClick.run();
                }
                pressed = false;
                state = mouseHover ? ButtonState.HOVER : ButtonState.DEFAULT;
                onDeselect.run();
            }
        }
    }

    public void select() {
        if (staySelected)
            state = ButtonState.SELECTED;
        pressed = false;
        onClick.run();
    }

    public void deselect() {
        state = ButtonState.DEFAULT;
        pressed = false;
        onDeselect.run();
    }

    public ButtonClickHandler noDeselect() {
        noDeselect = true;
        return this;
    }

    public ButtonClickHandler toggleMode() {
        toggle = true;
        return this;
    }

    public void runOnClick() {
        onClick.run();
    }

    public boolean isSelected() {
        return state == ButtonState.SELECTED;
    }

    public boolean isDefault() {
        return state == ButtonState.DEFAULT;
    }

    @Override
    public void delete() {
        onClick = null;
        onDeselect = null;
    }
}

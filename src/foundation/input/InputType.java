package foundation.input;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class InputType {
    public static final InputType
            NONE = new InputType(false, ' '), TAB_ON_SWITCH_TO = new InputType(false, ' '),

            MOUSE_RIGHT = new InputType(false, ' '), MOUSE_LEFT = new InputType(false, ' '), MOUSE_OVER = new InputType(false, ' '),
            ESCAPE = new InputType(false, ' '), BACKSPACE = new InputType(false, ' '),
            LEFT_ARROW = new InputType(false, ' '), RIGHT_ARROW = new InputType(false, ' '),

    MOUSE_SCROLL_UP = new InputType(false, ' '), MOUSE_SCROLL_DOWN = new InputType(false, ' '),
            MOUSE_SCROLL_UP_ONCE = new InputType(false, ' '), MOUSE_SCROLL_DOWN_ONCE = new InputType(false, ' ');

    public final boolean isCharInput;
    public final char c;

    InputType(boolean isCharInput, char c) {
        this.isCharInput = isCharInput;
        this.c = c;
    }

    public boolean isIPChar() {
        return isDigit() || Character.isLetter(c) || c == '.' || c == ':' || c == '%' || c == '/';
    }

    public boolean isMouseInput() {
        return this == MOUSE_OVER || this == MOUSE_RIGHT || this == MOUSE_LEFT;
    }

    public boolean isScrollInput() {
        return this == MOUSE_SCROLL_DOWN || this == MOUSE_SCROLL_UP;
    }

    public boolean isScrollInputOnce() {
        return this == MOUSE_SCROLL_DOWN_ONCE || this == MOUSE_SCROLL_UP_ONCE;
    }

    public boolean isArrowKey() {
        return this == LEFT_ARROW || this == RIGHT_ARROW;
    }

    public boolean isDigit() {
        return Character.isDigit(c);
    }

    public static InputType getInputType(InputEvent e) {
        if (e instanceof KeyEvent k) {
            switch (k.getKeyCode()) {
                case KeyEvent.VK_ESCAPE -> {
                    return ESCAPE;
                }
                case KeyEvent.VK_BACK_SPACE -> {
                    return BACKSPACE;
                }
                case KeyEvent.VK_LEFT -> {
                    return LEFT_ARROW;
                }
                case KeyEvent.VK_RIGHT -> {
                    return RIGHT_ARROW;
                }
            }
            char c = k.getKeyChar();
            if (c == KeyEvent.CHAR_UNDEFINED)
                return NONE;
            else
                return new InputType(true, c);
        }
        if (e instanceof MouseEvent m) {
            return switch (m.getButton()) {
                case MouseEvent.BUTTON1 -> MOUSE_LEFT;
                case MouseEvent.BUTTON3 -> MOUSE_RIGHT;
                default -> NONE;
            };
        }
        return NONE;
    }

    public static InputType getScrollInput(MouseWheelEvent e) {
        if (e.getUnitsToScroll() < 0)
            return MOUSE_SCROLL_UP;
        else
            return MOUSE_SCROLL_DOWN;
    }

    public static InputType getScrollInputOnce(MouseWheelEvent e) {
        if (e.getUnitsToScroll() < 0)
            return MOUSE_SCROLL_UP_ONCE;
        else
            return MOUSE_SCROLL_DOWN_ONCE;
    }
}

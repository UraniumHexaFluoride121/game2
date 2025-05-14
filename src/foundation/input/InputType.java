package foundation.input;

import foundation.MainPanel;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class InputType {
    public static final InputType
            NONE = new InputType(), TAB_ON_SWITCH_TO = new InputType(), PASTE_TEXT = new InputType(),

    MOUSE_RIGHT = new InputType(), MOUSE_LEFT = new InputType(), MOUSE_OVER = new InputType(),
            ESCAPE = new InputType(), BACKSPACE = new InputType(), ENTER = new InputType(),
            LEFT_ARROW = new InputType(), RIGHT_ARROW = new InputType();

    public final boolean isCharInput;
    public final char c;

    public InputType(boolean isCharInput, char c) {
        this.isCharInput = isCharInput;
        this.c = c;
    }

    public InputType() {
        this(false, ' ');
    }

    public boolean isIPChar() {
        return isDigit() || Character.isLetter(c) || c == '.' || c == ':' || c == '%' || c == '/';
    }

    public boolean isMouseInput() {
        return this == MOUSE_OVER || this == MOUSE_RIGHT || this == MOUSE_LEFT;
    }

    public boolean isArrowKey() {
        return this == LEFT_ARROW || this == RIGHT_ARROW;
    }

    public boolean isDigit() {
        return Character.isDigit(c);
    }

    public boolean isFileNameChar() {
        return isCharInput && (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '-' || c == ' ');
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
                case KeyEvent.VK_ENTER -> {
                    return ENTER;
                }
                case KeyEvent.VK_V -> {
                    if (MainPanel.controlHeld)
                        return PASTE_TEXT;
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
        return new ScrollInputType(e.getScrollAmount(), e.getUnitsToScroll() < 0);
    }

    @Override
    public String toString() {
        return "[Is char: " + isCharInput + ", char: " + c + "]";
    }
}

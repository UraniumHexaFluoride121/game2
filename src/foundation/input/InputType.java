package foundation.input;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public enum InputType {
    DIGIT_1(true, '1'), DIGIT_2(true, '2'), DIGIT_3(true, '3'), DIGIT_4(true, '4'), DIGIT_5(true, '5'),
    DIGIT_6(true, '6'), DIGIT_7(true, '7'), DIGIT_8(true, '8'), DIGIT_9(true, '9'), DIGIT_0(true, '0'),

    NONE(false, ' '),
    MOUSE_RIGHT(false, ' '), MOUSE_LEFT(false, ' '), MOUSE_OVER(false, ' '),
    ESCAPE(false, ' '), BACKSPACE(false, ' '),

    MOUSE_SCROLL_UP(false, ' '), MOUSE_SCROLL_DOWN(false, ' ');

    public final boolean isCharInput;
    public final char c;

    InputType(boolean isCharInput, char c) {
        this.isCharInput = isCharInput;
        this.c = c;
    }

    public boolean isMouseInput() {
        return this == MOUSE_OVER || this == MOUSE_RIGHT || this == MOUSE_LEFT;
    }

    public boolean isScrollInput() {
        return this == MOUSE_SCROLL_DOWN || this == MOUSE_SCROLL_UP;
    }

    public boolean isDigit() {
        return Character.isDigit(c);
    }

    public static InputType getInputType(InputEvent e) {
        if (e instanceof KeyEvent k) {
            return switch (k.getKeyCode()) {
                case KeyEvent.VK_ESCAPE -> ESCAPE;
                case KeyEvent.VK_BACK_SPACE -> BACKSPACE;
                case KeyEvent.VK_0 -> DIGIT_0;
                case KeyEvent.VK_1 -> DIGIT_1;
                case KeyEvent.VK_2 -> DIGIT_2;
                case KeyEvent.VK_3 -> DIGIT_3;
                case KeyEvent.VK_4 -> DIGIT_4;
                case KeyEvent.VK_5 -> DIGIT_5;
                case KeyEvent.VK_6 -> DIGIT_6;
                case KeyEvent.VK_7 -> DIGIT_7;
                case KeyEvent.VK_8 -> DIGIT_8;
                case KeyEvent.VK_9 -> DIGIT_9;
                default -> NONE;
            };
        }
        if (e instanceof MouseEvent m) {
            if (m instanceof MouseWheelEvent w) {
                if (w.getUnitsToScroll() < 0)
                    return MOUSE_SCROLL_UP;
                else
                    return MOUSE_SCROLL_DOWN;
            }
            return switch (m.getButton()) {
                case MouseEvent.BUTTON1 -> MOUSE_LEFT;
                case MouseEvent.BUTTON3 -> MOUSE_RIGHT;
                default -> NONE;
            };
        }
        return NONE;
    }
}

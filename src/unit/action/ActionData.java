package unit.action;

import foundation.input.ButtonClickHandler;
import foundation.input.InputType;

public class ActionData {
    public final int index;
    public final ButtonClickHandler clickHandler;
    public boolean enabled;

    public ActionData(int index, Runnable onClick, boolean enabled) {
        this.index = index;
        clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, false, onClick);
        this.enabled = enabled;
    }
}

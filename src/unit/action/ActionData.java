package unit.action;

import foundation.input.ButtonClickHandler;
import foundation.input.InputType;

public class ActionData {
    public final ButtonClickHandler clickHandler;
    public boolean enabled;

    public ActionData(Runnable onClick, boolean enabled) {
        clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, false, onClick);
        this.enabled = enabled;
    }
}

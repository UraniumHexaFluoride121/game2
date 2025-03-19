package unit.action;

import foundation.input.ButtonClickHandler;
import foundation.input.InputType;

public class ActionData {
    public ButtonClickHandler clickHandler;
    public ActionIconType type;

    public ActionData(ActionIconType type) {
        this.type = type;
    }

    public void init(Runnable onClick) {
        clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, false, onClick);
    }
}

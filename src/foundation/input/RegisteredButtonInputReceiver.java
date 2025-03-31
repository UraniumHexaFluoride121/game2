package foundation.input;

import foundation.math.ObjPos;

public interface RegisteredButtonInputReceiver extends ButtonInputReceiver {
    boolean posInside(ObjPos pos, InputType type);
    boolean blocking(InputType type);
    ButtonOrder getButtonOrder();
    default int getZOrder() {
        return 0;
    }
}

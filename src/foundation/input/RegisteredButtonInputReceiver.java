package foundation.input;

import foundation.math.ObjPos;
import render.RenderOrder;

public interface RegisteredButtonInputReceiver extends ButtonInputReceiver {
    boolean posInside(ObjPos pos, InputType type);
    boolean blocking(InputType type);
    RenderOrder getButtonOrderTemp();
    default int getZOrder() {
        return 0;
    };
}

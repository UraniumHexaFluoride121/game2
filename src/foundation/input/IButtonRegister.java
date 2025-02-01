package foundation.input;

import foundation.math.ObjPos;

public interface IButtonRegister {
    void register(RegisteredButtonInputReceiver b);
    void remove(RegisteredButtonInputReceiver b);
    boolean acceptInput(ObjPos pos, InputType type, boolean pressed);
}

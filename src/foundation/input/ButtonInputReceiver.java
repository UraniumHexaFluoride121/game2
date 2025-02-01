package foundation.input;

import foundation.math.ObjPos;

public interface ButtonInputReceiver {

    void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type);

    void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type);
}

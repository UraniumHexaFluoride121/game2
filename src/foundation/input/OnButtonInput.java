package foundation.input;

import foundation.Deletable;
import foundation.math.ObjPos;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class OnButtonInput implements RegisteredButtonInputReceiver, Deletable {
    private ButtonRegister register;
    private final ButtonOrder order;
    private Predicate<InputType> typePredicate;
    private Runnable onClick;

    public OnButtonInput(ButtonRegister register, ButtonOrder order, Predicate<InputType> typePredicate, Runnable onClick) {
        this.register = register;
        this.onClick = onClick;
        if (register != null)
            register.register(this);
        this.order = order;
        this.typePredicate = typePredicate;
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return true;
    }

    @Override
    public boolean blocking(InputType type) {
        return blocking;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return order;
    }

    private boolean blocking = false;

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!blocked && inside && typePredicate.test(type)) {
            blocking = true;
            onClick.run();
        } else
            blocking = false;
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        blocking = false;
    }


    @Override
    public void delete() {
        if (register != null) {
            register.remove(this);
            register = null;
        }
        onClick = null;
        typePredicate = null;
    }
}

package foundation.input;

import foundation.Deletable;
import foundation.math.ObjPos;
import render.RenderOrder;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class OnButtonInput implements RegisteredButtonInputReceiver, Deletable {
    private ButtonRegister register;
    private final RenderOrder order;
    private int zOrder = 0;
    private Predicate<InputType> typePredicate;
    private Consumer<InputType> onClick;

    public OnButtonInput(ButtonRegister register, RenderOrder order, Predicate<InputType> typePredicate, Runnable onClick) {
        this(register, order, typePredicate, type -> onClick.run());
    }

    public OnButtonInput(ButtonRegister register, RenderOrder order, Predicate<InputType> typePredicate, Consumer<InputType> onClick) {
        this.register = register;
        this.onClick = onClick;
        if (register != null)
            register.register(this);
        this.order = order;
        this.typePredicate = typePredicate;
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        return true;
    }

    @Override
    public boolean blocking(InputType type) {
        return blocking;
    }

    @Override
    public RenderOrder getButtonOrder() {
        return order;
    }

    public OnButtonInput setZOrder(int zOrder) {
        this.zOrder = zOrder;
        return this;
    }

    @Override
    public int getZOrder() {
        return zOrder;
    }

    private boolean blocking = false;

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!blocked && inside && typePredicate.test(type)) {
            blocking = true;
            onClick.accept(type);
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

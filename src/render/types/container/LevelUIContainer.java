package render.types.container;

import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.math.ObjPos;
import level.AbstractLevel;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;

public class LevelUIContainer<T extends AbstractLevel<?, ?>> extends UIContainer {
    public T level;

    public LevelUIContainer(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, T level) {
        super(register, buttonRegister, order, x, y);
        this.level = level;
    }

    public boolean posInsideLevelOffset(ObjPos pos) {
        return true;
    }

    @Override
    public final boolean posInside(ObjPos pos, InputType type) {
        ObjPos offset = level.levelRenderer.transformCameraPosToBlock(pos);
        return super.posInside(offset, type) && posInsideLevelOffset(offset);
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        super.buttonPressed(level.levelRenderer.transformCameraPosToBlock(pos), inside, blocked, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        super.buttonReleased(level.levelRenderer.transformCameraPosToBlock(pos), inside, blocked, type);
    }

    @Override
    public void delete() {
        super.delete();
        level = null;
    }
}

package render.types.container;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.math.ObjPos;
import level.AbstractLevel;
import render.GameRenderer;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;

import java.util.function.BiConsumer;

public class LevelUIContainer<T extends AbstractLevel<?, ?>> extends UIContainer {
    public T level;

    public LevelUIContainer(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, BiConsumer<GameRenderer, ButtonRegister> createRenderer, T level) {
        super(register, buttonRegister, order, buttonOrder, x, y, createRenderer);
        this.level = level;
    }

    public LevelUIContainer(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, T level) {
        super(register, buttonRegister, order, buttonOrder, x, y);
        this.level = level;
    }

    public boolean posInsideLevelOffset(ObjPos pos) {
        return true;
    }

    @Override
    public final boolean posInside(ObjPos pos) {
        ObjPos offset = level.levelRenderer.transformCameraPosToBlock(pos);
        return super.posInside(offset) && posInsideLevelOffset(offset);
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

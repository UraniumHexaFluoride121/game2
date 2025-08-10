package render.types.input.button;

import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.math.ObjPos;
import level.AbstractLevel;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;

public class LevelUIButton extends UIButton {
    public AbstractLevel<?, ?> level;
    public LevelUIButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float width, float height, float textSize, boolean staySelected, AbstractLevel<?, ?> level, Runnable onClick) {
        super(register, buttonRegister, order, x, y, width, height, textSize, staySelected, onClick);
        this.level = level;
    }

    public LevelUIButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float width, float height, float textSize, boolean staySelected, AbstractLevel<?, ?> level) {
        super(register, buttonRegister, order, x, y, width, height, textSize, staySelected);
        this.level = level;
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        return super.posInside(level.levelRenderer.transformCameraPosToBlock(pos), type);
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

package render.ui.types;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.math.ObjPos;
import level.Level;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;

public class LevelUIShapeButton extends UIShapeButton {
    public Level level;

    public LevelUIShapeButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, boolean staySelected, Level level) {
        super(register, buttonRegister, order, buttonOrder, x, y, width, height, staySelected);
        this.level = level;
    }

    public LevelUIShapeButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, boolean staySelected, Runnable onClick, Level level) {
        super(register, buttonRegister, order, buttonOrder, x, y, width, height, staySelected, onClick);
        this.level = level;
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return super.posInside(level.levelRenderer.transformCameraPosToBlock(pos));
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        super.buttonPressed(level.levelRenderer.transformCameraPosToBlock(pos), inside, blocked, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        super.buttonReleased(level.levelRenderer.transformCameraPosToBlock(pos), inside, blocked, type);
    }
}
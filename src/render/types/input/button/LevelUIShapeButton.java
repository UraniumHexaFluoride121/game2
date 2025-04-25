package render.types.input.button;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.math.ObjPos;
import level.AbstractLevel;
import level.Level;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.types.box.UIBox;
import render.types.text.TooltipManager;

import java.util.function.Consumer;

public class LevelUIShapeButton extends UIShapeButton {
    public AbstractLevel<?, ?> level;

    public LevelUIShapeButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, boolean staySelected, AbstractLevel<?, ?> level) {
        super(register, buttonRegister, order, buttonOrder, x, y, width, height, staySelected);
        this.level = level;
    }

    public LevelUIShapeButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, boolean staySelected, Runnable onClick, AbstractLevel<?, ?> level) {
        super(register, buttonRegister, order, buttonOrder, x, y, width, height, staySelected, onClick);
        this.level = level;
    }

    @Override
    public LevelUIShapeButton tooltip(Consumer<TooltipManager> action) {
        super.tooltip(action);
        return this;
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
}
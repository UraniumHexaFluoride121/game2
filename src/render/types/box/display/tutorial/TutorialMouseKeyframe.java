package render.types.box.display.tutorial;

import foundation.math.ObjPos;
import render.anim.sequence.KeyframeFunction;
import unit.action.AbstractActionSelector;

import static level.tile.Tile.*;

public class TutorialMouseKeyframe {
    public final float time;
    public final ObjPos pos;
    public final KeyframeFunction typeX;
    public final KeyframeFunction typeY;
    public boolean click = false;
    public float clickDelay = 0;
    public Runnable onClick = null;

    private TutorialMouseKeyframe(float time, ObjPos pos, KeyframeFunction typeX, KeyframeFunction typeY) {
        this.time = time;
        this.pos = pos;
        this.typeX = typeX;
        this.typeY = typeY;
    }

    public TutorialMouseKeyframe setOnClick(float clickDelay, Runnable onClick) {
        this.clickDelay = clickDelay;
        this.onClick = onClick;
        click = true;
        return this;
    }

    public static TutorialMouseKeyframe actionSelector(float time, int x, int y, float xOffset, float yOffset, int actionCount, int index, KeyframeFunction typeX, KeyframeFunction typeY) {
        ObjPos pos = TutorialMapElement.getRenderPos(x, y)
                .add(xOffset * TILE_SIZE, yOffset * TILE_SIZE)
                .add(AbstractActionSelector.actionCenter(actionCount, index));
        return new TutorialMouseKeyframe(time, pos, typeX, typeY);
    }

    public static TutorialMouseKeyframe tile(float time, int x, int y, float xOffset, float yOffset, KeyframeFunction typeX, KeyframeFunction typeY) {
        ObjPos pos = TutorialMapElement.getCenteredRenderPos(x, y)
                .add(xOffset * TILE_SIZE, yOffset * TILE_SIZE);
        return new TutorialMouseKeyframe(time, pos, typeX, typeY);
    }

    public static TutorialMouseKeyframe delayUntil(float time) {
        return delayUntil(time, KeyframeFunction.lerp(), KeyframeFunction.lerp());
    }

    public static TutorialMouseKeyframe delayUntil(float time, KeyframeFunction typeX, KeyframeFunction typeY) {
        return new TutorialMouseKeyframe(time, null, typeX, typeY);
    }
}

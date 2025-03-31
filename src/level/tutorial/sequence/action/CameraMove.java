package level.tutorial.sequence.action;

import foundation.math.ObjPos;
import level.Level;
import level.tile.Tile;

public class CameraMove extends TutorialAction {
    public static CameraMove toTile(Level level, int x, int y) {
        return new CameraMove(level, Tile.getCenteredRenderPos(x, y));
    }

    private CameraMove(Level l, ObjPos pos) {
        super(() -> l.levelRenderer.setCameraInterpBlockPos(pos));
    }
}

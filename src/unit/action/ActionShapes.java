package unit.action;

import java.awt.geom.Path2D;

public abstract class ActionShapes {
    public static final Path2D.Float FLAG = new Path2D.Float();

    static {
        FLAG.moveTo(
                0.4f, 0.2f);
        FLAG.lineTo(
                0.2f, 0.75f);
        FLAG.closePath();

        FLAG.moveTo(
                0.32f, 0.7f);
        FLAG.quadTo(
                0.6f, 0.6f,
                0.75f, 0.7f);
        FLAG.lineTo(
                0.8f, 0.5f);
        FLAG.quadTo(
                0.6f, 0.4f,
                0.4f, 0.5f);
        FLAG.closePath();
    }
}

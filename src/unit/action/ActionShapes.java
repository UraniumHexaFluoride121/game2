package unit.action;

import render.GameRenderer;
import render.Renderable;
import render.types.box.UIBox;
import render.types.input.button.UIShapeButton;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public abstract class ActionShapes {
    public static final Path2D.Float FLAG = new Path2D.Float();
    public static final Path2D.Float SHIELD = new Path2D.Float();
    public static final Path2D.Float SHIP = new Path2D.Float();
    public static final Path2D.Float SUPPLY = new Path2D.Float();
    public static Path2D.Float ANTIMATTER;
    public static final Shape PLUS = UIShapeButton.plus(new UIBox(1, 1), 0.6f, 0.3f);

    static {
        FLAG.moveTo(.4, .2);
        FLAG.lineTo(.2, .75);
        FLAG.closePath();

        FLAG.moveTo(.32, .7);
        FLAG.quadTo(
                .6, .6,
                .75, .7
        );
        FLAG.lineTo(.8, .5);
        FLAG.quadTo(
                .6, .4,
                .4, .5
        );
        FLAG.closePath();
    }

    private static final double SHIELD_P1_X = .58, SHIELD_P2_X = .23, SHIELD_P2_X_2 = .13, SHIELD_P3_X = .38, SHIELD_P1_X_OFFSET = .25, SHIELD_P4_X_OFFSET = .25, SHIELD_P2_Y_OFFSET = .1, SHIELD_P2_Y_OFFSET_2 = .05;
    static {
        SHIELD.moveTo(SHIELD_P1_X, .7);
        SHIELD.curveTo(
                SHIELD_P1_X - SHIELD_P1_X_OFFSET, .7,
                SHIELD_P2_X, .5 + SHIELD_P2_Y_OFFSET,
                SHIELD_P2_X, .5
        );
        SHIELD.curveTo(
                SHIELD_P2_X, .5 - SHIELD_P2_Y_OFFSET,
                SHIELD_P1_X - SHIELD_P1_X_OFFSET, .32,
                SHIELD_P3_X, .3
        );
        SHIELD.curveTo(
                SHIELD_P1_X - SHIELD_P1_X_OFFSET, .32,
                SHIELD_P2_X_2, .5 - SHIELD_P2_Y_OFFSET - SHIELD_P2_Y_OFFSET_2,
                SHIELD_P2_X_2, .5
        );
        SHIELD.curveTo(
                SHIELD_P2_X_2, .5 + SHIELD_P2_Y_OFFSET + SHIELD_P2_Y_OFFSET_2,
                SHIELD_P1_X - SHIELD_P1_X_OFFSET, .7,
                SHIELD_P1_X, .7
        );
        SHIELD.closePath();

        SHIELD.moveTo(SHIELD_P1_X, .7);
        SHIELD.quadTo(
                SHIELD_P1_X + SHIELD_P4_X_OFFSET * .7, .7,
                SHIELD_P1_X + SHIELD_P4_X_OFFSET, .65
        );
        SHIELD.quadTo(
                SHIELD_P1_X + SHIELD_P4_X_OFFSET * .7, .7,
                SHIELD_P1_X, .7
        );
        SHIELD.closePath();
    }

    static {
        SHIP.moveTo(.3, .53);
        SHIP.lineTo(.35, .57);
        SHIP.lineTo(.45, .6);
        SHIP.lineTo(.6, .58);
        SHIP.lineTo(.7, .52);
        SHIP.lineTo(.7, .48);
        SHIP.lineTo(.6, .42);
        SHIP.lineTo(.45, .4);
        SHIP.lineTo(.35, .43);
        SHIP.lineTo(.3, .47);
        SHIP.closePath();
        SHIP.moveTo(.4, .65);
        SHIP.lineTo(.45, .65);
        SHIP.lineTo(.45, .35);
        SHIP.lineTo(.4, .35);
        SHIP.closePath();
        SHIP.moveTo(.38, .64);
        SHIP.lineTo(.6, .64);
        SHIP.lineTo(.6, .63);
        SHIP.lineTo(.38, .63);
        SHIP.closePath();
        SHIP.moveTo(.38, .36);
        SHIP.lineTo(.6, .36);
        SHIP.lineTo(.6, .37);
        SHIP.lineTo(.38, .37);
        SHIP.closePath();
    }

    static {
        float size = 0.3f;
        SUPPLY.moveTo(.5f + Math.cos(Math.toRadians(30 + 60 * 0)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 0)) * size);
        SUPPLY.lineTo(.5f + Math.cos(Math.toRadians(30 + 60 * 1)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 1)) * size);
        SUPPLY.lineTo(.5f + Math.cos(Math.toRadians(30 + 60 * 2)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 2)) * size);
        SUPPLY.lineTo(.5f + Math.cos(Math.toRadians(30 + 60 * 3)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 3)) * size);
        SUPPLY.lineTo(.5f + Math.cos(Math.toRadians(30 + 60 * 4)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 4)) * size);
        SUPPLY.lineTo(.5f + Math.cos(Math.toRadians(30 + 60 * 5)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 5)) * size);
        SUPPLY.closePath();
        SUPPLY.moveTo(.5f + Math.cos(Math.toRadians(30 + 60 * 0)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 0)) * size);
        SUPPLY.lineTo(.5f, .5f);
        SUPPLY.closePath();
        SUPPLY.moveTo(.5f + Math.cos(Math.toRadians(30 + 60 * 2)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 2)) * size);
        SUPPLY.lineTo(.5f, .5f);
        SUPPLY.closePath();
        SUPPLY.moveTo(.5f + Math.cos(Math.toRadians(30 + 60 * 4)) * size, .5f + Math.sin(Math.toRadians(30 + 60 * 4)) * size);
        SUPPLY.lineTo(.5f, .5f);
        SUPPLY.closePath();
    }

    static {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(0.35f, 0.22f);
        path.lineTo(0.35f, 0.3f);
        path.lineTo(1 - 0.35f, 0.3f);
        path.lineTo(1 - 0.35f, 0.22f);
        path.closePath();
        path.moveTo(0.35f, 1 - 0.22f);
        path.lineTo(0.35f, 1 - 0.3f);
        path.lineTo(1 - 0.35f, 1 - 0.3f);
        path.lineTo(1 - 0.35f, 1 - 0.22f);
        path.closePath();
        path.moveTo(0.4f, 0.33f);
        path.lineTo(0.4f, 1 - 0.33f);
        path.lineTo(1 - 0.4f, 1 - 0.33f);
        path.lineTo(1 - 0.4f, 0.33f);
        path.closePath();
        AffineTransform t = new AffineTransform();
        t.rotate(Math.toRadians(30), 0.5f, 0.5f);
        t.translate(0.5f, 0.5f);
        t.scale(1.1f, 1.1f);
        t.translate(-0.5f, -0.5f);
        ANTIMATTER = (Path2D.Float) path.createTransformedShape(t);
    }

    public static void stealthIcon(Graphics2D g) {
        g.setStroke(Renderable.roundedStroke(.02f));
        Shape clip = g.getClip();
        Path2D.Float path = new Path2D.Float();
        path.moveTo(.27f, .27f);
        path.lineTo(.73f, .73f);
        g.clip(Renderable.inverseShape(Renderable.outlineShape(path, 0.11f)));
        GameRenderer.renderTransformed(g, () -> {
            g.scale(1.2, 1.2);
            g.translate(-.075, -.075);
            g.draw(ActionShapes.SHIP);
            g.fill(ActionShapes.SHIP);
        });
        g.setClip(clip);
        g.setStroke(Renderable.roundedStroke(.06f));
        g.draw(path);
    }
}

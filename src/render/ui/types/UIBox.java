package render.ui.types;

import foundation.input.ButtonClickHandler;
import render.GameRenderer;
import render.Renderable;
import render.ui.UIColourTheme;

import java.awt.*;
import java.awt.geom.Path2D;

public class UIBox implements Renderable {
    private static final BasicStroke STROKE = Renderable.sharpCornerStroke(0.17f * SCALING);
    protected Shape box;
    private ButtonClickHandler clickHandler = null;
    private UIColourTheme colourTheme = UIColourTheme.LIGHT_BLUE;
    private boolean borderOnly = false, centerOnly = false;
    protected float width, height, corner;
    private BoxShape shape;

    public UIBox(float width, float height) {
        this(width, height, 0.5f, BoxShape.RECTANGLE_ALL_CORNERS_CUT);
    }

    public UIBox(float width, float height, float corner, BoxShape shape) {
        this.width = width;
        this.height = height;
        this.corner = corner;
        this.shape = shape;
        updateShape();
    }

    public UIBox setShape(BoxShape shape) {
        this.shape = shape;
        updateShape();
        return this;
    }

    public UIBox setCorner(float corner) {
        this.corner = corner;
        updateShape();
        return this;
    }

    private void updateShape() {
        box = shape.create(width, height, corner);
    }

    public UIBox setClickHandler(ButtonClickHandler clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }
    
    public UIBox setColourTheme(UIColourTheme colourTheme) {
        this.colourTheme = colourTheme;
        return this;
    }

    public UIBox borderOnly() {
        borderOnly = true;
        centerOnly = false;
        return this;
    }

    public UIBox centerOnly() {
        centerOnly = true;
        borderOnly = false;
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderScaled(1f / SCALING, g, () -> {
            if (!borderOnly) {
                g.setColor(getBackgroundColour());
                g.fill(box);
            }
            if (!centerOnly) {
                g.setColor(getBorderColour());
                g.setStroke(STROKE);
                g.draw(box);
            }
        });
    }

    private Color getBorderColour() {
        if (clickHandler == null)
            return colourTheme.borderColour;
        return switch (clickHandler.state) {
            case DEFAULT -> colourTheme.borderColour;
            case HOVER -> colourTheme.borderColourHover;
            case PRESSED -> colourTheme.borderColourPressed;
            case SELECTED -> colourTheme.borderColourSelected;
        };
    }

    private Color getBackgroundColour() {
        if (clickHandler == null)
            return colourTheme.backgroundColour;
        return switch (clickHandler.state) {
            case DEFAULT -> colourTheme.backgroundColour;
            case HOVER -> colourTheme.backgroundColourHover;
            case PRESSED -> colourTheme.backgroundColourPressed;
            case SELECTED -> colourTheme.backgroundColourSelected;
        };
    }

    public Shape getShape() {
        return box;
    }

    public enum BoxShape {
        RECTANGLE_ALL_CORNERS_CUT((width, height, corner) -> new Polygon(
                new int[]{
                        0,
                        0,
                        (int) (corner * SCALING),
                        (int) ((width - corner) * SCALING),
                        (int) (width * SCALING),
                        (int) (width * SCALING),
                        (int) ((width - corner) * SCALING),
                        (int) (corner * SCALING)
                },
                new int[]{
                        (int) (corner * SCALING),
                        (int) ((height - corner) * SCALING),
                        (int) (height * SCALING),
                        (int) (height * SCALING),
                        (int) ((height - corner) * SCALING),
                        (int) (corner * SCALING),
                        0,
                        0
                },
                8
        )),
        RECTANGLE_RIGHT_CORNERS_CUT((width, height, corner) -> new Polygon(
                new int[]{
                        0,
                        (int) ((width - corner) * SCALING),
                        (int) (width * SCALING),
                        (int) (width * SCALING),
                        (int) ((width - corner) * SCALING),
                        0
                },
                new int[]{
                        (int) (height * SCALING),
                        (int) (height * SCALING),
                        (int) ((height - corner) * SCALING),
                        (int) (corner * SCALING),
                        0,
                        0
                },
                6
        )),
        RECTANGLE_LEFT_CORNERS_CUT((width, height, corner) -> new Polygon(
                new int[]{
                        0,
                        0,
                        (int) (corner * SCALING),
                        (int) (width * SCALING),
                        (int) (width * SCALING),
                        (int) (corner * SCALING)
                },
                new int[]{
                        (int) (corner * SCALING),
                        (int) ((height - corner) * SCALING),
                        (int) (height * SCALING),
                        (int) (height * SCALING),
                        0,
                        0
                },
                6
        )),
        RECTANGLE_TOP_CORNERS_CUT((width, height, corner) -> new Polygon(
                new int[]{
                        0,
                        0,
                        (int) (corner * SCALING),
                        (int) ((width - corner) * SCALING),
                        (int) (width * SCALING),
                        (int) (width * SCALING),
                },
                new int[]{
                        0,
                        (int) ((height - corner) * SCALING),
                        (int) (height * SCALING),
                        (int) (height * SCALING),
                        (int) ((height - corner) * SCALING),
                        0
                },
                6
        )),
        RECTANGLE((width, height, corner) -> new Polygon(
                new int[]{
                        0,
                        0,
                        (int) (width * SCALING),
                        (int) (width * SCALING),
                },
                new int[]{
                        0,
                        (int) (height * SCALING),
                        (int) (height * SCALING),
                        0
                },
                4
        ));

        private final ShapeSupplier shapeSupplier;

        public Shape create(float width, float height, float corner) {
            return shapeSupplier.create(width, height, corner);
        }

        BoxShape(ShapeSupplier shapeSupplier) {
            this.shapeSupplier = shapeSupplier;
        }
    }

    @FunctionalInterface
    private interface ShapeSupplier {
        Shape create(float width, float height, float corner);
    }
}

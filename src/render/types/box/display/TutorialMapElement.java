package render.types.box.display;

import foundation.Deletable;
import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import foundation.math.ObjPos;
import level.tile.Tile;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;
import render.UIColourTheme;
import render.anim.timer.SineAnimation;
import render.level.tile.HexagonRenderer;
import render.types.box.UIBox;
import render.types.text.DynamicTextRenderer;
import unit.UnitData;
import unit.UnitLike;
import unit.UnitPose;
import unit.UnitTeam;
import unit.type.UnitType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.HashSet;

import static level.tile.Tile.*;

public class TutorialMapElement extends BoxElement {
    private static final UIColourTheme THEME = new UIColourTheme(new Color(128, 128, 128), new Color(25, 22, 22));
    private static final Stroke stroke = Renderable.roundedStroke(0.2f);
    private static final Color borderColour = UIColourTheme.setAlpha(BORDER_COLOUR, 0.2f);

    public static final float TILE_SIZE_SMALL = 2f, TILE_SIZE_MEDIUM = 3f, TILE_SIZE_LARGE = 4.5f;

    private final float size;
    private final HexagonRenderer hexagonRenderer;
    private final Path2D.Float borderPath = new Path2D.Float();
    private final HorizontalAlign align;
    public boolean enabled = true;
    public final float width, height;
    private final UIBox box;
    private final HashSet<MapTile> tiles = new HashSet<>();

    public TutorialMapElement(float initialMargin, HorizontalAlign align, float width, float height, float tileSize) {
        super(initialMargin);
        this.align = align;
        this.width = width;
        this.height = height;
        box = new UIBox(width, height).setColourTheme(THEME).setCorner(1.2f);
        size = tileSize;
        hexagonRenderer = new HexagonRenderer(size, false, Tile.STROKE_WIDTH, Tile.BORDER_COLOUR).rotate(size);
    }

    public MapTile addTile(int x, int y) {
        ObjPos renderPos = getRenderPos(x, y);
        MapTile tile = new MapTile(renderPos, x, y);
        tiles.add(tile);
        AffineTransform t = new AffineTransform();
        t.translate(renderPos.x, renderPos.y);
        borderPath.append(t.createTransformedShape(hexagonRenderer.getPath()), false);
        return tile;
    }

    @Override
    public float width() {
        return width;
    }

    @Override
    public float height() {
        return height;
    }

    @Override
    public HorizontalAlign align() {
        return align;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderOffset(xOffset(), yOffset(), g, () -> {
            box.render(g);
            g.setColor(borderColour);
            g.setStroke(stroke);
            GameRenderer.renderOffset(width() / 2, height() / 2, g, () -> {
                g.draw(borderPath);
                tiles.forEach(map -> map.render(g));
            });
        });
    }

    private float xOffset() {
        return switch (align) {
            case LEFT -> 0;
            case CENTER -> -width() / 2;
            case RIGHT -> -width();
        };
    }

    private float yOffset() {
        return -height;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setIsEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public HitBox renderBox() {
        return HitBox.createFromOriginAndSize(xOffset(), yOffset(),
                width(), height());
    }

    @Override
    public void setClickHandler(ButtonClickHandler clickHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        super.delete();
        tiles.forEach(MapTile::delete);
    }

    private ObjPos getRenderPos(int x, int y) {
        return new ObjPos(x * SIN_60_DEG * size + ((y % 2) == 1 ? SIN_60_DEG * size / 2 : 0), (y - 0.5f) * size * 1.5f / 2);
    }

    public static class MapTile implements Renderable, Deletable {
        private final ObjPos renderPos;
        private final int x, y;
        private MapUnit unit = null;

        public MapTile(ObjPos renderPos, int x, int y) {
            this.renderPos = renderPos;
            this.x = x;
            this.y = y;
        }

        public MapTile addUnit(UnitType type, UnitTeam team) {
            unit = new MapUnit(type, team, new Point(x, y), renderPos);
            return this;
        }

        @Override
        public void render(Graphics2D g) {
            if (unit != null)
                unit.render(g);
            GameRenderer.renderOffset(renderPos, g, () -> {
            });
        }

        @Override
        public void delete() {
            if (unit != null)
                unit.delete();
        }
    }

    public static class MapUnit extends UnitLike implements Renderable, Deletable {
        private final UnitData data;
        private final SineAnimation idleAnimX, idleAnimY;
        private final ObjPos renderPos;

        public MapUnit(UnitType type, UnitTeam team, Point pos, ObjPos renderPos) {
            this.renderPos = renderPos;
            data = new UnitData(type, team, pos);
            data.initialise();
            idleAnimX = new SineAnimation((5f + (float) Math.random()) * data.type.getBobbingRate(), (float) Math.random() * 360);
            idleAnimY = new SineAnimation((7f + (float) Math.random()) * data.type.getBobbingRate(), (float) Math.random() * 360);
        }

        @Override
        public void render(Graphics2D g) {
            renderUnit(g, UnitPose.FORWARD, data.type.shieldHP != 0);
        }

        @Override
        public UnitData getData() {
            return data;
        }

        @Override
        public ObjPos getShipRenderOffset() {
            return new ObjPos(data.shipBobbingX(idleAnimX), data.shipBobbingY(idleAnimY));
        }

        @Override
        public ObjPos getRenderPos() {
            return renderPos;
        }

        private final DynamicTextRenderer textHP = createHPText(), textShieldHP = createShieldHPText();

        @Override
        public DynamicTextRenderer getHPText() {
            return textHP;
        }

        @Override
        public DynamicTextRenderer getShieldHPText() {
            return textShieldHP;
        }

        @Override
        public void delete() {
            textHP.delete();
            textShieldHP.delete();
        }
    }
}

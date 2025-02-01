package level;

import foundation.math.ObjPos;
import foundation.math.RandomType;
import render.GameRenderer;
import render.anim.LerpAnimation;
import render.renderables.HexagonRenderer;
import render.texture.ImageRenderer;

import java.awt.*;

public class Tile {
    public static final float STROKE_WIDTH = 0.1f, HIGHLIGHT_STROKE_WIDTH = 0.2f;
    public static final float SCREEN_STROKE_WIDTH_MARGIN = GameRenderer.scaleFloatToScreen(HIGHLIGHT_STROKE_WIDTH), BLOCK_STROKE_WIDTH_MARGIN = HIGHLIGHT_STROKE_WIDTH;

    public static final Color
            BORDER_COLOUR = new Color(175, 249, 255),
            BLUE_HIGHLIGHT_COLOUR = new Color(87, 177, 225),
            BLUE_TRANSPARENT_COLOUR = new Color(122, 151, 248, 142),
            FOW_COLOUR = new Color(0, 0, 0, 128), ILLEGAL_TILE_COLOUR = new Color(227, 90, 90);
    public static final float TILE_SIZE = 6;
    public static final float SIN_60_DEG = ((float) Math.sin(Math.toRadians(60)));

    public static final HexagonRenderer
            BORDER_RENDERER = new HexagonRenderer(TILE_SIZE, false, STROKE_WIDTH, BORDER_COLOUR),
            BORDER_HIGHLIGHT_RENDERER = new HexagonRenderer(TILE_SIZE, false, HIGHLIGHT_STROKE_WIDTH, BLUE_HIGHLIGHT_COLOUR),
            HIGHLIGHT_RENDERER = new HexagonRenderer(TILE_SIZE, true, HIGHLIGHT_STROKE_WIDTH, BLUE_HIGHLIGHT_COLOUR);

    public boolean isFoW = true;

    public final Point pos;
    public final ObjPos renderPos, renderPosCentered;
    public TileType type;
    public ImageRenderer imageRenderer;

    public LerpAnimation illegalTileTimer = new LerpAnimation(0.8f).finish();

    public Tile(int x, int y, TileType type, Level level) {
        this.type = type;
        pos = new Point(x, y);
        renderPos = getRenderPos(x, y);
        renderPosCentered = getCenteredRenderPos(x, y);
        if (type.tileTextures == null)
            imageRenderer = null;
        else
            imageRenderer = type.tileTextures.getRandomImage(level.random.getDoubleSupplier(RandomType.TILE_TEXTURE));
    }

    public boolean posInside(ObjPos pos) {
        return BORDER_RENDERER.pointInside(pos.copy().subtract(renderPos));
    }

    public void renderTile(Graphics2D g, HexagonRenderer renderer) {
        GameRenderer.renderOffset(renderPos, g, () -> renderer.render(g));
    }

    public void renderTile(Graphics2D g, Color color, HexagonRenderer renderer) {
        renderer.setColor(color);
        GameRenderer.renderOffset(renderPos, g, () -> renderer.render(g));
    }

    public void renderTerrain(Graphics2D g) {
        if (imageRenderer == null)
            return;
        GameRenderer.renderOffset(renderPosCentered, g, () -> imageRenderer.render(g, TILE_SIZE));
    }

    public void renderFogOfWar(Graphics2D g) {
        if (isFoW) {
            HIGHLIGHT_RENDERER.setColor(FOW_COLOUR);
            GameRenderer.renderOffset(renderPos, g, () -> HIGHLIGHT_RENDERER.render(g));
        } else {
            if (!illegalTileTimer.finished()) {
                HIGHLIGHT_RENDERER.setColor(new Color(ILLEGAL_TILE_COLOUR.getRed(), ILLEGAL_TILE_COLOUR.getGreen(), ILLEGAL_TILE_COLOUR.getBlue(), (int) (128 * illegalTileTimer.doubleTriangleProgress())));
                GameRenderer.renderOffset(renderPos, g, () -> HIGHLIGHT_RENDERER.render(g));
            }
        }
    }

    public void setIllegalTile() {
        illegalTileTimer.startTimer();
    }

    public void setTileType(TileType type, Level level) {
        this.type = type;
        if (type.tileTextures == null)
            imageRenderer = null;
        else
            imageRenderer = type.tileTextures.getRandomImage(level.random.getDoubleSupplier(RandomType.TILE_TEXTURE));
    }

    public static ObjPos getRenderPos(Point pos) {
        return getRenderPos(pos.x, pos.y);
    }

    public static ObjPos getRenderPos(int x, int y) {
        return new ObjPos(x * TILE_SIZE * 1.5f / 2 + TILE_SIZE / 2, y * SIN_60_DEG * TILE_SIZE + ((x % 2) == 0 ? SIN_60_DEG * TILE_SIZE / 2 : 0));
    }

    public static ObjPos getCenteredRenderPos(Point pos) {
        return getCenteredRenderPos(pos.x, pos.y);
    }

    public static ObjPos getCenteredRenderPos(int x, int y) {
        return new ObjPos(x * TILE_SIZE * 1.5f / 2 + TILE_SIZE / 2, (y + 0.5f) * SIN_60_DEG * TILE_SIZE + ((x % 2) == 0 ? SIN_60_DEG * TILE_SIZE / 2 : 0));
    }

    public static ObjPos getTilesBound(int tilesX, int tilesY) {
        return new ObjPos(tilesX * TILE_SIZE * 1.5f / 2 + TILE_SIZE / 4, (tilesY + 0.5f) * SIN_60_DEG * TILE_SIZE);
    }
}

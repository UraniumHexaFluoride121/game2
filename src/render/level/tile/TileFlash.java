package render.level.tile;

import foundation.Deletable;
import level.AbstractLevel;
import level.tile.TileSet;
import render.anim.LerpAnimation;

import java.awt.*;

import static level.tile.Tile.*;

public class TileFlash implements Deletable {
    private static final float HIGHLIGHT_TRANSPARENCY = 0.12f;
    private AbstractLevel<?, ?> level;
    private final LerpAnimation timer;
    private final HexagonBorder border;
    private final float transparency;
    private final Color c;
    private final TileSet tiles;

    public TileFlash(Color color, float transparency, TileSet tiles, AbstractLevel<?, ?> level) {
        this.transparency = transparency;
        this.tiles = tiles;
        this.level = level;
        timer = new LerpAnimation(1f);
        c = color;
        border = new HexagonBorder(tiles, getBorderColor());
        level.levelRenderer.registerTimerBlock(timer, this::delete);
        level.levelRenderer.tileFlashes.add(this);
    }

    public void renderHighlight(Graphics2D g) {
        HexagonRenderer hexagonRenderer = HIGHLIGHT_RENDERER.setColor(getHighlightColor());
        tiles.forEach(t -> level.getTile(t).renderTile(g, hexagonRenderer));
    }

    public void renderBorder(Graphics2D g) {
        border.setColor(getBorderColor());
        border.render(g);
    }

    public boolean finished() {
        return timer.finished();
    }

    private Color getHighlightColor() {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (timer.triangleProgress() * transparency * HIGHLIGHT_TRANSPARENCY * 255));
    }

    private Color getBorderColor() {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (timer.triangleProgress() * transparency * 255));
    }

    @Override
    public void delete() {
        level.levelRenderer.tileFlashes.remove(this);
        level = null;
    }
}

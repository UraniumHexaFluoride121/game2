package render.level.tile;

import level.Level;
import level.tile.Tile;
import render.Renderable;
import render.anim.ReversableAnimationTimer;
import render.anim.PowAnimation;

import java.awt.*;
import java.util.HashSet;

import static level.tile.Tile.*;

public class HighlightTileRenderer implements Renderable {
    private final HashSet<Tile> tiles = new HashSet<>();
    private final Color tileColour;
    private final ReversableAnimationTimer timer;

    public HighlightTileRenderer(Color tileColour, ReversableAnimationTimer timer, HashSet<Point> positions, Level l) {
        this.tileColour = tileColour;
        this.timer = timer;
        timer.setReversed(true);
        timer.startTimer();
        positions.forEach(p -> tiles.add(l.getTile(p)));
    }

    public HighlightTileRenderer(Color tileColour, HashSet<Point> positions, Level l) {
        this.tileColour = tileColour;
        this.timer = new PowAnimation(0.2f, 0.3f);
        timer.setReversed(true);
        timer.startTimer();
        positions.forEach(p -> tiles.add(l.getTile(p)));
    }

    public void close() {
        timer.setReversed(false);
    }

    public boolean finished() {
        return timer.finished() && !timer.reversed();
    }

    @Override
    public void render(Graphics2D g) {
        HexagonRenderer hexagonRenderer;
        if (timer.normalisedProgress() == 0) {
            hexagonRenderer = HIGHLIGHT_RENDERER.setColor(tileColour);
        } else {
            hexagonRenderer = new HexagonRenderer(TILE_SIZE, TILE_SIZE / 2 * (timer.normalisedProgress()), true, STROKE_WIDTH, tileColour);
        }
        tiles.forEach(t -> t.renderTile(g, hexagonRenderer));
    }
}

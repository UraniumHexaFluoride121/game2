package render.level.tile;

import foundation.Deletable;
import foundation.MainPanel;
import level.tile.Tile;
import level.tile.TileSet;
import render.anim.timer.PowAnimation;
import render.anim.timer.ReversableAnimationTimer;

import java.awt.*;
import java.util.HashMap;
import java.util.function.Function;

import static level.tile.Tile.*;

public class MultiColourHighlight implements ITileHighlight, Deletable {
    private final HexagonBorder border;
    private final HashMap<Point, Color> tileColours;
    private final ReversableAnimationTimer timer;
    private Runnable onFinish = null;

    public MultiColourHighlight(TileSet tiles, Function<Point, Color> getColour, float tileTransparency, Color borderColour) {
        tileColours = new HashMap<>();
        tiles.forEach(p -> tileColours.put(p, getColour.apply(p)));
        timer = new PowAnimation(0.2f, 0.3f);
        timer.setReversed(true);
        timer.startTimer();
        tileColours.replaceAll((p, c) -> new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * tileTransparency)));
        border = new HexagonBorder(tiles, borderColour);
    }

    public void close() {
        timer.setReversed(false);
    }

    public boolean finished() {
        return timer.finished() && !timer.reversed();
    }

    public MultiColourHighlight setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    @Override
    public void renderHighlight(Graphics2D g) {
        if (finished() && onFinish != null) {
            MainPanel.addTask(onFinish);
            onFinish = null;
        }
        tileColours.forEach((p, c) -> {
            HexagonRenderer hexagonRenderer;
            if (timer.normalisedProgress() == 0) {
                hexagonRenderer = HIGHLIGHT_RENDERER.setColor(c);
            } else {
                hexagonRenderer = new HexagonRenderer(TILE_SIZE, TILE_SIZE / 2 * (timer.normalisedProgress()), true, STROKE_WIDTH, c);
            }
            Tile.renderTile(g, hexagonRenderer, p);
        });
    }

    @Override
    public void renderBorder(Graphics2D g) {
        if (timer.reversed())
            border.render(g);
    }

    @Override
    public void delete() {
        onFinish = null;
    }
}

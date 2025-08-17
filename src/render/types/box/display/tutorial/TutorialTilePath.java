package render.types.box.display.tutorial;

import foundation.math.ObjPos;
import level.tile.AbstractTileSelector;
import level.tile.Tile;
import render.GameRenderer;
import render.level.tile.AbstractTilePath;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class TutorialTilePath extends AbstractTilePath {
    public final Collection<Point> points;

    public TutorialTilePath(Point origin, Collection<Point> points) {
        super(new Point(origin.y, -origin.x));
        this.points = points;
        rangeValid = true;
        costValid = true;
    }

    public void setShortestPath(Point end, Function<Point, Float> tileCost) {
        end = new Point(end.y, -end.x);
        ArrayList<Point> newPath = AbstractTileSelector.shortestPathToStatic(origin, end, points.stream().map(p -> new Point(p.y, -p.x)).toList(), tileCost);
        if (newPath != null) {
            path = newPath;
            this.end = end;
        }
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            TutorialMapElement.transformCoordinateSystem(g);
            super.render(g);
        });
    }

    @Override
    protected ObjPos getRenderPos(Point tile) {
        return Tile.getRenderPos(tile);
    }

    @Override
    public Point getOrigin() {
        return new Point(-origin.y, origin.x);
    }
}

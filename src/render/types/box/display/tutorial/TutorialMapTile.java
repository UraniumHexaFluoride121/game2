package render.types.box.display.tutorial;

import foundation.Deletable;
import foundation.math.ObjPos;
import level.tile.TileType;
import render.GameRenderer;
import render.Renderable;
import render.level.tile.HexagonRenderer;
import render.texture.ImageRenderer;
import unit.UnitTeam;
import unit.type.UnitType;

import java.awt.*;

import static level.tile.Tile.*;

public class TutorialMapTile implements Renderable, Deletable {
    private static final HexagonRenderer FOW_RENDERER = new HexagonRenderer(TILE_SIZE, true, HIGHLIGHT_STROKE_WIDTH, null)
            .rotate(TILE_SIZE);
    public final int x, y;
    public final Point pos;
    public TutorialMapUnit unit = null;
    public final ObjPos renderPos, centeredRenderPos;
    public final TileType type;
    public final ImageRenderer terrainRenderer;
    public final Shape hexagonShape;
    private TutorialMapElement map;
    public boolean fow = false;

    public TutorialMapTile(int x, int y, TileType type, Shape hexagonShape, TutorialMapElement map) {
        this.hexagonShape = hexagonShape;
        this.map = map;
        renderPos = TutorialMapElement.getRenderPos(x, y);
        centeredRenderPos = TutorialMapElement.getCenteredRenderPos(x, y);
        this.x = x;
        this.y = y;
        pos = new Point(x, y);
        this.type = type;
        if (type.tileTexturesSupplier != null)
            terrainRenderer = type.tileTexturesSupplier.get().getRandomImage();
        else
            terrainRenderer = null;
    }

    public TutorialMapUnit addUnit(UnitType type, UnitTeam team) {
        unit = new TutorialMapUnit(type, team, new Point(x, y), map);
        map.allUnits.add(unit);
        return unit;
    }

    @Override
    public void render(Graphics2D g) {
        if (unit != null)
            unit.render(g);
    }

    public void renderTerrain(Graphics2D g) {
        if (fow) {
            FOW_RENDERER.setColor(FOW_COLOUR_BACKGROUND);
            GameRenderer.renderOffset(renderPos, g, () -> FOW_RENDERER.render(g));
        }
        GameRenderer.renderOffset(centeredRenderPos, g, () -> {
            if (terrainRenderer != null) {
                GameRenderer.renderTransformed(g, () -> {
                    g.rotate(TutorialMapElement.DEG_30);
                    terrainRenderer.render(g, TILE_SIZE);
                });
            }
        });
        if (fow) {
            FOW_RENDERER.setColor(FOW_COLOUR);
            GameRenderer.renderOffset(renderPos, g, () -> FOW_RENDERER.render(g));
        }
    }

    @Override
    public void delete() {
        map = null;
    }
}

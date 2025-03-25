package render.level.map;

import foundation.math.ObjPos;
import level.AbstractLevelRenderer;
import level.tile.Tile;
import render.*;
import render.level.tile.HexagonRenderer;
import unit.TileMapDisplayable;
import unit.UnitTeam;

import java.awt.*;

import static level.tile.Tile.*;

public class MapUI extends AbstractRenderElement {
    private static final Color FOW_FILL = new Color(0, 0, 0, 100);
    public final ObjPos tileBound;
    private final HexagonRenderer hexagonRenderer;

    public MapUI(RenderRegister<OrderedRenderable> register, RenderOrder order, TileMapDisplayable display, float tileSize, float strokeWidth, float transparency) {
        super(register, order);
        tileBound = display.getTileBound().copy().multiply(tileSize / TILE_SIZE);
        hexagonRenderer = new HexagonRenderer(tileSize, true, 0, Tile.BORDER_COLOUR);
        Renderable tileRenderer = Renderable.renderImage(AbstractLevelRenderer.createTiles(tileSize, transparency, display, (g2, pos) ->
                Tile.renderTile(g2, new HexagonRenderer(tileSize, false, strokeWidth, BORDER_COLOUR), tileSize, pos)
        ), false, false, -1).translate(-BLOCK_STROKE_WIDTH_MARGIN / 2f, -BLOCK_STROKE_WIDTH_MARGIN / 2f);
        renderable = g -> {
            GameRenderer.renderOffset(-tileBound.x / 2, -tileBound.y / 2, g, () -> {
                display.forEachMapStructure((pos, team) -> {
                    hexagonRenderer.setColor(teamColour(team, 130));
                    Tile.renderTile(g, hexagonRenderer, tileSize, pos);
                });
                display.forEachUnitMapData((pos, team, visible) -> {
                    if (visible) {
                        g.setColor(teamColour(team, 200));
                        GameRenderer.renderOffset(pos.copy().addY(TILE_SIZE * SIN_60_DEG / 2).multiply(tileSize / TILE_SIZE), g, () -> {
                            GameRenderer.renderScaled(tileSize / 6f, g, () -> {
                                g.fillOval(-1, -1, 2, 2);
                            });
                        });
                    }
                });
                hexagonRenderer.setColor(FOW_FILL);
                display.forEachMapTileFoW((pos, isFoW) -> {
                    if (isFoW) {
                        Tile.renderTile(g, hexagonRenderer, tileSize, pos);
                    }
                });
                tileRenderer.render(g);
            });
        };
    }

    private static Color teamColour(UnitTeam team, int a) {
        Color c;
        if (team == null)
            c = new Color(200, 200, 200);
        else
            c = team.uiColour.borderColour;
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}

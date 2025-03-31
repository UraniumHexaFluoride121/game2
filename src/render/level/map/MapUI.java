package render.level.map;

import foundation.math.ObjPos;
import level.AbstractLevelRenderer;
import level.tile.Tile;
import level.tile.TileType;
import render.*;
import render.level.tile.HexagonRenderer;
import unit.TileMapDisplayable;
import unit.UnitTeam;

import java.awt.*;

import static level.tile.Tile.*;

public class MapUI extends AbstractRenderElement {
    private static final Color FOW_FILL = new Color(0, 0, 0, 100),
            NEBULA_COLOUR = new Color(162, 102, 236),
            DENSE_NEBULA_COLOUR = new Color(188, 92, 250),
            ASTEROID_COLOUR = new Color(228, 228, 228);
    public final ObjPos tileBound;
    private final HexagonRenderer hexagonRenderer, structureRenderer;
    private final float tileSize, transparency, strokeWidth;
    private TileMapDisplayable display;
    private Renderable nebulaRenderer, denseNebulaRenderer, asteroidRenderer;

    public static MapUI box(RenderRegister<OrderedRenderable> register, RenderOrder order, TileMapDisplayable display, float maxWidth, float maxHeight, float transparency) {
        float tileSize = Math.min(maxWidth / display.tilesX() / SIN_60_DEG, maxHeight / display.tilesY());
        MapUI map = new MapUI(register, order, display, tileSize, tileSize / 8f, transparency);
        map.translate(maxWidth / 2, maxHeight / 2);
        return map;
    }

    public MapUI(RenderRegister<OrderedRenderable> register, RenderOrder order, TileMapDisplayable display, float tileSize, float strokeWidth, float transparency) {
        super(register, order);
        this.display = display;
        this.tileSize = tileSize;
        this.strokeWidth = strokeWidth;
        this.transparency = transparency;
        tileBound = display.getTileBound().copy().multiply(tileSize / TILE_SIZE);
        hexagonRenderer = new HexagonRenderer(tileSize, true, 0, Tile.BORDER_COLOUR);
        structureRenderer = new HexagonRenderer(tileSize - strokeWidth, true, 0, Tile.BORDER_COLOUR);
        Renderable tileRenderer = Renderable.renderImage(AbstractLevelRenderer.createTiles(tileSize, transparency, display, (g2, pos, type) ->
                Tile.renderTile(g2, new HexagonRenderer(tileSize, false, strokeWidth, BORDER_COLOUR), tileSize, pos)
        ), false, false, -1).translate(-BLOCK_STROKE_WIDTH_MARGIN / 2f, -BLOCK_STROKE_WIDTH_MARGIN / 2f);
        update();
        renderable = g -> {
            GameRenderer.renderOffset(-tileBound.x / 2, -tileBound.y / 2, g, () -> {
                hexagonRenderer.setColor(FOW_FILL);
                tileRenderer.render(g);
                nebulaRenderer.render(g);
                asteroidRenderer.render(g);
                denseNebulaRenderer.render(g);
                display.forEachMapStructure((pos, team) -> {
                    structureRenderer.setColor(teamStructureColour(team, 255));
                    Tile.renderTile(g, structureRenderer, tileSize, pos.copy().addY(strokeWidth * TILE_SIZE / tileSize / 2));
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
                display.forEachMapTileFoW((pos, isFoW) -> {
                    if (isFoW) {
                        Tile.renderTile(g, hexagonRenderer, tileSize, pos);
                    }
                });
            });
        };
    }

    public void update() {
        nebulaRenderer = Renderable.renderImage(AbstractLevelRenderer.createTiles(tileSize, transparency * 2, display, (g2, pos, type) -> {
                    if (type == TileType.NEBULA)
                        renderTile(g2, new HexagonRenderer(tileSize * 0.75f, false, tileSize * 0.25f * SIN_60_DEG - strokeWidth, NEBULA_COLOUR), tileSize, pos.copy().addY(0.25f * TILE_SIZE * SIN_60_DEG / 2));
                }
        ), false, false, -1).translate(-BLOCK_STROKE_WIDTH_MARGIN / 2f, -BLOCK_STROKE_WIDTH_MARGIN / 2f);
        asteroidRenderer = Renderable.renderImage(AbstractLevelRenderer.createTiles(tileSize, transparency * 1.5f, display, (g2, pos, type) -> {
                    if (type == TileType.ASTEROIDS)
                        renderTile(g2, new HexagonRenderer(tileSize * 0.75f, false, tileSize * 0.25f * SIN_60_DEG - strokeWidth, ASTEROID_COLOUR), tileSize, pos.copy().addY(0.25f * TILE_SIZE * SIN_60_DEG / 2));
                }
        ), false, false, -1).translate(-BLOCK_STROKE_WIDTH_MARGIN / 2f, -BLOCK_STROKE_WIDTH_MARGIN / 2f);
        denseNebulaRenderer = Renderable.renderImage(AbstractLevelRenderer.createTiles(tileSize, transparency * 2.5f, display, (g2, pos, type) -> {
                    if (type == TileType.DENSE_NEBULA)
                        renderTile(g2, new HexagonRenderer(tileSize * 0.7f, false, tileSize * 0.3f * SIN_60_DEG - strokeWidth, DENSE_NEBULA_COLOUR), tileSize, pos.copy().addY(0.3f * TILE_SIZE * SIN_60_DEG / 2));
                }
        ), false, false, -1).translate(-BLOCK_STROKE_WIDTH_MARGIN / 2f, -BLOCK_STROKE_WIDTH_MARGIN / 2f);
    }

    private static Color teamColour(UnitTeam team, int a) {
        Color c;
        if (team == null)
            c = new Color(200, 200, 200);
        else
            c = team.uiColour.borderColour;
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static Color teamStructureColour(UnitTeam team, int a) {
        Color c;
        if (team == null)
            c = new Color(156, 156, 156);
        else
            c = team.uiColour.borderColourPressed;
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    @Override
    public void delete() {
        super.delete();
        display = null;
    }
}

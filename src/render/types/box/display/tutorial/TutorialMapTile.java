package render.types.box.display.tutorial;

import foundation.Deletable;
import foundation.math.ObjPos;
import level.structure.Structure;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileType;
import render.GameRenderer;
import render.Renderable;
import render.level.tile.HexagonRenderer;
import render.texture.ImageRenderer;
import render.types.UIHitPointBar;
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
    public Structure structure = null;
    public UnitTeam initialStructureTeam = null;
    private UIHitPointBar captureBar = null;
    public boolean capturing = false;
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

    public Structure addStructure(StructureType type, UnitTeam team) {
        structure = new Structure(pos, type, team);
        captureBar = Tile.newCaptureBar(structure);
        initialStructureTeam = team;
        return structure;
    }

    public void startCapture(UnitTeam team) {
        captureBar.setFill(0);
        captureBar.setColour(team.uiColour);
        capturing = true;
    }

    public TutorialMapTile forceCaptureSegments(int segments) {
        captureBar.setSegments(segments);
        return this;
    }

    public void setProgress(int progress) {
        captureBar.setFill(progress, 1f, 0.8f);
    }

    public void incrementProgress() {
        setProgress((int) (captureBar.getFill() + 1));
    }

    public void finishCapture(UnitTeam newTeam) {
        structure.setTeam(newTeam);
        capturing = false;
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
            if (structure != null)
                structure.renderer.render(g, TILE_SIZE);
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

    public void renderCaptureBar(Graphics2D g) {
        if (!fow && structure != null && capturing)
            Tile.renderCaptureBar(renderPos, captureBar, g);
    }

    public void reset() {
        if (captureBar != null)
            captureBar.setFill(0);
        capturing = false;
        if (structure != null) {
            structure.setTeam(initialStructureTeam);
        }
    }

    @Override
    public void delete() {
        map = null;
    }
}

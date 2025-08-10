package level.tile;

import foundation.math.HexagonCorner;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.math.RandomType;
import level.AbstractLevel;
import level.Level;
import level.structure.Structure;
import level.structure.StructureType;
import network.PacketReceiver;
import network.Writable;
import render.GameRenderer;
import render.UIColourTheme;
import render.anim.LerpAnimation;
import render.level.tile.HexagonRenderer;
import render.texture.ImageRenderer;
import render.types.UIHitPointBar;
import unit.Unit;
import unit.UnitTeam;
import unit.action.ActionShapes;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static unit.action.Action.*;

public class Tile implements Writable {
    public static final float STROKE_WIDTH = 0.1f, HIGHLIGHT_STROKE_WIDTH = 0.2f;
    public static final float BLOCK_STROKE_WIDTH_MARGIN = 2f, SCREEN_STROKE_WIDTH_MARGIN = GameRenderer.scaleFloatToScreen(BLOCK_STROKE_WIDTH_MARGIN);

    public static final Color
            BORDER_COLOUR = new Color(175, 249, 255),
            BLUE_HIGHLIGHT_COLOUR = new Color(87, 177, 225),
            SELECTED_COLOUR = new Color(159, 159, 159),
            MOUSE_OVER_COLOUR = new Color(172, 172, 172, 192),
            FOW_COLOUR_BACKGROUND = new Color(0, 0, 0, 153),
            FOW_COLOUR = new Color(0, 0, 0, 64),
            ILLEGAL_TILE_COLOUR = new Color(227, 90, 90);
    public static final float TILE_SIZE = 4.5f;
    public static final float SIN_60_DEG = ((float) Math.sin(Math.toRadians(60)));

    public static final HexagonRenderer
            BORDER_RENDERER = new HexagonRenderer(TILE_SIZE, false, STROKE_WIDTH, BORDER_COLOUR),
            SELECTED_BORDER_HIGHLIGHT_RENDERER = new HexagonRenderer(TILE_SIZE, false, HIGHLIGHT_STROKE_WIDTH, SELECTED_COLOUR),
            SEGMENTED_BORDER_HIGHLIGHT_RENDERER = new HexagonRenderer(false, HIGHLIGHT_STROKE_WIDTH, MOUSE_OVER_COLOUR),
            HIGHLIGHT_RENDERER = new HexagonRenderer(TILE_SIZE, true, HIGHLIGHT_STROKE_WIDTH, BLUE_HIGHLIGHT_COLOUR);

    static {
        for (int i = 0; i < 6; i++) {
            SEGMENTED_BORDER_HIGHLIGHT_RENDERER.addSegment(HexagonRenderer.hexagonSegment(TILE_SIZE, TILE_SIZE * 0.08f, false, i - 0.2f, i + 0.2f));
        }
    }

    public boolean isFoW = true;

    public final Point pos;
    public final ObjPos renderPos, renderPosCentered;
    public TileType type;
    public Structure structure = null;
    public double randomValue = 0;
    public ImageRenderer imageRenderer;
    public int miningBarFill = 0;
    private UIHitPointBar captureBar = null;

    public LerpAnimation illegalTileTimer = new LerpAnimation(0.8f).finish();

    public Tile(int x, int y, TileType type, AbstractLevel<?, ?> level) {
        pos = new Point(x, y);
        renderPos = getRenderPos(x, y);
        renderPosCentered = getCenteredRenderPos(x, y);
        setTileType(type, level);
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

    public static void renderTile(Graphics2D g, HexagonRenderer renderer, float tileSize, ObjPos renderPos) {
        GameRenderer.renderOffset(renderPos.copy().multiply(tileSize / TILE_SIZE), g, () -> renderer.render(g));
    }

    public static void renderTile(Graphics2D g, HexagonRenderer renderer, Point pos) {
        renderTile(g, renderer, TILE_SIZE, getRenderPos(pos));
    }

    public void renderTerrain(Graphics2D g) {
        GameRenderer.renderOffset(renderPosCentered, g, () -> {
            if (imageRenderer != null)
                imageRenderer.render(g, TILE_SIZE);
            if (hasAnyStructure()) {
                structure.renderer.render(g, TILE_SIZE);
                if (structure.exploding())
                    structure.renderExplosion(g, this);
            }
        });
    }

    public void renderFogOfWarBackground(Graphics2D g) {
        if (isFoW) {
            HIGHLIGHT_RENDERER.setColor(FOW_COLOUR_BACKGROUND);
            GameRenderer.renderOffset(renderPos, g, () -> HIGHLIGHT_RENDERER.render(g));
        }
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

    public static final BasicStroke ICON_STROKE = new BasicStroke(0.02f * TILE_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);

    public void renderCaptureBar(Graphics2D g, Level level) {
        if (captureBar == null)
            return;
        Unit u = level.getUnit(pos);
        if (u == null || u.getCaptureProgress() == -1) {
            captureBar.setFill(0);
            return;
        }
        if (isFoW || !hasStructure())
            return;
        GameRenderer.renderOffset(renderPos.x - TILE_SIZE * 0.5f / 2, renderPos.y + TILE_SIZE * 0.7f, g, () -> {
            captureBar.render(g);
            g.translate(TILE_SIZE * 0.32f, -TILE_SIZE * 0.06f);
            g.setColor(ICON_COLOUR);
            GameRenderer.renderScaled(TILE_SIZE / 4, g, () -> {
                g.fill(ActionShapes.FLAG);
            });
        });
    }

    public void setIllegalTile() {
        illegalTileTimer.startTimer();
    }

    public void setTileType(TileType type, AbstractLevel<?, ?> level) {
        setTileType(type, level.random.getDoubleSupplier(RandomType.TILE_TEXTURE).get());
    }

    public void setTileType(TileType type, double randomValue) {
        if (type == this.type)
            return;
        this.randomValue = randomValue;
        this.type = type;
        miningBarFill = initialMiningFill();
        if (type.tileTexturesSupplier == null) {
            imageRenderer = null;
        } else {
            imageRenderer = type.tileTextures.getRandomImage(randomValue);
        }
    }

    public void setTileType(TileData data) {
        setTileType(data.type(), data.randomValue());
        miningBarFill = data.miningBarFill();
    }

    public void setStructure(StructureType type, UnitTeam team) {
        setStructure(new Structure(pos, type, team));
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
        captureBar = new UIHitPointBar(
                TILE_SIZE * 0.02f, TILE_SIZE * 0.35f, TILE_SIZE * 0.1f, TILE_SIZE * 0.02f,
                structure.type.captureSteps, UIColourTheme.GRAYED_OUT).setRounding(TILE_SIZE * 0.1f * 0.75f);
    }

    public void removeStructure() {
        structure = null;
        captureBar = null;
    }

    public void explodeStructure() {
        structure.explode();
    }

    public boolean hasStructure() {
        return structure != null && !structure.exploding();
    }

    public boolean hasExplodingStructure() {
        return structure != null && structure.exploding();
    }

    public boolean hasAnyStructure() {
        return structure != null;
    }

    public void startCapturing(UIColourTheme theme) {
        captureBar.setFill(0);
        captureBar.setColour(theme);
    }

    public void setProgress(int progress) {
        captureBar.setFill(progress, 0.5f, 0.8f);
    }

    public void setProgress(int progress, Level level, boolean runOnComplete, Runnable onFillComplete) {
        setProgress(progress);
        if (runOnComplete)
            level.levelRenderer.registerTimerBlock(captureBar.getFillAnimation(), onFillComplete);
    }

    public int miningBarSegments() {
        return switch (type) {
            case ASTEROIDS -> 4;
            default -> 0;
        };
    }

    public int initialMiningFill() {
        return switch (type) {
            case ASTEROIDS -> 4;
            default -> 0;
        };
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        TileData d = getTileData();
        d.write(w);
        if (d.hasStructure()) {
            structure.write(w);
        }
    }

    public TileData getTileData() {
        return new TileData(type, randomValue, hasStructure(), miningBarFill);
    }

    public static TileData read(DataInputStream reader) throws IOException {
        return new TileData(PacketReceiver.readEnum(TileType.class, reader), reader.readDouble(), reader.readBoolean(), reader.readInt());
    }

    public static ObjPos getRenderPos(Point pos) {
        return getRenderPos(pos.x, pos.y);
    }

    public static ObjPos getRenderPos(int x, int y) {
        return new ObjPos(x * TILE_SIZE * 1.5f / 2 + TILE_SIZE / 2, y * SIN_60_DEG * TILE_SIZE + ((x % 2) == 0 ? SIN_60_DEG * TILE_SIZE / 2 : 0));
    }

    public static ObjPos getFractionalRenderPos(float x, float y) {
        return getRenderPos((int) x, (int) y).add(MathUtil.fraction(x) * Tile.TILE_SIZE, MathUtil.fraction(y) * Tile.TILE_SIZE * Tile.SIN_60_DEG);
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

    public static ObjPos getCornerRenderPos(Point pos, HexagonCorner corner) {
        ObjPos renderPos = getRenderPos(pos);
        return switch (corner) {
            case BOTTOM_LEFT -> renderPos.add(-TILE_SIZE / 4, 0);
            case MID_LEFT -> renderPos.add(-TILE_SIZE / 2, TILE_SIZE / 2 * SIN_60_DEG);
            case TOP_LEFT -> renderPos.add(-TILE_SIZE / 4, TILE_SIZE * SIN_60_DEG);
            case TOP_RIGHT -> renderPos.add(TILE_SIZE / 4, TILE_SIZE * SIN_60_DEG);
            case MID_RIGHT -> renderPos.add(TILE_SIZE / 2, TILE_SIZE / 2 * SIN_60_DEG);
            case BOTTOM_RIGHT -> renderPos.add(TILE_SIZE / 4, 0);
        };
    }
}

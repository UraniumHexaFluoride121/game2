package level;

import foundation.Deletable;
import foundation.Main;
import foundation.MainPanel;
import foundation.input.InputReceiver;
import foundation.input.InputType;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import level.tile.Tile;
import level.tile.TileImageRenderer;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import render.GameRenderer;
import render.RenderOrder;
import render.Renderable;
import render.anim.AnimationTimer;
import render.level.tile.HexagonBorder;
import render.level.tile.HighlightTileRenderer;
import render.level.tile.ITileHighlight;
import render.level.tile.RenderElement;
import render.level.ui.TooltipRenderer;
import render.texture.BackgroundRenderer;
import render.texture.BackgroundTexture;
import unit.TileMapDisplayable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import static level.tile.Tile.*;

public abstract class AbstractLevelRenderer<T extends AbstractLevel<?, ?>> implements Deletable, Renderable, Tickable, InputReceiver {
    public static final float MOUSE_EDGE_CAMERA_BORDER = 0.5f;
    private static final float MOUSE_EDGE_CAMERA_MOVE_SPEED = 20;

    public Vector<Object> mouseBlockingObjects = new Vector<>();
    public Vector<ITileHighlight> tileHighlights = new Vector<>();
    public HighlightTileRenderer highlightTileRenderer = null, tutorialHighlightRenderer = null;
    public HexagonBorder unitTileBorderRenderer = null, fowTileBorder = null, tutorialBorderRenderer = null;
    private BufferedImage borderImage;
    public final GameRenderer mainRenderer, backgroundRenderer, levelUIRenderer;
    public T level;
    protected boolean moveCameraEnabled = false;
    protected ObjPos prevMousePos = null;
    protected float shakeTimer = 0;
    protected final ObjPos cameraPosition = new ObjPos(), cameraVelocityVector = new ObjPos();

    protected ObjPos preShakePos = null;
    protected ObjPos shakeVector = null;
    protected boolean cameraShake = false;

    //Objects in this set indicate that they are running an animation
    private final HashSet<Object> animationBlocks = new HashSet<>();
    private final HashMap<AnimationTimer, Runnable> animationTimerBlocks = new HashMap<>();

    public AbstractLevelRenderer(T level) {
        this.level = level;
        mainRenderer = new GameRenderer(MainPanel.windowTransform, this::getMainCameraTransform);
        backgroundRenderer = new GameRenderer(MainPanel.windowTransform, null);
        levelUIRenderer = new GameRenderer(MainPanel.windowTransform, null);
    }

    public void createRenderers() {
        borderImage = createTiles(Tile.TILE_SIZE, 0.1f, level, (g, pos, type) -> Tile.renderTile(g, BORDER_RENDERER, TILE_SIZE, pos));
        new BackgroundRenderer(backgroundRenderer, RenderOrder.BACKGROUND, this::getCameraPosition).setTextures(BackgroundTexture.NORMAL_1);


        new RenderElement(mainRenderer, RenderOrder.TERRAIN, g -> {
            level.tileSelector.tileSet.forEach(t -> {
                t.renderFogOfWarBackground(g);
            });
        }).setZOrder(-1);

        //Terrain
        new RenderElement(mainRenderer, RenderOrder.TERRAIN, g -> {
            for (Tile[] tileColumn : level.tiles) {
                for (Tile tile : tileColumn) {
                    tile.renderTerrain(g);
                }
            }
        });

        //Tile borders
        new RenderElement(mainRenderer, RenderOrder.TILE_BORDER, Renderable.renderImage(borderImage, false, false, -1).translate(-Tile.BLOCK_STROKE_WIDTH_MARGIN / 2f, -Tile.BLOCK_STROKE_WIDTH_MARGIN / 2f));

        //Tile highlight
        new RenderElement(mainRenderer, RenderOrder.TILE_HIGHLIGHT, g -> {
            if (highlightTileRenderer != null) {
                highlightTileRenderer.render(g);
                if (highlightTileRenderer.finished())
                    highlightTileRenderer = null;
            }
            if (tutorialHighlightRenderer != null) {
                tutorialHighlightRenderer.render(g);
                if (tutorialHighlightRenderer.finished())
                    tutorialHighlightRenderer = null;
            }
            tileHighlights.forEach(f -> f.renderHighlight(g));
        });

        new RenderElement(mainRenderer, RenderOrder.FOG_OF_WAR, g -> {
            level.tileSelector.tileSet.forEach(t -> {
                t.renderFogOfWar(g);
            });
            if (fowTileBorder != null)
                fowTileBorder.render(g);
        });

        new RenderElement(mainRenderer, RenderOrder.TILE_BORDER_HIGHLIGHTS, g -> {
            if (unitTileBorderRenderer != null)
                unitTileBorderRenderer.render(g);
            if (tutorialBorderRenderer != null)
                tutorialBorderRenderer.render(g);
            if (level.tileSelector.getSelectedTile() != null && renderSelectedTile())
                level.tileSelector.getSelectedTile().renderTile(g, Tile.BLUE_HIGHLIGHT_COLOUR, BORDER_HIGHLIGHT_RENDERER);
            tileHighlights.forEach(f -> f.renderBorder(g));
        });
        new RenderElement(mainRenderer, RenderOrder.TILE_BORDER_HIGHLIGHTS, g -> {
            if (level.tileSelector.mouseOverTile != null && renderMouseOverTile() && mouseBlockingObjects.isEmpty())
                level.tileSelector.mouseOverTile.renderTile(g, Tile.BLUE_TRANSPARENT_COLOUR, BORDER_HIGHLIGHT_RENDERER);
        }).setZOrder(1);
        new TooltipRenderer(levelUIRenderer, RenderOrder.TOOLTIP);
    }

    protected boolean renderMouseOverTile() {
        return true;
    }

    protected boolean renderSelectedTile() {
        return true;
    }

    public void moveCameraStart() {
        prevMousePos = null;
        moveCameraEnabled = true;
    }

    public void moveCameraEnd() {
        prevMousePos = null;
        moveCameraEnabled = false;
    }

    public void enableCameraShake(ObjPos shakeVector) {
        if (preShakePos == null)
            preShakePos = cameraPosition.copy();
        this.shakeVector = shakeVector;
        cameraShake = true;
    }

    public void disableCameraShake() {
        if (preShakePos == null)
            return;
        cameraPosition.set(preShakePos);
        preShakePos = null;
        cameraShake = false;
    }

    public ObjPos getCameraPosition() {
        return cameraPosition.copy();
    }

    public AffineTransform getMainCameraTransform() {
        AffineTransform t = new AffineTransform();
        level.tileBound.copy().divide(-2).affineTranslate(t);
        MainPanel.BLOCK_DIMENSIONS.copy().divide(2).affineTranslate(t);
        cameraPosition.affineTranslate(t);
        return t;
    }

    protected ObjPos transformMousePosToCamera(Point p) {
        ObjPos pos = new ObjPos(p);
        pos.subtract(MainPanel.INSETS_OFFSET);
        pos.scaleToBlocks().flipY().addY(MainPanel.BLOCK_DIMENSIONS.y); //Scale to world block grid
        pos.add(level.tileBound.copy().divide(2)).add(MainPanel.BLOCK_DIMENSIONS.copy().divide(-2)).subtract(cameraPosition); //camera transform
        return pos;
    }

    public ObjPos transformCameraPosToBlock(ObjPos pos) {
        return pos.copy().subtract(level.tileBound.copy().divide(2)).subtract(MainPanel.BLOCK_DIMENSIONS.copy().divide(-2)).add(cameraPosition);
    }

    public void setCameraInterpBlockPos(ObjPos pos) {
        ObjPos newPos = pos.copy().inverse().add(level.tileBound.copy().divide(2));
        newPos.clamp(-level.tileBound.x / 2, level.tileBound.x / 2, -level.tileBound.y / 2, level.tileBound.y / 2);
        interpCameraTo = newPos;
    }

    public void setCameraBlockPosInstant(ObjPos pos) {
        ObjPos newPos = pos.copy().inverse().add(level.tileBound.copy().divide(2));
        newPos.clamp(-level.tileBound.x / 2, level.tileBound.x / 2, -level.tileBound.y / 2, level.tileBound.y / 2);
        cameraPosition.set(newPos);
    }

    public ObjPos getCameraInterpBlockPos(ObjPos pos) {
        return pos.copy().subtract(level.tileBound.copy().divide(2)).inverse();
    }

    public void animStateUpdate(boolean running) {

    }

    public boolean runningAnim() {
        return !animationBlocks.isEmpty() || !animationTimerBlocks.isEmpty();
    }

    public void registerAnimBlock(Object o) {
        animationBlocks.add(o);
        animStateUpdate(true);
    }

    public void removeAnimBlock(Object o) {
        animationBlocks.remove(o);
        if (!runningAnim())
            animStateUpdate(false);
    }

    public void registerTimerBlock(AnimationTimer t, Runnable r) {
        animationTimerBlocks.putIfAbsent(t, r);
        animStateUpdate(true);
    }

    public ObjPos interpCameraTo = null;

    public static BufferedImage createTiles(float tileSize, float transparency, TileMapDisplayable tileDisplayable, TileImageRenderer tileRenderer) {
        ObjPos tilesBound = MainPanel.RENDER_WINDOW_SIZE.copy().multiply(tileDisplayable.getTileBound().copy().divide(MainPanel.BLOCK_DIMENSIONS).multiply(tileSize / TILE_SIZE));

        BufferedImage borderImage = new BufferedImage(tilesBound.xInt() + (int) (Tile.SCREEN_STROKE_WIDTH_MARGIN) + 2, tilesBound.yInt() + (int) (Tile.SCREEN_STROKE_WIDTH_MARGIN) + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = borderImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.scale(MainPanel.windowTransform.getScaleX(), MainPanel.windowTransform.getScaleX());
        g.translate(BLOCK_STROKE_WIDTH_MARGIN / 2f, Tile.BLOCK_STROKE_WIDTH_MARGIN / 2f);
        for (int x = 0; x < tileDisplayable.tilesX(); x++) {
            for (int y = 0; y < tileDisplayable.tilesY(); y++) {
                tileRenderer.render(g, Tile.getRenderPos(x, y), tileDisplayable.getTileType(x, y));
            }
        }
        g.dispose();
        return Renderable.transparency(borderImage, transparency);
    }

    public void registerTileHighlight(ITileHighlight highlight, boolean mouseTileBlocking) {
        tileHighlights.add(highlight);
        if (mouseTileBlocking)
            mouseBlockingObjects.add(highlight);
    }

    public void removeTileHighlight(ITileHighlight highlight) {
        tileHighlights.remove(highlight);
        mouseBlockingObjects.remove(highlight);
    }

    @Override
    public void acceptPressed(InputType type) {
        level.buttonRegister.input(true, type, this::transformMousePosToCamera);
    }

    @Override
    public void acceptReleased(InputType type) {
        level.buttonRegister.input(false, type, this::transformMousePosToCamera);
    }

    @Override
    public void render(Graphics2D g) {
        backgroundRenderer.render(g);
        mainRenderer.render(g);
        levelUIRenderer.render(g);
    }

    public boolean moveCameraLeft = false, moveCameraRight = false, moveCameraUp = false, moveCameraDown = false;

    @Override
    public void tick(float deltaTime) {
        if (animationTimerBlocks.entrySet().removeIf(entry -> {
            if (entry.getKey().finished()) {
                entry.getValue().run();
                return true;
            }
            return false;
        })) {
            if (!runningAnim())
                animStateUpdate(false);
        }
        if (cameraShake) {
            shakeTimer += deltaTime;
            if (shakeTimer > 0.05f) {
                shakeTimer = 0;
                cameraPosition.add(shakeVector);
                shakeVector.inverse();
            }
        }
        if (interpCameraTo != null) {
            float distance = interpCameraTo.distance(cameraPosition);
            ObjPos to = interpCameraTo.copy().subtract(cameraPosition);
            ObjPos log = to.copy().addLength(4).log();
            to.normalise();
            ObjPos v = to.copy().setLength(to.dotProduct(log));
            cameraVelocityVector.expTo(v, 5, deltaTime);
            float l = to.dotProduct(cameraVelocityVector.copy().multiply(8 * deltaTime));
            if (l > 0)
                cameraPosition.add(to.copy().setLength(l));
            else
                cameraPosition.add(to.copy().setLength(l / 2));
            if (distance < 0.1f)
                interpCameraTo = null;
        } else {
            if (!runningAnim())
                cameraVelocityVector.lerpTo(ObjPos.ORIGIN, 4, deltaTime);
        }
        Point p = Main.window.getMousePosition();
        if (p != null) {
            ObjPos mousePos = new ObjPos(p).subtract(MainPanel.INSETS_OFFSET);
            ObjPos cameraTransformedPos = transformMousePosToCamera(p);
            if ((interpCameraTo == null || interpCameraTo.distance(cameraPosition) < 4) && !runningAnim() && !TutorialManager.isDisabled(TutorialElement.CAMERA_MOVEMENT)) {
                if (moveCameraEnabled) {
                    if (prevMousePos != null) {
                        if (interpCameraTo != null)
                            interpCameraTo = null;
                        cameraPosition.add(mousePos.copy().subtract(prevMousePos).scaleToBlocks().flipY());
                    }
                    prevMousePos = mousePos;
                } else {
                    if (interpCameraTo != null && (moveCameraUp || moveCameraDown || moveCameraLeft || moveCameraRight))
                        interpCameraTo = null;
                    if (moveCameraDown) {
                        moveCameraDown = false;
                        cameraPosition.addY(deltaTime * MOUSE_EDGE_CAMERA_MOVE_SPEED);
                    }
                    if (moveCameraUp) {
                        moveCameraUp = false;
                        cameraPosition.addY(-deltaTime * MOUSE_EDGE_CAMERA_MOVE_SPEED);
                    }
                    if (moveCameraLeft) {
                        moveCameraLeft = false;
                        cameraPosition.addX(deltaTime * MOUSE_EDGE_CAMERA_MOVE_SPEED);
                    }
                    if (moveCameraRight) {
                        moveCameraRight = false;
                        cameraPosition.addX(-deltaTime * MOUSE_EDGE_CAMERA_MOVE_SPEED);
                    }
                }
                cameraPosition.clamp(-level.tileBound.x / 2, level.tileBound.x / 2, -level.tileBound.y / 2, level.tileBound.y / 2);
            }
            level.buttonRegister.acceptInput(cameraTransformedPos, InputType.MOUSE_OVER, true, false);
        }
    }

    @Override
    public void delete() {
        level = null;
        animationBlocks.clear();
        animationTimerBlocks.clear();
        backgroundRenderer.delete();
        mainRenderer.delete();
        levelUIRenderer.delete();
    }
}

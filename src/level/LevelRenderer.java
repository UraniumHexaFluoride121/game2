package level;

import foundation.Deletable;
import foundation.Main;
import foundation.MainPanel;
import foundation.input.InputReceiver;
import foundation.input.InputType;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import level.tile.Tile;
import network.NetworkState;
import render.GameRenderer;
import render.RenderOrder;
import render.Renderable;
import render.renderables.HexagonBorder;
import render.renderables.HighlightTileRenderer;
import render.renderables.RenderElement;
import render.texture.BackgroundRenderer;
import render.texture.BackgroundTexture;
import render.texture.FiringRenderer;
import render.ui.implementation.*;
import unit.Unit;
import unit.UnitTeam;
import unit.action.Action;
import unit.weapon.WeaponInstance;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;

import static level.tile.Tile.*;

public class LevelRenderer implements Deletable, Renderable, Tickable, InputReceiver {
    private Level level;
    private final GameRenderer mainRenderer, backgroundRenderer, levelUIRenderer, firingAnimRenderer, topUIRenderer;
    private BufferedImage borderImage;
    public HighlightTileRenderer highlightTileRenderer = null;
    public HexagonBorder unitTileBorderRenderer = null, fowTileBorder = null;

    //Object in this set indicate that they are running an animation
    private final HashSet<Object> animationBlocks = new HashSet<>();
    public FiringRenderer firingRenderer;

    public LevelRenderer(Level level) {
        this.level = level;
        mainRenderer = new GameRenderer(MainPanel.windowTransform, this::getMainCameraTransform);
        backgroundRenderer = new GameRenderer(MainPanel.windowTransform, null);
        levelUIRenderer = new GameRenderer(MainPanel.windowTransform, null);
        firingAnimRenderer = new GameRenderer(MainPanel.windowTransform, null);
        topUIRenderer = new GameRenderer(MainPanel.windowTransform, null);
        createRenderers();
    }

    public UIConfirm confirm = null;
    public UIOnNextTurn onNextTurn = null;
    public UITurnBox turnBox = null;
    public UIDamage damageUI = null;
    public UIEndTurn endTurn = null;

    private void createRenderers() {
        createTiles();
        new BackgroundRenderer(backgroundRenderer, RenderOrder.BACKGROUND, this::getCameraPosition).setTextures(BackgroundTexture.NORMAL_1);

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
        });

        //Action render below units
        new RenderElement(mainRenderer, RenderOrder.ACTION_BELOW_UNITS, g -> {
            level.unitSet.forEach(u -> u.renderActionBelowUnits(g));
        });

        //Units
        new RenderElement(mainRenderer, RenderOrder.TILE_UNITS, g -> {
            level.unitSet.forEach(u -> {
                if (!u.isRenderFoW())
                    u.renderTile(g);
            });
        });

        new RenderElement(mainRenderer, RenderOrder.FOG_OF_WAR, g -> {
            level.tileSelector.tileSet.forEach(t -> {
                t.renderFogOfWar(g);
            });
            if (fowTileBorder != null)
                fowTileBorder.render(g);
        });

        new RenderElement(mainRenderer, RenderOrder.TILE_BORDER_HIGHLIGHTS, g -> {
            if (unitTileBorderRenderer != null) {
                unitTileBorderRenderer.render(g);
            } else if (level.tileSelector.selectedTile != null)
                level.tileSelector.selectedTile.renderTile(g, Tile.BLUE_HIGHLIGHT_COLOUR, BORDER_HIGHLIGHT_RENDERER);
        });
        new RenderElement(mainRenderer, RenderOrder.TILE_BORDER_HIGHLIGHTS, g -> {
            if (level.tileSelector.mouseOverTile != null && level.getActiveAction() != Action.FIRE)
                level.tileSelector.mouseOverTile.renderTile(g, Tile.BLUE_TRANSPARENT_COLOUR, BORDER_HIGHLIGHT_RENDERER);
        }).setZOrder(1);


        //Action render below units
        new RenderElement(mainRenderer, RenderOrder.ACTION_ABOVE_UNITS, g -> {
            level.unitSet.forEach(u -> u.renderActionAboveUnits(g));
        });

        //Action UI
        new RenderElement(mainRenderer, RenderOrder.ACTION_SELECTOR, g -> {
            if (level.selectedUnit != null)
                level.selectedUnit.renderActions(g);
        });

        //Damage UI
        damageUI = new UIDamage(mainRenderer, RenderOrder.DAMAGE_UI);

        UIUnitInfo unitInfo = new UIUnitInfo(levelUIRenderer, RenderOrder.LEVEL_UI, level);
        level.buttonRegister.register(unitInfo);

        endTurn = new UIEndTurn(levelUIRenderer, RenderOrder.LEVEL_UI, level);
        level.buttonRegister.register(endTurn);

        confirm = new UIConfirm(topUIRenderer, RenderOrder.LEVEL_UI, level);
        level.buttonRegister.register(confirm);

        onNextTurn = new UIOnNextTurn(levelUIRenderer, RenderOrder.LEVEL_UI, level);
        turnBox = new UITurnBox(levelUIRenderer, RenderOrder.LEVEL_UI, level);

        firingRenderer = new FiringRenderer(firingAnimRenderer, RenderOrder.BACKGROUND, level);
    }

    private void createTiles() {
        ObjPos tilesBound = MainPanel.RENDER_WINDOW_SIZE.copy().multiply(level.tileBound.copy().divide(MainPanel.BLOCK_DIMENSIONS));

        borderImage = new BufferedImage(tilesBound.xInt() + (int) (Tile.SCREEN_STROKE_WIDTH_MARGIN) + 2, tilesBound.yInt() + (int) (Tile.SCREEN_STROKE_WIDTH_MARGIN) + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = borderImage.createGraphics();
        g.translate(Tile.SCREEN_STROKE_WIDTH_MARGIN / 2f, Tile.SCREEN_STROKE_WIDTH_MARGIN / 2f);
        g.scale(MainPanel.windowTransform.getScaleX(), MainPanel.windowTransform.getScaleX());
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        for (int x = 0; x < level.tilesX; x++) {
            for (int y = 0; y < level.tilesY; y++) {
                level.tiles[x][y].renderTile(g, BORDER_RENDERER);
            }
        }
        g.dispose();
        Renderable.transparency(borderImage, 0.1f);
    }

    @Override
    public void acceptPressed(InputType type) {
        level.buttonRegister.input(true, type, this::transformMousePosToCamera);
    }

    @Override
    public void acceptReleased(InputType type) {
        level.buttonRegister.input(false, type, this::transformMousePosToCamera);
    }

    private boolean moveCameraEnabled = false;
    private ObjPos prevMousePos = null;
    private float shakeTimer = 0;
    private final ObjPos cameraPosition = new ObjPos();

    public final HashMap<UnitTeam, ObjPos> lastCameraPos = new HashMap<>();

    private ObjPos preShakePos = null;
    private ObjPos shakeVector = null;
    private boolean cameraShake = false;

    public void useLastCameraPos(UnitTeam prev, UnitTeam current) {
        if (level.networkState == NetworkState.LOCAL) {
            if (prev == null) {
                setCameraInterpBlockPos(lastCameraPos.get(current));
            } else {
                lastCameraPos.put(prev, getCameraInterpBlockPos(cameraPosition.copy()));
                setCameraInterpBlockPos(lastCameraPos.get(current));
            }
        }
    }

    public void useLastCameraPos(UnitTeam team) {
        ObjPos pos = lastCameraPos.get(team);
        if (pos == null)
            return;
        setCameraInterpBlockPos(pos);
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

    private ObjPos transformMousePosToCamera(Point p) {
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
        interpCameraTo = pos.copy().inverse().add(level.tileBound.copy().divide(2));
    }

    public ObjPos getCameraInterpBlockPos(ObjPos pos) {
        return pos.copy().subtract(level.tileBound.copy().divide(2)).inverse();
    }

    @Override
    public void delete() {
        level = null;
        backgroundRenderer.delete();
        mainRenderer.delete();
        levelUIRenderer.delete();
        firingAnimRenderer.delete();
        topUIRenderer.delete();
        animationBlocks.clear();
    }

    @Override
    public void render(Graphics2D g) {
        if (!isFiring() || firingRenderer.showLevel()) {
            renderLevelElements(g);
            if (confirm.visible) {
                g.setColor(new Color(0, 0, 0, 66));
                g.fillRect(0, 0, Main.window.getWidth(), Main.window.getHeight());
            }
            topUIRenderer.render(g);
        }
        if (isFiring()) {
            firingAnimRenderer.render(g);
        }
    }

    public void renderLevelElements(Graphics2D g) {
        backgroundRenderer.render(g);
        mainRenderer.render(g);
        levelUIRenderer.render(g);
    }

    public boolean runningAnim() {
        return !animationBlocks.isEmpty();
    }

    public void registerAnimBlock(Object o) {
        animationBlocks.add(o);
    }

    public void removeAnimBlock(Object o) {
        animationBlocks.remove(o);
    }

    public ObjPos interpCameraTo = null;

    @Override
    public void tick(float deltaTime) {
        if (cameraShake) {
            shakeTimer += deltaTime;
            if (shakeTimer > 0.05f) {
                shakeTimer = 0;
                cameraPosition.add(shakeVector);
                shakeVector.inverse();
            }
        }
        Point p = Main.window.getMousePosition();
        if (interpCameraTo != null) {
            cameraPosition.expTo(interpCameraTo, 4f, deltaTime);
            cameraPosition.lerpTo(interpCameraTo, 2f, deltaTime);
            if (interpCameraTo.equals(cameraPosition))
                interpCameraTo = null;
        }
        if (p != null && !runningAnim()) {
            ObjPos mousePos = new ObjPos(p).subtract(MainPanel.INSETS_OFFSET);
            ObjPos cameraTransformedPos = transformMousePosToCamera(p);
            if (moveCameraEnabled && (interpCameraTo == null || interpCameraTo.distance(cameraPosition) < 4)) {
                if (prevMousePos != null) {
                    if (interpCameraTo != null) {
                        interpCameraTo = null;
                    }
                    cameraPosition.add(mousePos.copy().subtract(prevMousePos).scaleToBlocks().flipY());
                    cameraPosition.clamp(-level.tileBound.x / 2, level.tileBound.x / 2, -level.tileBound.y / 2, level.tileBound.y / 2);
                }
                prevMousePos = mousePos;
            }
            level.buttonRegister.acceptInput(cameraTransformedPos, InputType.MOUSE_OVER, true);
        }
    }

    private boolean isFiring = false;

    public boolean isFiring() {
        return isFiring;
    }

    public void beginFiring(Unit a, Unit b, WeaponInstance weaponA, WeaponInstance weaponB) {
        isFiring = true;
        registerAnimBlock(firingAnimRenderer);
        firingRenderer.start(a, b, weaponA, weaponB);
    }

    public void endFiring(Unit a, Unit b) {
        isFiring = false;
        a.postFiring(b);
        b.postFiring(a);
        removeAnimBlock(firingAnimRenderer);
    }
}

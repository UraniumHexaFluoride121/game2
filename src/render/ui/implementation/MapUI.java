package render.ui.implementation;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import level.Level;
import level.tile.Tile;
import render.*;
import render.renderables.HexagonRenderer;
import render.renderables.RenderElement;
import render.ui.UIColourTheme;
import render.ui.types.LevelUIContainer;
import render.ui.types.UIBox;
import render.ui.types.UIButton;
import unit.UnitTeam;

import java.awt.*;

import static level.tile.Tile.*;

public class MapUI extends LevelUIContainer {
    private final float height, width, boxHeight, boxWidth;
    private static final HexagonRenderer tileFillRenderer = new HexagonRenderer(1, true, 0, Tile.BORDER_COLOUR);
    private static final Color BACKGROUND_FILL = new Color(0, 0, 0, 100), FOW_FILL = new Color(0, 0, 0, 100), CAMERA_BOX = new Color(184, 196, 214);
    private final StaticHitBox clickBox;
    private boolean mouseDown = false;

    public MapUI(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.MAP, ButtonOrder.MAP, Renderable.right() / 2, Renderable.top() / 2, level);
        width = level.tileBound.x / TILE_SIZE;
        height = level.tileBound.y / TILE_SIZE;
        boxWidth = width + 2;
        boxHeight = height + 2;
        clickBox = new StaticHitBox(boxHeight / 2, -boxHeight / 2, -boxWidth / 2, boxWidth / 2);
        Renderable tileRenderer = Renderable.renderImageCentered(level.levelRenderer.createTiles(1, (g2, t) ->
                t.renderTile(g2, new HexagonRenderer(1, false, 0.1f, Tile.BORDER_COLOUR), 1)
        ), false, -1).translate(Tile.BLOCK_STROKE_WIDTH_MARGIN / 2f / TILE_SIZE, Tile.BLOCK_STROKE_WIDTH_MARGIN / 2f / TILE_SIZE);
        addRenderables((r, b) -> {
            UIBox box = new UIBox(boxWidth, boxHeight);
            new RenderElement(r, RenderOrder.MAP, g -> {
                g.setColor(BACKGROUND_FILL);
                int right = (int) (Renderable.right() + 1), top = (int) (Renderable.top() + 1);
                g.fillRect(-right / 2, -top / 2, right + 1, top + 1);
            }, box.setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER)
                    .translate(-boxWidth / 2, -boxHeight / 2),
                    g -> {
                        GameRenderer.renderOffset(-level.tileBound.x / TILE_SIZE / 2, -level.tileBound.y / TILE_SIZE / 2, g, () -> {
                            level.tileSelector.tileSet.forEach(t -> {
                                if (t.hasStructure()) {
                                    tileFillRenderer.setColor(teamColour(t.structure.team, 130));
                                    t.renderTile(g, tileFillRenderer, 1);
                                }
                            });
                            level.unitSet.forEach(u -> {
                                if (u.renderVisible()) {
                                    g.setColor(teamColour(u.team, 200));
                                    GameRenderer.renderOffset(u.getRenderPos().copy().addY(TILE_SIZE * SIN_60_DEG / 2).divide(TILE_SIZE), g, () -> {
                                        GameRenderer.renderScaled(1 / 6f, g, () -> {
                                            g.fillOval(-1, -1, 2, 2);
                                        });
                                    });
                                }
                            });
                            tileFillRenderer.setColor(FOW_FILL);
                            level.tileSelector.tileSet.forEach(t -> {
                                if (t.isFoW) {
                                    t.renderTile(g, tileFillRenderer, 1);
                                }
                            });
                        });
                        tileRenderer.render(g);
                        Shape clip = g.getClip();
                        GameRenderer.renderOffsetScaled(-boxWidth / 2, -boxHeight / 2, 1f / Renderable.SCALING, g, () -> {
                            g.clip(box.getShape());
                        });
                        GameRenderer.renderOffset(-level.tileBound.x / TILE_SIZE / 2 - Renderable.right() / TILE_SIZE / 2, -level.tileBound.y / TILE_SIZE / 2 - Renderable.top() / TILE_SIZE / 2, g, () -> {
                            g.setStroke(Renderable.roundedStroke(0.1f * SCALING));
                            g.setColor(CAMERA_BOX);
                            GameRenderer.renderScaled(1f / Renderable.SCALING, g, () -> {
                                ObjPos cameraPosition = level.levelRenderer.getCameraInterpBlockPos(level.levelRenderer.getCameraPosition());
                                g.drawRoundRect((int) (cameraPosition.x / TILE_SIZE * SCALING),
                                        (int) (cameraPosition.y / TILE_SIZE * SCALING),
                                        (int) (Renderable.right() / TILE_SIZE * SCALING),
                                        (int) (Renderable.top() / TILE_SIZE * SCALING),
                                        SCALING, SCALING);
                            });
                        });
                        g.setClip(clip);
                    }
            ).setZOrder(-1);
            new UIButton(r, b, RenderOrder.MAP, ButtonOrder.MAP, -3, -boxHeight / 2 - 2, 6, 1.5f, 1.2f, false, () -> {
                setEnabled(false);
            }).setBold().setText("Close").setColourTheme(UIColourTheme.DEEP_RED);
            b.register(new RegisteredButtonInputReceiver() {
                @Override
                public boolean posInside(ObjPos pos) {
                    return true;
                }

                @Override
                public boolean blocking(InputType type) {
                    return type != InputType.MOUSE_RIGHT;
                }

                @Override
                public ButtonOrder getButtonOrder() {
                    return ButtonOrder.MAIN_BUTTONS;
                }

                @Override
                public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
                    if (!blocked) {
                        if (mouseDown && type == InputType.MOUSE_OVER) {
                            ObjPos clickPos = pos.copy().add(level.tileBound.x / TILE_SIZE / 2, level.tileBound.y / TILE_SIZE / 2).multiply(TILE_SIZE);
                            level.levelRenderer.setCameraInterpBlockPos(clickPos);
                        } else if (type == InputType.MOUSE_LEFT) {
                            if (clickBox.isPositionInside(pos)) {
                                mouseDown = true;
                                ObjPos clickPos = pos.copy().add(level.tileBound.x / TILE_SIZE / 2, level.tileBound.y / TILE_SIZE / 2).multiply(TILE_SIZE);
                                level.levelRenderer.setCameraInterpBlockPos(clickPos);
                            } else {
                                setEnabled(false);
                            }
                        }
                    }
                    if (type == InputType.MOUSE_RIGHT || type == InputType.ESCAPE)
                        setEnabled(false);
                }

                @Override
                public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
                    if (!blocked && type == InputType.MOUSE_LEFT)
                        mouseDown = false;
                }
            });
        });
    }

    private Color teamColour(UnitTeam team, int a) {
        Color c;
        if (team == null)
            c = new Color(200, 200, 200);
        else
            c = team.uiColour.borderColour;
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}

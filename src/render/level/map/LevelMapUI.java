package render.level.map;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import level.AbstractLevel;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import render.*;
import render.level.tile.RenderElement;
import render.types.box.UIBox;
import render.types.container.LevelUIContainer;
import render.types.input.button.UIButton;

import java.awt.*;

import static level.tile.Tile.*;

public class LevelMapUI extends LevelUIContainer<AbstractLevel<?, ?>> {
    private final float height, width, boxHeight, boxWidth;
    private static final Color BACKGROUND_FILL = new Color(0, 0, 0, 100), CAMERA_BOX = new Color(184, 196, 214);
    private final StaticHitBox clickBox;
    public MapUI mapUI;
    private boolean mouseDown = false;

    public LevelMapUI(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, AbstractLevel<?, ?> level) {
        super(register, buttonRegister, RenderOrder.MAP, ButtonOrder.MAP, Renderable.right() / 2, Renderable.top() / 2, level);
        width = level.tileBound.x / TILE_SIZE;
        height = level.tileBound.y / TILE_SIZE;
        boxWidth = width + 2;
        boxHeight = height + 2;
        clickBox = new StaticHitBox(boxHeight / 2, -boxHeight / 2, -boxWidth / 2, boxWidth / 2);
        addRenderables((r, b) -> {
            UIBox box = new UIBox(boxWidth, boxHeight);
            new RenderElement(r, RenderOrder.MAP, g -> {
                g.setColor(BACKGROUND_FILL);
                int right = (int) (Renderable.right() + 1), top = (int) (Renderable.top() + 1);
                g.fillRect(-right / 2, -top / 2, right + 1, top + 1);
            }, box.setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER)
                    .translate(-boxWidth / 2, -boxHeight / 2)
            ).setZOrder(-3);
            mapUI = new MapUI(r, RenderOrder.MAP, level, 1, 0.1f, 0.1f);
            mapUI.setZOrder(-2);
            new RenderElement(r, RenderOrder.MAP, g -> {
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
            }).setZOrder(-1);
            new UIButton(r, b, RenderOrder.MAP, ButtonOrder.MAP, -3, -boxHeight / 2 - 2, 6, 1.5f, 1.2f, false, () -> {
                setEnabled(false);
            }).setBold().setText("Close").setColourTheme(UIColourTheme.DEEP_RED);
            b.register(new RegisteredButtonInputReceiver() {
                @Override
                public boolean posInside(ObjPos pos, InputType type) {
                    return true;
                }

                @Override
                public boolean blocking(InputType type) {
                    return true;
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
                            if (TutorialManager.isEnabled(TutorialElement.CAMERA_MOVEMENT))
                                level.levelRenderer.setCameraInterpBlockPos(clickPos);
                        } else if (type == InputType.MOUSE_LEFT) {
                            if (clickBox.isPositionInside(pos)) {
                                mouseDown = true;
                                ObjPos clickPos = pos.copy().add(level.tileBound.x / TILE_SIZE / 2, level.tileBound.y / TILE_SIZE / 2).multiply(TILE_SIZE);
                                if (TutorialManager.isEnabled(TutorialElement.CAMERA_MOVEMENT))
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

    public void update() {
        mapUI.update();
    }

    @Override
    public void delete() {
        super.delete();
        mapUI = null;
    }
}

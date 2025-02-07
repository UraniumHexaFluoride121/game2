package render.ui.implementation;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import level.Level;
import level.tile.Tile;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.renderables.RenderElement;
import render.ui.UIColourTheme;
import render.ui.types.UIClickBlockingBox;
import render.ui.types.UIContainer;
import render.ui.types.UITextLabel;

public class UITileInfo extends UIContainer {
    private final UITextLabel title = new UITextLabel(14f, 0.9f, true).setTextLeftBold();
    private final UITextLabel structure = new UITextLabel(12.4f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextLeft("Structure:").setTextRightBold().setTextLeftBold().setRightOffset(0.2f);
    private final UITextLabel visibility = new UITextLabel(5f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextCenter("Visibility:").setTextCenterBold();
    private final UITextLabel defence = new UITextLabel(5f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextCenter("Defence:").setTextCenterBold();
    private final UITextLabel movement = new UITextLabel(5f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextCenter("Movement:").setTextCenterBold();
    private final UIHitPointBar visibilityBar = new UIHitPointBar(0.1f, 6, 1, 0.15f, 3, UIColourTheme.LIGHT_BLUE.borderColour, UIColourTheme.LIGHT_BLUE.backgroundColour, UIColourTheme.LIGHT_BLUE.borderColour).setRounding(0.5f);
    private final UIHitPointBar defenceBar = new UIHitPointBar(0.1f, 6, 1, 0.15f, 3, UIColourTheme.LIGHT_BLUE.borderColour, UIColourTheme.LIGHT_BLUE.backgroundColour, UIColourTheme.LIGHT_BLUE.borderColour).setRounding(0.5f);
    private final UIHitPointBar movementBar = new UIHitPointBar(0.1f, 6, 1, 0.15f, 3, UIColourTheme.LIGHT_BLUE.borderColour, UIColourTheme.LIGHT_BLUE.backgroundColour, UIColourTheme.LIGHT_BLUE.borderColour).setRounding(0.5f);
    public UITileInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, Level level) {
        super(register, buttonRegister, order, buttonOrder, x, y);
        addRenderables((r, b) -> {
            new UIClickBlockingBox(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 0, 0, 14, 8)
                    .setPosTransformer(p -> level.levelRenderer.transformCameraPosToBlock(p));
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    title.translate(-0.2f, 8.5f),
                    structure.translate(0.5f, 6.5f),
                    visibility.translate(0.5f, 4.8f),
                    defence.translate(0.5f, 2.8f),
                    movement.translate(0.5f, 0.8f)
            ).setZOrder(1);
            new RenderElement(r, RenderOrder.LEVEL_UI, visibilityBar.translate(7, 5)).setZOrder(2);
            new RenderElement(r, RenderOrder.LEVEL_UI, defenceBar.translate(7, 3)).setZOrder(2);
            new RenderElement(r, RenderOrder.LEVEL_UI, movementBar.translate(7, 1)).setZOrder(2);
        });
    }

    public boolean previouslyDisabled = false;

    @Override
    public UIContainer setEnabled(boolean enabled) {
        if (!enabled)
            previouslyDisabled = true;
        return super.setEnabled(enabled);
    }

    public void setTile(Tile tile) {
        title.updateTextLeft(tile.type.displayName + " tile");
        structure.updateTextRight(tile.structure == null ? "None" : tile.structure.type.displayName);
        if (previouslyDisabled) {
            visibilityBar.setFill(tile.type.visibility.barFill);
            defenceBar.setFill(tile.type.defence.barFill);
            movementBar.setFill(tile.type.movement.barFill);
            previouslyDisabled = false;
        } else {
            visibilityBar.setFill(tile.type.visibility.barFill, 0.8f, 0.6f);
            defenceBar.setFill(tile.type.defence.barFill, 0.8f, 0.6f);
            movementBar.setFill(tile.type.movement.barFill, 0.8f, 0.6f);
        }
    }
}

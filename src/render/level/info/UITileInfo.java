package render.level.info;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import level.Level;
import level.energy.EnergyManager;
import level.tile.Tile;
import render.*;
import render.level.tile.HexagonRenderer;
import render.level.tile.RenderElement;
import render.texture.ImageRenderer;
import render.types.UIHitPointBar;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.box.UIClickBlockingBox;
import render.types.input.button.UIShapeButton;
import render.types.text.UITextLabel;

import java.awt.*;

public class UITileInfo extends LevelUIContainer<Level> {
    public static final float BAR_SPACING = 1.4f, INITIAL_BAR_POS = 0.6f;
    private final UITextLabel title = new UITextLabel(14f, 0.9f, true).setTextLeftBold();
    private final UITextLabel structure = new UITextLabel(12.4f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextLeft("Structure:").setTextRightBold().setTextLeftBold().setRightOffset(0.2f);
    private final UITextLabel visibility = new UITextLabel(5f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextCenter("Visibility:").setTextCenterBold();
    private final UITextLabel defence = new UITextLabel(5f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextCenter("Defence:").setTextCenterBold();
    private final UITextLabel movement = new UITextLabel(5f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextCenter("Movement:").setTextCenterBold();
    private final UITextLabel mining = new UITextLabel(5f, 1, false, UITextLabel.DEFAULT_LINE_WIDTH, 0.8f).updateTextCenter(EnergyManager.displayName + ":").setTextCenterBold();

    private final UIHitPointBar visibilityBar = new UIHitPointBar(0.1f, 6, 1, 0.15f, 3, UIColourTheme.LIGHT_BLUE).setRounding(0.5f);
    private final UIHitPointBar defenceBar = new UIHitPointBar(0.1f, 6, 1, 0.15f, 3, UIColourTheme.LIGHT_BLUE).setRounding(0.5f);
    private final UIHitPointBar movementBar = new UIHitPointBar(0.1f, 6, 1, 0.15f, 3, UIColourTheme.LIGHT_BLUE).setRounding(0.5f);
    private final UIHitPointBar miningBar = new UIHitPointBar(0.1f, 6, 1, 0.15f, 3, UIColourTheme.LIGHT_BLUE).setRounding(0.5f);
    private final HexagonRenderer border = new HexagonRenderer(8, false, 0.3f, Level.FOW_TILE_BORDER_COLOUR);
    private final HexagonRenderer fill = new HexagonRenderer(8, true, 0.2f, new Color(30, 27, 27, 242));

    private ImageRenderer tileImage, structureImage;
    private RenderElement miningElement;
    private UIShapeButton structureInfoButton;
    private Point pos;
    private Level level;

    public UITileInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, Level level) {
        super(register, buttonRegister, order, buttonOrder, x, y, level);
        this.level = level;
        addRenderables((r, b) -> {
            float labelPos = INITIAL_BAR_POS - BAR_SPACING;
            float barPos = INITIAL_BAR_POS - BAR_SPACING - 0.05f;
            new UIClickBlockingBox(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI_BACK, 0, 0, 14, 17, box -> box.setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER_LIGHT));
            miningElement = new RenderElement(r, RenderOrder.LEVEL_UI,
                    mining.translate(0.5f, labelPos += BAR_SPACING),
                    miningBar.translate(7, barPos += BAR_SPACING)).setZOrder(2);
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    title.translate(-0.2f, 17.5f),
                    structure.translate(0.5f, 6.5f),
                    movement.translate(0.5f, labelPos += BAR_SPACING),
                    movementBar.translate(7, barPos += BAR_SPACING),
                    defence.translate(0.5f, labelPos += BAR_SPACING),
                    defenceBar.translate(7, barPos += BAR_SPACING),
                    visibility.translate(0.5f, labelPos += BAR_SPACING),
                    visibilityBar.translate(7, barPos += BAR_SPACING),
                    fill.translate(7, 8.5f),
                    g -> {
                        GameRenderer.renderOffset(7, 8.5f + 8 * Tile.SIN_60_DEG / 2, g, () -> {
                            if (tileImage != null)
                                tileImage.render(g, 8);
                            if (structureImage != null)
                                structureImage.render(g, 8);
                        });
                    },
                    border.translate(7, 8.5f)
            ).setZOrder(1);
            structureInfoButton = new UIShapeButton(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 14 - 2, 17 - 2, 1.5f, 1.5f, false)
                    .setShape(UIShapeButton::i).drawShape(0.25f).setBoxCorner(0.3f).setOnClick(() -> {
                        level.levelRenderer.structureInfoScreen.enable(level.getTile(pos));
                    });
            structureInfoButton.setZOrder(1);
        });
    }

    public boolean previouslyDisabled = false, miningPreviouslyDisabled = false;

    @Override
    public UIContainer setEnabled(boolean enabled) {
        if (!enabled) {
            previouslyDisabled = true;
            miningPreviouslyDisabled = true;
        }
        return super.setEnabled(enabled);
    }

    public void setTile(Tile tile) {
        pos = tile.pos;
        tileImage = tile.imageRenderer;
        miningBar.setSegments(tile.miningBarSegments());
        miningElement.setEnabled(tile.miningBarSegments() != 0 && level.currentVisibility.visibleTiles().contains(tile.pos));
        if (tile.miningBarSegments() == 0)
            miningPreviouslyDisabled = true;
        if (tile.hasStructure())
            structureImage = tile.structure.type.getLightImage(tile.structure.team);
        else
            structureImage = null;
        structureInfoButton.setEnabled(tile.hasStructure());
        title.updateTextLeft(tile.type.displayName + " tile");
        structure.updateTextRight(tile.structure == null ? "None" : tile.structure.type.getName());
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
        if (!miningElement.isEnabled()) {
            miningBar.setFill(tile.miningBarFill);
        } else if (miningPreviouslyDisabled) {
            miningPreviouslyDisabled = false;
            miningBar.setFill(tile.miningBarFill);
        } else {
            miningBar.setFill(tile.miningBarFill, 0.8f, 0.6f);
        }
    }

    @Override
    public void delete() {
        super.delete();
        miningElement = null;
        level = null;
    }
}

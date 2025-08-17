package render.level.info;

import foundation.input.ButtonRegister;
import foundation.math.MathUtil;
import level.Level;
import level.energy.EnergyManager;
import level.structure.Structure;
import level.tile.Tile;
import level.tile.TileType;
import level.tutorial.TutorialManager;
import render.*;
import render.level.tile.HexagonRenderer;
import render.level.tile.RenderElement;
import render.texture.ImageRenderer;
import render.types.UIHitPointBar;
import render.types.box.UIClickBlockingBox;
import render.types.box.UIDisplayBox;
import render.types.box.UIDisplayBoxButtonHandler;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.text.TextRenderable;
import render.types.text.UIStaticTooltip;
import render.types.text.UITextLabel;
import unit.action.Action;
import unit.stats.Modifier;
import unit.stats.ModifierCategory;

import java.awt.*;

import static render.UIColourTheme.*;

public class UITileInfo extends LevelUIContainer<Level> {
    public static final float BAR_SPACING = 1.4f, INITIAL_BAR_POS = 0.6f, HEIGHT = 17;
    private UIDisplayBox structure, concealment, movement, incomingDamage, mining;
    private UIDisplayBoxRenderElement elements;
    private UIStaticTooltip structureBox;
    private final UITextLabel title = new UITextLabel(14f, 0.9f, true).setTextLeftBold();

    private final HexagonRenderer border = new HexagonRenderer(8, false, 0.3f, Level.FOW_TILE_BORDER_COLOUR);
    private final HexagonRenderer fill = new HexagonRenderer(8, true, 0.2f, new Color(30, 27, 27, 242));

    private ImageRenderer tileImage, structureImage;
    private Point pos;

    public UITileInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, Level level) {
        super(register, buttonRegister, order, x, y, level);
        addRenderables((r, b) -> {
            elements = new UIDisplayBoxRenderElement(r, RenderOrder.LEVEL_UI, 0, 0.5f, 14, HEIGHT - 9.5f, uiBox -> {
            }, false);
            elements.box.setRenderBox(false);
            elements.setZOrder(2);
            structure = new UIDisplayBox(0, 0, 13, -1, uiBox -> uiBox.setColourTheme(Modifier.BLUE_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Structure")
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
            concealment = new UIDisplayBox(0, 0, 13, -1, uiBox -> uiBox.setColourTheme(Modifier.VIEW_RANGE_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, ModifierCategory.CONCEALMENT.getName())
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
            movement = new UIDisplayBox(0, 0, 13, -1, uiBox -> uiBox.setColourTheme(Modifier.MOVEMENT_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, ModifierCategory.MOVEMENT_COST_DISPLAY.getName())
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
            incomingDamage = new UIDisplayBox(0, 0, 13, -1, uiBox -> uiBox.setColourTheme(Modifier.INCOMING_DAMAGE_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, ModifierCategory.INCOMING_DAMAGE.getName())
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
            mining = new UIDisplayBox(0, 0, 13, -1, uiBox -> uiBox.setColourTheme(Modifier.MINING_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, EnergyManager.displayName)
                    .addSpace(0.2f, 0)
                    .addBar(new UIHitPointBar(0f, 1, 0.6f, 0.15f, 1, Modifier.MINING_BACKGROUND).barOnly().setRounding(0.5f), HorizontalAlign.CENTER, 0, true)
                    .addSpace(0.1f, 0)
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                    .setColumnVerticalAlign(1, VerticalAlign.TOP);

            elements.box.addBox(structure, HorizontalAlign.CENTER, 0, false);
            elements.box.setColumnVerticalAlign(0, VerticalAlign.TOP);

            elements.box.addSpace(0.3f, 0);
            elements.box.addBox(concealment, HorizontalAlign.CENTER, 0, false);

            elements.box.addSpace(0.3f, 0);
            elements.box.addBox(movement, HorizontalAlign.CENTER, 0, false);

            elements.box.addSpace(0.3f, 0);
            elements.box.addBox(incomingDamage, HorizontalAlign.CENTER, 0, false);

            elements.box.addSpace(0.3f, 0);
            elements.box.addBox(mining, HorizontalAlign.CENTER, 0, false);

            UIDisplayBoxButtonHandler buttonHandler = new UIDisplayBoxButtonHandler(r, b, RenderOrder.LEVEL_UI, elements.box);
            buttonHandler.addTooltip(0, 0, true).add(button -> {
                float width = 17;
                structureBox = new UIStaticTooltip(-width - 0.5f, 0, width, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox energyIncomeStats = new UIDisplayBox(0, 0, width - 2, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, EnergyManager.displayName + " Income:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                UIDisplayBox energyIncome = new UIDisplayBox(0, 0, width - 1, -1, uiBox -> uiBox.setColourTheme(Modifier.MINING_BACKGROUND), false)
                        .addText(0.8f, HorizontalAlign.LEFT, EnergyManager.displayName + " Income")
                        .addSpace(0.2f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "This structure increases " + EnergyManager.displayName + " income, which gets credited to you at the start of your turn.")
                        .addSpace(0.3f, 0)
                        .addBox(energyIncomeStats, HorizontalAlign.CENTER, 0, false);
                UIDisplayBox repairStats = new UIDisplayBox(0, 0, width - 2, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.REPAIR.getName())
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                UIDisplayBox repair = new UIDisplayBox(0, 0, width - 1, -1, uiBox -> uiBox.setColourTheme(Modifier.GREEN_BACKGROUND), false)
                        .addText(0.8f, HorizontalAlign.LEFT, "Unit " + ModifierCategory.REPAIR.getName())
                        .addSpace(0.2f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "This structure repairs the HP of allied units on the same tile at the start of their turn.")
                        .addSpace(0.3f, 0)
                        .addBox(repairStats, HorizontalAlign.CENTER, 0, false);
                UIDisplayBox resupply = new UIDisplayBox(0, 0, width - 1, -1, uiBox -> uiBox.setColourTheme(Modifier.RESUPPLY_BACKGROUND), false)
                        .addText(0.8f, HorizontalAlign.LEFT, "Unit " + Action.RESUPPLY.getName())
                        .addSpace(0.2f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "This structure fully resupplies the ammunition of allied units on the same tile at the start of their turn.");

                UIDisplayBox captureStats = new UIDisplayBox(0, 0, width - 1, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, "Turns to capture:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);

                structureBox.addText(1f, HorizontalAlign.LEFT, null)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, null)
                        .addSpace(0.3f, 0)
                        .addBox(captureStats, HorizontalAlign.CENTER, 0, false)
                        .addSpace(0.3f, 0)
                        .addBox(energyIncome, HorizontalAlign.CENTER, 0, false)
                        .addSpace(0.3f, 0)
                        .addBox(repair, HorizontalAlign.CENTER, 0, false)
                        .addSpace(0.3f, 0)
                        .addBox(resupply, HorizontalAlign.CENTER, 0, false)
                ;
                return structureBox;
            });
            buttonHandler.addTooltip(2, 0, true).add(button -> {
                UIStaticTooltip displayBox = new UIStaticTooltip(-11.5f, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                displayBox.addText(0.9f, HorizontalAlign.LEFT, ModifierCategory.CONCEALMENT.getName())
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "This value shows how much of a unit's " + ModifierCategory.VIEW_RANGE.getName().toLowerCase() + " is blocked by this tile. " +
                                "Higher values make it harder to see through the fog of war.\n\nTiles directly adjacent to allied units are always revealed, " +
                                "no matter how high the " + ModifierCategory.CONCEALMENT.getName().toLowerCase() + " value is.\n\n" +
                                "A unit's " + ModifierCategory.VIEW_RANGE.getName().toLowerCase() + " can be found by hovering over the Hull tab in the unit info box.");
                return displayBox;
            });
            buttonHandler.addTooltip(4, 0, true).add(button -> {
                UIStaticTooltip displayBox = new UIStaticTooltip(-11.5f, 2, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                displayBox.addText(0.9f, HorizontalAlign.LEFT, ModifierCategory.MOVEMENT_COST_DISPLAY.getName())
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "This is the " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " for this tile. " +
                                "A unit can only move to tiles where the total " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " of the tiles " +
                                "along its path is lower than the unit's max " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase());
                return displayBox;
            });
            buttonHandler.addTooltip(6, 0, true).add(button -> {
                UIStaticTooltip displayBox = new UIStaticTooltip(-11.5f, 2, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                displayBox.addText(0.9f, HorizontalAlign.LEFT, ModifierCategory.INCOMING_DAMAGE.getName())
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "When a unit is on this tile, it will be affected by the " +
                                ModifierCategory.INCOMING_DAMAGE.getName().toLowerCase() + " modifier listed here. Applies only to weapon damage.");
                return displayBox;
            });
            buttonHandler.addTooltip(8, 0, true).add(button -> {
                UIStaticTooltip displayBox = new UIStaticTooltip(-11.5f, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                displayBox.addText(0.9f, HorizontalAlign.LEFT, EnergyManager.displayName)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT,
                                "This is the number of turns left that this tile can be mined for " + EnergyManager.displayName + " before it is depleted. Once depleted, the tile will turn into an " +
                                        TileType.EMPTY.getName().toLowerCase() + " tile.");
                return displayBox;
            });

            new UIClickBlockingBox(r, b, RenderOrder.LEVEL_UI, 0, 0, 14, 17, box -> box.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX))
                    .setZOrder(-10);
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    title.translate(-0.2f, HEIGHT + 0.5f),
                    fill.translate(7, HEIGHT - 8.5f),
                    g -> {
                        GameRenderer.renderOffset(7, HEIGHT - 8.5f + 8 * Tile.SIN_60_DEG / 2, g, () -> {
                            if (tileImage != null)
                                tileImage.render(g, 8);
                            if (structureImage != null)
                                structureImage.render(g, 8);
                        });
                    },
                    border.translate(7, HEIGHT - 8.5f)
            ).setZOrder(1);
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
        UIHitPointBar bar = mining.getBar(2, 0);
        bar.setSegments(tile.miningBarSegments());
        mining.setText(0, 1, tile.miningBarSegments() + (tile.miningBarSegments() == 1 ? " Turn" : " Turns"));
        elements.box.setElementEnabled(tile.miningBarSegments() != 0 && level.currentVisibility.visibleTiles().contains(tile.pos), 8, 0);
        if (tile.miningBarSegments() == 0)
            miningPreviouslyDisabled = true;
        Structure structure = tile.structure;
        if (tile.hasStructure()) {
            structureImage = structure.type.getLightImage(structure.team);
            structureBox.setText(0, 0, structure.type.getName());
            structureBox.setText(2, 0, structure.type.description);
            structureBox.getDisplayBox(4, 0).setText(0, 1, structure.type.captureSteps + " Turns");

            structureBox.setElementEnabled(structure.type.energyIncome != 0, 6, 0);
            structureBox.getDisplayBox(6, 0).getDisplayBox(4, 0).setText(0, 1, "+" + structure.type.energyIncome + TextRenderable.ENERGY_ICON.display);

            structureBox.setElementEnabled(structure.type.unitRegen != 0, 8, 0);
            structureBox.getDisplayBox(8, 0).getDisplayBox(4, 0).setText(0, 1, "+" + structure.type.unitRegen + TextRenderable.REPAIR_ICON.display);

            structureBox.setElementEnabled(structure.type.resupply, 10, 0);
        } else
            structureImage = null;
        structureBox.setEnabled(tile.hasStructure());
        title.updateTextLeft(tile.type.getName() + " tile");
        this.structure.setText(0, 1, structure == null ? "None" : structure.type.getName());
        concealment.setText(0, 1, tile.type.concealment > 50 ? "Infinite" : MathUtil.floatToString(tile.type.concealment, 1) + ModifierCategory.CONCEALMENT.icon());
        movement.setText(0, 1, MathUtil.floatToString(tile.type.moveCost, 1) + ModifierCategory.MOVEMENT_COST_DISPLAY.icon());
        boolean hasDamageModifier = tile.type.damageModifier.effect(ModifierCategory.INCOMING_DAMAGE) != 1;
        incomingDamage.setText(0, 1, hasDamageModifier ? Modifier.percentMultiplicative(tile.type.damageModifier.effect(ModifierCategory.INCOMING_DAMAGE)) + ModifierCategory.INCOMING_DAMAGE.icon() : "Unchanged");

        if (miningPreviouslyDisabled && tile.miningBarSegments() != 0) {
            miningPreviouslyDisabled = false;
            bar.setFill(tile.miningBarFill);
        } else {
            bar.setFill(tile.miningBarFill, 0.8f, 0.6f);
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && (!TutorialManager.isTutorial() || TutorialManager.tileInfoEnabled);
    }

    @Override
    public void delete() {
        super.delete();
        elements = null;
        structure = null;
        concealment = null;
        movement = null;
        incomingDamage = null;
        mining = null;
        structureBox = null;
    }
}

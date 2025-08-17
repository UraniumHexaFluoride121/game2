package render.level.info;

import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import foundation.math.HitBox;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.Level;
import level.energy.EnergyManager;
import level.tile.TileModifier;
import level.tile.TileSet;
import level.tile.TileType;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventUIDeselect;
import level.tutorial.sequence.event.EventUISelect;
import level.tutorial.sequence.event.UIElement;
import render.*;
import render.level.tile.MultiColourHighlight;
import render.level.tile.RenderElement;
import render.types.UIHitPointBar;
import render.types.box.UIBox;
import render.types.box.UIDisplayBox;
import render.types.box.UIDisplayBoxButtonHandler;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.container.LevelUIContainer;
import render.types.input.button.UIShapeButton;
import render.types.text.*;
import unit.ShipClass;
import unit.Unit;
import unit.UnitPose;
import unit.action.Action;
import unit.stats.Modifier;
import unit.stats.ModifierCategory;
import unit.weapon.WeaponEffectiveness;

import java.awt.*;

import static render.UIColourTheme.*;
import static unit.Unit.*;

public class UIUnitInfo extends LevelUIContainer<Level> {
    private static final float HEIGHT = 16;
    private final UITextLabel title = new UITextLabel(11f, 0.9f, true).setTextLeftBold();
    private UIDisplayBox hp, shield, movement, weapons, ammo, repair, resupply, stealth, mining;
    private final UIBox box = new UIBox(11, HEIGHT).setColourTheme(LIGHT_BLUE_BOX);
    private final HitBox hitBox = HitBox.createFromOriginAndSize(0.5f, 0.5f, 11, HEIGHT);
    private Level level;
    public UIShapeButton infoButton, viewFiringRange, viewEffectiveness;
    private MultiColourHighlight rangeHighlight = null, effectivenessHighlight = null;

    public UIUnitInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, Level level) {
        super(register, buttonRegister, order, 0, 0, level);
        this.level = level;
        addRenderables((r, b) -> {
            UIDisplayBoxRenderElement systems = new UIDisplayBoxRenderElement(r, RenderOrder.LEVEL_UI, 0.5f, 0.5f, 11, HEIGHT - 7, uiBox -> {
            }, false);
            systems.box.setRenderBox(false);
            hp = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.GREEN_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Hull")
                    .addSpace(0.2f, 0)
                    .addBar(new UIHitPointBar(0f, 1, 0.5f, 0.15f, 1, Modifier.GREEN_BACKGROUND).barOnly(), HorizontalAlign.CENTER, 0, true)
                    .addSpace(0.1f, 0)
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                    .setColumnVerticalAlign(1, VerticalAlign.TOP)
                    .addOnUpdate(() -> {
                        UIHitPointBar bar = hp.getBar(2, 0);
                        float fill = bar.getRenderFill() / bar.getSegments();
                        UIColourTheme theme;
                        if (fill < 0.35f) {
                            theme = Modifier.RED_BACKGROUND;
                        } else if (fill < 0.75f) {
                            theme = Modifier.YELLOW_BACKGROUND;
                        } else
                            theme = Modifier.GREEN_BACKGROUND;
                        bar.setColour(theme);
                        hp.modifyBox(uiBox -> uiBox.setColourTheme(theme));
                    });
            shield = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.SHIELD_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Shield")
                    .addSpace(0.2f, 0)
                    .addBar(new UIHitPointBar(0f, 1, 0.5f, 0.15f, 1, Modifier.SHIELD_BACKGROUND).barOnly(), HorizontalAlign.CENTER, 0, true)
                    .addSpace(0.1f, 0)
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                    .setColumnVerticalAlign(1, VerticalAlign.TOP);
            movement = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.MOVEMENT_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Engines")
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
            weapons = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.DAMAGE_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Weapons")
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                    .addSpace(0.15f, 1)
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                    .addSpace(0.15f, 1)
                    .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
            ammo = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.DAMAGE_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Ammunition")
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
            repair = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.GREEN_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Repair")
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
            resupply = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.RESUPPLY_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Resupply");
            stealth = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.YELLOW_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Stealth");
            mining = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Modifier.MINING_BACKGROUND), false)
                    .setWidthMargin(0.2f)
                    .addText(0.7f, HorizontalAlign.LEFT, "Mining")
                    .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
            systems.box.addText(0.6f, HorizontalAlign.CENTER, "Hover for more info").getText().setItalic(true);
            systems.box.addSpace(0.3f, 0);

            systems.box.addBox(hp, HorizontalAlign.CENTER, 0, false);
            systems.box.setColumnVerticalAlign(0, VerticalAlign.TOP);

            systems.box.addSpace(0.3f, 0);
            systems.box.addBox(shield, HorizontalAlign.CENTER, 0, false);

            systems.box.addSpace(0.3f, 0);
            systems.box.addBox(movement, HorizontalAlign.CENTER, 0, false);

            systems.box.addSpace(0.3f, 0);
            systems.box.addBox(weapons, HorizontalAlign.CENTER, 0, false);

            systems.box.addSpace(0.3f, 0);
            systems.box.addBox(ammo, HorizontalAlign.CENTER, 0, false);

            systems.box.addSpace(0.3f, 0);
            systems.box.addBox(repair, HorizontalAlign.CENTER, 0, false);

            systems.box.addSpace(0.3f, 0);
            systems.box.addBox(resupply, HorizontalAlign.CENTER, 0, false);

            systems.box.addSpace(0.3f, 0);
            systems.box.addBox(stealth, HorizontalAlign.CENTER, 0, false);

            systems.box.addSpace(0.3f, 0);
            systems.box.addBox(mining, HorizontalAlign.CENTER, 0, false);

            UIDisplayBoxButtonHandler buttonHandler = new UIDisplayBoxButtonHandler(r, b, RenderOrder.LEVEL_UI, systems.box);
            buttonHandler.addTooltip(2, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 1.8f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox hpStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, "Unit Class:")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.HP.getName() + ":")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                UIDisplayBox viewRangeStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.VIEW_RANGE.getName() + ":")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Hull").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addBar(new UIHitPointBar(0.1f, 1, 1f, 0.2f, 1, Modifier.GREEN_BACKGROUND).setRounding(0.6f), HorizontalAlign.CENTER, 0, true)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "Every unit has a limited amount of HP. The more damage a unit has taken, the less damage it can deal with its weapons. When its HP drops to 0, it is destroyed.")
                        .addSpace(0.3f, 0)
                        .addBox(hpStats, HorizontalAlign.CENTER, 0, false)
                        .addSpace(0.6f, 0)
                        .addText(1, HorizontalAlign.LEFT, ModifierCategory.VIEW_RANGE.getName())
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "The unit's " + ModifierCategory.VIEW_RANGE.getName().toLowerCase() + " determines how far the unit can reveal tiles hidden by fog of war. Tiles directly adjacent to allied units are always revealed.")
                        .addSpace(0.3f, 0)
                        .addBox(viewRangeStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    UIHitPointBar bar = tooltip.getBar(2, 0);
                    bar.setSegments((int) unit.stats.maxHP()).setFill(unit.data.renderHP);
                    float fill = bar.getRenderFill() / bar.getSegments();
                    UIColourTheme theme;
                    if (fill < 0.35f) {
                        theme = Modifier.RED_BACKGROUND;
                    } else if (fill < 0.75f) {
                        theme = Modifier.YELLOW_BACKGROUND;
                    } else
                        theme = Modifier.GREEN_BACKGROUND;
                    bar.setColour(theme);
                    hpStats.setText(0, 1, unit.data.type.shipClass.icon.display + unit.data.type.shipClass.getName());
                    hpStats.setText(2, 1, MathUtil.floatToString(unit.stats.maxHP(), 1) + TextRenderable.HP_ICON.display);
                    viewRangeStats.setText(0, 1, MathUtil.floatToString(unit.stats.maxViewRange(), 1) + TextRenderable.VIEW_RANGE_ICON.display);
                });
                return tooltip;
            });
            buttonHandler.addTooltip(4, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 0.5f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox shieldStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.SHIELD_HP.getName() + ":")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                UIDisplayBox shieldRegenStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.SHIELD_REGEN.getName() + ":")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, EnergyManager.displayName + " Cost:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Shield").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addBar(new UIHitPointBar(0.1f, 1, 1f, 0.2f, 1, Modifier.SHIELD_BACKGROUND).setRounding(0.6f), HorizontalAlign.CENTER, 0, true)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "This unit has a shield that protects the hull from taking damage. Any damage received will first drain the shield HP before impacting the hull.")
                        .addSpace(0.3f, 0)
                        .addBox(shieldStats, HorizontalAlign.CENTER, 0, false)
                        .addSpace(1.2f, 0)
                        .addText(1, HorizontalAlign.LEFT, ModifierCategory.SHIELD_REGEN.getName())
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "This unit is capable of regenerating its shield using the " + StyleElement.MODIFIER_SHIELD_HP.display + Action.SHIELD_REGEN.getName() + " " + TextRenderable.SHIELD_REGEN_ICON.display + StyleElement.NO_COLOUR.display + " action.")
                        .addSpace(0.3f, 0)
                        .addBox(shieldRegenStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    if (unit.stats.maxShieldHP() == 0)
                        return;
                    tooltip.getBar(2, 0).setSegments((int) unit.stats.maxShieldHP()).setFill(unit.data.shieldRenderHP);
                    shieldStats.setText(0, 1, MathUtil.floatToString(unit.stats.maxShieldHP(), 1) + TextRenderable.SHIELD_ICON.display);
                    boolean shieldRegen = unit.data.type.canPerformAction(Action.SHIELD_REGEN);
                    for (int i = 7; i <= 12; i++) {
                        tooltip.setElementEnabled(shieldRegen, i, 0);
                    }
                    if (shieldRegen) {
                        shieldRegenStats.setText(0, 1, MathUtil.floatToString(unit.stats.shieldRegen(), 1) + TextRenderable.SHIELD_REGEN_ICON.display)
                                .setText(2, 1, unit.getActionCostText(Action.SHIELD_REGEN) + TextRenderable.ENERGY_ICON.display);
                    }
                });
                return tooltip;
            });
            buttonHandler.addTooltip(6, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 1.5f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox moveStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, "Max " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName() + ":")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, EnergyManager.displayName + " Cost Multiplier:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Engines").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT,
                                "A unit can only move to tiles where the total " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " of the tiles along its path is lower than the unit's max " +
                                        ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + ".\n\nDifferent tile types have different " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + ".")
                        .addSpace(0.3f, 0)
                        .addBox(moveStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    moveStats.setText(0, 1, MathUtil.floatToString(unit.stats.maxMovement(), 1) + TextRenderable.MOVE_ICON.display)
                            .setText(2, 1, MathUtil.floatToString(unit.stats.movementCostMultiplier(), 1) + "x" + TextRenderable.ENERGY_ICON.display);
                });
                return tooltip;
            });
            buttonHandler.addTooltip(8, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 0.5f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox weaponEffectiveness = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, ShipClass.FIGHTER.icon.display + ShipClass.FIGHTER.getName() + "s:")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, ShipClass.CORVETTE.icon.display + ShipClass.CORVETTE.getName() + "s:")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, ShipClass.CRUISER.icon.display + ShipClass.CRUISER.getName() + "s:")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, ShipClass.CAPITAL_SHIP.icon.display + ShipClass.CAPITAL_SHIP.getName() + "s:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                UIDisplayBox weaponStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, "Base " + ModifierCategory.DAMAGE.getName() + ":")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.FIRING_RANGE.getName() + ":")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, EnergyManager.displayName + " Cost:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Weapons").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "This unit has a weapons system that it can use to attack enemy units.")
                        .addSpace(0.6f, 0)
                        .addText(0.8f, HorizontalAlign.LEFT, "Effectiveness:")
                        .addSpace(0.3f, 0)
                        .addBox(weaponEffectiveness, HorizontalAlign.CENTER, 0, false)
                        .addSpace(0.5f, 0)
                        .addText(0.8f, HorizontalAlign.LEFT, "Characteristics:")
                        .addSpace(0.3f, 0)
                        .addBox(weaponStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    if (unit.weapons.isEmpty())
                        return;
                    for (ShipClass value : ShipClass.values()) {
                        WeaponEffectiveness e = WeaponEffectiveness.againstClass(unit, value);
                        int index = switch (value) {
                            case FIGHTER -> 0;
                            case CORVETTE -> 2;
                            case CRUISER -> 4;
                            case CAPITAL_SHIP -> 6;
                        };
                        weaponEffectiveness.getText(index, 0).setTextColour(e.boxColourGray().borderColour);
                        weaponEffectiveness.getText(index, 1).setTextColour(e.boxColourGray().borderColour)
                                .updateText(e.name);
                    }
                    weaponStats.setText(0, 1, MathUtil.floatToString(unit.stats.baseDamage(), 1) + TextRenderable.DAMAGE_ICON.display)
                            .setText(2, 1, unit.stats.getRangeText())
                            .setText(4, 1, unit.getActionCostText(Action.FIRE) + TextRenderable.ENERGY_ICON.display);
                });
                return tooltip;
            });
            buttonHandler.addTooltip(10, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 3.5f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox ammoStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.AMMO_CAPACITY.getName() + ":")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Ammunition").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT,
                                "This unit has a weapons system that requires ammo to use. Once the ammo runs out, the weapons cannot be fired.\n\n" +
                                        "Ammo can be resupplied by certain structures if the unit is on the same tile at the start of the turn, as well " +
                                        "as by units which have the " + Action.RESUPPLY.colouredIconName(StyleElement.NO_COLOUR, false) + " action.")
                        .addSpace(0.3f, 0)
                        .addBox(ammoStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    ammoStats.setText(0, 1, unit.stats.ammoCapacity() + TextRenderable.AMMO_ICON.display);
                });
                return tooltip;
            });
            buttonHandler.addTooltip(12, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 2f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox repairStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.REPAIR.getName() + ":")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, EnergyManager.displayName + " Cost:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Repair").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT,
                                "This unit has the " + Action.REPAIR.colouredIconName(StyleElement.NO_COLOUR, false) + " action, " +
                                        "which allows it to repair the HP of allied units that are adjacent to this unit.")
                        .addSpace(0.3f, 0)
                        .addBox(repairStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    if (!unit.data.type.canPerformAction(Action.REPAIR))
                        return;
                    repairStats.setText(0, 1, MathUtil.floatToString(unit.stats.repair(), 1) + TextRenderable.REPAIR_ICON.display)
                            .setText(2, 1, unit.getActionCostText(Action.REPAIR) + TextRenderable.ENERGY_ICON.display);
                });
                return tooltip;
            });
            buttonHandler.addTooltip(14, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 2f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox resupplyStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, EnergyManager.displayName + " Cost:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Resupply").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT,
                                "This unit has the " + Action.RESUPPLY.colouredIconName(StyleElement.NO_COLOUR, false) + " action, " +
                                        "which allows it to fully replenish the ammunition of allied units that are adjacent to this unit.")
                        .addSpace(0.3f, 0)
                        .addBox(resupplyStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    if (!unit.data.type.canPerformAction(Action.RESUPPLY))
                        return;
                    resupplyStats.setText(0, 1, unit.getActionCostText(Action.RESUPPLY) + TextRenderable.ENERGY_ICON.display);
                });
                return tooltip;
            });
            buttonHandler.addTooltip(16, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 0.5f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox stealthStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, "Activation Cost:")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "Cost While Active:")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.3f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Stealth").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT,
                                "This unit has the " + Action.STEALTH.colouredIconName(StyleElement.NO_COLOUR, false) + " action, " +
                                        "which allows it to hide from enemy units. When in stealth mode, it is only visible to directly adjacent enemy units, but loses the ability to fire its weapons.\n\n" +
                                        "Units with this action do not have access to the " + Action.CAPTURE.colouredIconName(StyleElement.NO_COLOUR, false) + " action.")
                        .addSpace(0.6f, 0)
                        .addText(0.8f, HorizontalAlign.LEFT, EnergyManager.displayName + " Costs:")
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT, "The " + Action.STEALTH.colouredIconName(StyleElement.NO_COLOUR, false) + " action has two " + EnergyManager.displayName +
                                " costs. The first is a one-time cost for activating stealth mode, and the other is a per-turn cost which reduces income while stealth mode is active. Exiting stealth mode has no cost.")
                        .addSpace(0.3f, 0)
                        .addBox(stealthStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    if (!unit.data.type.canPerformAction(Action.STEALTH))
                        return;
                    stealthStats
                            .setText(0, 1, unit.getActionCostText(Action.STEALTH) + TextRenderable.ENERGY_ICON.display)
                            .setText(2, 1, unit.getPerTurnActionCostText(Action.STEALTH) + TextRenderable.ENERGY_ICON.display + " / Turn");
                });

                return tooltip;
            });
            buttonHandler.addTooltip(18, 0, true).add(button -> {
                UIStaticTooltip tooltip = new UIStaticTooltip(12, 2f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                UIDisplayBox miningStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                        .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.MINING_INCOME.getName() + ":")
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                tooltip.addText(1, HorizontalAlign.LEFT, "Mining").setColumnVerticalAlign(0, VerticalAlign.TOP)
                        .addSpace(0.3f, 0)
                        .addText(0.6f, HorizontalAlign.LEFT,
                                "This unit has the " + Action.MINE.colouredIconName(StyleElement.NO_COLOUR, false) + " action, " +
                                        "which allows it to mine " + TileType.ASTEROIDS.getName().toLowerCase() + " tiles. To do this, the unit must be positioned on an " + TileType.ASTEROIDS.getName().toLowerCase() + " tile.\n\n" +
                                        "While mining, the " + EnergyManager.displayName + " income received at the start of the turn is increased. After a few turns the " + TileType.ASTEROIDS.getName().toLowerCase() + " will deplete, turning the tile into an " + TileType.EMPTY.getName().toLowerCase() + " tile.")
                        .addSpace(0.3f, 0)
                        .addBox(miningStats, HorizontalAlign.CENTER, 0, false);
                tooltip.setOnStartRender(() -> {
                    Unit unit = getUnit(level);
                    if (!unit.data.type.canPerformAction(Action.MINE))
                        return;
                    miningStats.setText(0, 1, unit.getPerTurnActionCostText(Action.MINE) + TextRenderable.ENERGY_ICON.display + " / Turn");
                });
                return tooltip;
            });

            new RenderElement(r, RenderOrder.LEVEL_UI,
                    g -> {
                        GameRenderer.renderTransformed(g, () -> {
                            Unit unit = getUnit(level);
                            g.translate(0.5, 0.5);
                            box.render(g);
                            GameRenderer.renderOffset(0, HEIGHT - 10, g, () -> {
                                Renderable.renderImage(unit.data.type.getImage(unit.data.team, UnitPose.INFO), false, true, 11).render(g);
                            });
                            GameRenderer.renderOffset(-0.2f, HEIGHT + 0.5f, g, () -> {
                                title.updateTextLeft(unit.data.type.shipClass.icon.display + unit.data.type.getName());
                                title.render(g);
                            });
                            hp.getBar(2, 0).setSegments((int) unit.stats.maxHP()).setFill(unit.data.renderHP);
                            hp.setText(0, 1, MathUtil.floatToString(unit.data.renderHP, 1) + " / " + MathUtil.floatToString(unit.stats.maxHP(), 1) + TextRenderable.HP_ICON.display);

                            boolean hasShield = unit.stats.maxShieldHP() != 0;
                            systems.box.setElementEnabled(hasShield, 3, 0);
                            systems.box.setElementEnabled(hasShield, 4, 0);
                            if (hasShield) {
                                shield.getBar(2, 0).setSegments((int) unit.stats.maxShieldHP()).setFill(unit.data.shieldRenderHP);
                                shield.setText(0, 1, MathUtil.floatToString(unit.data.shieldRenderHP, 1) + " / " + MathUtil.floatToString(unit.stats.maxShieldHP(), 1) + TextRenderable.SHIELD_ICON.display);
                            }

                            movement.setText(0, 1, MathUtil.floatToString(unit.stats.maxMovement(), 1) + TextRenderable.MOVE_ICON.display);

                            boolean hasWeapons = !unit.weapons.isEmpty(), consumesAmmo = unit.stats.consumesAmmo();
                            systems.box.setElementEnabled(hasWeapons, 7, 0);
                            systems.box.setElementEnabled(hasWeapons, 8, 0);
                            systems.box.setElementEnabled(consumesAmmo, 9, 0);
                            systems.box.setElementEnabled(consumesAmmo, 10, 0);
                            if (hasWeapons) {
                                weapons.setText(0, 1, MathUtil.floatToString(unit.stats.baseDamage(), 1) + TextRenderable.DAMAGE_ICON.display);
                                boolean nonStandardRange = unit.stats.hasNonStandardRange();
                                weapons.setElementEnabled(nonStandardRange, 1, 1);
                                weapons.setElementEnabled(nonStandardRange, 2, 1);
                                StringBuilder s = new StringBuilder();
                                for (ShipClass value : ShipClass.values()) {
                                    WeaponEffectiveness e = WeaponEffectiveness.againstClass(unit, value);
                                    s.append(e.textColourGray().display).append(value.icon.display);
                                }
                                weapons.setText(4, 1, s.toString());
                                if (nonStandardRange)
                                    weapons.setText(2, 1, unit.stats.getRangeText());
                                if (consumesAmmo) {
                                    ammo.setText(0, 1, ((unit.data.ammo == 1 && unit.stats.ammoCapacity() > 1) || unit.data.ammo == 0 ? StyleElement.RED.display : "") + unit.data.ammo + " / " + unit.stats.ammoCapacity() + TextRenderable.AMMO_ICON.display);
                                }
                            }

                            boolean hasRepair = unit.data.type.canPerformAction(Action.REPAIR);
                            systems.box.setElementEnabled(hasRepair, 11, 0);
                            systems.box.setElementEnabled(hasRepair, 12, 0);
                            if (hasRepair) {
                                repair.setText(0, 1, MathUtil.floatToString(unit.stats.repair(), 1) + TextRenderable.REPAIR_ICON.display);
                            }

                            boolean hasResupply = unit.data.type.canPerformAction(Action.RESUPPLY);
                            systems.box.setElementEnabled(hasResupply, 13, 0);
                            systems.box.setElementEnabled(hasResupply, 14, 0);

                            boolean hasStealth = unit.data.type.canPerformAction(Action.STEALTH);
                            systems.box.setElementEnabled(hasStealth, 15, 0);
                            systems.box.setElementEnabled(hasStealth, 16, 0);

                            boolean hasMining = unit.data.type.canPerformAction(Action.MINE);
                            systems.box.setElementEnabled(hasMining, 17, 0);
                            systems.box.setElementEnabled(hasMining, 18, 0);
                            if (hasMining) {
                                mining.setText(0, 1, "+" + -unit.stats.getPerTurnActionCost(Action.MINE).get() + TextRenderable.ENERGY_ICON.display + " / Turn");
                            }
                        });
                    }).setZOrder(-1);
            infoButton = new UIShapeButton(r, b, RenderOrder.LEVEL_UI, 9.5f, HEIGHT - 1.5f, 1.5f, 1.5f, false)
                    .setShape(UIShapeButton::i).drawShape(0.25f).setBoxCorner(0.3f).setOnClick(() -> {
                        Unit unit = getUnit(level);
                        if (unit != null)
                            level.levelRenderer.unitInfoScreen.enable(unit);
                    }).tooltip(t -> t.add(-1, AbstractUITooltip.dark(), "Click to view detailed unit info"));
            viewFiringRange = new UIShapeButton(r, b, RenderOrder.LEVEL_UI, 7.5f, HEIGHT - 1.5f, 1.5f, 1.5f, true) {
                @Override
                public boolean selectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.ACTION_DESELECT) && TutorialManager.isEnabled(TutorialElement.VIEW_FIRING_RANGE);
                }

                @Override
                public boolean deselectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.VIEW_FIRING_RANGE_DESELECT);
                }
            }
                    .textRenderable(TextRenderable.RANGE_ICON, 1).setColourTheme(Modifier.createBackgroundTheme(StyleElement.MODIFIER_FIRING_RANGE).backgroundModifier(c -> UIColourTheme.applyAlpha(c, 1.5f))).setBoxCorner(0.3f).setOnClick(() -> {
                        Unit unit = getUnit(level);
                        if (unit == null || level.getActiveTeam() != level.getThisTeam())
                            return;
                        closeEffectivenessView();
                        level.endAction();
                        TileSet tiles = unit.stats.tilesInFiringRange(level.currentVisibility, unit.data, false);
                        MultiColourHighlight highlight = new MultiColourHighlight(tiles, p -> Action.FIRE.tileColour, 1, FIRE_TILE_BORDER_COLOUR);
                        highlight.setOnFinish(() -> {
                            level.levelRenderer.removeTileHighlight(highlight);
                            if (highlight == rangeHighlight)
                                viewFiringRange.deselect();
                        });
                        rangeHighlight = highlight;
                        level.levelRenderer.registerTileHighlight(highlight, true);
                        TutorialManager.acceptEvent(new EventUISelect(level, UIElement.VIEW_FIRING_RANGE));
                    }).setOnDeselect(this::closeFiringRangeView).toggleMode()
                    .tooltip(t -> t.add(-1, AbstractUITooltip.dark(), "Click to view unit " + ModifierCategory.FIRING_RANGE.getName().toLowerCase()));
            viewEffectiveness = new UIShapeButton(r, b, RenderOrder.LEVEL_UI, 5.5f, HEIGHT - 1.5f, 1.5f, 1.5f, true) {
                @Override
                public boolean selectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.ACTION_DESELECT) && TutorialManager.isEnabled(TutorialElement.VIEW_EFFECTIVENESS);
                }

                @Override
                public boolean deselectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.VIEW_EFFECTIVENESS_DESELECT);
                }
            }
                    .setShape(UIShapeButton::target).drawShape(0.12f).setColourTheme(Modifier.DAMAGE_BACKGROUND.backgroundModifier(c -> UIColourTheme.applyAlpha(c, 1.5f))).setBoxCorner(0.3f).setOnClick(() -> {
                        Unit unit = getUnit(level);
                        if (unit == null || level.getActiveTeam() != level.getThisTeam())
                            return;
                        closeFiringRangeView();
                        level.endAction();
                        TileSet tiles = TileSet.all(level).m(level, t -> t.unitFilter(TileModifier.withVisibleEnemies(unit.data.team, level)));
                        MultiColourHighlight highlight = new MultiColourHighlight(tiles, p -> unit.getWeaponEffectivenessAgainst(level.getUnit(p)), 0.25f, new Color(0, 0, 0, 0));
                        highlight.setOnFinish(() -> {
                            level.levelRenderer.removeTileHighlight(highlight);
                            if (highlight == effectivenessHighlight)
                                viewEffectiveness.deselect();
                        });
                        effectivenessHighlight = highlight;
                        level.levelRenderer.registerTileHighlight(highlight, true);
                        level.levelRenderer.weaponEffectivenessInfo.setEnabled(true);
                        TutorialManager.acceptEvent(new EventUISelect(level, UIElement.VIEW_EFFECTIVENESS));
                    }).setOnDeselect(this::closeEffectivenessView).toggleMode()
                    .tooltip(t -> t.add(12, AbstractUITooltip.dark(), "Click to view this unit's weapon effectiveness against enemies. The colour depends on which weapon effectiveness modifier would be applied when attacking.\n\nPress middle mouse while weapon effectiveness view is active to view colour coding."));
            new OnButtonInput(b, RenderOrder.LEVEL_UI, type -> type.isCharInput, type -> {
                switch (type.c) {
                    case 'c' -> {
                        if (viewFiringRange.isSelected())
                            viewFiringRange.deselect();
                        else
                            viewFiringRange.select();
                    }
                    case 'x' -> {
                        if (viewEffectiveness.isSelected())
                            viewEffectiveness.deselect();
                        else
                            viewEffectiveness.select();
                    }
                    default -> {
                        viewEffectiveness.deselect();
                        viewFiringRange.deselect();
                    }
                }
            });
        });
    }

    private static Unit getUnit(Level level) {
        return level.selectedUnit;
    }

    public void closeFiringRangeView() {
        if (rangeHighlight != null && viewFiringRange.deselectEnabled()) {
            rangeHighlight.close();
            TutorialManager.acceptEvent(new EventUIDeselect(level, UIElement.VIEW_FIRING_RANGE));
        }
    }

    public void closeEffectivenessView() {
        if (effectivenessHighlight != null && viewEffectiveness.deselectEnabled()) {
            effectivenessHighlight.close();
            level.levelRenderer.weaponEffectivenessInfo.setEnabled(false);
            TutorialManager.acceptEvent(new EventUIDeselect(level, UIElement.VIEW_EFFECTIVENESS));
        }
    }

    @Override
    public boolean isEnabled() {
        return getUnit(level) != null && getUnit(level).renderVisible() && super.isEnabled();
    }

    @Override
    public void delete() {
        super.delete();
        renderable = null;
        level = null;
        viewFiringRange = null;
        hp = null;
        shield = null;
        movement = null;
        weapons = null;
        ammo = null;
        repair = null;
        resupply = null;
        stealth = null;
        mining = null;
    }

    @Override
    public boolean posInsideLevelOffset(ObjPos pos) {
        return hitBox.isPositionInside(pos);
    }

    @Override
    public boolean blocking(InputType type) {
        return super.blocking(type) || type.isMouseInput();
    }

    public boolean showFiringRange() {
        return rangeHighlight != null && !rangeHighlight.finished();
    }

    public boolean showEffectiveness() {
        return effectivenessHighlight != null && !effectivenessHighlight.finished();
    }
}

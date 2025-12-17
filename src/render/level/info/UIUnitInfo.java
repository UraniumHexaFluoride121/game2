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
import render.types.container.UIScrollSurface;
import render.types.input.button.UIShapeButton;
import render.types.text.*;
import unit.ShipClass;
import unit.Unit;
import unit.UnitPose;
import unit.action.Action;
import unit.stats.attribute.UnitAttribute;
import unit.stats.modifiers.types.ModifierCategory;
import unit.weapon.WeaponEffectiveness;

import java.awt.*;
import java.util.function.BiFunction;

import static render.UIColourTheme.*;
import static unit.Unit.*;

public class UIUnitInfo extends LevelUIContainer<Level> {
    public static final float HEIGHT = 16;
    private final UITextLabel title = new UITextLabel(11f, 0.9f, true).setTextLeftBold();
    private UIDisplayBox hp, shield, movement, weapons, ammo, repair, resupply, stealth, mining, defenceNetwork;
    private final UIBox boxBackground = new UIBox(11, HEIGHT).setColourTheme(LIGHT_BLUE_BOX).centerOnly();
    private final UIBox boxBorder = new UIBox(11, HEIGHT).setColourTheme(LIGHT_BLUE_BOX).borderOnly();
    private final HitBox hitBox = HitBox.createFromOriginAndSize(0.5f, 0.5f, 11, HEIGHT);
    private Level level;
    public UIShapeButton viewFiringRange, viewEffectiveness;
    private MultiColourHighlight rangeHighlight = null, effectivenessHighlight = null;
    private UIScrollSurface scrollSurface;
    private UIDisplayBoxRenderElement systems;
    private boolean scrollToTop = true;

    public UIUnitInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, Level level) {
        super(register, buttonRegister, order, 0, 0, level);
        this.level = level;
        addRenderables((r, b) -> {
            scrollSurface = new UIScrollSurface(r, b, RenderOrder.LEVEL_UI, 0.5f, 0.5f, 11, HEIGHT - 7.8f, false, (r2, b2) -> {
                systems = new UIDisplayBoxRenderElement(r2, RenderOrder.LEVEL_UI, 0, 0, 11, -1, uiBox -> {
                }, false);
                systems.box.setRenderBox(false);
                hp = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(ModifierCategory.HP_GREEN), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Hull")
                        .addSpace(0.2f, 0)
                        .addBar(new UIHitPointBar(0f, 1, 0.5f, 0.15f, 1, ModifierCategory.HP_GREEN).barOnly(), HorizontalAlign.CENTER, 0, true)
                        .addSpace(0.1f, 0)
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                        .setColumnVerticalAlign(1, VerticalAlign.TOP)
                        .addOnUpdate(() -> {
                            UIHitPointBar bar = hp.getBar(2, 0);
                            float fill = bar.getRenderFill() / bar.getSegments();
                            UIColourTheme theme;
                            if (fill < 0.35f) {
                                theme = ModifierCategory.HP_RED;
                            } else if (fill < 0.75f) {
                                theme = ModifierCategory.HP_YELLOW;
                            } else
                                theme = ModifierCategory.HP_GREEN;
                            bar.setColour(theme);
                            hp.modifyBox(uiBox -> uiBox.setColourTheme(theme));
                        });
                shield = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(ModifierCategory.SHIELD_HP.colour), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Shield")
                        .addSpace(0.2f, 0)
                        .addBar(new UIHitPointBar(0f, 1, 0.5f, 0.15f, 1, ModifierCategory.SHIELD_HP.colour).barOnly(), HorizontalAlign.CENTER, 0, true)
                        .addSpace(0.1f, 0)
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                        .setColumnVerticalAlign(1, VerticalAlign.TOP);
                movement = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(ModifierCategory.MOVEMENT_SPEED_DISPLAY.colour), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Engines")
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
                weapons = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(ModifierCategory.DAMAGE_BACKGROUND), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Weapons")
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.15f, 1)
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, null)
                        .addSpace(0.15f, 1)
                        .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                ammo = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(ModifierCategory.DAMAGE_BACKGROUND), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Ammunition")
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
                repair = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Action.REPAIR.colourTheme), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Repair")
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
                resupply = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Action.RESUPPLY.colourTheme), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Resupply");
                stealth = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Action.STEALTH.colourTheme), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Stealth");
                mining = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(Action.MINE.colourTheme), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, "Mining")
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, null);
                defenceNetwork = new UIDisplayBox(0, 0, 10, -1, uiBox -> uiBox.setColourTheme(UnitAttribute.DEFENCE_NETWORK.boxTheme), false)
                        .setWidthMargin(0.2f)
                        .addText(0.7f, HorizontalAlign.LEFT, UnitAttribute.DEFENCE_NETWORK.getName())
                        .addText(0.7f, HorizontalAlign.RIGHT, 1, UnitAttribute.DEFENCE_NETWORK.getIcon());
                //systems.box.addText(0.6f, HorizontalAlign.CENTER, "Hover for more info").getText().setItalic(true);
                systems.box.setColumnVerticalAlign(0, VerticalAlign.TOP);

                systems.box.addBox(hp, HorizontalAlign.CENTER, 0, false);
                int boxHP = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(shield, HorizontalAlign.CENTER, 0, false);
                int boxShield = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(movement, HorizontalAlign.CENTER, 0, false);
                int boxMovement = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(weapons, HorizontalAlign.CENTER, 0, false);
                int boxWeapons = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(ammo, HorizontalAlign.CENTER, 0, false);
                int boxAmmo = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(repair, HorizontalAlign.CENTER, 0, false);
                int boxRepair = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(resupply, HorizontalAlign.CENTER, 0, false);
                int boxResupply = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(stealth, HorizontalAlign.CENTER, 0, false);
                int boxStealth = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(mining, HorizontalAlign.CENTER, 0, false);
                int boxMining = systems.box.getLastIndex(0);

                systems.box.addSpace(0.3f, 0);
                systems.box.addBox(defenceNetwork, HorizontalAlign.CENTER, 0, false);
                int boxDefenceNetwork = systems.box.getLastIndex(0);

                UIDisplayBoxButtonHandler buttonHandler = new UIDisplayBoxButtonHandler(r, b2, RenderOrder.LEVEL_UI, systems.box);
                buttonHandler.addTooltip(boxHP, 0, true).add(button -> {
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
                            .addBar(new UIHitPointBar(0.1f, 1, 1f, 0.2f, 1, ModifierCategory.HP_GREEN).setRounding(0.6f), HorizontalAlign.CENTER, 0, true)
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
                            theme = ModifierCategory.HP_RED;
                        } else if (fill < 0.75f) {
                            theme = ModifierCategory.HP_YELLOW;
                        } else
                            theme = ModifierCategory.HP_GREEN;
                        bar.setColour(theme);
                        hpStats.setText(0, 1, unit.data.type.shipClass.icon.display + unit.data.type.shipClass.getName());
                        hpStats.setText(2, 1, cardModifiedDisplayBoth(unit.stats.baseMaxHP(), unit.stats.maxHP(), 1, UIUnitInfo::moreIsBetter) + TextRenderable.HP_ICON.display);
                        viewRangeStats.setText(0, 1, MathUtil.floatToString(unit.stats.maxViewRange(), 1) + TextRenderable.VIEW_RANGE_ICON.display);
                    });
                    return tooltip;
                });
                buttonHandler.addTooltip(boxShield, 0, true).add(button -> {
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
                            .addBar(new UIHitPointBar(0.1f, 1, 1f, 0.2f, 1, ModifierCategory.SHIELD_HP.colour).setRounding(0.6f), HorizontalAlign.CENTER, 0, true)
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
                                    .setText(2, 1, cardModifiedActionDisplayBoth(unit, Action.SHIELD_REGEN, false) + TextRenderable.ENERGY_ICON.display);
                        }
                    });
                    return tooltip;
                });
                buttonHandler.addTooltip(boxMovement, 0, true).add(button -> {
                    UIStaticTooltip tooltip = new UIStaticTooltip(12, 1.5f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                    UIDisplayBox moveStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                            .addText(0.6f, HorizontalAlign.LEFT, "Max " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName() + ":")
                            .addSpace(0.3f, 0)
                            .addText(0.6f, HorizontalAlign.LEFT, EnergyManager.displayName + " Cost Multi.:")
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
                buttonHandler.addTooltip(boxWeapons, 0, true).add(button -> {
                    UIStaticTooltip tooltip = new UIStaticTooltip(12, 0.5f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                    UIDisplayBox weaponEffectiveness = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                            .addText(0.6f, HorizontalAlign.LEFT, ShipClass.FIGHTER.icon.display + ShipClass.FIGHTER.getName() + "s:")
                            .addSpace(0.3f, 0)
                            .addText(0.6f, HorizontalAlign.LEFT, ShipClass.CORVETTE.icon.display + ShipClass.CORVETTE.getName() + "s:")
                            .addSpace(0.3f, 0)
                            .addText(0.6f, HorizontalAlign.LEFT, ShipClass.CRUISER.icon.display + ShipClass.CRUISER.getName() + "s:")
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
                            .addText(0.8f, HorizontalAlign.LEFT, ModifierCategory.WEAPON_EFFECTIVENESS_SHORT.getName() + ":")
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
                            };
                            weaponEffectiveness.getText(index, 0).setTextColour(e.boxColourGray().borderColour);
                            weaponEffectiveness.getText(index, 1).setTextColour(e.boxColourGray().borderColour)
                                    .updateText(e.name);
                        }
                        weaponStats.setText(0, 1, cardModifiedDisplayBoth(unit.stats.baseDamage(), unit.stats.attackDamage(), 1, UIUnitInfo::moreIsBetter) + TextRenderable.DAMAGE_ICON.display)
                                .setText(2, 1, unit.stats.getRangeText())
                                .setText(4, 1, cardModifiedActionDisplayBoth(unit, Action.FIRE, false) + TextRenderable.ENERGY_ICON.display);
                    });
                    return tooltip;
                });
                buttonHandler.addTooltip(boxAmmo, 0, true).add(button -> {
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
                buttonHandler.addTooltip(boxRepair, 0, true).add(button -> {
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
                                .setText(2, 1, cardModifiedActionDisplayBoth(unit, Action.REPAIR, false) + TextRenderable.ENERGY_ICON.display);
                    });
                    return tooltip;
                });
                buttonHandler.addTooltip(boxResupply, 0, true).add(button -> {
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
                        resupplyStats.setText(0, 1, cardModifiedActionDisplayBoth(unit, Action.RESUPPLY, false) + TextRenderable.ENERGY_ICON.display);
                    });
                    return tooltip;
                });
                buttonHandler.addTooltip(boxStealth, 0, true).add(button -> {
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
                                .setText(0, 1, cardModifiedActionDisplayBoth(unit, Action.STEALTH, false) + TextRenderable.ENERGY_ICON.display)
                                .setText(2, 1, cardModifiedActionDisplayBoth(unit, Action.STEALTH, true) + TextRenderable.ENERGY_ICON.display + " / Turn");
                    });

                    return tooltip;
                });
                buttonHandler.addTooltip(boxMining, 0, true).add(button -> {
                    UIStaticTooltip tooltip = new UIStaticTooltip(12, 2f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                    UIDisplayBox miningStats = new UIDisplayBox(0, 0, 11, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX_DARK), false)
                            .addText(0.6f, HorizontalAlign.LEFT, ModifierCategory.MINING_INCOME.getName() + ":")
                            .addText(0.6f, HorizontalAlign.RIGHT, 1, null);
                    tooltip.addText(1, HorizontalAlign.LEFT, "Mining").setColumnVerticalAlign(0, VerticalAlign.TOP)
                            .addSpace(0.3f, 0)
                            .addText(0.6f, HorizontalAlign.LEFT,
                                    "This unit has the " + Action.MINE.colouredIconName(StyleElement.NO_COLOUR, false) + " action, " +
                                            "which allows it to mine " + TileType.ASTEROIDS.getName().toLowerCase() + " tiles. To do this, the unit must be positioned on an " + TileType.ASTEROIDS.getName().toLowerCase() + " tile.\n\n" +
                                            "While mining, the " + EnergyManager.displayName + " income received at the start of the turn is increased. After a few turns the " + TileType.ASTEROIDS.getName().toLowerCase() + " will deplete, turning the tile into " + TileType.EMPTY.getNameArticle().toLowerCase() + " tile.")
                            .addSpace(0.3f, 0)
                            .addBox(miningStats, HorizontalAlign.CENTER, 0, false);
                    tooltip.setOnStartRender(() -> {
                        Unit unit = getUnit(level);
                        if (!unit.data.type.canPerformAction(Action.MINE))
                            return;
                        miningStats.setText(0, 1, cardModifiedActionDisplayBoth(unit, Action.MINE, false) + TextRenderable.ENERGY_ICON.display + " / Turn");
                    });
                    return tooltip;
                });
                buttonHandler.addTooltip(boxDefenceNetwork, 0, true).add(button -> {
                    UIStaticTooltip tooltip = new UIStaticTooltip(12, 2f, 12, -1, uiBox -> uiBox.setColourTheme(LIGHT_BLUE_BOX), false, button);
                    tooltip.addText(1, HorizontalAlign.LEFT, UnitAttribute.DEFENCE_NETWORK.getName()).setColumnVerticalAlign(0, VerticalAlign.TOP)
                            .addSpace(0.3f, 0)
                            .addText(0.6f, HorizontalAlign.LEFT, UnitAttribute.DEFENCE_NETWORK.description);
                    return tooltip;
                });
                new RenderElement(r, RenderOrder.LEVEL_UI, g -> {
                    g.translate(0.5, 0.5);
                    boxBorder.render(g);
                }).setZOrder(5);
                new RenderElement(r, RenderOrder.LEVEL_UI,
                        g -> {
                            GameRenderer.renderTransformed(g, () -> {
                                Unit unit = getUnit(level);
                                g.translate(0.5, 0.5);
                                boxBackground.render(g);
                                GameRenderer.renderOffset(0, HEIGHT - 10, g, () -> {
                                    Renderable.renderImage(unit.data.type.getImage(unit.data.team, UnitPose.INFO, unit.data.stealthMode), false, true, 11).render(g);
                                });
                                GameRenderer.renderOffset(-0.2f, HEIGHT + 0.5f, g, () -> {
                                    title.updateTextLeft(unit.data.type.shipClass.icon.display + unit.data.type.getName());
                                    title.render(g);
                                });
                                hp.getBar(2, 0).setSegments((int) unit.stats.maxHP()).setFill(unit.data.renderHP);
                                hp.setText(0, 1, MathUtil.floatToString(unit.data.renderHP, 1) + " / " + MathUtil.floatToString(unit.stats.maxHP(), 1) + TextRenderable.HP_ICON.display);

                                boolean hasShield = unit.stats.maxShieldHP() != 0;
                                systems.box.setElementEnabled(hasShield, boxShield - 1, 0);
                                systems.box.setElementEnabled(hasShield, boxShield, 0);
                                if (hasShield) {
                                    shield.getBar(2, 0).setSegments((int) unit.stats.maxShieldHP()).setFill(unit.data.shieldRenderHP);
                                    shield.setText(0, 1, MathUtil.floatToString(unit.data.shieldRenderHP, 1) + " / " + MathUtil.floatToString(unit.stats.maxShieldHP(), 1) + TextRenderable.SHIELD_ICON.display);
                                }

                                movement.setText(0, 1, MathUtil.floatToString(unit.stats.maxMovement(), 1) + TextRenderable.MOVE_ICON.display);

                                boolean hasWeapons = !unit.weapons.isEmpty(), consumesAmmo = unit.stats.consumesAmmo();
                                systems.box.setElementEnabled(hasWeapons, boxWeapons - 1, 0);
                                systems.box.setElementEnabled(hasWeapons, boxWeapons, 0);
                                systems.box.setElementEnabled(consumesAmmo, boxAmmo - 1, 0);
                                systems.box.setElementEnabled(consumesAmmo, boxAmmo, 0);
                                if (hasWeapons) {
                                    weapons.setText(0, 1, MathUtil.floatToString(unit.stats.attackDamage(), 1) + TextRenderable.DAMAGE_ICON.display);
                                    boolean nonStandardRange = unit.stats.hasNonStandardRange();
                                    weapons.setElementEnabled(nonStandardRange, 1, 1);
                                    weapons.setElementEnabled(nonStandardRange, 2, 1);
                                    weapons.setText(4, 1, WeaponEffectiveness.effectivenessSummary(null, c -> WeaponEffectiveness.againstClass(unit, c)));
                                    if (nonStandardRange)
                                        weapons.setText(2, 1, unit.stats.getRangeText());
                                    if (consumesAmmo) {
                                        ammo.setText(0, 1, ((unit.data.ammo == 1 && unit.stats.ammoCapacity() > 1) || unit.data.ammo == 0 ? StyleElement.RED.display : "") + unit.data.ammo + " / " + unit.stats.ammoCapacity() + TextRenderable.AMMO_ICON.display);
                                    }
                                }

                                boolean hasRepair = unit.data.type.canPerformAction(Action.REPAIR);
                                systems.box.setElementEnabled(hasRepair, boxRepair - 1, 0);
                                systems.box.setElementEnabled(hasRepair, boxRepair, 0);
                                if (hasRepair) {
                                    repair.setText(0, 1, MathUtil.floatToString(unit.stats.repair(), 1) + TextRenderable.REPAIR_ICON.display);
                                }

                                boolean hasResupply = unit.data.type.canPerformAction(Action.RESUPPLY);
                                systems.box.setElementEnabled(hasResupply, boxResupply - 1, 0);
                                systems.box.setElementEnabled(hasResupply, boxResupply, 0);

                                boolean hasStealth = unit.data.type.canPerformAction(Action.STEALTH);
                                systems.box.setElementEnabled(hasStealth, boxStealth - 1, 0);
                                systems.box.setElementEnabled(hasStealth, boxStealth, 0);

                                boolean hasMining = unit.data.type.canPerformAction(Action.MINE);
                                systems.box.setElementEnabled(hasMining, boxMining - 1, 0);
                                systems.box.setElementEnabled(hasMining, boxMining, 0);
                                if (hasMining) {
                                    mining.setText(0, 1, cardModifiedActionDisplayBoth(unit, Action.MINE, false) + TextRenderable.ENERGY_ICON.display + " / Turn");
                                }

                                boolean hasDefenceNetwork = unit.stats.hasUnitAttribute(UnitAttribute.DEFENCE_NETWORK);
                                systems.box.setElementEnabled(hasDefenceNetwork, boxDefenceNetwork - 1, 0);
                                systems.box.setElementEnabled(hasDefenceNetwork, boxDefenceNetwork, 0);
                                systems.box.setY(-systems.box.height);
                                float newScrollMax = Math.max(0, systems.box.height - scrollSurface.height + 0.5f);
                                if (!MathUtil.equal(scrollSurface.getScrollMax(), newScrollMax, 0.01f)) {
                                    scrollSurface.setScrollMax(newScrollMax);
                                    scrollSurface.setScrollAmount(0);
                                }
                                if (scrollToTop) {
                                    scrollToTop = false;
                                    scrollSurface.setScrollAmount(0);
                                }
                            });
                        },
                        new TextRenderer("Hover for more info", 0.6f).setItalic(true).setTextAlign(HorizontalAlign.CENTER)
                                .translate(0.5f + 11 / 2f, 0.5f + HEIGHT - 7.7f))
                        .setZOrder(-1);
            });
            scrollSurface.setScrollSpeed(0.1f);
            viewFiringRange = new UIShapeButton(r, b, RenderOrder.LEVEL_UI, 9.5f, HEIGHT - 1.5f, 1.5f, 1.5f, true) {
                @Override
                public boolean selectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.ACTION_DESELECT) && TutorialManager.isEnabled(TutorialElement.VIEW_FIRING_RANGE);
                }

                @Override
                public boolean deselectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.VIEW_FIRING_RANGE_DESELECT);
                }
            }
                    .textRenderable(TextRenderable.RANGE_ICON, 1).setColourTheme(ModifierCategory.FIRING_RANGE.colour.backgroundModifier(c -> UIColourTheme.applyAlpha(c, 1.5f))).setBoxCorner(0.3f).setOnClick(() -> {
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
            viewEffectiveness = new UIShapeButton(r, b, RenderOrder.LEVEL_UI, 7.5f, HEIGHT - 1.5f, 1.5f, 1.5f, true) {
                @Override
                public boolean selectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.ACTION_DESELECT) && TutorialManager.isEnabled(TutorialElement.VIEW_EFFECTIVENESS);
                }

                @Override
                public boolean deselectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.VIEW_EFFECTIVENESS_DESELECT);
                }
            }
                    .setShape(UIShapeButton::target).drawShape(0.12f).setColourTheme(ModifierCategory.DAMAGE_BACKGROUND.backgroundModifier(c -> UIColourTheme.applyAlpha(c, 1.5f))).setBoxCorner(0.3f).setOnClick(() -> {
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
                    .tooltip(t -> t.add(12, AbstractUITooltip.dark(), "Click to view this unit's " + ModifierCategory.WEAPON_EFFECTIVENESS.getName().toLowerCase() + " against enemies. " +
                            "The colour depends on which " + ModifierCategory.WEAPON_EFFECTIVENESS.getName().toLowerCase() + " modifier would be applied when attacking.\n\n" +
                            "Press middle mouse while " + ModifierCategory.WEAPON_EFFECTIVENESS.getName().toLowerCase() + " view is active to view colour coding."));
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
        systems = null;
        scrollSurface = null;
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

    public void onTileSelected() {
        scrollToTop = true;
    }

    public String cardModifiedDisplayBoth(float base, float actual, int decimals, BiFunction<Float, Float, StyleElement> getColour) {
        if (MathUtil.equal(base, actual, 0.001f))
            return MathUtil.floatToString(base, decimals);
        String actualText = MathUtil.floatToString(actual, decimals);
        String baseText = "(" + MathUtil.floatToString(base, decimals) + ")";

        StyleElement s = getColour.apply(base, actual);
        return s == null ? actualText + baseText : s.display + actualText + " [NO_COLOUR]" + baseText;
    }

    public String cardModifiedActionDisplayBoth(Unit u, Action a, boolean perTurn) {
        int comparison = perTurn ? u.stats.isPerTurnModifiedActionCostBetterThanBase(a) : u.stats.isModifiedActionCostBetterThanBase(a);
        String actual = perTurn ? u.stats.getPerTurnActionCostText(a) : u.stats.getActionCostText(a);
        String base = perTurn ? u.stats.getBasePerTurnActionCostText(a) : u.stats.getBaseActionCostText(a);
        if (comparison == 0)
            return actual;
        String baseText = "(" + base + ")";
        StyleElement s = switch (comparison) {
            case 1 -> StyleElement.RED;
            case -1 -> StyleElement.GREEN;
            default -> throw new RuntimeException();
        };
        return s.display + actual + " [NO_COLOUR]" + baseText;
    }

    public String cardModifiedReplace(float base, float actual, int decimals, StyleElement end, BiFunction<Float, Float, StyleElement> getColour) {
        if (MathUtil.equal(base, actual, 0.001f))
            return MathUtil.floatToString(base, decimals);
        String actualText = MathUtil.floatToString(actual, decimals);
        StyleElement s = getColour.apply(base, actual);
        return s == null ? actualText : s.display + actualText + (end == null ? "" : end.display);
    }

    public static StyleElement moreIsBetter(float base, float actual) {
        if (MathUtil.equal(base, actual, 0.001f))
            return null;
        if (actual > base)
            return StyleElement.GREEN;
        else
            return StyleElement.RED;
    }

    public static StyleElement moreIsWorse(float base, float actual) {
        if (MathUtil.equal(base, actual, 0.001f))
            return null;
        if (actual > base)
            return StyleElement.RED;
        else
            return StyleElement.GREEN;
    }
}

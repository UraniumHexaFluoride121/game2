package unit.stats;

import render.types.text.StyleElement;
import render.types.text.TextRenderable;

import java.util.function.Function;

import static render.types.text.StyleElement.*;
import static render.types.text.TextRenderable.*;

public enum ModifierCategory implements ColouredName {
    DAMAGE("Damage", MODIFIER_DAMAGE, DAMAGE_ICON, 1, 1),
    INCOMING_DAMAGE("Incoming Damage", MODIFIER_INCOMING_DAMAGE, DAMAGE_ICON, 1, 1),
    SHIELD_DAMAGE("Shield Damage", MODIFIER_SHIELD_DAMAGE, null, 1, 1),
    INCOMING_SHIELD_DAMAGE("Incoming Shield Damage", MODIFIER_INCOMING_SHIELD_DAMAGE, null, 1, 1),

    //Additive
    REPAIR("Repair", MODIFIER_REPAIR, REPAIR_ICON, 0, 0),
    HP("Max HP", MODIFIER_HP, HP_ICON, 0, 0),
    SHIELD_HP("Max Shield HP", MODIFIER_SHIELD_HP, SHIELD_ICON, 0, 0),
    SHIELD_REGEN("Shield Regeneration", MODIFIER_SHIELD_REGEN, SHIELD_REGEN_ICON, 0, 0),
    FIRING_RANGE("Firing Range", MODIFIER_FIRING_RANGE, RANGE_ICON, 0, 0),
    AMMO_CAPACITY("Ammo Capacity", MODIFIER_AMMO_CAPACITY, AMMO_ICON, 0, 0),
    VIEW_RANGE("View Range", MODIFIER_VIEW_RANGE, VIEW_RANGE_ICON, 0, 0),
    MINING_INCOME("Mining Income", MODIFIER_MINING, ENERGY_ICON, 0, 0),

    //Action
    MINING("Mining", MODIFIER_MINING, ENERGY_ICON, 0, 0),

    //Display only
    MOVEMENT_SPEED_DISPLAY("Move Distance", MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1, 1),
    MOVEMENT_COST_DISPLAY("Move Cost", MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1, 1),

    MOVEMENT_COST_ALL("Move Cost (All)", MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1, 1),
    MOVEMENT_COST_EMPTY("Move Cost (Empty)", MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1, 1),
    MOVEMENT_COST_NEBULA("Move Cost (Nebula)", MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1, 1),
    MOVEMENT_COST_DENSE_NEBULA("Move Cost (Dense Nebula)", MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1, 1),
    MOVEMENT_COST_ASTEROIDS("Move Cost (Asteroids)", MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1, 1);

    private final String name;
    private final StyleElement textColour;
    private final TextRenderable icon;
    public final float defaultEffect, identity;

    ModifierCategory(String name, StyleElement textColour, TextRenderable icon, float defaultEffect, float identity) {
        this.name = name;
        this.textColour = textColour;
        this.icon = icon;
        this.defaultEffect = defaultEffect;
        this.identity = identity;
    }

    @Override
    public String getName() {
        return name;
    }

    public String icon() {
        return icon == null ? "" : icon.display;
    }

    @Override
    public String colour() {
        return textColour.display;
    }

    public String displayEffectName() {
        return "\n" + colouredName(null, false);
    }

    public String displayEffectValue(float effect, Function<Float, String> displayMethod) {
        return "\n" + colour() + displayMethod.apply(effect) + icon();
    }
}

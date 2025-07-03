package render.types.text;

import java.awt.*;
import java.util.function.BiFunction;

public enum StyleElement {
    NO_COLOUR(TextStyle::setColour, null),
    MODIFIER_GRAY(TextStyle::setColour, new Color(140, 140, 140)),
    MODIFIER_BLUE(TextStyle::setColour, new Color(51, 151, 211)),

    ENERGY_COST_GREEN(TextStyle::setColour, new Color(105, 214, 87)),
    ENERGY_COST_RED(TextStyle::setColour, new Color(214, 83, 83)),

    MODIFIER_RESUPPLY(TextStyle::setColour, new Color(161, 101, 37)),
    MODIFIER_VIEW_RANGE(TextStyle::setColour, new Color(45, 218, 152)),
    MODIFIER_FIRING_RANGE(TextStyle::setColour, new Color(228, 108, 67)),
    MODIFIER_DAMAGE(TextStyle::setColour, new Color(218, 45, 45)),
    MODIFIER_AMMO_CAPACITY(TextStyle::setColour, new Color(218, 45, 45)),
    MODIFIER_INCOMING_DAMAGE(TextStyle::setColour, new Color(213, 99, 99)),
    MODIFIER_SHIELD_DAMAGE(TextStyle::setColour, new Color(45, 218, 204)),
    MODIFIER_INCOMING_SHIELD_DAMAGE(TextStyle::setColour, new Color(45, 218, 204)),
    MODIFIER_MOVEMENT_SPEED(TextStyle::setColour, new Color(63, 131, 236)),
    MODIFIER_REPAIR(TextStyle::setColour, new Color(51, 218, 45)),
    MODIFIER_HP(TextStyle::setColour, new Color(51, 218, 45)),
    MODIFIER_SHIELD_HP(TextStyle::setColour, new Color(45, 218, 204)),
    MODIFIER_SHIELD_REGEN(TextStyle::setColour, new Color(45, 218, 204)),
    MODIFIER_MINING(TextStyle::setColour, new Color(134, 49, 213)),

    EMPTY_TILE(TextStyle::setColour, new Color(112, 112, 112)),
    NEBULA_TILE(TextStyle::setColour, new Color(156, 64, 182)),
    DENSE_NEBULA_TILE(TextStyle::setColour, new Color(120, 49, 186)),
    ASTEROID_TILE(TextStyle::setColour, new Color(145, 145, 145)),

    BOX_GRAY(TextStyle::setColour, new Color(191, 191, 191)),
    BLUE(TextStyle::setColour, new Color(46, 151, 220)),
    GREEN(TextStyle::setColour, new Color(57, 218, 59)),
    DARK_GREEN(TextStyle::setColour, new Color(40, 113, 42)),
    YELLOW(TextStyle::setColour, new Color(218, 186, 57)),
    ORANGE(TextStyle::setColour, new Color(218, 124, 57)),
    RESUPPLY(TextStyle::setColour, new Color(161, 101, 37)),
    RED(TextStyle::setColour, new Color(216, 70, 70));

    public final BiFunction<StyleElement, TextStyle, TextStyle> modifier;
    public final Color colour;
    public final String display;

    StyleElement(BiFunction<StyleElement, TextStyle, TextStyle> modifier, Color colour) {
        this.modifier = modifier;
        this.colour = colour;
        display = "[" + name() + "]";
    }
}

package unit.stats.modifiers;

import level.tile.TileType;
import render.UIColourTheme;
import unit.stats.Modifier;
import unit.stats.SingleModifier;

import java.awt.*;
import java.util.function.Function;

import static render.types.text.StyleElement.*;
import static unit.stats.ModifierCategory.*;

public abstract class TileSingleModifier {
    public static UIColourTheme NEBULA_PURPLE = new UIColourTheme(
            new Color(156, 64, 182), new Color(164, 58, 193, 144)
    );
    public static UIColourTheme DENSE_NEBULA_PURPLE = new UIColourTheme(
            new Color(121, 64, 182), new Color(128, 58, 193, 144)
    );
    public static UIColourTheme GRAY = new UIColourTheme(
            new Color(184, 184, 184), new Color(74, 74, 74, 200)
    );

    public static final Function<TileType, SingleModifier>
            EMPTY = type -> new SingleModifier(1f, "Empty Tile",
            "The enemy unit is currently on an " + type.colouredName(null, false) + " Tile" + NO_COLOUR.display +
                    ", which provides no " + INCOMING_DAMAGE.colouredName(NO_COLOUR, true) + " reduction for the enemy.",
            INCOMING_DAMAGE.displayEffectName(),
            INCOMING_DAMAGE.displayEffectValue(1f, Modifier::percentMultiplicative),
            INCOMING_DAMAGE, GRAY, Modifier.BLUE_BACKGROUND),

    NEBULA = type -> new SingleModifier(0.88f, "Nebula Tile",
            "The enemy unit is currently on a " + type.colouredName(null, false) + " Tile" + NO_COLOUR.display +
                    ", which slightly reduces " + INCOMING_DAMAGE.colouredName(NO_COLOUR, true) + " for the enemy.",
            INCOMING_DAMAGE.displayEffectName(),
            INCOMING_DAMAGE.displayEffectValue(0.88f, Modifier::percentMultiplicative),
            INCOMING_DAMAGE, NEBULA_PURPLE, Modifier.RED_BACKGROUND),

    DENSE_NEBULA = type -> new SingleModifier(0.82f, "Dense Nebula Tile",
            "The enemy unit is currently on a " + type.colouredName(null, false) + " Tile" + NO_COLOUR.display +
                    ", which reduces " + INCOMING_DAMAGE.colouredName(NO_COLOUR, true) + " for the enemy.",
            INCOMING_DAMAGE.displayEffectName(),
            INCOMING_DAMAGE.displayEffectValue(0.82f, Modifier::percentMultiplicative),
            INCOMING_DAMAGE, DENSE_NEBULA_PURPLE, Modifier.RED_BACKGROUND),

    ASTEROID_FIELD = type -> new SingleModifier(0.76f, "Asteroid Field Tile",
            "The enemy unit is currently on an " + type.colouredName(null, false) + " Tile" + NO_COLOUR.display +
                    ", which reduces " + INCOMING_DAMAGE.colouredName(NO_COLOUR, true) + " for the enemy.",
            INCOMING_DAMAGE.displayEffectName(),
            INCOMING_DAMAGE.displayEffectValue(0.76f, Modifier::percentMultiplicative),
            INCOMING_DAMAGE, GRAY, Modifier.RED_BACKGROUND);
}

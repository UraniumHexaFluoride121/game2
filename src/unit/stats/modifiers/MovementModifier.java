package unit.stats.modifiers;

import level.tile.TileType;
import render.UIColourTheme;
import render.types.text.StyleElement;
import unit.stats.EmptyModifier;
import unit.stats.Modifier;
import unit.stats.ModifierCategory;
import unit.stats.SingleModifier;

public abstract class MovementModifier {
    public static final EmptyModifier NO_ASTEROID_FIELDS = new EmptyModifier("Cannot Navigate " + TileType.ASTEROIDS.getName() + "s",
            "This unit is too slow to navigate " + TileType.ASTEROIDS.colouredName(StyleElement.NO_COLOUR, true) + " tiles.",
            StyleElement.MODIFIER_MOVEMENT_SPEED.display + "Unit cannot move through " + TileType.ASTEROIDS.getName() + " tiles",
            null,
            UIColourTheme.DEEP_RED, Modifier.RED_BACKGROUND);

    public static final EmptyModifier NORMAL_ASTEROID_FIELDS = new EmptyModifier("Normal " + TileType.ASTEROIDS.getName() + " Navigation",
            "This unit is fast enough to safely navigate " + TileType.ASTEROIDS.colouredName(StyleElement.NO_COLOUR, true) + " tiles, but doing so has a high " + ModifierCategory.MOVEMENT_COST_DISPLAY.colouredName(StyleElement.NO_COLOUR, true) + ".",
            StyleElement.MODIFIER_MOVEMENT_SPEED.display + "Unit can move through " + TileType.ASTEROIDS.getName() + " tiles normally",
            null,
            UIColourTheme.DEEP_YELLOW, Modifier.YELLOW_BACKGROUND);

    public static final SingleModifier FAST_ASTEROID_FIELDS = new SingleModifier(0.5f, "Improved " + TileType.ASTEROIDS.getName() + " Navigation",
            "This unit has exceptional manoeuvrability, allowing it to move through " + TileType.ASTEROIDS.colouredName(StyleElement.NO_COLOUR, true) + " tiles with a reduced " + ModifierCategory.MOVEMENT_COST_DISPLAY.colouredName(StyleElement.NO_COLOUR, true) + ".",
            ModifierCategory.MOVEMENT_COST_ASTEROIDS.displayEffectName(),
            ModifierCategory.MOVEMENT_COST_ASTEROIDS.displayEffectValue(0.5f, Modifier::percentMultiplicative),
            ModifierCategory.MOVEMENT_COST_ASTEROIDS,
            UIColourTheme.DEEP_GREEN, Modifier.GREEN_BACKGROUND);

    public static final SingleModifier MINING_ASTEROID_FIELDS = new SingleModifier(0.35f, TileType.ASTEROIDS.getName() + " Protection System",
            "This unit comes with special equipment that can clear a path through through " + TileType.ASTEROIDS.colouredName(null, true) + "s" + StyleElement.NO_COLOUR.display + ", greatly reducing " + ModifierCategory.MOVEMENT_COST_DISPLAY.colouredName(StyleElement.NO_COLOUR, true) + ".",
            ModifierCategory.MOVEMENT_COST_ASTEROIDS.displayEffectName(),
            ModifierCategory.MOVEMENT_COST_ASTEROIDS.displayEffectValue(0.35f, Modifier::percentMultiplicative),
            ModifierCategory.MOVEMENT_COST_ASTEROIDS,
            UIColourTheme.DEEP_GREEN, Modifier.GREEN_BACKGROUND);
}

package unit.stats.modifiers.groups;

import level.tile.TileType;
import render.types.text.StyleElement;
import unit.stats.modifiers.types.EmptyModifier;
import unit.stats.modifiers.types.Modifier;
import unit.stats.modifiers.types.ModifierCategory;
import unit.stats.modifiers.types.SingleModifier;

public abstract class MovementModifier {
    public static final EmptyModifier NO_ASTEROID_FIELDS = new EmptyModifier("Cannot Navigate " + TileType.ASTEROIDS.getName() + "s",
            "This unit not manoeuvrable enough to navigate " + TileType.ASTEROIDS.colouredName(StyleElement.NO_COLOUR, true) + " tiles.",
            StyleElement.MODIFIER_MOVEMENT_SPEED.display + "Unit cannot move through " + TileType.ASTEROIDS.getName() + " tiles",
            Modifier.RED);

    public static final EmptyModifier NORMAL_ASTEROID_FIELDS = new EmptyModifier("Normal " + TileType.ASTEROIDS.getName() + " Navigation",
            "This unit is manoeuvrable enough to safely navigate " + TileType.ASTEROIDS.colouredName(StyleElement.NO_COLOUR, true) + " tiles, but doing so has a high " + ModifierCategory.MOVEMENT_COST_DISPLAY.colouredName(StyleElement.NO_COLOUR, true) + ".",
            StyleElement.MODIFIER_MOVEMENT_SPEED.display + "Unit can move through " + TileType.ASTEROIDS.getName() + " tiles normally",
            Modifier.YELLOW);

    public static final SingleModifier FAST_ASTEROID_FIELDS = new SingleModifier(0.5f, "Improved " + TileType.ASTEROIDS.getName() + " Navigation",
            "This unit has exceptional manoeuvrability, allowing it to move through " + TileType.ASTEROIDS.colouredName(StyleElement.NO_COLOUR, true) + " tiles with a reduced " + ModifierCategory.MOVEMENT_COST_DISPLAY.colouredName(StyleElement.NO_COLOUR, true) + ".",
            Modifier::percentMultiplicative,
            ModifierCategory.MOVEMENT_COST_ASTEROIDS)
            .setListAndMainColour(Modifier.GREEN);

    public static final SingleModifier MINING_ASTEROID_FIELDS = new SingleModifier(0.35f, TileType.ASTEROIDS.getName() + " Protection System",
            "This unit comes with special equipment that can clear a path through " + TileType.ASTEROIDS.colouredName(null, true) + "s" + StyleElement.NO_COLOUR.display + ", greatly reducing " + ModifierCategory.MOVEMENT_COST_DISPLAY.colouredName(StyleElement.NO_COLOUR, true) + ".",
            Modifier::percentMultiplicative,
            ModifierCategory.MOVEMENT_COST_ASTEROIDS)
            .setListAndMainColour(Modifier.GREEN);
}

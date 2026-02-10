package unit.stats.attribute;

import foundation.IInternalName;
import render.UIColourTheme;
import render.types.text.StyleElement;
import render.types.text.TextRenderable;
import unit.stats.ColouredIconName;
import unit.stats.modifiers.types.ModifierCategory;

import java.util.function.Function;

public enum UnitAttribute implements ColouredIconName, IInternalName {
    DEFENCE_NETWORK(StyleElement.ATTRIBUTE_DEFENCE_NETWORK, TextRenderable.DEFENCE_NETWORK_ICON,
            "DEFENCE_NETWORK", "Defence Network",
            a -> "Combat damage taken by this unit is shared evenly among adjacent units of the same team that also have " +
                    a.colouredIconName(StyleElement.NO_COLOUR, false) + "."
    );

    public final UIColourTheme boxTheme;
    public final StyleElement colour;
    public final TextRenderable icon;
    private final String internalName, displayName;
    public final String description;

    UnitAttribute(StyleElement colour, TextRenderable icon, String internalName, String displayName, Function<UnitAttribute, String> description) {
        this.colour = colour;
        this.icon = icon;
        this.internalName = internalName;
        this.displayName = displayName;
        boxTheme = UIColourTheme.createBoxTheme(colour);
        this.description = description.apply(this);
    }

    @Override
    public String colour() {
        return colour.display;
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String getIcon() {
        return icon.display;
    }

    public static ModifierCategory getCategory(UnitAttribute a) {
        return switch (a) {
            case DEFENCE_NETWORK -> ModifierCategory.DEFENCE_NETWORK;
        };
    }

    @Override
    public String getInternalName() {
        return internalName;
    }
}

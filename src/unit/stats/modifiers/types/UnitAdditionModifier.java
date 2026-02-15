package unit.stats.modifiers.types;

import render.UIColourTheme;
import render.types.text.StyleElement;
import unit.type.UnitType;

public class UnitAdditionModifier extends EmptyModifier implements CardModifier {
    public final UnitType[] units;

    public UnitAdditionModifier(String name, String description, String effectDescription, UnitType... units) {
        super(name, description, effectDescription, UIColourTheme.createBoxTheme(StyleElement.MODIFIER_ADD_UNIT));
        this.units = units;
    }

    @Override
    public String effectCondition(ModifierCategory cat) {
        return "";
    }
}

package unit.stats.modifiers.groups;

import render.types.text.StyleElement;
import unit.stats.modifiers.types.Modifier;
import unit.stats.modifiers.types.SingleModifier;

import java.awt.*;

import static unit.stats.modifiers.types.ModifierCategory.*;

public abstract class WeaponDamageModifier {
    public static final SingleModifier
            WEAPON_WEAKNESS = new SingleModifier(0.6f, "Ineffective Weapons",
            "The attacking unit's weapon is ineffective against the enemy unit, resulting in reduced " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true),
            Modifier::percentMultiplicative, DAMAGE)
            .setListAndMainColour(Modifier.RED),

    NORMAL_STRENGTH = new SingleModifier(1f, "Normal Weapon Strength",
            "The attacking unit's weapon performs normally against the enemy unit, resulting in no extra " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + ".",
            Modifier::percentMultiplicative, DAMAGE)
            .setListAndMainColour(Modifier.BLUE),

    WEAPON_STRENGTH = new SingleModifier(1.4f, "Effective Weapons",
            "The attacking unit's weapon is effective against the enemy unit, resulting in increased " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true),
            Modifier::percentMultiplicative, DAMAGE)
            .setListAndMainColour(Modifier.GREEN);

    public static Color getDamageModifierColour(Modifier... modifiers) {
        if (modifiers != null)
            for (Modifier m : modifiers) {
                if (m == NORMAL_STRENGTH) return new Color(92, 187, 228);
                if (m == WEAPON_STRENGTH) return new Color(130, 200, 77);
                if (m == WEAPON_WEAKNESS) return new Color(197, 74, 74);
            }
        return new Color(135, 135, 135);
    }
}

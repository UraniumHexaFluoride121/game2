package unit.weapon;

import foundation.math.MathUtil;
import render.UIColourTheme;
import render.types.text.StyleElement;
import unit.ShipClass;
import unit.Unit;
import unit.stats.modifiers.types.Modifier;
import unit.stats.modifiers.types.ModifierCategory;
import unit.stats.modifiers.groups.WeaponDamageModifier;
import unit.type.UnitType;

import java.awt.*;
import java.util.function.Function;

public enum WeaponEffectiveness {
    STRONG("Strong", StyleElement.GREEN.display), NORMAL("Normal", StyleElement.BLUE.display), WEAK("Weak", StyleElement.RED.display);


    public final String name, textColour;
    public static final UIColourTheme BOX_GRAY = new UIColourTheme(
            new Color(191, 191, 191), new Color(30, 30, 30, 239)
    ), BOX_BLUE = new UIColourTheme(
            new Color(68, 157, 220), new Color(17, 27, 32, 239)
    ), BOX_GREEN = new UIColourTheme(
            new Color(106, 220, 68), new Color(21, 32, 17, 239)
    ), BOX_RED = new UIColourTheme(
            new Color(220, 68, 68), new Color(32, 17, 17, 239)
    );

    WeaponEffectiveness(String name, String colour) {
        this.name = name;
        this.textColour = colour;
    }

    public static WeaponEffectiveness againstType(FiringData data) {
        return fromMultiplier(Modifier.multiplicativeEffect(ModifierCategory.DAMAGE, data.getBestWeaponAgainst(false).template.getModifiers(data.otherUnit.data.type)));
    }

    public static WeaponEffectiveness againstClass(Unit attacking, ShipClass shipClass) {
        return againstClass(attacking.data.type, shipClass);
    }

    public static WeaponEffectiveness againstClass(UnitType attacking, ShipClass shipClass) {
        return fromMultiplier(attacking.weapons.stream().map(w -> Modifier.multiplicativeEffect(ModifierCategory.DAMAGE, w.getModifiers(shipClass))).reduce(Float::max).get());
    }

    public static WeaponEffectiveness fromMultiplier(float multiplier) {
        return MathUtil.compare(multiplier, 1, WEAK, NORMAL, STRONG);
    }

    public static WeaponEffectiveness fromMultiplier(float multiplier, float epsilon) {
        return MathUtil.compare(multiplier, 1, epsilon, WEAK, NORMAL, STRONG);
    }

    public static String effectivenessSummary(StyleElement end, Function<ShipClass, WeaponEffectiveness> effectiveness) {
        StringBuilder s = new StringBuilder();
        for (ShipClass value : ShipClass.values()) {
            s.append(effectiveness.apply(value).textColourGray().display).append(value.icon.display);
        }
        return end == null ? s.toString() : s + end.display;
    }

    public UIColourTheme modifierColour() {
        return switch (this) {
            case STRONG -> WeaponDamageModifier.WEAPON_STRENGTH.colour();
            case NORMAL -> WeaponDamageModifier.NORMAL_STRENGTH.colour();
            case WEAK -> WeaponDamageModifier.WEAPON_WEAKNESS.colour();
        };
    }

    public UIColourTheme boxColour() {
        return switch (this) {
            case STRONG -> BOX_GREEN;
            case NORMAL -> BOX_BLUE;
            case WEAK -> BOX_RED;
        };
    }

    public UIColourTheme boxColourGray() {
        return switch (this) {
            case STRONG -> BOX_GREEN;
            case NORMAL -> BOX_GRAY;
            case WEAK -> BOX_RED;
        };
    }

    public StyleElement textColourGray() {
        return switch (this) {
            case STRONG -> StyleElement.GREEN;
            case NORMAL -> StyleElement.BOX_GRAY;
            case WEAK -> StyleElement.RED;
        };
    }
}

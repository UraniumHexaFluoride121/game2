package unit.stats.modifiers.groups;

import render.types.text.StyleElement;
import unit.ShipClass;
import unit.action.Action;
import unit.stats.attribute.UnitAttribute;
import unit.stats.modifiers.types.*;
import unit.type.UnitType;

public abstract class CardModifiers {
    //Income modifiers
    public static SingleCardModifier incomeBoost(int level) {
        return new SingleCardModifier(switch (level) {
            case 1 -> 2;
            case 2 -> 5;
            case 3 -> 8;
            default -> throw new RuntimeException();
        }, ModifierCategory.INCOME.getName() + " Boost " + romanNumeral(level),
                "A " + levelKeyword(level) + " increase to income.",
                Modifier::signedAdditive, ModifierCategory.INCOME, null);
    }

    //Action cost modifiers
    public static final float MAX_ACTION_COST_REDUCTION = 0.5f;

    public static SingleUnitActionModifier classActionCost(ShipClass shipClass, Action action, int level) {
        return new SingleUnitActionModifier(-level,
                ModifierCategory.ACTION_COST.getName() + ": " + action.getName(),
                "Decreases the " + ModifierCategory.ACTION_COST.getName().toLowerCase() + " of the " +
                        action.colouredIconName(StyleElement.NO_COLOUR, false) + " action for all " + shipClass.getClassName().toLowerCase() + " units. " +
                        effectLimit(ModifierCategory.ACTION_COST.getName().toLowerCase(), Modifier.percentMultiplicative(MAX_ACTION_COST_REDUCTION)),
                Modifier::signedAdditive, ModifierCategory.ACTION_COST, shipClassAppend(shipClass), ActionDependent.unitClass(shipClass, action)
        );
    }

    //Attribute modifiers
    public static SingleUnitModifier typeAttribute(UnitType type, UnitAttribute attribute) {
        return new SingleUnitModifier(
                1, attribute.getName(),
                "Your " + type.getPluralName() + " now have " + attribute.colouredIconName(StyleElement.NO_COLOUR, false) + ".\n\nEffects:\n" +
                attribute.description,
                Modifier.string("+ " + attribute.getName()), UnitAttribute.getCategory(attribute), shipTypeAppend(type), UnitDependent.unitType(type)
        );
    }

    //Movement speed modifiers
    public static final float MAX_MOVE_COST_REDUCTION = 0.7f;

    public static SingleUnitModifier classMoveSpeed(ShipClass shipClass, int level) {
        return new SingleUnitModifier(
                switch (level) {
                    case 1 -> -0.1f;
                    case 2 -> -0.2f;
                    case 3 -> -0.3f;
                    default -> throw new RuntimeException();
                }, "Engine Efficiency " + romanNumeral(level),
                "Decreases " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " for " + shipClass.getClassName() + " units on all tile types. " +
                        effectLimit(ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase(), Modifier.percentMultiplicative(MAX_MOVE_COST_REDUCTION)),
                Modifier::percentAdditive, ModifierCategory.MOVEMENT_COST_ALL, shipClassAppend(shipClass), UnitDependent.unitClass(shipClass)
        );
    }

    public static SingleUnitModifier typeMoveSpeed(UnitType type, int level) {
        return new SingleUnitModifier(
                switch (level) {
                    case 1 -> -0.07f;
                    case 2 -> -0.14f;
                    case 3 -> -0.21f;
                    default -> throw new RuntimeException();
                }, "Engine Efficiency " + romanNumeral(level),
                "Decreases " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " for " + type.getPluralName() + ", for all tile types. " +
                        effectLimit(ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase(), Modifier.percentMultiplicative(MAX_MOVE_COST_REDUCTION)),
                Modifier::percentAdditive, ModifierCategory.MOVEMENT_COST_ALL, shipTypeAppend(type), UnitDependent.unitType(type)
        );
    }

    //Damage modifiers
    public static SingleUnitModifier classDamage(ShipClass shipClass, int level) {
        return new SingleUnitModifier(
                switch (level) {
                    case 1 -> 0.05f;
                    case 2 -> 0.1f;
                    case 3 -> 0.15f;
                    default -> throw new RuntimeException();
                }, "Weapon Enhancement " + romanNumeral(level),
                "Increases base " + ModifierCategory.DAMAGE.getName().toLowerCase() + " dealt by all " + shipClass.getClassName() + " units.",
                Modifier::percentAdditive, ModifierCategory.DAMAGE, shipClassAppend(shipClass), UnitDependent.unitClass(shipClass)
        );
    }

    //HP modifiers
    public static SingleUnitModifier classHP(ShipClass shipClass, int level) {
        return new SingleUnitModifier(
                level, "Hull Integrity " + romanNumeral(level),
                "Increases " + ModifierCategory.HP.getName().toLowerCase() + " of all " + shipClass.getClassName() + " units.",
                Modifier::signedAdditive, ModifierCategory.HP, shipClassAppend(shipClass), UnitDependent.unitClass(shipClass)
        );
    }

    public static SingleUnitModifier typeHP(UnitType type, int level) {
        return new SingleUnitModifier(
                level, "Hull Integrity " + romanNumeral(level),
                "Increases " + ModifierCategory.HP.getName().toLowerCase() + " of all " + type.getPluralName(),
                Modifier::signedAdditive, ModifierCategory.HP, shipTypeAppend(type), UnitDependent.unitType(type)
        );
    }

    //Ammo modifiers
    public static SingleCardModifier ammoIncrease(UnitType type, int amount) {
        return new SingleUnitModifier(amount, "Enlarged Ammo Storage",
                "Increases the the " + ModifierCategory.AMMO_CAPACITY.getName().toLowerCase() + " for all " + type.getPluralName().toLowerCase() + ".",
                Modifier::signedAdditive, ModifierCategory.AMMO_CAPACITY, shipTypeAppend(type), UnitDependent.unitType(type)
        );
    }


    private static String shipClassAppend(ShipClass shipClass) {
        return shipClass.icon.display + shipClass.getClassName();
    }

    private static String shipTypeAppend(UnitType type) {
        return type.getName();
    }

    private static String effectLimit(String name, String effect) {
        return "Card modifiers can at most affect the " + name + " by " + effect + ".";
    }

    public static String levelKeyword(int level) {
        return switch (level) {
            case 1 -> "minor";
            case 2 -> "moderate";
            case 3 -> "major";
            default -> throw new RuntimeException();
        };
    }

    public static String romanNumeral(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> throw new RuntimeException();
        };
    }
}

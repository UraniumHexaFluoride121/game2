package unit.info;

import render.ui.implementation.UnitInfoScreen;
import unit.ShipClass;

public record AttributeData(UnitInfoScreen.AttributeType type, String text, int order) {
    private static int orderCounter = 0;
    //Should generally follow the order of: ability -> movement -> recon -> weapon -> defence -> misc

    //Positive
    public static final AttributeData
            HAS_SHIELD = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
            "Has a powerful shield for defence", orderCounter++),
            HIGH_MOVEMENT_SPEED = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
            "High movement speed", orderCounter++),
            QUICK_ASTEROID_FIELD = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Can move through asteroid fields", orderCounter++),
            HIGH_VIEW_RANGE = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Extended view range", orderCounter++),
            RANGED_WEAPON = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Has a ranged weapon", orderCounter++),
            ANTI_SHIELD = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Effective against shields", orderCounter++),
            ANTI_CAPITAL_SHIP_MISSILES = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Missiles effective against large ships", orderCounter++),
            ANTI_FIGHTER = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Effective against " + ShipClass.FIGHTER.getName().toLowerCase() + " class", orderCounter++),
            ANTI_CORVETTE = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Effective against " + ShipClass.CORVETTE.getName().toLowerCase() + " class", orderCounter++),
            ANTI_CRUISER = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Effective against " + ShipClass.CRUISER.getName().toLowerCase() + " class", orderCounter++),
            ANTI_CAPITAL_SHIP = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Effective against " + ShipClass.CAPITAL_SHIP.getName().toLowerCase() + " class", orderCounter++),
            BALANCED = new AttributeData(UnitInfoScreen.AttributeType.POSITIVE,
                    "Generally an all-round, balanced ship", orderCounter++);
    //Neutral
    public static final AttributeData
            SLOW_ASTEROID_FIELD = new AttributeData(UnitInfoScreen.AttributeType.NEUTRAL,
            "Can slowly move through asteroid fields", orderCounter++),
            CARRIER_LOADING = new AttributeData(UnitInfoScreen.AttributeType.NEUTRAL,
                    "Can be docked to carriers", orderCounter++);
    //Negative
    public static final AttributeData
            NO_ASTEROID_FIELD = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
            "Cannot move through asteroid fields", orderCounter++),
            LOW_VIEW_RANGE = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Limited view range", orderCounter++),
            MAIN_GUN_LIMITED_AMMO = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Primary weapon has limited ammo", orderCounter++),
            INEFFECTIVE_AGAINST_SHIELDS = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Ineffective against shields", orderCounter++),
            INEFFECTIVE_AGAINST_LARGE = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Ineffective against large, well armoured ships", orderCounter++),
            INEFFECTIVE_AGAINST_SMALL = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Ineffective against small, maneuverable ships", orderCounter++),
            INEFFECTIVE_AGAINST_FIGHTER = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Ineffective against " + ShipClass.FIGHTER.getName().toLowerCase() + " class", orderCounter++),
            INEFFECTIVE_AGAINST_CORVETTE = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Ineffective against " + ShipClass.CORVETTE.getName().toLowerCase() + " class", orderCounter++),
            INEFFECTIVE_AGAINST_CRUISER = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Ineffective against " + ShipClass.CRUISER.getName().toLowerCase() + " class", orderCounter++),
            INEFFECTIVE_AGAINST_CAPITAL_SHIP = new AttributeData(UnitInfoScreen.AttributeType.NEGATIVE,
                    "Ineffective against " + ShipClass.CAPITAL_SHIP.getName().toLowerCase() + " class", orderCounter++);
}

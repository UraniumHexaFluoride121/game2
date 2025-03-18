package unit.bot;

import unit.ShipClass;

public enum BotTileDataType {
    ENEMY_UNIT_POSITIONS,
    ALLIED_UNITS,
    ALLIED_UNITS_NEEDED,
    SCOUT_NEEDED,
    ENEMY_FIGHTERS_DAMAGE,
    ENEMY_CORVETTES_DAMAGE,
    ENEMY_CRUISERS_DAMAGE,
    ENEMY_CAPITAL_SHIPS_DAMAGE;

    public static BotTileDataType enemyDamageTypeFromClass(ShipClass shipClass) {
        return switch (shipClass) {
            case FIGHTER -> ENEMY_FIGHTERS_DAMAGE;
            case CORVETTE -> ENEMY_CORVETTES_DAMAGE;
            case CRUISER -> ENEMY_CRUISERS_DAMAGE;
            case CAPITAL_SHIP -> ENEMY_CAPITAL_SHIPS_DAMAGE;
        };
    }
}

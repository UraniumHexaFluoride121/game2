package unit.weapon;

import foundation.NamedEnum;

public enum WeaponType implements NamedEnum {
    PLASMA("Plasma",
            "Plasma weapons are cheap, lightweight and compact, making them ideal " +
            "for use on fighters. They also happen to be highly effective against fighters, but " +
            "any amount of armour, often found on larger ships, is enough to block most damage. " +
            "They are, however, still the best weapon for taking down shields."),
    CANNON("Cannon",
            "The cannon is a powerful weapon when used against corvette class ships, " +
            "and can also deal significant damage to cruisers. It does, however, struggle to damage " +
            "to capital ships, fighters, and shields."),
    RAIL_GUN("Rail Gun",
            "These high-powered guns are enough to punch through even the toughest of hull " +
            "armour, making them effective against capital ships and especially cruisers. While the armour " +
            "of capital ships is no match for the rail gun, it does lose some of its effectiveness due to the " +
            "sheer size and ability to absorb damage that capital ships have."),
    EXPLOSIVE("Explosive",
            "Explosion-based weapons are some of the only weapons that can effectively destroy " +
            "capital ships. They also perform well against other large and slow ships, but don't do much " +
            "against shields.");

    private final String displayName;
    public final String infoText;

    WeaponType(String displayName, String infoText) {
        this.displayName = displayName;
        this.infoText = infoText;
    }

    @Override
    public String getName() {
        return displayName;
    }
}

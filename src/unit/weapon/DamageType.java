package unit.weapon;

public enum DamageType {
    INITIAL_COMBAT_DAMAGE(false),
    REDIRECTED_COMBAT_DAMAGE(false),
    ENVIRONMENT_DAMAGE(false);

    public final boolean ignoreShield;

    DamageType(boolean ignoreShield) {
        this.ignoreShield = ignoreShield;
    }
}

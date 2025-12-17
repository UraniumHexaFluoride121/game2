package unit.weapon;

public class WeaponInstance {
    public final ProjectileType projectileType;
    public final WeaponTemplate template;

    public WeaponInstance(WeaponTemplate template) {
        this.template = template;
        projectileType = template.projectileType;
    }

    public Damage getDamageAgainst(FiringData data) {
        return new Damage(data.thisUnit.stats.attackDamage(),
                data.getShieldDamageMultiplier(this) * data.unitDamageMultiplier(),
                data.getDamageMultiplier(this) * data.unitDamageMultiplier(),
                DamageType.INITIAL_COMBAT_DAMAGE, data.thisData.pos, data.otherData.pos);
    }

    public enum FireAnimState {
        HOLD,
        FIRE,
        EMPTY
    }
}

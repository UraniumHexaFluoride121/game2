package unit.weapon;

import unit.ShipClass;
import unit.stats.modifiers.types.Modifier;
import unit.type.UnitType;

import java.util.ArrayList;
import java.util.HashMap;

public class WeaponTemplate {
    public boolean runsAnim = false;
    public final ProjectileType projectileType;
    public HashMap<ShipClass, ArrayList<Modifier>> classModifiers = new HashMap<>();
    public HashMap<UnitType, ArrayList<Modifier>> typeModifiers = new HashMap<>();
    public boolean counterattack = true;

    public WeaponTemplate(ProjectileType projectileType) {
        this.projectileType = projectileType;
        for (ShipClass value : ShipClass.values()) {
            classModifiers.put(value, new ArrayList<>());
        }
    }

    public WeaponTemplate classModifier(ShipClass shipClass, Modifier modifier) {
        classModifiers.get(shipClass).add(modifier);
        return this;
    }

    public WeaponTemplate typeModifier(UnitType type, Modifier modifier) {
        typeModifiers.putIfAbsent(type, new ArrayList<>());
        typeModifiers.get(type).add(modifier);
        return this;
    }

    public ArrayList<Modifier> getModifiers(UnitType type) {
        if (typeModifiers.containsKey(type))
            return typeModifiers.get(type);
        return classModifiers.get(type.shipClass);
    }

    public ArrayList<Modifier> getModifiers(ShipClass shipClass) {
        return classModifiers.get(shipClass);
    }

    public WeaponTemplate runAnim() {
        runsAnim = true;
        return this;
    }

    public WeaponTemplate noCounterattack() {
        counterattack = false;
        return this;
    }
}

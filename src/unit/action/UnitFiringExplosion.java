package unit.action;

import foundation.Deletable;
import foundation.math.ObjPos;
import level.structure.ObjectExplosion;
import unit.Unit;

public class UnitFiringExplosion extends ObjectExplosion implements Deletable {
    private Unit attacking, defending;

    public UnitFiringExplosion(ObjPos center, Unit attacking, Unit defending) {
        super(center, 0.5f);
        this.attacking = attacking;
        this.defending = defending;
    }

    @Override
    public void delete() {
        attacking.postFiring(defending, true);
        defending.postFiring(attacking, false);
        attacking = null;
        defending = null;
    }
}
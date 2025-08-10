package unit.action;

import render.anim.LerpAnimation;
import unit.Unit;

public class UnitFiringAnim extends UnitDataFiringAnim {
    private static final float ANIM_BLOCK = 0.7f;
    private Unit attacking, defending;

    public UnitFiringAnim(Unit attacking, Unit defending, Runnable onFinished, boolean arrow) {
        super(attacking.getLevel().getTile(attacking.data.pos).renderPosCentered, defending.getLevel().getTile(defending.data.pos).renderPosCentered, attacking.data, defending.data, onFinished, arrow);
        this.attacking = attacking;
        this.defending = defending;
        attacking.getLevel().levelRenderer.registerTimerBlock(new LerpAnimation(ANIM_BLOCK), () -> {
        });
    }

    @Override
    public void delete() {
        attacking.postFiring(defending, true, false);
        defending.postFiring(attacking, false, false);
        attacking = null;
        defending = null;
        super.delete();
    }
}

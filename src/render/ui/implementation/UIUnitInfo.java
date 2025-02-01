package render.ui.implementation;

import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import level.Level;
import render.*;
import render.ui.types.UIBox;
import render.ui.types.UITextLabel;
import unit.Unit;
import unit.UnitPose;
import unit.weapon.WeaponInstance;

public class UIUnitInfo extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private final UITextLabel title = new UITextLabel(11f, 0.9f, true).setTextLeftBold();
    private final UITextLabel hp = new UITextLabel(9.4f, 1, false);
    private final UITextLabel ammo = new UITextLabel(9.4f, 1, false);
    private final UIBox box = new UIBox(11, 14);
    private final StaticHitBox hitBox = StaticHitBox.createFromOriginAndSize(0.5f, 0.5f, 11, 14);
    private Level level;

    public UIUnitInfo(RenderRegister<OrderedRenderable> register, RenderOrder order, Level level) {
        super(register, order);
        this.level = level;
        hp.updateTextLeft("HP:");
        ammo.updateTextLeft("Ammo:");
        renderable = g -> {
            if (!isVisible())
                return;
            GameRenderer.renderTransformed(g, () -> {
                Unit unit = level.selectedUnit;
                g.translate(0.5, 0.5);
                box.render(g);
                GameRenderer.renderOffset(0, 4, g, () -> {
                    Renderable.renderImage(unit.type.getImage(unit.team, UnitPose.INFO), false, true, 11).render(g);
                });
                GameRenderer.renderOffset(-0.2f, 14.5f, g, () -> {
                    title.updateTextLeft(unit.type.displayName.toUpperCase());
                    title.render(g);
                });
                GameRenderer.renderOffset(0.5f, 4.5f, g, () -> {
                    hp.updateTextRight(MathUtil.floatToString(unit.hitPoints, 1));
                    hp.render(g);
                    if (!unit.weapons.isEmpty()) {
                        g.translate(0, -1);
                        WeaponInstance weapon = unit.getAmmoWeapon();
                        ammo.setRightOffset(weapon == null ? 0.4f : 0);
                        ammo.updateTextRight(weapon == null ? "--" : weapon.ammo + "/" + weapon.ammoCapacity);
                        ammo.render(g);
                    }
                });
            });
        };
    }

    private boolean isVisible() {
        return level.selectedUnit != null;
    }

    @Override
    public void delete() {
        super.delete();
        renderable = null;
        level = null;
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return isVisible() && hitBox.isPositionInside(level.levelRenderer.transformCameraPosToBlock(pos));
    }

    @Override
    public boolean blocking(InputType type) {
        return type.isMouseInput();
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return ButtonOrder.LEVEL_UI;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {

    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {

    }
}

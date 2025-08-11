package render.level.info;

import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import level.Level;
import render.*;
import render.anim.timer.PowAnimation;
import render.types.box.UIDisplayBox;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.container.LevelUIContainer;
import unit.stats.Modifier;
import unit.stats.modifiers.WeaponDamageModifier;
import unit.weapon.WeaponEffectiveness;

import java.awt.*;

public class UIWeaponEffectivenessInfo extends LevelUIContainer<Level> {
    private UIDisplayBoxRenderElement displayBox;
    private PowAnimation anim = new PowAnimation(0.4f, 0.75f);

    public UIWeaponEffectivenessInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.LEVEL_UI, Renderable.right() / 2, 0.5f, level);
        anim.finish();
        addRenderables((r, b) -> {
            displayBox = new UIDisplayBoxRenderElement(r, RenderOrder.LEVEL_UI, 0, 0, 20, -1, box -> box.setColourTheme(WeaponEffectiveness.BOX_GRAY), false);
            displayBox.box.setHorizontalAlign(HorizontalAlign.CENTER)
                    .addText(0.8f, HorizontalAlign.CENTER, "Weapon effectiveness colour coding:")
                    .addSpace(0.3f, 0)
                    .addBox(newBox(WeaponDamageModifier.NORMAL_STRENGTH, "Normal Weapon Strength"), HorizontalAlign.LEFT, 0)
                    .addSpace(0.3f, 0)
                    .addBox(newBox(WeaponDamageModifier.STRENGTH_1, "Weapon Strength I - III"), HorizontalAlign.LEFT, 0)
                    .addBox(newBox(null, "Unable to Attack"), HorizontalAlign.RIGHT, 1)
                    .addSpace(0.3f, 1)
                    .addBox(newBox(WeaponDamageModifier.WEAKNESS_1, "Weapon Weakness I - III"), HorizontalAlign.RIGHT, 1)
                    .addBox(new UIDisplayBox(0, 2, 20, -1, box -> box.setColourTheme(WeaponEffectiveness.BOX_GRAY), true)
                            .addText(0.6f, HorizontalAlign.CENTER, "Press middle mouse to show / hide colour coding"), HorizontalAlign.CENTER, 2)
                    .setColumnVerticalAlign(1, VerticalAlign.BOTTOM).setColumnVerticalAlign(2, VerticalAlign.TOP);
            new OnButtonInput(b, RenderOrder.LEVEL_UI, type -> type == InputType.MOUSE_MIDDLE, () -> {
                anim.setReversed(!anim.reversed());
            });
        });
    }

    @Override
    public void render(Graphics2D g) {
        displayBox.box.attemptUpdate(g);
        GameRenderer.renderOffset(0, anim.normalisedProgress() * -(displayBox.box.height + 0.8f), g, () -> {
            super.render(g);
        });
    }

    @Override
    public void delete() {
        super.delete();
        displayBox = null;
    }

    private static UIDisplayBox newBox(Modifier m, String s) {
        return new UIDisplayBox(0, 0, 9.5f, -1, box -> box.setColourTheme(
                Modifier.createBackgroundTheme(WeaponDamageModifier.getDamageModifierColour(m))), false)
                .addText(0.6f, HorizontalAlign.LEFT, s);
    }
}

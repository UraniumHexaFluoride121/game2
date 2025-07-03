package render.level.info;

import foundation.input.*;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.Level;
import render.*;
import render.level.tile.RenderElement;
import render.types.box.UIBox;
import render.types.container.LevelUIContainer;
import render.types.container.UIElementScrollSurface;
import render.types.text.TextRenderer;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.text.UITextLabel;
import unit.Unit;
import unit.stats.Modifier;
import unit.weapon.FiringData;
import unit.weapon.WeaponEffectiveness;
import unit.weapon.WeaponInstance;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static render.types.text.StyleElement.*;
import static render.types.text.TextRenderable.*;
import static unit.stats.ModifierCategory.*;

public class UIDamageModifiers extends LevelUIContainer<Level> {
    private Point tile = null, prev = null;
    private UIElementScrollSurface<UIDisplayBoxRenderElement> scrollSurface;
    private UIDisplayBoxRenderElement totalDamage, enemyInfo;
    private final TextRenderer modifierCount = new TextRenderer(null, 0.55f, UITextLabel.TEXT_COLOUR_DARK).setTextAlign(HorizontalAlign.CENTER).setItalic(true);
    private final TextRenderer scroll = new TextRenderer("Scroll up / down to see all modifiers.", 0.55f, UITextLabel.TEXT_COLOUR_DARK).setTextAlign(HorizontalAlign.CENTER).setItalic(true);

    public UIDamageModifiers(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, Renderable.right() / 2, 0, level);
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    new UIBox(12, 6)
                            .setColourTheme(WeaponEffectiveness.BOX_GRAY)
                            .translate(-6, 2 + 2.5f),
                    modifierCount.translate(0, 1.2f + 2.5f),
                    g -> {
                        if (scrollSurface.getScrollMax() > 0)
                            GameRenderer.renderOffset(0, .4f + 2.5f, g, () -> {
                                scroll.render(g);
                            });
                    }
            ).setZOrder(-10);
            scrollSurface = new UIElementScrollSurface<UIDisplayBoxRenderElement>(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                    -6, 2 + 2.5f, 12, 6, false, count -> (count + 1) * 0.4f) {
                @Override
                public boolean posInside(ObjPos pos, InputType type) {
                    return type instanceof ScrollInputType;
                }
            }.setPerElementScroll(e -> e.box.height);
            scrollSurface.setScrollSpeed(0.2f).addScrollBar(0.5f, 0.25f, -0.25f).setScrollBarButtonEnabled(false);
            totalDamage = new UIDisplayBoxRenderElement(r, RenderOrder.LEVEL_UI, -10, 0.5f, 20, 2, box -> box.setColourTheme(WeaponEffectiveness.BOX_GRAY), false);
            totalDamage.box
                    .addText(0.6f, HorizontalAlign.CENTER, "Total damage:")
                    .addSpace(0.2f, 0)
                    .addText(0.6f, HorizontalAlign.CENTER, null);
            enemyInfo = new UIDisplayBoxRenderElement(r, RenderOrder.LEVEL_UI, -6, 2 + 2.5f + 6.5f, 12, 1.2f, box -> box.setColourTheme(WeaponEffectiveness.BOX_GRAY), false);
            enemyInfo.box
                    .addText(0.6f, HorizontalAlign.LEFT, "Enemy:")
                    .addText(0.6f, HorizontalAlign.CENTER, 1, null)
                    .addText(0.6f, HorizontalAlign.RIGHT, 2, null);
            new OnButtonInput(b, ButtonOrder.LEVEL_UI, type -> type == InputType.MOUSE_MIDDLE, () -> {
                level.levelRenderer.damageModifierInfo.enable();
            });
        });
    }

    @Override
    public void render(Graphics2D g) {
        if (tile != null)
            super.render(g);
        prev = tile;
        tile = null;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && prev != null;
    }

    public void show(Point tile, Unit thisUnit, Unit otherUnit, Graphics2D g) {
        this.tile = tile;
        if (tile.equals(prev))
            return;
        scrollSurface.clear();
        FiringData firingData = thisUnit.getCurrentFiringData(otherUnit);
        AtomicReference<Float> height = new AtomicReference<>(0f);
        WeaponInstance weapon = firingData.getBestWeaponAgainst(false);
        float unitDamageMultiplier = firingData.unitDamageMultiplier();
        float initialDamage = thisUnit.stats.baseDamage();
        float damage = weapon.getDamageAgainst(firingData);
        float multiplier = damage / (initialDamage * unitDamageMultiplier);
        ArrayList<FiringData.DamageModifier> modifiers = firingData.damageModifiers(weapon);
        level.levelRenderer.damageModifierInfo.update(modifiers, g);
        modifiers.forEach(m -> {
            scrollSurface.addElement((r, b, i) -> {
                UIColourTheme colour = m.modifier().listColour();
                UIDisplayBoxRenderElement e = new UIDisplayBoxRenderElement(r, RenderOrder.LEVEL_UI, 0.6f, 0, 10.8f, -1, box -> {
                    box.setColourTheme(colour);
                }, false);
                e.box.addText(0.6f, HorizontalAlign.LEFT, m.modifier().name());
                e.box.getText(0, 0).setTextColour(colour.borderColour);
                StringBuilder effect = new StringBuilder();
                if (m.modifier().hasCategory(DAMAGE))
                    effect.append(Modifier.percentMultiplicative(m.modifier().effect(DAMAGE))).append(DAMAGE_ICON.display).append("\n");
                if (m.modifier().hasCategory(SHIELD_DAMAGE))
                    effect.append(Modifier.percentMultiplicative(m.modifier().effect(SHIELD_DAMAGE))).append("\n");
                if (m.modifier().hasCategory(INCOMING_DAMAGE))
                    effect.append(Modifier.percentMultiplicative(m.modifier().effect(INCOMING_DAMAGE))).append(DAMAGE_ICON.display).append("\n");
                if (m.modifier().hasCategory(INCOMING_SHIELD_DAMAGE))
                    effect.append(Modifier.percentMultiplicative(m.modifier().effect(INCOMING_SHIELD_DAMAGE))).append("\n");
                if (!effect.isEmpty()) {
                    effect.deleteCharAt(effect.length() - 1);
                    e.box.addText(0.6f, HorizontalAlign.RIGHT, 1, effect.toString());
                    e.box.getText(0, 1).setTextColour(colour.borderColour);
                }
                e.box.attemptUpdate(g);
                height.updateAndGet(v -> v + e.box.height + 0.4f);
                e.translate(0, -height.get());
                return e;
            });
        });
        WeaponEffectiveness effectiveness = WeaponEffectiveness.againstType(firingData);
        String preModifierColour = MathUtil.compare(initialDamage * unitDamageMultiplier, initialDamage, RED, MODIFIER_BLUE, GREEN).display;
        String finalColour = MathUtil.compare(damage, initialDamage * unitDamageMultiplier, RED, MODIFIER_BLUE, GREEN).display;
        modifierCount.updateText(scrollSurface.getElements().size() + (scrollSurface.getElements().size() == 1 ? " modifier" : " modifiers") + " in effect. Press middle mouse for details.");
        totalDamage.box.setText(2, 0,
                MODIFIER_GRAY.display + MathUtil.floatToString(initialDamage, 1) + DAMAGE_ICON.display + "   " + RIGHT_ARROW.display + "   " + MODIFIER_GRAY.display +
                        preModifierColour + Modifier.percentMultiplicative(unitDamageMultiplier) + " (Unit Damage)   " + MODIFIER_GRAY.display + RIGHT_ARROW.display + "   " +
                        finalColour + Modifier.percentMultiplicative(multiplier) + " (Modifiers)   " + MODIFIER_GRAY.display + RIGHT_ARROW.display + "   " +
                        finalColour + MathUtil.floatToString(damage, 1) + DAMAGE_ICON.display);
        totalDamage.box.modifyBox(box -> box.setColourTheme(WeaponEffectiveness.fromMultiplier(multiplier, 0.2f).boxColour()));
        enemyInfo.box.setText(0, 1, otherUnit.type.shipClass.icon.display + otherUnit.type.getName())
                .setText(0, 2, effectiveness.textColour + effectiveness.name);
        enemyInfo.box.modifyBox(box -> box.setColourTheme(effectiveness.boxColour()));
    }

    @Override
    public void delete() {
        super.delete();
        scrollSurface = null;
        totalDamage = null;
        enemyInfo = null;
    }
}

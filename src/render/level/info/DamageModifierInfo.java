package render.level.info;

import foundation.input.ButtonRegister;
import level.Level;
import render.*;
import render.level.tile.RenderElement;
import render.types.container.UIElementScrollSurface;
import render.types.text.MultiLineTextBox;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.box.UIDisplayBox;
import render.types.text.UITextLabel;
import unit.weapon.FiringData;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

public class DamageModifierInfo extends InfoScreen {
    public UIElementScrollSurface<UIDisplayBoxRenderElement> scrollSurface;

    public DamageModifierInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, 24, 24, level, false);
        box(b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX_DARK));
        addRenderables((r, b) -> {
            scrollSurface = new UIElementScrollSurface<UIDisplayBoxRenderElement>(r, b, RenderOrder.INFO_SCREEN,
                    0, 0, 24, 16.5f, false, count -> (count + 1) * 0.5f)
                    .setPerElementScroll(e -> e.box.height);
            scrollSurface.addScrollBar(0.4f, 0.3f, -0.4f);
            new RenderElement(r, RenderOrder.INFO_SCREEN,
                    new UITextLabel(18, 1.5f, false).setTextLeftBold()
                            .updateTextLeft("Damage Modifiers").translate(0.8f, 22.3f),
                    new MultiLineTextBox(1, 21.5f, 22, 0.6f, HorizontalAlign.LEFT)
                            .updateText("""
                                    These are the damage modifiers currently in effect for attacking the selected enemy unit. Certain modifiers \
                                    are applied to the attacking unit, while others are applied to the enemy unit being attacked. \
                                    This can be seen in the top right corner of each modifier.
                                    
                                    All modifiers are applied multiplicatively to the attacking unit's base damage, along with the penalty \
                                    for unit damage, which is applied to units that have taken damage. The more damage a unit has taken, the greater \
                                    the damage penalty, up to -75%.
                                    """)
            );
        });
    }

    public void enable() {
        setEnabled(true);
    }

    public void update(ArrayList<FiringData.DamageModifier> modifiers, Graphics2D g) {
        ArrayList<FiringData.DamageModifier> sorted = new ArrayList<>(modifiers);
        sorted.sort(Comparator.comparingInt(m -> m.isOnAttackingUnit() ? 0 : 1));
        scrollSurface.clear();
        AtomicReference<Float> height = new AtomicReference<>((float) 0);
        sorted.forEach(m -> {
            scrollSurface.addElement((r, b, i) -> {
                UIDisplayBoxRenderElement e = m.modifier().renderBox(r, RenderOrder.INFO_SCREEN, 1, 0, 22);
                UIDisplayBox unitBox = new UIDisplayBox(0, 0, 10, -1, box -> box.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX), true);
                unitBox.addText(0.55f, HorizontalAlign.RIGHT, 1, m.isOnAttackingUnit() ? "Attacking Unit" : "Enemy Unit");
                e.box.addBox(unitBox, HorizontalAlign.RIGHT, 1)
                        .setColumnVerticalAlign(1, VerticalAlign.TOP);
                e.box.attemptUpdate(g);
                height.updateAndGet(v -> v + e.box.height + 0.5f);
                e.translate(0, -height.get());
                return e;
            });
        });
    }
}

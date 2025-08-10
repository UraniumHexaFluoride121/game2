package render.level.info;

import foundation.input.ButtonRegister;
import level.Level;
import render.*;
import render.level.tile.RenderElement;
import render.types.container.UIElementScrollSurface;
import render.types.text.MultiLineTextBox;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.text.UITextLabel;
import unit.stats.Modifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static unit.stats.ModifierCategory.*;

public class MovementModifierInfo extends InfoScreen {
    public UIElementScrollSurface<UIDisplayBoxRenderElement> scrollSurface;

    public MovementModifierInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, 24, 24, level, false);
        box(b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX_DARK));
        addRenderables((r, b) -> {
            scrollSurface = new UIElementScrollSurface<UIDisplayBoxRenderElement>(r, b, RenderOrder.INFO_SCREEN,
                    0, 0, 24, 16.5f, false, count -> (count + 1) * 0.5f)
                    .setPerElementScroll(e -> e.box.height);
            scrollSurface.addScrollBar(0.4f, 0.3f, -0.4f);
            new RenderElement(r, RenderOrder.INFO_SCREEN,
                    new UITextLabel(18, 1.5f, false).setTextLeftBold()
                            .updateTextLeft("Movement Modifiers").translate(0.8f, 22.3f),
                    new MultiLineTextBox(1, 21.5f, 22, 0.6f, HorizontalAlign.LEFT)
                            .updateText("These are the movement modifiers currently in effect for this unit, which include " + MOVEMENT_COST_DISPLAY.getName().toLowerCase() +
                                    " modifiers, as well as any other special modifiers. Units have a max " + MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + " (not affected by modifiers), " +
                                    "and each tile the unit moves over has a " + MOVEMENT_COST_DISPLAY.getName().toLowerCase() + ". The unit can move to a tile as long as the total cost of all " +
                                    "the tiles along the unit's path is lower than its max " + MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + ".\n\n" +
                                    "Each tile type has a different " + MOVEMENT_COST_DISPLAY.getName().toLowerCase() + ", which can be influenced by modifiers. Certain modifiers " +
                                    "affect the " + MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " of specific tile types, while others affect all tile types. Modifier effects " +
                                    "are multiplicative.")
            );
        });
    }

    public void enable() {
        setEnabled(true);
    }

    public void update(ArrayList<Modifier> modifiers, Graphics2D g) {
        scrollSurface.clear();
        AtomicReference<Float> height = new AtomicReference<>((float) 0);
        modifiers.forEach(m -> {
            scrollSurface.addElement((r, b, i) -> {
                UIDisplayBoxRenderElement e = m.renderBox(r, RenderOrder.INFO_SCREEN, 1, 0, 22);
                e.box.attemptUpdate(g);
                height.updateAndGet(v -> v + e.box.height + 0.5f);
                e.translate(0, -height.get());
                return e;
            });
        });
    }
}

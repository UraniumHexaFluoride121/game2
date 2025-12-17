package render.level.info;

import foundation.input.*;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.Level;
import render.*;
import render.level.tile.RenderElement;
import render.level.tile.TilePath;
import render.types.box.UIBox;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.container.LevelUIContainer;
import render.types.container.UIElementScrollSurface;
import render.types.text.*;
import unit.Unit;
import unit.stats.modifiers.types.Modifier;
import unit.stats.modifiers.types.ModifierCategory;
import unit.weapon.WeaponEffectiveness;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static render.types.text.TextRenderable.*;
import static unit.stats.modifiers.types.ModifierCategory.*;

public class UIMovementModifiers extends LevelUIContainer<Level> {
    public static final ModifierCategory[] tileMovementModifierCategories = {
            MOVEMENT_COST_ALL,
            MOVEMENT_COST_EMPTY,
            MOVEMENT_COST_NEBULA,
            MOVEMENT_COST_DENSE_NEBULA,
            MOVEMENT_COST_ASTEROIDS
    };
    private Point end = null, prev = null;
    private UIElementScrollSurface<UIDisplayBoxRenderElement> scrollSurface;
    private UIDisplayBoxRenderElement result;
    private final TextRenderer modifierCount = new TextRenderer(null, 0.55f, UITextLabel.TEXT_COLOUR_DARK).setTextAlign(HorizontalAlign.CENTER).setItalic(true);
    private final TextRenderer scroll = new TextRenderer("Scroll up / down to see all modifiers.", 0.55f, UITextLabel.TEXT_COLOUR_DARK).setTextAlign(HorizontalAlign.CENTER).setItalic(true);
    private final TextRenderer costText = new TextRenderer(null, 0.8f, UITextLabel.TEXT_COLOUR).setTextAlign(HorizontalAlign.CENTER).setBold(true);
    private final TextRenderer costModifier = new TextRenderer("Cost modifiers in effect:", 0.6f, UITextLabel.TEXT_COLOUR).setTextAlign(HorizontalAlign.CENTER).setBold(true);

    public UIMovementModifiers(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.LEVEL_UI, Renderable.right() / 2, 0, level);
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    new UIBox(20, 5)
                            .setColourTheme(WeaponEffectiveness.BOX_GRAY)
                            .translate(-10, 2 + 1.5f),
                    modifierCount.translate(0, 1.2f + 1.5f),
                    g -> {
                        if (scrollSurface.getScrollMax() > 0)
                            GameRenderer.renderOffset(0, .4f + 1.5f, g, () -> {
                                scroll.render(g);
                            });
                    }, costModifier.translate(0, 6.8f), costText.translate(0, 7.5f)
            ).setZOrder(-10);
            scrollSurface = new UIElementScrollSurface<UIDisplayBoxRenderElement>(r, b, RenderOrder.LEVEL_UI,
                    -10, 2 + 1.5f, 20, 3.2f, false, count -> count * 0.4f) {
                @Override
                public boolean posInside(ObjPos pos, InputType type) {
                    return type instanceof ScrollInputType;
                }
            }.setPerElementScroll(e -> e.box.height);
            scrollSurface.setScrollSpeed(0.2f).addScrollBar(0.5f, 0.25f, -0.25f).setScrollBarButtonEnabled(false);
            result = new UIDisplayBoxRenderElement(r, RenderOrder.LEVEL_UI, 0, 0.5f, 20, 1, box -> box.setColourTheme(WeaponEffectiveness.BOX_GRAY), false);
            result.box.addText(0.6f, HorizontalAlign.CENTER, null).setHorizontalAlign(HorizontalAlign.CENTER);
            new OnButtonInput(b, RenderOrder.LEVEL_UI, type -> type == InputType.MOUSE_MIDDLE, () -> {
                level.levelRenderer.movementModifierInfo.enable();
            });
        });
    }

    @Override
    public void render(Graphics2D g) {
        if (end != null)
            super.render(g);
        prev = end;
        end = null;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && prev != null;
    }

    public void show(TilePath path, Unit unit, Graphics2D g) {
        end = path.getLastTile();
        if (end == null || end.equals(prev))
            return;
        scrollSurface.clear();
        AtomicReference<Float> height = new AtomicReference<>(0f);
        ArrayList<Modifier> modifiers = unit.stats.getMovementModifiers();
        level.levelRenderer.movementModifierInfo.update(modifiers, g);
        modifiers.forEach(m -> {
            scrollSurface.addElement((r, b, i) -> {
                UIColourTheme colour = m.listColour();
                UIDisplayBoxRenderElement e = new UIDisplayBoxRenderElement(r, RenderOrder.LEVEL_UI, 0.6f, 0, 18.8f, -1, box -> {
                    box.setColourTheme(colour);
                }, false);
                e.box.addText(0.6f, HorizontalAlign.LEFT, m.name());
                e.box.getText(0, 0).setTextColour(colour.borderColour);
                StringBuilder effect = new StringBuilder();
                for (ModifierCategory cat : tileMovementModifierCategories) {
                    if (m.hasCategory(cat))
                        effect.append(StyleElement.removeStyle(m.effectDescriptionValue(cat))).append(" ").append(cat.getShortenedName()).append("\n");
                }
                if (!effect.isEmpty()) {
                    effect.deleteCharAt(effect.length() - 1);
                    e.box.addText(0.6f, HorizontalAlign.RIGHT, 1, effect.toString());
                    e.box.getText(0, 1).setTextColour(colour.borderColour);
                }
                e.box.getTextElement(0, 0).setAlign(effect.isEmpty() ? HorizontalAlign.CENTER : HorizontalAlign.LEFT);
                e.box.attemptUpdate(g);
                height.updateAndGet(v -> v + e.box.height + 0.4f);
                e.translate(0, -height.get() + 0.2f);
                return e;
            });
        });
        float cost = path.getPathCost(unit.stats::moveCost, level);
        float baseCost = path.getPathCost(type -> type.moveCost, level);
        String colour = MathUtil.compare(cost / baseCost, 1, .2f, StyleElement.GREEN, StyleElement.MODIFIER_BLUE, StyleElement.RED).display;
        modifierCount.updateText(scrollSurface.getElements().size() + (scrollSurface.getElements().size() == 1 ? " modifier" : " modifiers") + " in effect. Press middle mouse for details.");
        result.box.setText(ModifierCategory.MOVEMENT_COST_DISPLAY.getName() + ": " +
                StyleElement.MODIFIER_GRAY.display + MathUtil.floatToString(baseCost, 1) + MOVE_ICON.display + "   " + RIGHT_ARROW.display + "   " +
                colour + Modifier.percentMultiplicative(cost / baseCost) + " (Modifiers)   " + StyleElement.MODIFIER_GRAY.display + RIGHT_ARROW.display + "   " +
                MathUtil.floatToString(cost, 1) + MOVE_ICON.display
        );
        costText.updateText(MathUtil.compare(cost, unit.stats.maxMovement(), StyleElement.NO_COLOUR, StyleElement.NO_COLOUR, StyleElement.RED).display +
                MathUtil.floatToString(cost, 1) + MOVE_ICON.display + " / " +
                MathUtil.floatToString(unit.stats.maxMovement(), 1) + MOVE_ICON.display
        );
    }

    @Override
    public void delete() {
        super.delete();
        scrollSurface = null;
    }
}

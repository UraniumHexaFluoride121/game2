package render.level.info;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import foundation.math.MathUtil;
import level.Level;
import level.energy.EnergyManager;
import level.structure.Structure;
import level.tile.Tile;
import render.*;
import render.level.tile.RenderElement;
import render.types.UIFullScreenColour;
import render.types.box.UIBox;
import render.types.box.UIImageBox;
import render.types.box.UITooltipBox;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.container.UIElementScrollSurface;
import render.types.input.button.UIButton;
import render.types.text.AbstractUITooltip;
import render.types.text.MultiLineTextBox;
import render.HorizontalAlign;
import render.types.text.UITextLabel;

public class StructureInfoScreen extends LevelUIContainer<Level> {
    private static final float WIDTH = 35, HEIGHT = 22;
    private final UITextLabel nameText;
    private UIImageBox imageBox;
    private UIElementScrollSurface<UIContainer> attributes;
    private final MultiLineTextBox description = new MultiLineTextBox(12, HEIGHT - 4.5f, WIDTH - 13, 0.9f, HorizontalAlign.LEFT).setTextColour(UITextLabel.TEXT_COLOUR_DARK);

    public StructureInfoScreen(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.INFO_SCREEN, ButtonOrder.INFO_SCREEN, Renderable.right() / 2 - WIDTH / 2, Renderable.top() / 2 - HEIGHT / 2, level);
        nameText = new UITextLabel(WIDTH - 10 - 4, 2.5f, false).setTextLeftBold();
        addRenderables((r, b) -> {
            new UIFullScreenColour(r, RenderOrder.INFO_SCREEN_BACKGROUND, UnitInfoScreen.FULL_SCREEN_MENU_BACKGROUND_COLOUR)
                    .setZOrder(-10).translate(-(Renderable.right() / 2 - WIDTH / 2), -(Renderable.top() / 2 - HEIGHT / 2));
            imageBox = new UIImageBox(10, 10, null);
            new UIButton(r, b, RenderOrder.INFO_SCREEN, ButtonOrder.INFO_SCREEN,
                    3.5f - (Renderable.right() / 2 - WIDTH / 2), Renderable.top() - 2.5f - (Renderable.top() / 2 - HEIGHT / 2), 9, 2, 1.4f, false, this::disable)
                    .setText("Back").setBold().setColourTheme(UIColourTheme.DEEP_RED);
            new RenderElement(r, RenderOrder.INFO_SCREEN,
                    new UIBox(WIDTH, HEIGHT).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER),
                    imageBox.setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(1, HEIGHT - 1 - 10),
                    nameText.translate(10 + 2, HEIGHT - 3),
                    description
            ).setZOrder(-1);
            new OnButtonInput(b, ButtonOrder.INFO_SCREEN, t -> t == InputType.ESCAPE, this::disable);
            attributes = new UIElementScrollSurface<>(r, b, RenderOrder.INFO_SCREEN, ButtonOrder.INFO_SCREEN, 0, 1, 15, HEIGHT - 12, false, count -> count * 1.5f + 0.5f);
            attributes.setScrollSpeed(0.25f);
        });
    }

    private void disable() {
        level.levelRenderer.structureInfoScreen.setEnabled(false);
        level.levelRenderer.endTurn.setEnabled(true);
    }

    public void enable(Tile tile) {
        Structure s = tile.structure;
        imageBox.setImage(s.type.getLightImage(s.team));
        nameText.updateTextLeft(s.type.getName());
        description.updateText(s.type.description);
        attributes.forEach(UIContainer::delete);
        attributes.clear();
        addAttribute("Turns to capture:", s.canBeCaptured ? String.valueOf(s.type.captureSteps) : "N/A", "The number of turns it takes to capture this structure");
        addAttribute("Can resupply:", s.type.resupply ? "Yes" : "No", "Whether or not this structure can resupply allied units");
        addAttribute("Unit repair:", s.type.unitRegen == 0 ? "None" : MathUtil.floatToString(s.type.unitRegen, 1), "The amount of HP that this structure can repair for allied units");
        addAttribute("Income:", s.type.energyIncome == 0 ? "None" : String.valueOf(s.type.energyIncome), "The amount of " + EnergyManager.displayName + " income that this structure provides");
        level.levelRenderer.endTurn.setEnabled(false);
        setEnabled(true);
    }

    private void addAttribute(String name, String value, String tooltip) {
        attributes.addElement((r, b, i) -> {
            return new UIContainer(r, b, RenderOrder.INFO_SCREEN, ButtonOrder.INFO_SCREEN, 1, -(i + 1) * 1.5f)
                    .addRenderables((r2, b2) -> {
                        new RenderElement(r2, RenderOrder.INFO_SCREEN, new UITextLabel(10, 1, false)
                                .setTextLeftBold().updateTextLeft(name)
                                .setTextRightBold().updateTextRight(value));
                        new UITooltipBox(r2, b2, ButtonOrder.INFO_SCREEN, 0, 0, 10, 1)
                                .tooltip(t -> t.add(12, AbstractUITooltip.dark(), tooltip));
                    });
        });
    }

    @Override
    public boolean blocking(InputType type) {
        return true;
    }

    @Override
    public void delete() {
        super.delete();
        imageBox = null;
        attributes.forEach(UIContainer::delete);
        attributes.clear();
    }
}

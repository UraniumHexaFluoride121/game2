package mainScreen;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import level.structure.StructureType;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.UIColourTheme;
import render.level.tile.RenderElement;
import render.types.box.UIBox;
import render.types.box.UIImageBox;
import render.types.box.UITooltipBox;
import render.types.container.UIContainer;
import render.types.container.UIElementScrollSurface;
import render.types.input.UINumberSelector;
import render.types.text.UITextLabel;
import render.types.text.UITooltip;
import unit.UnitTeam;

public class StructureGenerationSettings extends UIContainer {
    private UIElementScrollSurface<StructureGenerationElement> elements;

    private static final StructureGenerationPreset DEFAULT = new StructureGenerationPreset()
            .add(StructureType.REFINERY, 2, 1);

    public StructureGenerationSettings(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y) {
        super(register, buttonRegister, order, buttonOrder, x, y);
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BACKGROUND,
                    new UIBox(19, 28).setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER)
            );
            elements = new UIElementScrollSurface<StructureGenerationElement>(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    0, 0, 19, 28, false, count -> count * 5.5f + 1f)
                    .addElements(StructureType.SPAWNABLE_TYPES.length, (r2, b2, i) -> {
                        return new StructureGenerationElement(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 1, -(i + 1) * 5.5f - 0.5f, StructureType.SPAWNABLE_TYPES[i]);
                    });
        });
        loadPreset(DEFAULT, false);
    }

    public void loadPreset(StructureGenerationPreset preset, boolean verifyTeams) {
        elements.forEach((e, i) -> {
            StructureType s = StructureType.SPAWNABLE_TYPES[i];
            e.neutral.setValue(preset.neutralMap.get(s), verifyTeams);
            e.captured.setValue(preset.capturedMap.get(s), verifyTeams);
        });
    }

    public StructureGenerationPreset getPreset() {
        StructureGenerationPreset preset = new StructureGenerationPreset();
        elements.forEach((e, i) -> {
            preset.add(StructureType.SPAWNABLE_TYPES[i], e.neutral.getValue(), e.captured.getValue());
        });
        return preset;
    }

    private static class StructureGenerationElement extends UIContainer {
        public UINumberSelector neutral, captured;

        public StructureGenerationElement(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, StructureType s) {
            super(register, buttonRegister, order, buttonOrder, x, y);
            addRenderables((r3, b3) -> {
                new RenderElement(r3, RenderOrder.TITLE_SCREEN_BACKGROUND,
                        new UIImageBox(5f, 5f, s.getLightImage(UnitTeam.BLUE)).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER),
                        new UIBox(19 - 5 - 2 - 0.5f, 5f).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(5.5f, 0),
                        new UITextLabel(7, 1, false).setTextCenterBold().updateTextCenter(s.getName())
                                .translate(7.5f, 3.8f),
                        new UITextLabel(4.5f, 0.9f, false).setTextCenterBold().updateTextCenter("Neutral:")
                                .translate(6f, 2.3f),
                        new UITextLabel(4.5f, 0.9f, false).setTextCenterBold().updateTextCenter("Captured:")
                                .translate(6f, 0.8f)
                );
                neutral = new UINumberSelector(r3, b3, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS_BACK,
                        11.5f, 2.3f, 0.9f, 1.5f, 0, 10, 0).setCorner(0.3f)
                        .setOnChanged(() -> MainPanel.titleScreen.playerBoxes.verifyTeams());
                captured = new UINumberSelector(r3, b3, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS_BACK,
                        11.5f, 0.8f, 0.9f, 1.5f, 0, 10, 0).setCorner(0.3f)
                        .setOnChanged(() -> MainPanel.titleScreen.playerBoxes.verifyTeams());
                new UITooltipBox(r3, b3, ButtonOrder.MAIN_BUTTONS, 6f, 2.3f, 10.5f, 0.9f)
                        .tooltip(t -> t.add(12, UITooltip.light(), "The number of neutral structures to spawn close to the centre of the map"));
                new UITooltipBox(r3, b3, ButtonOrder.MAIN_BUTTONS, 6f, 0.8f, 10.5f, 0.9f)
                        .tooltip(t -> t.add(12, UITooltip.light(), "The number of player-controlled structures to spawn for each player. These are spawned close to the player bases"));
            });
        }
    }
}

package render.level.info;

import foundation.input.*;
import level.Level;
import level.energy.EnergyDisplay;
import render.*;
import render.level.tile.RenderElement;
import render.texture.ImageRenderer;
import render.types.UIHitPointBar;
import render.types.box.UIBox;
import render.types.box.UIImageBox;
import render.types.box.UIShapeDisplayBox;
import render.types.container.*;
import render.types.input.button.UIButton;
import render.types.input.button.UIShapeButton;
import render.types.text.*;
import unit.Unit;
import unit.UnitPose;
import unit.action.Action;
import unit.action.ActionIconType;
import unit.info.AttributeData;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class UnitInfoScreen extends LevelUIContainer<Level> {
    public static final Color FULL_SCREEN_MENU_BACKGROUND_COLOUR = new Color(0, 0, 0, 208);
    private static final float sideMargin = 6;
    private final float width, height;

    private Renderable unitImage = null;
    private final UITextLabel nameText, classText;
    private final HashMap<UnitCharacteristic, BarDisplay> overviewBars = new HashMap<>();
    private AttributeData[] attributes;
    private AttributeRenderer attributeRenderer;
    private UIScrollSurface attributesScrollSurface, actionScrollSurface;
    private UITabSwitcher tabSwitcher;
    private final MultiLineTextBox unitDescription = new MultiLineTextBox(0.5f, 0, 28, 0.8f, HorizontalAlign.LEFT).setTextColour(UITextLabel.TEXT_COLOUR_DARK);
    private UIElementScrollSurface<BarDisplay> characteristicDisplay;

    public UnitInfoScreen(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.INFO_SCREEN, 0, 0, level);
        width = Renderable.right() - sideMargin * 2;
        height = Renderable.top() - 5;
        nameText = new UITextLabel(27, 2.5f, false).setTextLeftBold();
        classText = new UITextLabel(12, 1.2f, false).setTextLeftBold()
                .setLeftColour(UITextLabel.TEXT_COLOUR_DARK);
        addRenderables((r, b) -> {
            new UIButton(r, b, RenderOrder.INFO_SCREEN,
                    3.5f, Renderable.top() - 2.5f, 9, 2, 1.4f, false, this::disable)
                    .setText("Back").setBold().setColourTheme(UIColourTheme.DEEP_RED);
            new OnButtonInput(b, RenderOrder.INFO_SCREEN_BACKGROUND, t -> t == InputType.ESCAPE, this::disable);
            new RenderElement(r, RenderOrder.INFO_SCREEN_BACKGROUND, g -> {
                g.setColor(FULL_SCREEN_MENU_BACKGROUND_COLOUR);
                g.fillRect(0, 0, (int) Math.ceil(Renderable.right()), (int) Math.ceil(Renderable.top()));
            });
            tabSwitcher = new UITabSwitcher(r, b, RenderOrder.INFO_SCREEN,
                    sideMargin, 1, width, height)
                    .addTab(4.5f, "Overview", (r2, b2) -> {
                        new RenderElement(r2, RenderOrder.INFO_SCREEN, g -> {
                            unitImage.render(g);
                            GameRenderer.renderOffset(2, height - 3, g, () -> {
                                nameText.render(g);
                                g.translate(0, -1.5f);
                                classText.render(g);
                                g.translate(0, -1.2f);
                                unitDescription.render(g);
                            });
                        }, new UITextLabel(25, 1, false).setTextLeftBold().updateTextLeft("Feature overview").translate(2, 15.5f))
                                .setZOrder(-1);
                        attributesScrollSurface = new UIScrollSurface(r2, b2, RenderOrder.INFO_SCREEN, 1, 0, 27, 15, false, (r3, b3) -> {
                            attributeRenderer = new AttributeRenderer(r3);
                        }).addScrollBar(0.5f, 0.4f, 0).setScrollSpeed(0.3f);
                        characteristicDisplay = new UIElementScrollSurface<>(r2, b2, RenderOrder.INFO_SCREEN, width - 15.3f, 0, 15, 14, false, count -> 0f);
                    }).addTab(4, "Actions", (r2, b2) -> {
                        new RenderElement(r2, RenderOrder.INFO_SCREEN, new UITextLabel(27, 2.5f, false).setTextLeftBold().updateTextLeft("Action overview")
                                .translate(2, height - 3));
                        actionScrollSurface = new UIScrollSurface(r2, b2, RenderOrder.INFO_SCREEN, 1, 0, width - 2, height - 5, false, (r3, b3) -> {
                        }).addScrollBar(0.5f, 0.4f, 0).setScrollSpeed(0.4f);
                    });
        });
    }

    private void disable() {
        level.levelRenderer.unitInfoScreen.setEnabled(false);
        level.levelRenderer.endTurn.setEnabled(true);
    }

    public void enable(Unit unit) {
        unitDescription.updateText(unit.data.type.description);
        tabSwitcher.selectTab(0);
        unitImage = new UIImageBox(14, 14, ImageRenderer.renderImageCentered(unit.data.type.getImage(unit.data.team, UnitPose.INFO), true))
                .setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER)
                .translate(width - 1 - 14, height - 1 - 14);
        nameText.updateTextLeft(unit.data.type.getName());
        classText.updateTextLeft(unit.data.type.shipClass.getName() + " class");
        List<Map.Entry<UnitCharacteristic, UnitCharacteristicValue>> characteristics = unit.data.type.unitCharacteristics.sequencedEntrySet().stream().toList();
        characteristicDisplay.modifyAndResize(characteristics.size(), (r, b, i) -> {
            BarDisplay barDisplay = new BarDisplay(r, b, RenderOrder.INFO_SCREEN, 0, -(i + 1) * 1.7f, 8, characteristics.get(i).getKey().getName());
            barDisplay.bar.setFill(characteristics.get(i).getValue().fill);
            return barDisplay;
        }, (b, i) -> {
            b.bar.setFill(characteristics.get(i).getValue().fill);
            b.setText(characteristics.get(i).getKey().getName());
        });
        overviewBars.forEach((c, b) -> b.bar.setFill(unit.data.type.unitCharacteristics.get(c).fill));
        attributes = unit.data.type.infoAttributes;
        attributeRenderer.refreshRenderer();
        attributesScrollSurface.setScrollAmount(0);
        level.levelRenderer.endTurn.setEnabled(false);
        actionElements.forEach(ActionOverviewElement::delete);
        actionElements.clear();
        actionScrollSurface.addRenderables((r, b) -> {
            ArrayList<Action> actions = new ArrayList<>();
            if (unit.data.type.canCapture)
                actions.add(Action.CAPTURE);
            actions.addAll(Arrays.asList(unit.data.type.actions));
            actions.sort(Comparator.comparingInt(a -> -a.getOrder()));
            for (int i = 0; i < actions.size(); i++) {
                actionElements.add(new ActionOverviewElement(r, b, i + 1, actions.get(i), unit));
            }
        });
        actionScrollSurface.setScrollMax((actionElements.size() - 1) * 6 - attributesScrollSurface.height);
        setEnabled(true);
    }

    @Override
    public boolean blocking(InputType type) {
        return true;
    }

    @Override
    public void delete() {
        super.delete();
        attributesScrollSurface = null;
        actionScrollSurface = null;
        overviewBars.clear();
        actionElements.clear();
        tabSwitcher = null;
        characteristicDisplay = null;
    }

    private final ArrayList<ActionOverviewElement> actionElements = new ArrayList<>();

    private static class ActionOverviewElement extends UIContainer {
        private final UITextLabel name = new UITextLabel(12, 1, false).setTextCenterBold();
        private final MultiLineTextBox infoText = new MultiLineTextBox(5, 3, 35, 0.65f, HorizontalAlign.LEFT)
                .setTextColour(UITextLabel.TEXT_COLOUR_DARK);
        private EnergyDisplay energyDisplay;

        private ActionOverviewElement(GameRenderer renderer, ButtonRegister buttonRegister, int index, Action action, Unit unit) {
            super(renderer, buttonRegister, RenderOrder.INFO_SCREEN, 0, -index * 6 + .5f);
            name.updateTextCenter(action.getName());
            infoText.updateText(action.infoText);
            addRenderables((r, b) -> {
                String actionCostText = unit.getActionCostText(action);
                energyDisplay = new EnergyDisplay(3, true).setText(actionCostText + TextRenderable.ENERGY.display);
                new RenderElement(r, RenderOrder.INFO_SCREEN,
                        new UIBox(44, 5).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(1, 0),
                        ((Renderable) g -> {
                            GameRenderer.renderOffset(1, 1.7f, g, () -> {
                                GameRenderer.renderScaled(3 / Action.ACTION_BUTTON_SIZE, g, () -> {
                                    action.renderIcon(g, ActionIconType.ENABLED, ButtonState.DEFAULT);
                                });
                            });
                            GameRenderer.renderOffset((44 - 12) / 2f, 3.8f, g, () -> {
                                name.render(g);
                            });
                            infoText.render(g);
                        }).translate(1, 0), energyDisplay
                        .translate(2 + 3 / 2f, 0.4f));
            });
        }

        @Override
        public void delete() {
            super.delete();
            energyDisplay.delete();
        }
    }

    public enum AttributeType {
        POSITIVE(UIColourTheme.DEEP_GREEN, UIShapeButton::plus), NEUTRAL(UIColourTheme.DEEP_YELLOW, UIShapeButton::dot), NEGATIVE(UIColourTheme.DEEP_RED, UIShapeButton::minus);

        public final UIColourTheme colour;
        public final Function<UIBox, Shape> shapeFunction;

        AttributeType(UIColourTheme theme, Function<UIBox, Shape> shapeFunction) {
            colour = theme;
            this.shapeFunction = shapeFunction;
        }
    }

    private static class Attribute extends UIContainer {
        public Attribute(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, float x, float y, AttributeData data) {
            super(register, buttonRegister, RenderOrder.INFO_SCREEN, x, y);
            addRenderables((r, b) -> {
                new UIShapeDisplayBox(r, RenderOrder.INFO_SCREEN, .5f, .5f, 2, 2)
                        .setColourTheme(data.type().colour).setShape(data.type().shapeFunction);
                new UITextDisplayBox(r, RenderOrder.INFO_SCREEN, 3.2f, .5f, 23.3f, 2, 1f)
                        .setText(data.text()).setBold().setColourTheme(data.type().colour);
            });
        }
    }

    private static class BarDisplay extends UIContainer {
        public UIHitPointBar bar;
        public UITextLabel textLabel;

        public BarDisplay(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float spacing, String text) {
            super(register, buttonRegister, order, x, y);
            textLabel = new UITextLabel(7, 1, false)
                    .setTextLeftBold().updateTextLeft(text);
            bar = new UIHitPointBar(0.2f, 6, 1, 0.15f, 5, UIColourTheme.LIGHT_BLUE).setRounding(0.5f);
            addRenderables((r, b) -> {
                new RenderElement(r, RenderOrder.INFO_SCREEN, textLabel, bar.translate(spacing, -0.1f));
            });
        }

        public void setText(String text) {
            textLabel.updateTextLeft(text);
        }
    }

    private class AttributeRenderer extends AbstractRenderElement {
        public GameRenderer renderer;

        public AttributeRenderer(GameRenderer renderer) {
            super(renderer, RenderOrder.INFO_SCREEN);
            this.renderer = new GameRenderer(new AffineTransform(), null);
        }

        @Override
        public void render(Graphics2D g) {
            renderer.render(g);
        }

        public void refreshRenderer() {
            renderer.delete();
            renderer = new GameRenderer(new AffineTransform(), null);
            float i = 0;
            TreeSet<AttributeData> sorted = new TreeSet<>(Comparator.comparingInt(AttributeData::order));
            sorted.addAll(List.of(attributes));
            for (AttributeData data : sorted) {
                i -= 3;
                new Attribute(renderer, null, 0, i, data);
            }
            attributesScrollSurface.setScrollMax(-i - attributesScrollSurface.height);
        }
    }
}
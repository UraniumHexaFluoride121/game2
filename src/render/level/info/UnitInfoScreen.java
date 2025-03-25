package render.level.info;

import foundation.input.*;
import level.Level;
import render.*;
import render.types.UIHitPointBar;
import render.types.text.*;
import render.level.tile.RenderElement;
import render.texture.ImageRenderer;
import render.UIColourTheme;
import render.types.box.*;
import render.types.input.button.UIButton;
import render.types.input.button.UIShapeButton;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.container.UIScrollSurface;
import render.types.container.UITabSwitcher;
import unit.Unit;
import unit.UnitPose;
import unit.action.Action;
import unit.action.ActionIconType;
import unit.info.AttributeData;
import unit.info.UnitCharacteristic;
import unit.weapon.DamageType;
import unit.weapon.WeaponTemplate;

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
    private UIScrollSurface attributesScrollSurface, actionScrollSurface, weaponsScrollSurface;
    private UITabSwitcher tabSwitcher;
    private final HashMap<DamageType, BarDisplay> weaponDetailBars = new HashMap<>();
    private final MultiLineTextBox weaponTypeText = new MultiLineTextBox(30, 24, 15, .7f, TextAlign.LEFT);
    private final UITextLabel weaponTypeLabel = new UITextLabel(17, 1, true).updateTextLeft("Weapon type info:").setTextLeftBold();

    public UnitInfoScreen(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, Level level) {
        super(register, buttonRegister, order, buttonOrder, 0, 0, level);
        width = Renderable.right() - sideMargin * 2;
        height = Renderable.top() - 5;
        nameText = new UITextLabel(27, 2.5f, false).setTextLeftBold();
        classText = new UITextLabel(12, 1.2f, false).setTextLeftBold()
                .setLeftColour(UITextLabel.TEXT_COLOUR_DARK);
        addRenderables((r, b) -> {
            new UIButton(r, b, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN,
                    3.5f, Renderable.top() - 2.5f, 9, 2, 1.4f, false, this::disable)
                    .setText("Exit").setBold().setColourTheme(UIColourTheme.DEEP_RED);
            new OnButtonInput(b, ButtonOrder.UNIT_INFO_SCREEN, t -> t == InputType.ESCAPE, this::disable);
            new RenderElement(r, RenderOrder.UNIT_INFO_SCREEN_BACKGROUND, g -> {
                g.setColor(FULL_SCREEN_MENU_BACKGROUND_COLOUR);
                g.fillRect(0, 0, (int) Math.ceil(Renderable.right()), (int) Math.ceil(Renderable.top()));
            });
            tabSwitcher = new UITabSwitcher(r, b, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN,
                    sideMargin, 1, width, height)
                    .addTab(4.5f, "Overview", (r2, b2) -> {
                        new RenderElement(r2, RenderOrder.UNIT_INFO_SCREEN, g -> {
                            unitImage.render(g);
                            GameRenderer.renderOffset(2, height - 3, g, () -> {
                                nameText.render(g);
                                g.translate(0, -1.5f);
                                classText.render(g);
                            });
                        }, new UITextLabel(25, 1, false).setTextLeftBold().updateTextLeft("Feature overview").translate(2, 15.5f))
                                .setZOrder(-1);
                        attributesScrollSurface = new UIScrollSurface(r2, b2, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, 1, 0, 27, 15, false, (r3, b3) -> {
                            attributeRenderer = new AttributeRenderer(r3);
                        }).addScrollBar(0.5f, 0.4f, 0);
                        for (int i = 0; i < UnitCharacteristic.values().length; i++) {
                            UnitCharacteristic characteristic = UnitCharacteristic.values()[i];
                            overviewBars.put(characteristic, new BarDisplay(r2, b2, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, width - 14.3f, 12 - i * 1.7f, 7, characteristic.getName()));
                        }
                    }).addTab(4, "Actions", (r2, b2) -> {
                        new RenderElement(r2, RenderOrder.UNIT_INFO_SCREEN, new UITextLabel(27, 2.5f, false).setTextLeftBold().updateTextLeft("Action overview")
                                .translate(2, height - 3));
                        actionScrollSurface = new UIScrollSurface(r2, b2, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, 1, 0, width - 2, height - 5, false, (r3, b3) -> {
                        }).addScrollBar(0.5f, 0.4f, 0);
                    }).addTab(4, "Weapons", (r2, b2) -> {
                        new RenderElement(r2, RenderOrder.UNIT_INFO_SCREEN, new UITextLabel(27, 2.5f, false).setTextLeftBold().updateTextLeft("Weapons")
                                .translate(2, height - 3), weaponTypeText,
                                new UITextLabel(26, 1, false).updateTextLeft("Select a weapon to see details").setTextLeftBold().translate(1, height - 11.5f),
                                new UITextLabel(17, 1, true).updateTextLeft("About weapons:").setTextLeftBold().translate(29, 8),
                                new UITextLabel(17, 1, true).updateTextLeft("Weapon effectiveness:").setTextLeftBold().translate(29, 18),
                                weaponTypeLabel.translate(29, 25),
                                new MultiLineTextBox(30, 7, 15, .7f, TextAlign.LEFT)
                                        .updateText("When firing at an enemy unit, the weapon that can do the most damage to the shield and / or hull " +
                                                "will automatically be selected. Each weapon type has its strengths and weaknesses, so having multiple weapons " +
                                                "allows a ship to be effective against a broader selection of enemies. Some weapons also require ammunition. Once " +
                                                "you run out of ammunition for that weapon, it can no longer be fired until the unit is resupplied.")
                        );
                        weaponsScrollSurface = new UIScrollSurface(r2, b2, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, 1, 0, width - 22, height - 12, false, (r3, b3) -> {
                        }).addScrollBar(0.5f, 0.4f, 0);
                        for (int i = 0; i < DamageType.values().length; i++) {
                            DamageType type = DamageType.values()[i];
                            weaponDetailBars.put(type, new BarDisplay(r2, b2, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, 30, 10f + 1.5f * i, 10, type.getName()));
                        }
                    });
        });
    }

    private void disable() {
        level.levelRenderer.unitInfoScreen.setEnabled(false);
        level.levelRenderer.endTurn.setEnabled(true);
    }

    public void enable(Unit unit) {
        weaponDetailBars.forEach((t, b) -> {
            b.bar.setFill(0);
        });
        weaponTypeText.updateText("Select weapon to see info")
                .setTextColour(UITextLabel.TEXT_COLOUR_DARK);
        weaponTypeLabel.updateTextLeft("Weapon type info:");
        tabSwitcher.selectTab(0);
        unitImage = new UIImageBox(14, 14, ImageRenderer.renderImageCentered(unit.type.getImage(unit.team, UnitPose.INFO), true))
                .setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER)
                .translate(width - 1 - 14, height - 1 - 14);
        nameText.updateTextLeft(unit.type.getName());
        classText.updateTextLeft(unit.type.shipClass.getName() + " class");
        overviewBars.forEach((c, b) -> b.bar.setFill(unit.type.unitCharacteristics.get(c).fill));
        attributes = unit.type.infoAttributes;
        attributeRenderer.refreshRenderer();
        attributesScrollSurface.setScrollAmount(0);
        level.levelRenderer.endTurn.setEnabled(false);
        actionElements.forEach(ActionOverviewElement::delete);
        actionElements.clear();
        actionScrollSurface.addRenderables((r, b) -> {
            ArrayList<Action> actions = new ArrayList<>();
            if (unit.type.canCapture)
                actions.add(Action.CAPTURE);
            actions.addAll(Arrays.asList(unit.type.actions));
            actions.sort(Comparator.comparingInt(a -> -a.getOrder()));
            for (int i = 0; i < actions.size(); i++) {
                actionElements.add(new ActionOverviewElement(r, b, i + 1, actions.get(i)));
            }
        });
        actionScrollSurface.setScrollMax((actionElements.size() - 1) * 6 - attributesScrollSurface.height);
        weaponElements.forEach(WeaponElement::delete);
        weaponElements.clear();
        weaponsScrollSurface.addRenderables((r, b) -> {
            for (int i = 0; i < unit.type.weapons.size(); i++) {
                weaponElements.add(new WeaponElement(r, b, i + 1, unit.type.weapons.get(i), this));
            }
        });
        weaponsScrollSurface.setScrollMax((weaponElements.size()) * 6 - weaponsScrollSurface.height);
        weaponDetailBars.forEach((t, d) -> {
            d.bar.setFill(0);
        });
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
        weaponsScrollSurface = null;
        weaponDetailBars.clear();
        overviewBars.clear();
        actionElements.clear();
        tabSwitcher = null;
    }

    private final ArrayList<WeaponElement> weaponElements = new ArrayList<>();

    private static class WeaponElement extends UIContainer {
        private final UITextLabel name = new UITextLabel(12, 1, false).setTextCenterBold();
        public UIButton button;

        private WeaponElement(GameRenderer renderer, ButtonRegister buttonRegister, int index, WeaponTemplate weapon, UnitInfoScreen infoScreen) {
            super(renderer, buttonRegister, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, 0, -index * 6 + .5f);
            name.updateTextCenter(switch (index) {
                case 1 -> "Primary weapon";
                case 2 -> "Secondary weapon";
                case 3 -> "Tertiary weapon";
                default -> "Weapon " + index;
            });
            addRenderables((r, b) -> {
                button = new UIButton(r, b, RenderOrder.UNIT_INFO_SCREEN_BACKGROUND, ButtonOrder.UNIT_INFO_SCREEN, 1, 0, 24, 5, 0, true, () -> {
                    infoScreen.weaponDetailBars.forEach((t, d) -> {
                        d.bar.setFill(weapon.damageTypes.get(t).fill, 1, 0.6f);
                    });
                    infoScreen.weaponTypeText.updateText(weapon.weaponType.infoText)
                            .setTextColour(UITextLabel.TEXT_COLOUR);
                    infoScreen.weaponTypeLabel.updateTextLeft("Weapon type info: " + weapon.weaponType.getName());
                }).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER);
                new RenderElement(r, RenderOrder.UNIT_INFO_SCREEN, new UIBox(4, 4).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(1.5f, .5f),
                        ((Renderable) g -> {
                            GameRenderer.renderOffset(5 / 2f, 5 / 2f, g, () -> {
                                weapon.projectileType.infoImage.render(g, 4);
                            });
                            GameRenderer.renderOffset((24 - 12) / 2f, 3.8f, g, () -> {
                                name.render(g);
                            });
                        }).translate(1, 0),
                        new FixedTextRenderer("Weapon Type:", .7f, UITextLabel.TEXT_COLOUR_DARK)
                                .setTextAlign(TextAlign.LEFT).setBold(true).translate(7, 2.9f),
                        new FixedTextRenderer(weapon.weaponType.getName(), .7f, UITextLabel.TEXT_COLOUR_DARK)
                                .setTextAlign(TextAlign.RIGHT).setBold(true).translate(19, 2.9f),
                        new FixedTextRenderer("Firing Range:", .7f, UITextLabel.TEXT_COLOUR_DARK)
                                .setTextAlign(TextAlign.LEFT).setBold(true).translate(7, 1.8f),
                        new FixedTextRenderer(weapon.rangeText, .7f, UITextLabel.TEXT_COLOUR_DARK)
                                .setTextAlign(TextAlign.RIGHT).setBold(true).translate(19, 1.8f),
                        new FixedTextRenderer("Ammo Capacity:", .7f, UITextLabel.TEXT_COLOUR_DARK)
                                .setTextAlign(TextAlign.LEFT).setBold(true).translate(7, .7f),
                        new FixedTextRenderer(weapon.requiresAmmo ? String.valueOf(weapon.ammoCapacity) : "Infinite", .7f, UITextLabel.TEXT_COLOUR_DARK)
                                .setTextAlign(TextAlign.RIGHT).setBold(true).translate(19, .7f)
                );
            });
        }

        @Override
        public void delete() {
            super.delete();
            button = null;
        }
    }

    private final ArrayList<ActionOverviewElement> actionElements = new ArrayList<>();

    private static class ActionOverviewElement extends UIContainer {
        private final UITextLabel name = new UITextLabel(12, 1, false).setTextCenterBold();
        private final MultiLineTextBox infoText = new MultiLineTextBox(5, 3, 35, 0.65f, TextAlign.LEFT)
                .setTextColour(UITextLabel.TEXT_COLOUR_DARK);
        private final Action action;

        private ActionOverviewElement(GameRenderer renderer, ButtonRegister buttonRegister, int index, Action action) {
            super(renderer, buttonRegister, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, 0, -index * 6 + .5f);
            this.action = action;
            name.updateTextCenter(action.getName());
            infoText.updateText(action.infoText);
            addRenderables((r, b) -> {
                new UIButton(r, b, RenderOrder.UNIT_INFO_SCREEN_BACKGROUND, ButtonOrder.UNIT_INFO_SCREEN, 1, 0, 44, 5, 0, true)
                        .setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER);
                new RenderElement(r, RenderOrder.UNIT_INFO_SCREEN, ((Renderable) g -> {
                    GameRenderer.renderOffset(1, 1, g, () -> {
                        GameRenderer.renderScaled(3 / Action.ACTION_BUTTON_SIZE, g, () -> {
                            action.renderIcon(g, ActionIconType.ENABLED, ButtonState.DEFAULT);
                        });
                    });
                    GameRenderer.renderOffset((44 - 12) / 2f, 3.8f, g, () -> {
                        name.render(g);
                    });
                    infoText.render(g);
                }).translate(1, 0));
            });
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
            super(register, buttonRegister, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, x, y);
            addRenderables((r, b) -> {
                new UIShapeDisplayBox(r, RenderOrder.UNIT_INFO_SCREEN, .5f, .5f, 2, 2)
                        .setColourTheme(data.type().colour).setShape(data.type().shapeFunction);
                new UITextDisplayBox(r, RenderOrder.UNIT_INFO_SCREEN, 3.2f, .5f, 23.3f, 2, 1f)
                        .setText(data.text()).setBold().setColourTheme(data.type().colour);
            });
        }
    }

    private static class BarDisplay extends UIContainer {
        public UIHitPointBar bar;
        public UITextLabel textLabel;

        public BarDisplay(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float spacing, String text) {
            super(register, buttonRegister, order, buttonOrder, x, y);
            textLabel = new UITextLabel(6, 1, false)
                    .setTextLeftBold().updateTextLeft(text);
            bar = new UIHitPointBar(0.2f, 6, 1, 0.15f, 5, UIColourTheme.LIGHT_BLUE).setRounding(0.5f);
            addRenderables((r, b) -> {
                new RenderElement(r, RenderOrder.UNIT_INFO_SCREEN, textLabel, bar.translate(spacing, -0.1f));
            });
        }
    }

    private class AttributeRenderer extends AbstractRenderElement {
        public GameRenderer renderer;

        public AttributeRenderer(GameRenderer renderer) {
            super(renderer, RenderOrder.UNIT_INFO_SCREEN);
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
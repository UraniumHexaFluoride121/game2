package render.ui.implementation;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import level.Level;
import render.*;
import render.renderables.RenderElement;
import render.texture.ImageRenderer;
import render.ui.UIColourTheme;
import render.ui.types.*;
import unit.Unit;
import unit.UnitPose;
import unit.info.AttributeData;
import unit.info.UnitCharacteristic;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;

public class UnitInfoScreen extends LevelUIContainer {
    private static final Color backgroundColour = new Color(0, 0, 0, 208);
    private static final float sideMargin = 6;
    private final float width, height;

    private Renderable unitImage = null;
    private final UITextLabel nameText, classText;
    private final HashMap<UnitCharacteristic, BarDisplay> overviewBars = new HashMap<>();
    private AttributeData[] attributes;
    private AttributeRenderer attributeRenderer;
    private UIScrollSurface attributesScrollSurface;

    public UnitInfoScreen(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, Level level) {
        super(register, buttonRegister, order, buttonOrder, 0, 0, level);
        width = Renderable.right() - sideMargin * 2;
        height = Renderable.top() - 5;
        nameText = new UITextLabel(27, 2.5f, false).setTextLeftBold();
        classText = new UITextLabel(12, 1.2f, false).setTextLeftBold()
                .setLeftColour(UITextLabel.TEXT_COLOUR_DARK);
        addRenderables((r, b) -> {
            new UIButton(r, b, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN,
                    0.5f, Renderable.top() - 2.5f, 9, 2, 1.4f, false, this::disable)
                    .setText("Exit").setBold().setColourTheme(UIColourTheme.DEEP_RED);
            new OnButtonInput(b, ButtonOrder.UNIT_INFO_SCREEN, t -> t == InputType.ESCAPE, this::disable);
            new RenderElement(r, RenderOrder.UNIT_INFO_SCREEN_BACKGROUND, g -> {
                g.setColor(backgroundColour);
                g.fillRect(0, 0, (int) Math.ceil(Renderable.right()), (int) Math.ceil(Renderable.top()));
            });
            new UITabSwitcher(r, b, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN,
                    sideMargin, 1, width, height)
                    .addTab(4.5f, "Overview", (r2, b2) -> {
                        new RenderElement(r2, RenderOrder.UNIT_INFO_SCREEN, g -> {
                            unitImage.render(g);
                            GameRenderer.renderOffset(2, height - 3, g, () -> {
                                nameText.render(g);
                                g.translate(0, -1.5f);
                                classText.render(g);
                            });
                        }, new UITextLabel(25, 1, false).setTextLeftBold().updateTextLeft("Features overview").translate(2, 15.5f))
                                .setZOrder(-1);
                        attributesScrollSurface = new UIScrollSurface(r2, b2, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, 1, 0, 27, 15, false, (r3, b3) -> {
                            attributeRenderer = new AttributeRenderer(r3);
                        }).addScrollBar(0.5f,0.4f, 0);
                        for (int i = 0; i < UnitCharacteristic.values().length; i++) {
                            UnitCharacteristic characteristic = UnitCharacteristic.values()[i];
                            overviewBars.put(characteristic, new BarDisplay(r2, b2, RenderOrder.UNIT_INFO_SCREEN, ButtonOrder.UNIT_INFO_SCREEN, width - 14.3f, 12 - i * 1.7f, characteristic.getName()));
                        }
                    });
        });
    }

    private void disable() {
        level.levelRenderer.unitInfoScreen.setEnabled(false);
        level.levelRenderer.endTurn.setEnabled(true);
    }

    public void enable(Unit unit) {
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
        overviewBars.clear();
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

        public BarDisplay(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, String text) {
            super(register, buttonRegister, order, buttonOrder, x, y);
            textLabel = new UITextLabel(6, 1, false)
                    .setTextLeftBold().updateTextLeft(text);
            bar = new UIHitPointBar(0.2f, 6, 1, 0.15f, 5, UIColourTheme.LIGHT_BLUE).setRounding(0.5f);
            addRenderables((r, b) -> {
                new RenderElement(r, RenderOrder.UNIT_INFO_SCREEN, textLabel, bar.translate(7, -0.1f));
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
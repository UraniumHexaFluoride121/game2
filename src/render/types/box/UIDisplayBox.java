package render.types.box;

import foundation.Deletable;
import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import foundation.math.ObjPos;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;
import render.VerticalAlign;
import render.types.UIHitPointBar;
import render.types.text.MultiLineTextBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class UIDisplayBox implements Renderable, Deletable {
    public final float x, y;
    public float originalWidth, width, height, widthMargin;
    protected UIBox box;
    protected boolean renderBox = true, enabled = true;
    protected HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    protected ArrayList<ArrayList<BoxElement>> elements = new ArrayList<>();
    protected ArrayList<Float> columnHeights = new ArrayList<>();
    protected ArrayList<VerticalAlign> columnVerticalAlign = new ArrayList<>();
    protected final boolean dynamicHeight;

    private Runnable updateSize;
    private Runnable onUpdate = () -> {
    };

    public UIDisplayBox(float x, float y, float width, float height, Consumer<UIBox> boxModifier, boolean dynamicWidth) {
        this.x = x;
        this.y = y;
        this.width = width;
        originalWidth = width;
        this.height = height;
        box = new UIBox(width, height);
        boxModifier.accept(box);
        this.widthMargin = box.corner / 2;
        dynamicHeight = height == -1;
        if (dynamicWidth)
            updateSize = () -> {
                setHeightToTextSize();
                setWidthToTextSize();
                onUpdate.run();
            };
        else
            updateSize = () -> {
                setHeightToTextSize();
                onUpdate.run();
            };
    }

    public UIDisplayBox setWidthMargin(float widthMargin) {
        this.widthMargin = widthMargin;
        updateSize.run();
        return this;
    }

    public UIDisplayBox setRenderBox(boolean renderBox) {
        this.renderBox = renderBox;
        return this;
    }

    public UIDisplayBox setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public UIDisplayBox addOnUpdate(Runnable onUpdate) {
        Runnable prev = this.onUpdate;
        this.onUpdate = () -> {
            prev.run();
            onUpdate.run();
        };
        return this;
    }

    public UIDisplayBox addText(float textSize, HorizontalAlign align, String s) {
        return addText(textSize, align, 0, s);
    }

    public UIDisplayBox addText(float textSize, HorizontalAlign align, int columnIndex, String s) {
        updateColumnCount(columnIndex);
        elements.get(columnIndex).add(new TextBoxElement(originalWidth - widthMargin * 2, textSize, align, s, updateSize));
        return this;
    }

    public UIDisplayBox addBox(UIDisplayBox box, HorizontalAlign align, int columnIndex) {
        updateColumnCount(columnIndex);
        elements.get(columnIndex).add(new DisplayBoxElement(box, align, updateSize));
        return this;
    }

    public UIDisplayBox addBar(UIHitPointBar bar, HorizontalAlign align, int columnIndex, boolean maxWidth) {
        updateColumnCount(columnIndex);
        if (maxWidth)
            bar.setWidth(bar.barOnly ? originalWidth - widthMargin * 2 + bar.spacing * 2 : originalWidth - widthMargin * 2);
        elements.get(columnIndex).add(new HPBarBoxElement(bar, align));
        return this;
    }

    public UIDisplayBox addSpace(float height, int columnIndex) {
        updateColumnCount(columnIndex);
        elements.get(columnIndex).add(new BoxElementSpace(height));
        updateSize.run();
        return this;
    }

    private void updateColumnCount(int lastIndex) {
        while (elements.size() <= lastIndex) {
            elements.add(new ArrayList<>());
            columnVerticalAlign.add(VerticalAlign.CENTER);
            columnHeights.add(0f);
        }
    }

    public UIDisplayBox setText(String s) {
        return setText(0, 0, s);
    }

    public UIDisplayBox setText(int index, int columnIndex, String s) {
        ((TextBoxElement) elements.get(columnIndex).get(index)).text.updateText(s);
        return this;
    }

    public MultiLineTextBox getText() {
        return getText(0, 0);
    }

    public MultiLineTextBox getText(int index, int columnIndex) {
        return ((TextBoxElement) elements.get(columnIndex).get(index)).text;
    }

    public TextBoxElement getTextElement(int index, int columnIndex) {
        return (TextBoxElement) elements.get(columnIndex).get(index);
    }

    public UIHitPointBar getBar(int index, int columnIndex) {
        return ((HPBarBoxElement) elements.get(columnIndex).get(index)).bar;
    }

    public UIDisplayBox getDisplayBox(int index, int columnIndex) {
        return ((DisplayBoxElement) elements.get(columnIndex).get(index)).box;
    }

    public UIDisplayBox setElementEnabled(boolean enabled, int index, int columnIndex) {
        BoxElement e = elements.get(columnIndex).get(index);
        if (e.isEnabled() != enabled) {
            e.setIsEnabled(enabled);
            updateSize.run();
        }
        return this;
    }

    public UIDisplayBox setHorizontalAlign(HorizontalAlign horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
        return this;
    }

    public UIDisplayBox setColumnVerticalAlign(int index, VerticalAlign align) {
        columnVerticalAlign.set(index, align);
        return this;
    }

    public UIDisplayBox modifyBox(Consumer<UIBox> action) {
        action.accept(box);
        return this;
    }

    public float getWidth() {
        return width;
    }

    public void attemptUpdate(Graphics2D g) {
        elements.forEach(column -> column.forEach(e -> e.attemptUpdate(g)));
    }

    public HitBox getHitBox() {
        return HitBox.createFromOriginAndSize(x, y, width, height);
    }

    public HitBox getElementRenderBox(int index, int columnIndex) {
        BoxElement e = elements.get(columnIndex).get(index);
        if (!e.isEnabled())
            return null;
        ObjPos origin = new ObjPos(x, y);
        switch (horizontalAlign) {
            case CENTER -> origin.add(-width / 2, 0);
            case RIGHT -> origin.add(-width, 0);
        }
        origin.add(0, switch (columnVerticalAlign.get(columnIndex)) {
            case TOP -> height - widthMargin;
            case CENTER -> (height + columnHeights.get(columnIndex)) / 2 - widthMargin;
            case BOTTOM -> columnHeights.get(columnIndex) - widthMargin;
        });
        for (int i = 0; i < index; i++) {
            BoxElement other = elements.get(columnIndex).get(i);
            if (other.isEnabled()) {
                origin.add(0, -other.height());
            }
        }
        origin.add(switch (e.align()) {
            case LEFT -> widthMargin;
            case CENTER -> width / 2;
            case RIGHT -> width - widthMargin;
        }, 0);
        return e.renderBox().translate(origin);
    }

    @Override
    public void render(Graphics2D g) {
        attemptUpdate(g);
        if (!enabled)
            return;
        GameRenderer.renderOffset(x, y, g, () -> {
            switch (horizontalAlign) {
                case CENTER -> g.translate(-width / 2, 0);
                case RIGHT -> g.translate(-width, 0);
            }
            if (renderBox)
                box.render(g);
            for (int i = 0; i < elements.size(); i++) {
                ArrayList<BoxElement> column = elements.get(i);
                int finalI = i;
                GameRenderer.renderTransformed(g, () -> {
                    g.translate(0, switch (columnVerticalAlign.get(finalI)) {
                        case TOP -> height - widthMargin;
                        case CENTER -> (height + columnHeights.get(finalI)) / 2 - widthMargin;
                        case BOTTOM -> columnHeights.get(finalI) - widthMargin;
                    });
                    column.forEach(e -> {
                        if (!e.isEnabled())
                            return;
                        GameRenderer.renderTransformed(g, () -> {
                            g.translate(switch (e.align()) {
                                case LEFT -> widthMargin;
                                case CENTER -> width / 2;
                                case RIGHT -> width - widthMargin;
                            }, 0);
                            e.render(g);
                        });
                        g.translate(0, -e.height());
                    });
                });
            }
        });
    }

    private void setHeightToTextSize() {
        columnHeights = new ArrayList<>(elements.stream().map(column -> column.stream().filter(BoxElement::isEnabled).map(BoxElement::height).reduce(0f, Float::sum) + widthMargin * 2).toList());
        if (dynamicHeight) {
            height = columnHeights.stream().reduce(0f, Float::max);
            box.setHeight(height);
        }
    }

    private void setWidthToTextSize() {
        width = elements.stream().map(column -> column.stream().filter(BoxElement::isEnabled).map(BoxElement::width).reduce(0f, Float::max) + widthMargin * 2).reduce(0f, Float::max);
        box.setWidth(width);
    }

    public void setClickHandler(ButtonClickHandler clickHandler) {
        box.setClickHandler(clickHandler);
    }

    public void setClickHandler(ButtonClickHandler clickHandler, int index, int columnIndex) {
        elements.get(columnIndex).get(index).setClickHandler(clickHandler);
    }

    @Override
    public void delete() {
        elements.forEach(column -> column.forEach(BoxElement::delete));
        onUpdate = null;
        updateSize = null;
        box.setClickHandler(null);
    }

    public interface BoxElement extends Deletable {
        float width();

        float height();

        HorizontalAlign align();

        void render(Graphics2D g);

        void attemptUpdate(Graphics2D g);

        boolean isEnabled();

        void setIsEnabled(boolean enabled);

        HitBox renderBox();

        void setClickHandler(ButtonClickHandler clickHandler);
    }

    public static final class BoxElementSpace implements BoxElement {
        private final float height;
        public boolean enabled = true;

        public BoxElementSpace(float height) {
            this.height = height;
        }

        @Override
        public float width() {
            return 0;
        }

        @Override
        public float height() {
            return height;
        }

        @Override
        public HorizontalAlign align() {
            return HorizontalAlign.LEFT;
        }

        @Override
        public void render(Graphics2D g) {

        }

        @Override
        public void attemptUpdate(Graphics2D g) {

        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setIsEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public HitBox renderBox() {
            return null;
        }

        @Override
        public void setClickHandler(ButtonClickHandler clickHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {

        }
    }

    public static class TextBoxElement implements BoxElement {
        public final MultiLineTextBox text;
        public HorizontalAlign align;
        public boolean enabled = true;

        protected TextBoxElement(float width, float textSize, HorizontalAlign align, String s, Runnable onUpdate) {
            this.align = align;
            text = new MultiLineTextBox(0, 0, width, textSize, align).updateText(s).setOnUpdate(onUpdate);
        }

        public TextBoxElement setAlign(HorizontalAlign align) {
            this.align = align;
            text.setTextAlign(align);
            return this;
        }

        @Override
        public float width() {
            return text.getTextWidth();
        }

        @Override
        public float height() {
            return (text.rows() - 0.15f) * text.textSize;
        }

        @Override
        public HorizontalAlign align() {
            return align;
        }

        @Override
        public void render(Graphics2D g) {
            g.translate(0, -text.textSize * 0.75f);
            text.render(g);
        }

        @Override
        public void attemptUpdate(Graphics2D g) {
            text.attemptUpdate(g);
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setIsEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public HitBox renderBox() {
            return HitBox.createFromOriginAndSize(0, 0, width(), height());
        }

        @Override
        public void setClickHandler(ButtonClickHandler clickHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {
            text.delete();
        }
    }

    public static class DisplayBoxElement implements BoxElement {
        public final UIDisplayBox box;
        public final HorizontalAlign align;
        public boolean enabled = true;

        protected DisplayBoxElement(UIDisplayBox box, HorizontalAlign align, Runnable onUpdate) {
            this.box = box;
            this.align = align;
            box.addOnUpdate(onUpdate);
        }

        @Override
        public float width() {
            return box.width;
        }

        @Override
        public float height() {
            return box.height;
        }

        @Override
        public HorizontalAlign align() {
            return align;
        }

        @Override
        public void render(Graphics2D g) {
            GameRenderer.renderOffset(switch (align) {
                case LEFT -> 0;
                case CENTER -> -width() / 2;
                case RIGHT -> -width();
            }, -height(), g, () -> box.render(g));
        }

        @Override
        public void attemptUpdate(Graphics2D g) {
            box.attemptUpdate(g);
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setIsEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public HitBox renderBox() {
            return HitBox.createFromOriginAndSize(switch (align) {
                case LEFT -> 0;
                case CENTER -> -width() / 2;
                case RIGHT -> -width();
            }, -height(), width(), height());
        }

        @Override
        public void setClickHandler(ButtonClickHandler clickHandler) {
            box.setClickHandler(clickHandler);
        }

        @Override
        public void delete() {
            box.delete();
        }
    }

    public static class HPBarBoxElement implements BoxElement {
        private final UIHitPointBar bar;
        private final HorizontalAlign align;
        public boolean enabled = true;

        protected HPBarBoxElement(UIHitPointBar bar, HorizontalAlign align) {
            this.bar = bar;
            this.align = align;
        }

        @Override
        public float width() {
            return bar.barOnly ? bar.getTotalBarWidth() : bar.width;
        }

        @Override
        public float height() {
            return bar.barOnly ? bar.getSegmentHeight() : bar.height;
        }

        @Override
        public HorizontalAlign align() {
            return align;
        }

        @Override
        public void render(Graphics2D g) {
            GameRenderer.renderOffset(switch (align) {
                case LEFT -> 0;
                case CENTER -> -width() / 2;
                case RIGHT -> -width();
            } - (bar.barOnly ? bar.spacing : 0), bar.barOnly ? -bar.height + bar.spacing : -bar.height, g, () -> bar.render(g));
        }

        @Override
        public void attemptUpdate(Graphics2D g) {

        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setIsEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public HitBox renderBox() {
            return HitBox.createFromOriginAndSize(switch (align) {
                case LEFT -> 0;
                case CENTER -> -width() / 2;
                case RIGHT -> -width();
            } - (bar.barOnly ? bar.spacing : 0), bar.barOnly ? -bar.height + bar.spacing : -bar.height,
                    width(), height());
        }

        @Override
        public void setClickHandler(ButtonClickHandler clickHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {

        }
    }
}

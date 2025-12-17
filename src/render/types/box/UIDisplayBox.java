package render.types.box;

import foundation.Deletable;
import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;
import render.VerticalAlign;
import render.texture.ImageRenderer;
import render.types.UIHitPointBar;
import render.types.box.display.*;
import render.types.box.display.tutorial.TutorialMapElement;
import render.types.text.MultiLineTextBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class UIDisplayBox implements Renderable, Deletable {
    public float x, y;
    public float originalWidth, width, height, widthMargin;
    protected UIBox box;
    protected boolean renderBox = true, enabled = true;
    protected HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    protected ArrayList<Column> columns = new ArrayList<>();
    protected final boolean dynamicHeight;

    private Runnable updateSize;
    private final ArrayList<Runnable> onUpdate = new ArrayList<>();

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
                setWidthToSize();
                setHeightToSize();
                onUpdate.forEach(Runnable::run);
            };
        else
            updateSize = () -> {
                setHeightToSize();
                onUpdate.forEach(Runnable::run);
            };
    }

    public UIDisplayBox setX(float x) {
        this.x = x;
        return this;
    }

    public UIDisplayBox setY(float y) {
        this.y = y;
        return this;
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

    public int getLastIndex(int column) {
        return columns.get(column).elements.size() - 1;
    }

    public UIDisplayBox addOnUpdate(Runnable onUpdate) {
        this.onUpdate.add(onUpdate);
        return this;
    }

    public UIDisplayBox addText(float textSize, HorizontalAlign align, String s) {
        return addText(textSize, align, 0, s);
    }

    public UIDisplayBox addText(float textSize, HorizontalAlign align, int columnIndex, String s) {
        updateColumnCount(columnIndex);
        elements(columnIndex).add(new TextBoxElement(widthMargin, originalWidth - widthMargin * 2, textSize, align, s, updateSize, false));
        return this;
    }

    public DisplayBoxParagraph addParagraph(String header, String text) {
        return new DisplayBoxParagraph(header, text, this);
    }

    public DisplayBoxParagraph addParagraph(DisplayBoxParagraph paragraph) {
        int c = paragraph.column;
        if (paragraph.topSpacing != 0) {
            addSpace(paragraph.topSpacing, c);
            paragraph.elementCount++;
        }
        if (paragraph.header != null) {
            addText(paragraph.headerSize, HorizontalAlign.LEFT, c, paragraph.header);
            if (paragraph.dynamicWidth)
                getTextElement(getLastIndex(c), c).setMaxWidth(true);
            addSpace(0.3f, c);
            paragraph.elementCount += 2;
        }
        addText(0.7f, HorizontalAlign.LEFT, c, paragraph.text);
        if (paragraph.dynamicWidth)
            getTextElement(getLastIndex(c), c).setMaxWidth(true);
        paragraph.elementCount++;
        if (paragraph.bottomSpacing != 0) {
            addSpace(paragraph.bottomSpacing, c);
            paragraph.elementCount++;
        }
        paragraph.firstElementIndex = getLastIndex(c) - paragraph.elementCount + 1;
        return paragraph;
    }

    public UIDisplayBox addRenderable(int columnIndex, float width, float height, HorizontalAlign align, Renderable renderable) {
        updateColumnCount(columnIndex);
        RenderableElement e = new RenderableElement(widthMargin, width, height, align, renderable);
        elements(columnIndex).add(e);
        if (e.maxWidth)
            e.setWidth(width - e.rightMargin - e.leftMargin);
        return this;
    }

    public UIDisplayBox addElement(int columnIndex, BoxElement e) {
        updateColumnCount(columnIndex);
        if (e.leftMargin == -1 && e.rightMargin == -1)
            e.setMargin(widthMargin);
        elements(columnIndex).add(e);
        if (e.maxWidth())
            e.setWidth(width - e.rightMargin - e.leftMargin);
        return this;
    }

    public UIDisplayBox addImage(int columnIndex, float width, float height, float renderWidth, HorizontalAlign align, ImageRenderer image) {
        updateColumnCount(columnIndex);
        ImageElement e = new ImageElement(widthMargin, width, height, renderWidth, align, image);
        elements(columnIndex).add(e);
        if (e.maxWidth)
            e.setWidth(width - e.rightMargin - e.leftMargin);
        return this;
    }

    public UIDisplayBox setImage(int index, int columnIndex, ImageRenderer image) {
        ((ImageElement) element(columnIndex, index)).image = image;
        return this;
    }

    public UIDisplayBox addBox(UIDisplayBox box, HorizontalAlign align, int columnIndex, boolean maxWith) {
        updateColumnCount(columnIndex);
        DisplayBoxElement e = new DisplayBoxElement(widthMargin, box, align, updateSize, maxWith);
        elements(columnIndex).add(e);
        if (maxWith)
            e.setWidth(width - e.rightMargin - e.leftMargin);
        return this;
    }

    public UIDisplayBox addBar(UIHitPointBar bar, HorizontalAlign align, int columnIndex, boolean maxWidth) {
        updateColumnCount(columnIndex);
        if (maxWidth)
            bar.setWidth(bar.barOnly ? originalWidth - widthMargin * 2 + bar.spacing * 2 : originalWidth - widthMargin * 2);
        elements(columnIndex).add(new HPBarBoxElement(widthMargin, bar, align));
        return this;
    }

    public UIDisplayBox addSpace(float height, int columnIndex) {
        updateColumnCount(columnIndex);
        elements(columnIndex).add(new BoxElementSpace(height));
        updateSize.run();
        return this;
    }

    public UIDisplayBox addTutorialMap(HorizontalAlign align, float width, float height, float tileSize, float lifetime, int columnIndex, Consumer<TutorialMapElement> action) {
        updateColumnCount(columnIndex);
        TutorialMapElement e = new TutorialMapElement(widthMargin, align, width, height, tileSize, lifetime);
        elements(columnIndex).add(e);
        action.accept(e);
        updateSize.run();
        return this;
    }

    public UIDisplayBox setColumnTopMarginToElement(int columnIndex, int otherColumn, int element, VerticalAlign otherAlign) {
        updateColumnCount(columnIndex);
        addOnUpdate(() -> {
            float newMargin = height - getColumnYOffset(otherColumn) + element(otherColumn, element).height() * otherAlign.ordinal() / 2;
            for (int i = 0; i < element; i++) {
                newMargin += element(otherColumn, i).height();
            }
            Column column = columns.get(columnIndex);
            if (!MathUtil.equal(newMargin, column.topMargin, 0.01f)) {
                column.topMargin = newMargin;
                updateSize.run();
            }
        });
        updateSize.run();
        return this;
    }

    public UIDisplayBox setColumnBottomMarginToElement(int columnIndex, int otherColumn, int element, VerticalAlign otherAlign) {
        updateColumnCount(columnIndex);
        addOnUpdate(() -> {
            float newMargin = getColumnYOffset(otherColumn) + element(otherColumn, element).height() * (1 - otherAlign.ordinal() / 2f);
            for (int i = 0; i <= element; i++) {
                newMargin -= element(otherColumn, i).height();
            }
            Column column = columns.get(columnIndex);
            if (!MathUtil.equal(newMargin, column.bottomMargin, 0.01f)) {
                column.bottomMargin = newMargin;
                updateSize.run();
            }
        });
        updateSize.run();
        return this;
    }

    public UIDisplayBox setColumnTopMargin(float margin, int columnIndex) {
        updateColumnCount(columnIndex);
        columns.get(columnIndex).topMargin = margin + widthMargin;
        return this;
    }

    public UIDisplayBox setColumnBottomMargin(float margin, int columnIndex) {
        updateColumnCount(columnIndex);
        columns.get(columnIndex).bottomMargin = margin + widthMargin;
        return this;
    }

    public UIDisplayBox setElementLeftMarginToElement(int columnIndex, int element, int otherColumn, int otherElement, HorizontalAlign otherElementAlign, float offset) {
        addOnUpdate(() -> {
            BoxElement e = element(columnIndex, element);
            BoxElement eOther = element(otherColumn, otherElement);
            float newMargin = getElementXOffset(eOther) + eOther.width() * (otherElementAlign.ordinal() - eOther.align().ordinal()) / 2 + offset;
            if (!MathUtil.equal(newMargin, e.leftMargin, 0.01f)) {
                e.leftMargin = newMargin;
                updateElementWidth(e);
                updateSize.run();
            }
        });
        updateSize.run();
        return this;
    }

    public UIDisplayBox setElementRightMarginToElement(int columnIndex, int element, int otherColumn, int otherElement, HorizontalAlign otherElementAlign, float offset) {
        addOnUpdate(() -> {
            BoxElement e = element(columnIndex, element);
            BoxElement eOther = element(otherColumn, otherElement);
            float newMargin = width - (getElementXOffset(eOther) + eOther.width() * (otherElementAlign.ordinal() - eOther.align().ordinal()) / 2) + offset;
            if (!MathUtil.equal(newMargin, e.rightMargin, 0.01f)) {
                e.rightMargin = newMargin;
                updateElementWidth(e);
                updateSize.run();
            }
        });
        updateSize.run();
        return this;
    }

    public UIDisplayBox setElementLeftMargin(float margin, int columnIndex, int i) {
        BoxElement e = element(columnIndex, i);
        e.leftMargin = margin + widthMargin;
        updateElementWidth(e);
        return this;
    }

    public UIDisplayBox setElementRightMargin(float margin, int columnIndex, int i) {
        BoxElement e = element(columnIndex, i);
        e.rightMargin = margin + widthMargin;
        updateElementWidth(e);
        return this;
    }

    public float columnHeight(int columnIndex) {
        return columns.get(columnIndex).height;
    }

    private void updateColumnCount(int lastIndex) {
        while (columns.size() <= lastIndex) {
            columns.add(new Column(widthMargin));
        }
    }

    public UIDisplayBox setText(String s) {
        return setText(0, 0, s);
    }

    public UIDisplayBox setText(int index, int columnIndex, String s) {
        ((TextBoxElement) element(columnIndex, index)).text.updateText(s);
        return this;
    }

    public MultiLineTextBox getText() {
        return getText(0, 0);
    }

    public MultiLineTextBox getText(int index, int columnIndex) {
        return ((TextBoxElement) element(columnIndex, index)).text;
    }

    public TextBoxElement getTextElement(int index, int columnIndex) {
        return (TextBoxElement) element(columnIndex, index);
    }

    public UIHitPointBar getBar(int index, int columnIndex) {
        return ((HPBarBoxElement) element(columnIndex, index)).bar;
    }

    public UIDisplayBox getDisplayBox(int index, int columnIndex) {
        return ((DisplayBoxElement) element(columnIndex, index)).box;
    }

    public TutorialMapElement getTutorialMap(int index, int columnIndex) {
        return ((TutorialMapElement) element(columnIndex, index));
    }

    public UIDisplayBox setElementEnabled(boolean enabled, int index, int columnIndex) {
        BoxElement e = element(columnIndex, index);
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
        updateColumnCount(index);
        columns.get(index).align = align;
        updateSize.run();
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
        columns.forEach(column -> column.elements.forEach(e -> e.attemptUpdate(g)));
    }

    public HitBox getHitBox() {
        return HitBox.createFromOriginAndSize(x, y, width, height);
    }

    public HitBox getElementRenderBox(int index, int columnIndex) {
        BoxElement e = element(columnIndex, index);
        if (!e.isEnabled())
            return null;
        ObjPos origin = new ObjPos(x, y);
        switch (horizontalAlign) {
            case CENTER -> origin.add(-width / 2, 0);
            case RIGHT -> origin.add(-width, 0);
        }
        origin.add(0, getColumnYOffset(columnIndex));
        for (int i = 0; i < index; i++) {
            BoxElement other = element(columnIndex, i);
            if (other.isEnabled()) {
                origin.add(0, -other.height());
            }
        }
        origin.add(getElementXOffset(e), 0);
        return e.renderBox().translate(origin);
    }

    private float getElementXOffset(BoxElement e) {
        return switch (e.align()) {
            case LEFT -> e.leftMargin;
            case CENTER -> (width + e.leftMargin - e.rightMargin) / 2;
            case RIGHT -> width - e.rightMargin;
        };
    }

    private float getColumnYOffset(int columnIndex) {
        Column column = columns.get(columnIndex);
        return switch (column.align) {
            case TOP -> height - column.topMargin;
            case CENTER -> (height - column.topMargin + column.height + column.bottomMargin) / 2;
            case BOTTOM -> column.height + column.bottomMargin;
        };
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
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                int finalI = i;
                GameRenderer.renderTransformed(g, () -> {
                    g.translate(0, getColumnYOffset(finalI));
                    column.elements.forEach(e -> {
                        if (!e.isEnabled())
                            return;
                        GameRenderer.renderTransformed(g, () -> {
                            g.translate(getElementXOffset(e), 0);
                            e.render(g);
                        });
                        g.translate(0, -e.height());
                    });
                });
            }
        });
    }

    private void setHeightToSize() {
        for (Column column : columns) {
            column.height = column.elements.stream().filter(BoxElement::isEnabled).map(BoxElement::height).reduce(0f, Float::sum);
        }
        if (dynamicHeight) {
            height = columns.stream().map(column -> column.height + column.topMargin + column.bottomMargin).reduce(0f, Float::max);
            box.setHeight(height);
        }
    }

    private void setWidthToSize() {
        width = columns.stream().map(column -> column.elements.stream().filter(BoxElement::isEnabled).map(BoxElement::width).reduce(0f, Float::max) + widthMargin * 2).reduce(0f, Float::max);
        box.setWidth(width);
    }

    public void setWidth(float width) {
        this.width = width;
        originalWidth = width;
        box.setWidth(width);
        updateSize.run();
    }

    public void setClickHandler(ButtonClickHandler clickHandler) {
        box.setClickHandler(clickHandler);
    }

    public void setClickHandler(ButtonClickHandler clickHandler, int index, int columnIndex) {
        element(columnIndex, index).setClickHandler(clickHandler);
    }

    private ArrayList<BoxElement> elements(int columnIndex) {
        return columns.get(columnIndex).elements;
    }

    private BoxElement element(int columnIndex, int i) {
        return elements(columnIndex).get(i);
    }

    private void updateElementWidth(BoxElement e) {
        e.setWidth(width - e.leftMargin - e.rightMargin);
    }

    @Override
    public void delete() {
        columns.forEach(column -> column.elements.forEach(BoxElement::delete));
        columns.clear();
        onUpdate.clear();
        updateSize = null;
        box.setClickHandler(null);
    }

    protected static class Column {
        public VerticalAlign align = VerticalAlign.CENTER;
        public float height = 0, topMargin, bottomMargin;
        public ArrayList<BoxElement> elements = new ArrayList<>();

        public Column(float initialMargin) {
            topMargin = initialMargin;
            bottomMargin = initialMargin;
        }
    }
}

package render.types.box;

import java.util.function.Consumer;

public class DisplayBoxParagraph {
    public float topSpacing = 1, bottomSpacing = 0;
    public float headerSize = 1;
    public int column = 0;
    public String header;
    public String text;
    public final UIDisplayBox box;
    public int elementCount = 0;
    public int firstElementIndex = 0;
    public boolean dynamicWidth = false;

    public DisplayBoxParagraph(String header, String text, UIDisplayBox box) {
        this.header = header;
        this.text = text;
        this.box = box;
    }

    public DisplayBoxParagraph setTopSpacing(float topSpacing) {
        this.topSpacing = topSpacing;
        return this;
    }

    public DisplayBoxParagraph setHeaderSize(float headerSize) {
        this.headerSize = headerSize;
        return this;
    }

    public DisplayBoxParagraph setColumn(int column) {
        this.column = column;
        return this;
    }

    public DisplayBoxParagraph setBottomSpacing(float bottomSpacing) {
        this.bottomSpacing = bottomSpacing;
        return this;
    }

    public DisplayBoxParagraph setDynamicWidth() {
        dynamicWidth = true;
        return this;
    }

    public DisplayBoxParagraph mainHeader() {
        return setTopSpacing(0).setHeaderSize(1.2f);
    }

    public DisplayBoxParagraph defaultBottomSpacing() {
        return setBottomSpacing(0.5f);
    }

    public DisplayBoxParagraph finalise() {
        return box.addParagraph(this);
    }

    public DisplayBoxParagraph forEachElement(Consumer<Integer> action) {
        for (int i = firstElementIndex; i < firstElementIndex + elementCount; i++) {
            action.accept(i);
        }
        return this;
    }
}
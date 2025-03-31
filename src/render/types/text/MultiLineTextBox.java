package render.types.text;

import render.GameRenderer;
import render.Renderable;

import java.awt.*;
import java.util.ArrayList;

public class MultiLineTextBox implements Renderable {
    private String s, newString;
    private final float x, y, width;
    public float textSize;
    private final TextAlign textAlign;
    private final ArrayList<FixedTextRenderer> textRenderers = new ArrayList<>();
    private Color textColour = UITextLabel.TEXT_COLOUR;
    private boolean bold = true;
    private boolean forceUpdate = false;

    public MultiLineTextBox(float x, float y, float width, float textSize, TextAlign textAlign) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.textSize = textSize;
        this.textAlign = textAlign;
    }

    public MultiLineTextBox updateText(String s) {
        newString = s;
        if (newString == null)
            textRenderers.clear();
        return this;
    }

    private void updateText(Graphics2D g) {
        textRenderers.clear();
        FixedTextRenderer prevText = null;
        StringBuilder builder = new StringBuilder();
        int prevSpace = 0;
        while (true) {
            int nextSpace = s.indexOf(' ', prevSpace + 1);
            int nextBreak = s.indexOf('\n', prevSpace + 1);
            boolean lineBreak;
            if (nextBreak != -1 && (nextBreak < nextSpace || nextSpace == -1)) {
                nextSpace = nextBreak;
                lineBreak = true;
            } else
                lineBreak = false;
            if (nextSpace == -1) {
                builder.append(s, prevSpace, s.length());
            } else {
                builder.append(s, prevSpace, nextSpace);
            }
            FixedTextRenderer text = new FixedTextRenderer(builder.toString(), textSize, textColour)
                    .setBold(bold).setTextAlign(textAlign);
            text.updateText(g);

            if (text.getTextWidth() > width) {
                if (prevText == null)
                    throw new RuntimeException("Multiline text box was not wide enough to fit the provided text: " + s);
                textRenderers.add(prevText);
                builder = new StringBuilder();
                prevSpace++;
                prevText = null;
            } else if (lineBreak) {
                textRenderers.add(text);
                builder = new StringBuilder();
                prevSpace = nextSpace;
                prevText = null;
            } else {
                prevText = text;
                prevSpace = nextSpace;
                if (nextSpace == -1) {
                    textRenderers.add(text);
                    break;
                }
            }
        }
    }

    public MultiLineTextBox setTextColour(Color textColour) {
        this.textColour = textColour;
        forceUpdate = true;
        return this;
    }

    public MultiLineTextBox setTextSize(float textSize) {
        this.textSize = textSize;
        forceUpdate = true;
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        if (newString != null) {
            if (!newString.equals(s)) {
                s = newString;
                updateText(g);
                newString = null;
            }
        }
        if (forceUpdate) {
            forceUpdate = false;
            if (s != null)
                updateText(g);
        }
        for (int i = 0; i < textRenderers.size(); i++) {
            int finalI = i;
            GameRenderer.renderOffset(x, y - textSize * i, g, () -> {
                textRenderers.get(finalI).render(g);
            });
        }
    }
}

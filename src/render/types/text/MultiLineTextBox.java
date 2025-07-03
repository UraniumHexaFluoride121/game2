package render.types.text;

import foundation.Deletable;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;

import java.awt.*;
import java.util.ArrayList;

public class MultiLineTextBox implements Renderable, Deletable {
    private String s, newString;
    private final float x, y;
    private float width;
    public float textSize;
    private HorizontalAlign textAlign;
    private final ArrayList<TextRenderer> textRenderers = new ArrayList<>();
    private Color textColour = UITextLabel.TEXT_COLOUR;
    private boolean bold = true, italic = false;
    private boolean forceUpdate = false;
    private Runnable updateCallback = null;

    public MultiLineTextBox(float x, float y, float width, float textSize, HorizontalAlign textAlign) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.textSize = textSize;
        this.textAlign = textAlign;
    }

    public MultiLineTextBox updateText(String s) {
        newString = s;
        if (newString == null) {
            this.s = null;
            textRenderers.clear();
        }
        return this;
    }

    private void updateText(Graphics2D g) {
        textRenderers.clear();
        TextRenderer prevText = null;
        TextStyle prevStyle = new TextStyle();
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
            TextRenderer text = new TextRenderer(builder.toString(), textSize, textColour)
                    .setBold(bold).setItalic(italic).setTextAlign(textAlign);
            text.setInitialStyle(prevStyle);
            text.updateText(g);

            if (text.getTextWidth() > width) {
                if (prevText == null)
                    throw new RuntimeException("Multiline text box was not wide enough to fit the provided text: " + s);
                textRenderers.add(prevText);
                builder = new StringBuilder();
                prevSpace++;
                prevStyle = prevText.getFinalStyle();
                prevText = null;
            } else if (lineBreak) {
                textRenderers.add(text);
                builder = new StringBuilder();
                prevSpace = nextSpace;
                prevStyle = text.getFinalStyle();
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
        if (updateCallback != null)
            updateCallback.run();
    }

    public MultiLineTextBox setWidth(float width) {
        this.width = width;
        forceUpdate = true;
        return this;
    }

    public MultiLineTextBox setItalic(boolean italic) {
        this.italic = italic;
        forceUpdate = true;
        return this;
    }

    public MultiLineTextBox setTextAlign(HorizontalAlign textAlign) {
        this.textAlign = textAlign;
        return this;
    }

    public MultiLineTextBox setOnUpdate(Runnable callback) {
        updateCallback = callback;
        return this;
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

    public float getTextWidth() {
        float max = 0;
        for (TextRenderer t : textRenderers) {
            max = Math.max(max, t.getTextWidth());
        }
        return max;
    }

    public int rows() {
        return textRenderers.size();
    }

    public void attemptUpdate(Graphics2D g) {
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
    }

    public boolean isEmpty() {
        return rows() == 0;
    }

    @Override
    public void render(Graphics2D g) {
        attemptUpdate(g);
        for (int i = 0; i < textRenderers.size(); i++) {
            int finalI = i;
            GameRenderer.renderOffset(x, y - textSize * i, g, () -> {
                textRenderers.get(finalI).render(g);
            });
        }
    }

    @Override
    public void delete() {
        updateCallback = null;
    }
}

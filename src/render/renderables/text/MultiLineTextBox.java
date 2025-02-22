package render.renderables.text;

import render.GameRenderer;
import render.Renderable;
import render.ui.types.UITextLabel;

import java.awt.*;
import java.util.ArrayList;

public class MultiLineTextBox implements Renderable {
    private String s, newString;
    private final float x, y, width, textSize;
    private final TextAlign textAlign;
    private final ArrayList<FixedTextRenderer> textRenderers = new ArrayList<>();
    private Color textColour = UITextLabel.TEXT_COLOUR;
    private boolean bold = true;

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
        if (newString.equals(this.s))
            return;
        s = newString;
        textRenderers.clear();
        FixedTextRenderer prevText = null;
        StringBuilder builder = new StringBuilder();
        int prevSpace = 0;
        while (true) {
            int nextSpace = s.indexOf(' ', prevSpace + 1);
            if (nextSpace == -1) {
                builder.append(s, prevSpace, s.length());
            } else {
                builder.append(s, prevSpace, nextSpace);
            }
            FixedTextRenderer text = new FixedTextRenderer(builder.toString(), textSize, textColour);
            text.updateText(g);
            if (text.getTextWidth() > width) {
                if (prevText == null)
                    throw new RuntimeException("Multiline text box was not wide enough to fit the provided text: " + s);
                textRenderers.add(prevText);
                builder = new StringBuilder();
                prevSpace++;
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
        for (FixedTextRenderer text : textRenderers) {
            text.setBold(bold).setTextAlign(textAlign);
        }
    }

    public MultiLineTextBox setTextColour(Color textColour) {
        this.textColour = textColour;
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        if (newString != null) {
            updateText(g);
            newString = null;
        }
        for (int i = 0; i < textRenderers.size(); i++) {
            int finalI = i;
            GameRenderer.renderOffset(x, y - textSize * i, g, () -> {
                textRenderers.get(finalI).render(g);
            });
        }
    }
}

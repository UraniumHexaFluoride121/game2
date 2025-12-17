package render.types.text;

import render.Renderable;

public interface TextObject extends Renderable {
    TextObject updateText(String s);
}

package unit.stats;

import render.types.text.StyleElement;

public interface ColouredIconName extends ColouredName {
    String getIcon();

    default String colouredIconName(StyleElement end, boolean lowerCase) {
        String s = (lowerCase ? getName().toLowerCase() : getName()) + getIcon();
        if (end == null)
            return colour() + s;
        else
            return colour() + s + end.display;
    }
}

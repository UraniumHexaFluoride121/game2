package unit.stats;

import foundation.NamedEnum;
import render.types.text.StyleElement;

public interface ColouredName extends NamedEnum {
    String colour();

    default String colouredName(StyleElement end, boolean lowerCase) {
        String s = lowerCase ? getName().toLowerCase() : getName();
        if (end == null)
            return colour() + s;
        else
            return colour() + s + end.display;
    }
}

package foundation.input;

import render.types.text.TooltipManager;

import java.util.function.Consumer;

public interface TooltipHolder {
    ButtonClickHandler getClickHandler();

    TooltipManager getManager();

    default TooltipHolder tooltip(Consumer<TooltipManager> action) {
        action.accept(getManager());
        return this;
    }
}

package render.anim;

import java.util.function.UnaryOperator;

public record KeyframeFunction(UnaryOperator<Float> function) {
    public static KeyframeFunction lerp() {
        return new KeyframeFunction(v -> v);
    }

    public static KeyframeFunction pow(float exp) {
        return new KeyframeFunction(v -> (float) Math.pow(v, exp));
    }
}

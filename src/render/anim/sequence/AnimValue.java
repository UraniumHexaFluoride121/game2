package render.anim.sequence;

@FunctionalInterface
public interface AnimValue {
    float getValue(float time);

    default AnimValue andThen(AnimValue value) {
        return t -> value.getValue(this.getValue(t));
    }

    AnimValue UNCHANGED = t -> t;

    static AnimValue staticValue(float value) {
        return t -> value;
    }

    static AnimValue scaledValue(float scale) {
        return t -> t * scale;
    }
}

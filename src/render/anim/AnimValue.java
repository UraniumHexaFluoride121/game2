package render.anim;

@FunctionalInterface
public interface AnimValue {
    float getValue(float time);

    AnimValue UNCHANGED = v -> v;
}

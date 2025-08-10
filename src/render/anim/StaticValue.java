package render.anim;

public record StaticValue(float value) implements AnimValue {
    @Override
    public float getValue(float time) {
        return value;
    }
}

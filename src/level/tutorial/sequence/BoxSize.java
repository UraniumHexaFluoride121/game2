package level.tutorial.sequence;

public enum BoxSize {
    SMALL(7),
    SMALL_MEDIUM(10),
    MEDIUM(12),
    LARGE(15),
    EXTRA_LARGE(17),
    EXTRA_EXTRA_LARGE(19);

    public final float width;

    BoxSize(float width) {
        this.width = width;
    }
}

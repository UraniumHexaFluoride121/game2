package level.tile;

public enum TileTypeCharacteristic {
    HIGH(3), MEDIOCRE(2), LOW(1), VERY_LOW(0.5f), NONE(0);

    public final float barFill;

    TileTypeCharacteristic(float barFill) {
        this.barFill = barFill;
    }
}

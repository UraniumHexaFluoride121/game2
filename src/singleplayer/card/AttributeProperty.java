package singleplayer.card;

public record AttributeProperty(float value, Object... obj) {
    public static final Object INCOME = new Object();
    public static final Object OFFENCE = new Object();
    public static final Object DEFENCE = new Object();
    public static final Object MOVE_SPEED = new Object();
    public static final Object ACTION_COST = new Object();
    public static final Object UNIQUE = new Object();
}

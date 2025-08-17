package foundation.math;

import unit.UnitPose;

import java.awt.*;

public enum HexagonalDirection {
    UP, DOWN, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT;

    public Point offset(Point p) {
        return switch (this) {
            case UP -> new Point(p.x, p.y + 1);
            case DOWN -> new Point(p.x, p.y - 1);
            case UP_LEFT -> new Point(p.x - 1, p.y + 1 - (p.x & 1));
            case UP_RIGHT -> new Point(p.x + 1, p.y + 1 - (p.x & 1));
            case DOWN_LEFT -> new Point(p.x - 1, p.y - (p.x & 1));
            case DOWN_RIGHT -> new Point(p.x + 1, p.y - (p.x & 1));
        };
    }

    public UnitPose getPose() {
        return switch (this) {
            case UP -> UnitPose.UP;
            case DOWN -> UnitPose.DOWN;
            case UP_LEFT -> UnitPose.UP_LEFT;
            case UP_RIGHT -> UnitPose.UP_RIGHT;
            case DOWN_LEFT -> UnitPose.DOWN_LEFT;
            case DOWN_RIGHT -> UnitPose.DOWN_RIGHT;
        };
    }

    public HexagonCorner clockwiseCorner() {
        return switch (this) {
            case DOWN -> HexagonCorner.BOTTOM_LEFT;
            case DOWN_LEFT -> HexagonCorner.MID_LEFT;
            case UP_LEFT -> HexagonCorner.TOP_LEFT;
            case UP -> HexagonCorner.TOP_RIGHT;
            case UP_RIGHT -> HexagonCorner.MID_RIGHT;
            case DOWN_RIGHT -> HexagonCorner.BOTTOM_RIGHT;
        };
    }

    public HexagonCorner counterClockwiseCorner() {
        return switch (this) {
            case DOWN -> HexagonCorner.BOTTOM_RIGHT;
            case DOWN_LEFT -> HexagonCorner.BOTTOM_LEFT;
            case UP_LEFT -> HexagonCorner.MID_LEFT;
            case UP -> HexagonCorner.TOP_LEFT;
            case UP_RIGHT -> HexagonCorner.TOP_RIGHT;
            case DOWN_RIGHT -> HexagonCorner.MID_RIGHT;
        };
    }

    public HexagonalDirection clockwise() {
        return switch (this) {
            case DOWN -> DOWN_LEFT;
            case DOWN_LEFT -> UP_LEFT;
            case UP_LEFT -> UP;
            case UP -> UP_RIGHT;
            case UP_RIGHT -> DOWN_RIGHT;
            case DOWN_RIGHT -> DOWN;
        };
    }

    public HexagonalDirection counterClockwise() {
        return switch (this) {
            case DOWN -> DOWN_RIGHT;
            case DOWN_LEFT -> DOWN;
            case UP_LEFT -> DOWN_LEFT;
            case UP -> UP_LEFT;
            case UP_RIGHT -> UP;
            case DOWN_RIGHT -> UP_RIGHT;
        };
    }

    public static boolean isNextTo(Point p1, Point p2) {
        for (HexagonalDirection d : values()) {
            if (d.offset(p1).equals(p2))
                return true;
        }
        return false;
    }

    public static HexagonalDirection directionTo(Point p1, Point p2) {
        for (HexagonalDirection d : values()) {
            if (d.offset(p1).equals(p2))
                return d;
        }
        return null;
    }
}

package foundation.math;

import unit.UnitPose;

import java.awt.*;

public enum HexagonalDirection {
    UP, DOWN, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT;

    public Point offset(Point p) {
        return switch (this) {
            case UP -> new Point(p.x, p.y + 1);
            case DOWN -> new Point(p.x, p.y - 1);
            case UP_LEFT -> new Point(p.x - 1, p.y + 1 - p.x % 2);
            case UP_RIGHT -> new Point(p.x + 1, p.y + 1 - p.x % 2);
            case DOWN_LEFT -> new Point(p.x - 1, p.y - p.x % 2);
            case DOWN_RIGHT -> new Point(p.x + 1, p.y - p.x % 2);
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

package foundation.math;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

public enum HexagonCorner {
    TOP_LEFT, TOP_RIGHT,
    MID_LEFT, MID_RIGHT,
    BOTTOM_LEFT, BOTTOM_RIGHT;

    public BorderInfo getConnectingCorner(Point thisPoint, HashMap<Point, HashSet<HexagonalDirection>> borders) {
        return switch (this) {
            case BOTTOM_RIGHT -> {
                Point point = HexagonalDirection.DOWN.offset(thisPoint);
                HexagonalDirection d = containsCorner(TOP_RIGHT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                point = HexagonalDirection.DOWN_RIGHT.offset(thisPoint);
                d = containsCorner(MID_LEFT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                yield null;
            }
            case BOTTOM_LEFT -> {
                Point point = HexagonalDirection.DOWN.offset(thisPoint);
                HexagonalDirection d = containsCorner(TOP_LEFT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                point = HexagonalDirection.DOWN_LEFT.offset(thisPoint);
                d = containsCorner(MID_RIGHT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                yield null;
            }
            case MID_LEFT -> {
                Point point = HexagonalDirection.DOWN_LEFT.offset(thisPoint);
                HexagonalDirection d = containsCorner(TOP_RIGHT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                point = HexagonalDirection.UP_LEFT.offset(thisPoint);
                d = containsCorner(BOTTOM_RIGHT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                yield null;
            }
            case TOP_LEFT -> {
                Point point = HexagonalDirection.UP_LEFT.offset(thisPoint);
                HexagonalDirection d = containsCorner(MID_RIGHT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                point = HexagonalDirection.UP.offset(thisPoint);
                d = containsCorner(BOTTOM_LEFT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                yield null;
            }
            case TOP_RIGHT -> {
                Point point = HexagonalDirection.UP_RIGHT.offset(thisPoint);
                HexagonalDirection d = containsCorner(MID_LEFT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                point = HexagonalDirection.UP.offset(thisPoint);
                d = containsCorner(BOTTOM_RIGHT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                yield null;
            }
            case MID_RIGHT -> {
                Point point = HexagonalDirection.DOWN_RIGHT.offset(thisPoint);
                HexagonalDirection d = containsCorner(TOP_LEFT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                point = HexagonalDirection.UP_RIGHT.offset(thisPoint);
                d = containsCorner(BOTTOM_LEFT, borders.get(point));
                if (d != null)
                    yield new BorderInfo(d, point);
                yield null;
            }
        };
    }

    private HexagonalDirection containsCorner(HexagonCorner corner, HashSet<HexagonalDirection> borders) {
        if (borders == null)
            return null;
        for (HexagonalDirection border : borders) {
            if (border.clockwiseCorner() == corner || border.counterClockwiseCorner() == corner)
                return border;
        }
        return null;
    }

    public record BorderInfo(HexagonalDirection border, Point point) {

    }
}

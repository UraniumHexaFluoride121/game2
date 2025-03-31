package level.tile;

import foundation.math.ObjPos;

import java.awt.*;

@FunctionalInterface
public interface TileImageRenderer {
    void render(Graphics2D g, ObjPos pos, TileType type);
}

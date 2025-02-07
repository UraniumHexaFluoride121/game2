package level.tile;

import render.texture.AsyncImageSequence;
import render.texture.CachedImageSequence;
import render.texture.ImageSequence;

import static level.tile.TileTypeCharacteristic.*;

public enum TileType {
    EMPTY("Empty", HIGH, NONE, HIGH,
            null,
            new AsyncImageSequence("background/firing/empty/", 5, 1, 1, false),
            null
    ),
    NEBULA("Nebula", LOW, LOW, MEDIOCRE,
            new CachedImageSequence("tiles/nebula/", 20, 1, 1, true),
            new AsyncImageSequence("background/firing/nebula/", 10, 1, 1, false),
            null
    ),
    DENSE_NEBULA("Dense Nebula", NONE, MEDIOCRE, LOW,
            new CachedImageSequence("tiles/denseNebula/", 10, 1, 1, true),
            new AsyncImageSequence("background/firing/denseNebula/left/", 10, 1, 1, false),
            new AsyncImageSequence("background/firing/denseNebula/right/", 10, 1, 1, false)
    ),
    ASTEROIDS("Asteroid Field", MEDIOCRE, HIGH, VERY_LOW,
            new CachedImageSequence("tiles/asteroids/asteroids", 7, true),
            new AsyncImageSequence("background/firing/asteroids/bg_firing_asteroids_left", 5, false),
            new AsyncImageSequence("background/firing/asteroids/bg_firing_asteroids_right", 5, false)
    );

    public final String displayName;
    public final TileTypeCharacteristic visibility, defence, movement;
    public final ImageSequence tileTextures, firingTexturesLeft, firingTexturesRight;

    TileType(String displayName, TileTypeCharacteristic visibility, TileTypeCharacteristic defence, TileTypeCharacteristic movement, ImageSequence tileTextures, ImageSequence firingTexturesLeft, ImageSequence firingTexturesRight) {
        this.displayName = displayName;
        this.visibility = visibility;
        this.defence = defence;
        this.movement = movement;
        this.tileTextures = tileTextures;
        this.firingTexturesLeft = firingTexturesLeft;
        if (firingTexturesRight == null)
            this.firingTexturesRight = firingTexturesLeft;
        else
            this.firingTexturesRight = firingTexturesRight;
    }
}

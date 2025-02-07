package level.tile;

import render.texture.AsyncImageSequence;
import render.texture.CachedImageSequence;
import render.texture.ImageSequence;

public enum TileType {
    EMPTY(null,
            new AsyncImageSequence("background/firing/empty/", 5, 1, 1, false),
            null
    ),
    NEBULA(new CachedImageSequence("tiles/nebula/", 20, 1, 1, true),
            new AsyncImageSequence("background/firing/nebula/", 10, 1, 1, false),
            null
    ),
    DENSE_NEBULA(new CachedImageSequence("tiles/denseNebula/", 10, 1, 1, true),
            new AsyncImageSequence("background/firing/denseNebula/left/", 10, 1, 1, false),
            new AsyncImageSequence("background/firing/denseNebula/right/", 10, 1, 1, false)
    ),
    ASTEROIDS(
            new CachedImageSequence("tiles/asteroids/asteroids", 7, true),
            new AsyncImageSequence("background/firing/asteroids/bg_firing_asteroids_left", 5, false),
            new AsyncImageSequence("background/firing/asteroids/bg_firing_asteroids_right", 5, false)
    );

    public final ImageSequence tileTextures, firingTexturesLeft, firingTexturesRight;

    TileType(ImageSequence tileTextures, ImageSequence firingTexturesLeft, ImageSequence firingTexturesRight) {
        this.tileTextures = tileTextures;
        this.firingTexturesLeft = firingTexturesLeft;
        if (firingTexturesRight == null)
            this.firingTexturesRight = firingTexturesLeft;
        else
            this.firingTexturesRight = firingTexturesRight;
    }
}

package level.tile;

import foundation.MainPanel;
import render.texture.AsyncImageSequence;
import render.texture.CachedImageSequence;
import render.texture.ImageSequence;
import render.types.text.StyleElement;
import unit.stats.ColouredName;
import unit.stats.Modifier;
import unit.stats.modifiers.TileSingleModifier;

import java.util.function.Function;
import java.util.function.Supplier;

import static level.tile.TileTypeCharacteristic.*;

public enum TileType implements ColouredName {
    EMPTY("Empty", StyleElement.EMPTY_TILE, HIGH, NONE, HIGH,
            TileSingleModifier.EMPTY, 1, 1f,
            null,
            new AsyncImageSequence("background/firing/empty/", 5, 1, 1, false), null),
    NEBULA("Nebula", StyleElement.NEBULA_TILE, LOW, LOW, MEDIOCRE,
            TileSingleModifier.NEBULA, 1.6f, 1.7f,
            () -> new CachedImageSequence("tiles/nebula/", 20, 1, 1, true),
            new AsyncImageSequence("background/firing/nebula/", 10, 1, 1, false), null),
    DENSE_NEBULA("Dense Nebula", StyleElement.DENSE_NEBULA_TILE, NONE, MEDIOCRE, LOW,
            TileSingleModifier.DENSE_NEBULA, 2f, 100f,
            () -> new CachedImageSequence("tiles/denseNebula/", 10, 1, 1, true),
            new AsyncImageSequence("background/firing/denseNebula/left/", 10, 1, 1, false), new AsyncImageSequence("background/firing/denseNebula/right/", 10, 1, 1, false)),
    ASTEROIDS("Asteroid Field", StyleElement.ASTEROID_TILE, MEDIOCRE, HIGH, VERY_LOW,
            TileSingleModifier.ASTEROID_FIELD, 4.5f, 1.5f,
            () -> new CachedImageSequence("tiles/asteroids/asteroids", 7, true),
            new AsyncImageSequence("background/firing/asteroids/bg_firing_asteroids_left", 5, false), new AsyncImageSequence("background/firing/asteroids/bg_firing_asteroids_right", 5, false));

    private final String displayName;
    private final StyleElement textColour;
    public final TileTypeCharacteristic visibility, defence, movement;
    public final Modifier damageModifier;
    public final float moveCost, concealment;
    public final Supplier<ImageSequence> tileTexturesSupplier;
    public ImageSequence tileTextures;
    public final ImageSequence firingTexturesLeft, firingTexturesRight;

    TileType(String displayName, StyleElement textColour, TileTypeCharacteristic visibility, TileTypeCharacteristic defence, TileTypeCharacteristic movement, Function<TileType, ? extends Modifier> damageModifier, float moveCost, float concealment, Supplier<ImageSequence> tileTexturesSupplier, ImageSequence firingTexturesLeft, ImageSequence firingTexturesRight) {
        this.displayName = displayName;
        this.textColour = textColour;
        this.visibility = visibility;
        this.defence = defence;
        this.movement = movement;
        this.damageModifier = damageModifier.apply(this);
        this.moveCost = moveCost;
        this.concealment = concealment;
        this.tileTexturesSupplier = tileTexturesSupplier;
        this.firingTexturesLeft = firingTexturesLeft;
        if (firingTexturesRight == null)
            this.firingTexturesRight = firingTexturesLeft;
        else
            this.firingTexturesRight = firingTexturesRight;
    }

    @Override
    public String colour() {
        return textColour.display;
    }

    @Override
    public String getName() {
        return displayName;
    }

    public static void init() {
        MainPanel.setLoadBarEnabled(true);
        for (int i = 0; i < values().length; i++) {
            TileType type = values()[i];
            if (type.tileTexturesSupplier != null)
                type.tileTextures = type.tileTexturesSupplier.get();
            MainPanel.setLoadBarProgress((i + 1f) / values().length);
        }
        MainPanel.setLoadBarEnabled(false);
    }
}

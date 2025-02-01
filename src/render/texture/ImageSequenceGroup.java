package render.texture;

public enum ImageSequenceGroup {
    PLASMA_HIT(
            CachedImageSequence.PLASMA_HIT_SEED_0,
            CachedImageSequence.PLASMA_HIT_SEED_1,
            CachedImageSequence.PLASMA_HIT_SEED_2,
            CachedImageSequence.PLASMA_HIT_SEED_3,
            CachedImageSequence.PLASMA_HIT_SEED_4
    ), EXPLOSION(
            CachedImageSequence.EXPLOSION_SEED_0,
            CachedImageSequence.EXPLOSION_SEED_1,
            CachedImageSequence.EXPLOSION_SEED_2,
            CachedImageSequence.EXPLOSION_SEED_3,
            CachedImageSequence.EXPLOSION_SEED_4
    );

    public final CachedImageSequence[] sequences;

    ImageSequenceGroup(CachedImageSequence... sequences) {
        this.sequences = sequences;
    }

    public CachedImageSequence getRandomSequence() {
        return sequences[(int)(Math.random() * sequences.length)];
    }
}

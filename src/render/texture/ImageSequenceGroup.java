package render.texture;

import foundation.MainPanel;

import java.util.function.Supplier;

public enum ImageSequenceGroup {
    EXPLOSION(
            CachedImageSequence.EXPLOSION_SEED_0,
            CachedImageSequence.EXPLOSION_SEED_1,
            CachedImageSequence.EXPLOSION_SEED_2,
            CachedImageSequence.EXPLOSION_SEED_3,
            CachedImageSequence.EXPLOSION_SEED_4
    ), BULLET_HIT(
            CachedImageSequence.BULLET_HIT_SEED_0,
            CachedImageSequence.BULLET_HIT_SEED_1,
            CachedImageSequence.BULLET_HIT_SEED_2,
            CachedImageSequence.BULLET_HIT_SEED_3,
            CachedImageSequence.BULLET_HIT_SEED_4
    ), PLASMA_HIT(
            CachedImageSequence.PLASMA_HIT_SEED_0,
            CachedImageSequence.PLASMA_HIT_SEED_1,
            CachedImageSequence.PLASMA_HIT_SEED_2,
            CachedImageSequence.PLASMA_HIT_SEED_3,
            CachedImageSequence.PLASMA_HIT_SEED_4
    );

    public final CachedImageSequence[] sequences;
    private final Supplier<CachedImageSequence>[] sequenceSuppliers;

    ImageSequenceGroup(Supplier<CachedImageSequence>... sequences) {
        sequenceSuppliers = sequences;
        this.sequences = new CachedImageSequence[sequences.length];
    }

    public CachedImageSequence getRandomSequence() {
        return sequences[(int) (Math.random() * sequences.length)];
    }

    public static void init() {
        ImageSequenceGroup[] values = values();
        MainPanel.setLoadBarEnabled(true);
        for (int i = 0; i < values.length; i++) {
            ImageSequenceGroup type = values[i];
            for (int j = 0; j < type.sequences.length; j++) {
                type.sequences[j] = type.sequenceSuppliers[j].get();
                MainPanel.setLoadBarProgress((float) i / values.length + (j + 1f) / (type.sequences.length * values.length));
            }
        }
        MainPanel.setLoadBarEnabled(false);
    }
}

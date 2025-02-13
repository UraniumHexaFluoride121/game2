package render.texture;

import java.util.function.Supplier;

public class CachedImageSequence implements ImageSequence {
    public static final CachedImageSequence
            BULLET_HIT_SEED_0 = new CachedImageSequence("effects/bullet_hit/seed_0/", 20, 1, 5, true),
            BULLET_HIT_SEED_1 = new CachedImageSequence("effects/bullet_hit/seed_1/", 20, 1, 5, true),
            BULLET_HIT_SEED_2 = new CachedImageSequence("effects/bullet_hit/seed_2/", 20, 1, 5, true),
            BULLET_HIT_SEED_3 = new CachedImageSequence("effects/bullet_hit/seed_3/", 20, 1, 5, true),
            BULLET_HIT_SEED_4 = new CachedImageSequence("effects/bullet_hit/seed_4/", 20, 1, 5, true);

    public static final CachedImageSequence
            PLASMA_HIT_SEED_0 = new CachedImageSequence("effects/plasma_hit/seed_0/", 20, 1, 5, true),
            PLASMA_HIT_SEED_1 = new CachedImageSequence("effects/plasma_hit/seed_1/", 20, 1, 5, true),
            PLASMA_HIT_SEED_2 = new CachedImageSequence("effects/plasma_hit/seed_2/", 20, 1, 5, true),
            PLASMA_HIT_SEED_3 = new CachedImageSequence("effects/plasma_hit/seed_3/", 20, 1, 5, true),
            PLASMA_HIT_SEED_4 = new CachedImageSequence("effects/plasma_hit/seed_4/", 20, 1, 5, true);

    public static final CachedImageSequence
            EXPLOSION_SEED_0 = new CachedImageSequence("effects/explosion/seed_0/", 20, 1, 5, true),
            EXPLOSION_SEED_1 = new CachedImageSequence("effects/explosion/seed_1/", 20, 1, 5, true),
            EXPLOSION_SEED_2 = new CachedImageSequence("effects/explosion/seed_2/", 20, 1, 5, true),
            EXPLOSION_SEED_3 = new CachedImageSequence("effects/explosion/seed_3/", 20, 1, 5, true),
            EXPLOSION_SEED_4 = new CachedImageSequence("effects/explosion/seed_4/", 20, 1, 5, true);

    public final ImageRenderer[] images;
    public final int frames;

    public CachedImageSequence(String path, int frames, boolean center) {
        this.frames = frames;
        images = new ImageRenderer[frames];
        for (int i = 0; i < frames; i++) {
            if (center) {
                images[i] = ImageRenderer.renderImageCentered(new ResourceLocation(path + "_" + i + ".png"), true, true);
            } else
                images[i] = ImageRenderer.renderImage(new ResourceLocation(path + "_" + i + ".png"), true, true);
        }
    }

    public CachedImageSequence(String path, int frames, int frameStart, int step, boolean center) {
        this.frames = frames;
        images = new ImageRenderer[frames];
        for (int i = 0; i < frames; i++) {
            String frame = String.format("%04d", frameStart + i * step);
            if (center) {
                images[i] = ImageRenderer.renderImageCentered(new ResourceLocation(path + frame + ".png"), true, true);
            } else
                images[i] = ImageRenderer.renderImage(new ResourceLocation(path + frame + ".png"), true, true);
        }
    }

    @Override
    public ImageRenderer getImage(int index) {
        return images[index];
    }

    @Override
    public ImageRenderer getRandomImage() {
        return images[(int) (Math.random() * images.length)];
    }

    @Override
    public ImageRenderer getRandomImage(Supplier<Double> random) {
        return images[(int) (random.get() * images.length)];
    }

    @Override
    public ImageRenderer getRandomImage(double random) {
        return images[(int) (random * images.length)];
    }

    @Override
    public int imageCount() {
        return frames;
    }
}

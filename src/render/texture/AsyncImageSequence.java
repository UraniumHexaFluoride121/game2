package render.texture;

import java.util.function.Supplier;

import static level.Level.*;

public class AsyncImageSequence implements ImageSequence {
    public final ResourceLocation[] images;
    public final int frames;
    private boolean center;

    public AsyncImageSequence(String path, int frames, boolean center) {
        this.frames = frames;
        this.center = center;
        images = new ResourceLocation[frames];
        for (int i = 0; i < frames; i++) {
            images[i] = new ResourceLocation(path + "_" + i + ".png");
        }
    }

    public AsyncImageSequence(String path, int frames, int frameStart, int step, boolean center) {
        this.frames = frames;
        this.center = center;
        images = new ResourceLocation[frames];
        for (int i = 0; i < frames; i++) {
            String frame = String.format("%04d", frameStart + i * step);
            images[i] = new ResourceLocation(path + frame + ".png");
        }
    }

    @Override
    public ImageRenderer getImage(int index) {
        return getImage(images[index]);
    }

    @Override
    public ImageRenderer getRandomImage() {
        return getImage(images[(int) (Math.random() * images.length)]);
    }

    @Override
    public ImageRenderer getRandomImage(Supplier<Double> random) {
        return getImage(images[(int) (random.get() * images.length)]);
    }

    @Override
    public ImageRenderer getRandomImage(double random) {
        return getImage(images[(int) (random * images.length)]);
    }

    private ImageRenderer getImage(ResourceLocation location) {
        AsyncImageRenderer image = new AsyncImageRenderer();
        EXECUTOR.submit(() -> {
            if (center) {
                image.setRenderer(ImageRenderer.renderImageCentered(location, true, false));
            } else {
                image.setRenderer(ImageRenderer.renderImage(location, true, false));
            }
        });
        return image;
    }

    @Override
    public int imageCount() {
        return frames;
    }
}

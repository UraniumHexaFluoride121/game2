package render.texture;

import java.awt.*;

public class CachedImageCounter implements ImageCounter {
    private final ImageSequence sequence;
    private final ImageRenderer[] images;
    private int counter = 0;

    public CachedImageCounter(ImageSequence sequence) {
        this.sequence = sequence;
        images = new ImageRenderer[sequence.imageCount()];
        for (int i = 0; i < sequence.imageCount(); i++) {
            images[i] = sequence.getImage(i);
        }
    }

    @Override
    public void render(Graphics2D g, float width) {
        images[counter].render(g, width);
    }

    public void increment() {
        counter++;
        if (counter >= sequence.imageCount())
            counter = sequence.imageCount() - 1;
    }

    public void increment(int amount) {
        counter += amount;
        if (counter >= sequence.imageCount())
            counter = sequence.imageCount() - 1;
    }

    public void start() {
        counter = 0;
    }

    public void end() {
        counter = sequence.imageCount() - 1;
    }
}

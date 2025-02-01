package render.texture;

import java.awt.*;

public class VolatileImageCounter implements ImageCounter {
    private final ImageSequence sequence;
    private ImageRenderer image;
    private int counter = 0;

    public VolatileImageCounter(ImageSequence sequence) {
        this.sequence = sequence;
        image = sequence.getImage(counter);
    }

    @Override
    public void render(Graphics2D g, float width) {
        image.render(g, width);
    }

    public void increment() {
        counter++;
        if (counter >= sequence.imageCount())
            counter = sequence.imageCount() - 1;
        image = sequence.getImage(counter);
    }

    public void increment(int amount) {
        counter += amount;
        if (counter >= sequence.imageCount())
            counter = sequence.imageCount() - 1;
        image = sequence.getImage(counter);
    }

    public void start() {
        counter = 0;
        image = sequence.getImage(counter);
    }

    public void end() {
        counter = sequence.imageCount() - 1;
        image = sequence.getImage(counter);
    }
}

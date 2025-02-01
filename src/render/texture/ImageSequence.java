package render.texture;

import java.util.function.Supplier;

public interface ImageSequence {
    ImageRenderer getImage(int index);
    ImageRenderer getRandomImage();
    ImageRenderer getRandomImage(Supplier<Double> random);
    int imageCount();
}

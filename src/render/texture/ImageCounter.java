package render.texture;

public interface ImageCounter extends ImageRenderer {
    void increment();
    void increment(int amount);
    void start();
    void end();
}

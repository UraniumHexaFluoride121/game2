package render.anim.timer;

public class DeltaTime {
    private long time;

    public DeltaTime() {
        time = System.currentTimeMillis();
    }

    public float get() {
        long prev = time;
        time = System.currentTimeMillis();
        return (time - prev) / 1000f;
    }
}

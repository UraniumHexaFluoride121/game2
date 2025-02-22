package foundation.input;

public class ScrollInputType extends InputType {
    public int scrollAmount;
    public boolean up;

    public ScrollInputType(int scrollAmount, boolean up) {
        this.scrollAmount = scrollAmount;
        this.up = up;
    }
}

package foundation;

import java.util.TreeMap;

public class WeightedSelector<T> {
    TreeMap<Float, T> map = new TreeMap<>();

    public WeightedSelector() {
    }

    public WeightedSelector<T> add(float weight, T element) {
        map.put((map.isEmpty() ? 0 : map.lastKey()) + weight, element);
        return this;
    }

    public T get() {
        return map.ceilingEntry((float) Math.random()).getValue();
    }
}

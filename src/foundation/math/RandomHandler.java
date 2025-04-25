package foundation.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class RandomHandler {
    private final long seed;
    private final Random mainRandom;
    private final HashMap<RandomType, Random> randoms = new HashMap<>();

    public RandomHandler(long seed) {
        this.seed = seed;
        mainRandom = new Random(seed);
        for (RandomType type : RandomType.values()) {
            randoms.put(type, new Random(mainRandom.nextInt()));
        }
    }

    public synchronized Random getRandom(RandomType type) {
        return randoms.get(type);
    }

    public synchronized Supplier<Double> getDoubleSupplier(RandomType type) {
        return getRandom(type)::nextDouble;
    }

    public Random generateNewRandomSource(RandomType type) {
        return new Random(getRandom(type).nextInt());
    }

    public int generateNewRandomSeed(RandomType type) {
        return getRandom(type).nextInt();
    }

    public float generateFloat(RandomType type) {
        return getRandom(type).nextFloat();
    }

    public <T> T randomFromArray(T[] array, RandomType type) {
        return array[(int) (array.length * generateFloat(type))];
    }

    public <T> T[] randomise(T[] array, RandomType type) {
        ArrayList<T> list = new ArrayList<>(List.of(array));
        for (int i = 0; i < array.length; i++) {
            array[i] = list.remove((int) (generateFloat(type) * list.size()));
        }
        return array;
    }

    public <T> ArrayList<T> randomise(ArrayList<T> array, RandomType type) {
        ArrayList<T> list = new ArrayList<>(array);
        for (int i = 0; i < array.size(); i++) {
            array.set(i, list.remove((int) (generateFloat(type) * list.size())));
        }
        return array;
    }

    public <T> ArrayList<T> randomSelection(List<T> array, int count, RandomType type) {
        ArrayList<T> list = new ArrayList<>(array), newList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            newList.add(list.remove((int) (generateFloat(type) * list.size())));
        }
        return newList;
    }
}

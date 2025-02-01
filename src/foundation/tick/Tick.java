package foundation.tick;

import foundation.Main;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Tick extends Thread {
    private final Set<RegisteredTickable> qRegister = ConcurrentHashMap.newKeySet(), qRemove = ConcurrentHashMap.newKeySet();
    private final TreeMap<TickOrder, HashSet<RegisteredTickable>> tickables = new TreeMap<>();

    public Tick() {
        for (TickOrder value : TickOrder.values()) {
            tickables.put(value, new HashSet<>());
        }
    }

    public synchronized void register(RegisteredTickable t) {
        qRegister.add(t);
    }

    public synchronized void remove(RegisteredTickable t) {
        qRemove.add(t);
    }

    //we queue tickables when removing and adding to avoid ConcurrentModificationException
    private synchronized void processQueued() {
        qRegister.forEach(t -> tickables.get(t.getTickOrder()).add(t));
        qRegister.clear();

        qRemove.forEach(t -> tickables.get(t.getTickOrder()).remove(t));
        qRemove.clear();
    }

    public static final float MAX_DELTA_TIME = 0.01f;
    public static final float TOTAL_DELTA_TIME_CAP = 0.1f;

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long newTime;
        while (true) {
            //calculate deltaTime
            newTime = System.currentTimeMillis();
            float deltaTime = (newTime - time) / 1000f;
            time = newTime;

            //tick tickable objects
            deltaTime = Math.min(deltaTime, TOTAL_DELTA_TIME_CAP);
            do {
                //add and remove tickables
                processQueued();
                float finalDT = deltaTime;
                deltaTime -= MAX_DELTA_TIME;
                tickables.forEach((_, set) -> set.forEach(t -> t.tick(Math.min(finalDT, MAX_DELTA_TIME))));
            } while (deltaTime > 0);


            //render frame
            BufferStrategy strategy = Main.window.getBufferStrategy();
            do {
                Graphics drawGraphics = strategy.getDrawGraphics();
                Main.window.paintComponents(drawGraphics);
                drawGraphics.dispose();
                strategy.show();
            } while (strategy.contentsRestored());
            try {
                TimeUnit.MILLISECONDS.sleep(3 - (System.currentTimeMillis() - time));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

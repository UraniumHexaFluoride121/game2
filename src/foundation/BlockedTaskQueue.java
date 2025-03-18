package foundation;

import foundation.tick.Tickable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class BlockedTaskQueue implements Tickable, Deletable {
    private final ArrayList<Runnable> tasksQ = new ArrayList<>(), tasks = new ArrayList<>();
    private Supplier<Boolean> block;

    public BlockedTaskQueue(Supplier<Boolean> block) {
        this.block = block;
    }

    public BlockedTaskQueue() {
        block = () -> !isEmpty();
    }

    public synchronized void addTask(Runnable r) {
        tasksQ.add(r);
    }

    public synchronized boolean isEmpty() {
        return tasks.isEmpty() && tasksQ.isEmpty();
    }

    @Override
    public synchronized void tick(float deltaTime) {
        tasks.addAll(tasksQ);
        tasksQ.clear();
        if (!block.get()) {
            AtomicBoolean blocked = new AtomicBoolean(false);
            tasks.removeIf(task -> {
                if (blocked.get() || block.get()) {
                    blocked.set(true);
                    return false;
                }
                task.run();
                return true;
            });
        }
    }

    @Override
    public synchronized void delete() {
        tasks.clear();
        tasksQ.clear();
        block = null;
    }
}

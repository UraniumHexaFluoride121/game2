package foundation;

import foundation.tick.Tickable;

import java.util.ArrayList;

public class TaskQueue implements Tickable, Deletable {
    private final ArrayList<Runnable> tasksQ = new ArrayList<>(), tasks = new ArrayList<>();

    public TaskQueue() {
    }

    public synchronized void addTask(Runnable r) {
        tasksQ.add(r);
    }

    @Override
    public synchronized void tick(float deltaTime) {
        tasks.addAll(tasksQ);
        tasksQ.clear();
        tasks.forEach(Runnable::run);
        tasks.clear();
    }

    @Override
    public synchronized void delete() {
        tasks.clear();
        tasksQ.clear();
    }
}

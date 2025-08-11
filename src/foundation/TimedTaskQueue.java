package foundation;

import foundation.tick.Tickable;

import java.util.ArrayList;

public class TimedTaskQueue implements Tickable, Deletable {
    private final ArrayList<Task> tasksQ = new ArrayList<>(), tasks = new ArrayList<>();

    public TimedTaskQueue() {
    }

    public synchronized TimedTaskQueue addTask(float time, Runnable r) {
        tasksQ.add(new Task(r, System.currentTimeMillis() + (long) (time * 1000)));
        return this;
    }

    public synchronized TimedTaskQueue andThen(float time, Runnable r) {
        tasks.addAll(tasksQ);
        tasksQ.clear();
        if (tasks.isEmpty())
            addTask(time, r);
        else
            tasksQ.add(new Task(r, tasks.getLast().time + (long) (time * 1000)));
        return this;
    }

    public synchronized boolean hasIncompleteTask() {
        return !tasks.isEmpty() || !tasksQ.isEmpty();
    }

    @Override
    public synchronized void tick(float deltaTime) {
        tasks.addAll(tasksQ);
        tasksQ.clear();
        tasks.removeIf(t -> {
            if (System.currentTimeMillis() > t.time) {
                t.r.run();
                return true;
            }
            return false;
        });
    }

    @Override
    public synchronized void delete() {
        tasks.clear();
        tasksQ.clear();
    }

    private record Task(Runnable r, long time) {

    }
}

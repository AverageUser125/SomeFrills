package com.somefrills.misc;

import com.somefrills.events.TickEventPost;
import meteordevelopment.orbit.EventHandler;

import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.somefrills.Main.mc;

public class DelayedRun {

    private record Task(Runnable runnable, long executeAt) implements Comparator<Task> {
        @Override
        public int compare(Task t1, Task t2) {
            return Long.compare(t1.executeAt, t2.executeAt);
        }
    }

    // Min-heap ordered by execution time
    private static final PriorityQueue<Task> tasks = new PriorityQueue<>();

    // Thread-safe staging queue
    private static final Queue<Task> futureTasks = new ConcurrentLinkedQueue<>();

    /**
     * Schedule a task after delay (in milliseconds)
     */
    public static void runDelayed(Runnable r, long delayMs) {
        long executeAt = System.currentTimeMillis() + delayMs;
        futureTasks.add(new Task(r, executeAt));
    }

    /**
     * Runs in the next game tick (main thread)
     */
    public static void runNextTick(Runnable r) {
        mc.send(r);
    }

    /**
     * Runs now if on main thread, otherwise next tick
     */
    public static void runOrNextTick(Runnable r) {
        mc.execute(r);
    }

    /**
     * Tick handler — runs every tick
     */
    @EventHandler
    public static void onTick(TickEventPost event) {
        long now = System.currentTimeMillis();

        // 1. Execute only READY tasks (efficient)
        while (!tasks.isEmpty()) {
            Task task = tasks.peek();

            if (task.executeAt > now) {
                break; // earliest task is not ready → nothing else is
            }

            tasks.poll(); // remove it

            try {
                task.runnable.run();
            } catch (Exception e) {
                System.err.println("DelayedRun task crashed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 2. Add newly scheduled tasks AFTER execution (preserves semantics)
        Task t;
        while ((t = futureTasks.poll()) != null) {
            tasks.add(t);
        }
    }
}
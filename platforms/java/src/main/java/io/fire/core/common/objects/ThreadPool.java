package io.fire.core.common.objects;

import lombok.NoArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
public class ThreadPool {

    ExecutorService pool = Executors.newFixedThreadPool(1);


    /**
     * Set the size of a thread pool, this kills the current one and replaces it with a new one
     *
     * @param size
     */
    public void setSize(int size) {
        pool.shutdown();
        pool = Executors.newFixedThreadPool(size);
    }


    /**
     * Schedule a task for execution by a thread worker
     *
     * @param r
     */
    public void run(Runnable r) {
        pool.execute(r);
    }


    /**
     * Stop the thread pool
     */
    public void shutdown() {
        pool.shutdown();
    }

}

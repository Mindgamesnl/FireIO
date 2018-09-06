package io.fire.core.common.objects;

import lombok.NoArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
public class ThreadPool {

    ExecutorService pool = Executors.newFixedThreadPool(1);

    public void setSize(int size) {
        pool.shutdown();
        pool = Executors.newFixedThreadPool(size);
    }

    public void run(Runnable r) {
        pool.execute(r);
    }
    public void shutdown() {
        pool.shutdown();
    }

}

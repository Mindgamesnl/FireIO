import io.fire.core.client.FireIoClient;
import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class rateclient {
    public static void main(String[] args) {
        List<FireIoClient> fireIoClientList = new ArrayList<>();
        ThreadPoolExecutor executor =
                (ThreadPoolExecutor) Executors.newFixedThreadPool(30);
        final int[] connected = {0};
        System.out.print("starting");
        Instant start = Instant.now();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Clients: " + connected[0]);
            }
        }, 0, 1, TimeUnit.SECONDS);
        for (int i = 1; i <= 1000; i++) {
            executor.submit(() -> {
                FireIoClient client = new FireIoClient("localhost", 80)
                        .setPassword("testpassword1")
                        .connect();

                client.on(Event.CONNECT, a -> {
                    connected[0]++;
                    if (connected[0] == 1000) {
                        Instant end = Instant.now();
                        System.out.println("Opened started 1000 clients in " + (Duration.between(start, end).getNano() / 1000000) + " miliseconds!");
                    }
                })

                .on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
                    connected[0]--;
                });
                fireIoClientList.add(client);
            });
        }
    }
}

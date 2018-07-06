import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.superclasses.Client;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ratedserver {
    public static void main(String[] args) {
        try {
            final int[] clients = {0};
            FireIoServer server = new FireIoServer(80)
                    .setPassword("testpassword1")
                    .setRateLimiter(1005, 1)
                    .setThreadPoolSize(16)

                    .on("go", a -> {
                        System.out.print("Clients at end: " + clients[0]);
                    })

                    .on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
                        clients[0]--;
                    })

                    .on(Event.CONNECT, eventPayload -> {
                        clients[0]++;
                    });

            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

            ses.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Clients: " + clients[0]);
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
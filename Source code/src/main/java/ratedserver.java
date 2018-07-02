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
            FireIoServer server = new FireIoServer(80)
                    .setPassword("testpassword1")
                    .setRateLimiter(1005, 1)
                    .setThreadPoolSize(16);

            server.registerEndpoint("/performanceTest", (req -> "Hello World!"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

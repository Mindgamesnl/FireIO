package io.fire.core.server.modules.socket.tasks;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.FireIoConnection;
import io.fire.core.server.modules.socket.handlers.SocketClientHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.TimerTask;

public class IdleKick extends TimerTask {

    private FireIoServer server;

    public IdleKick(FireIoServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        Instant now = Instant.now();
        for (FireIoConnection connection : server.getClientModule().connectionMap.values()) {
            SocketClientHandler handler = connection.getHandler();
            if (handler != null) {
                if (!handler.authenticated) {
                    if (Duration.between(handler.getInitiated(), now).getSeconds() >= 4) {
                        connection.close();
                    }
                }
            }
        }
    }
}

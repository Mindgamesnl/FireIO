package io.fire.core.server.modules.socket.tasks;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.FireIoConnection;
import io.fire.core.server.modules.socket.handlers.SocketClientHandler;

import java.util.Date;
import java.util.TimerTask;

public class IdleKick extends TimerTask {

    private FireIoServer server;

    public IdleKick(FireIoServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        Date now = new Date();
        for (FireIoConnection connection : server.getClientModule().connectionMap.values()) {
            SocketClientHandler handler = connection.getHandler();
            if (handler != null) {
                if (!handler.authenticated) {
                    int secondsBetween = Math.toIntExact((handler.getInitiated().getTime() - now.getTime()) / 1000);
                    if (secondsBetween >= 30) {
                        connection.close();
                    }
                }
            }
        }
    }
}

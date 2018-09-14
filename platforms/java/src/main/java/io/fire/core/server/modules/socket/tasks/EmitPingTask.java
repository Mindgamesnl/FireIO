package io.fire.core.server.modules.socket.tasks;

import io.fire.core.common.packets.PingPacket;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.FireIoConnection;
import io.fire.core.server.modules.socket.handlers.SocketClientHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.TimerTask;

public class EmitPingTask extends TimerTask {

    private FireIoServer server;

    public EmitPingTask(FireIoServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        Instant now = Instant.now();
        for (FireIoConnection connection : server.getClientModule().connectionMap.values()) {
            SocketClientHandler handler = connection.getHandler();
            if (handler != null) {
                try {
                    handler.emit(new PingPacket(Instant.now()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

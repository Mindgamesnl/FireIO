package io.fire.core.server.modules.socket.tasks;

import io.fire.core.common.io.objects.IoFrame;
import io.fire.core.common.io.objects.IoFrameSet;
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
                    for (IoFrame frame : new IoFrameSet(new PingPacket(Instant.now())).getFrames()) {
                        handler.getIoManager().forceWrite(frame.getBuffer(), false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

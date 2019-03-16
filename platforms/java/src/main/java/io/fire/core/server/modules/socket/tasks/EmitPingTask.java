package io.fire.core.server.modules.socket.tasks;

import io.fire.core.common.io.enums.IoFrameType;
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


    /**
     * Emit the current UNIX time stamp to all the clients
     * so that they can run health checks
     */
    @Override
    public void run() {
        for (FireIoConnection connection : server.getClientModule().connectionMap.values()) {
            SocketClientHandler handler = connection.getHandler();
            if (handler != null) {
                for (IoFrame frame : new IoFrameSet(IoFrameType.PING_PACKET).getFrames()) {
                    handler.getIoManager().forceWrite(frame.getBuffer(), false);
                }
            } else if (handler.isHasClosed() && !handler.isOpen()) {
                //already closed, remove as garbage collection
            }
        }
    }
}

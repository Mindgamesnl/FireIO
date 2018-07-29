import io.fire.core.common.interfaces.Packet;
import io.fire.core.server.FireIoServer;

import java.io.IOException;

public class concurrentserver {
    
    public static void main(String[] args) {
        System.out.println("s");
        FireIoServer server = null;
        try {
            server = new FireIoServer(80);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final int[] received = {0};
        server.on("a", eventPayload -> {
            received[0]++;
            System.out.println("Received: " + received[0]);
        });
    }
    
}

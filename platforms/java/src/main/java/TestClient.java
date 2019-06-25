import io.fire.core.client.FireIoClient;
import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.enums.EventPriority;
import io.fire.core.server.FireIoServer;

import java.io.IOException;

public class TestClient {

    public static void main(String[] args) {
        FireIoClient client = new FireIoClient("localhost", 80);
        client.setPassword("welkom01");

        client.getEventHandler().registerEvent(Event.DISCONNECT).onExecute(message -> {
            System.out.println("Event Disconnected: " + message);
            System.exit(0);
        });

        client.getEventHandler().registerEvent(Event.CONNECT).onExecute(message -> {
            System.out.println("Event Connected: " + message);
            try {
                client.send("pong", "Hoi zoe!");
                client.send("pong", "Tsja");
                client.send("pong", "Dit ga je nooit lezen want git intereseerd je niets");
                client.send("pong", "Maar ik wou je gewoon even vertellen dat ik van je houd");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        client.getEventHandler().registerTextChannel("welcome", EventPriority.HIGH).onExecute(message -> {
            System.out.println("Event welcome: " + message);
        });

        client.connect();
    }

}

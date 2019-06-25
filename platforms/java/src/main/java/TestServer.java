import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.enums.EventPriority;
import io.fire.core.server.FireIoServer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class TestServer {

    public static void main(String[] args) {
        try {
            FireIoServer server = new FireIoServer(80);
            server.setPassword("welkom01");

            server.getEventHandler().registerEvent(Event.DISCONNECT).onExecute((client, message) -> {
                System.out.println("Event Disconnected: " + message + " client:" + client.getId());
            });

            server.getEventHandler().registerEvent(Event.CONNECT).onExecute((client, message) -> {
                System.out.println("Event Connected: " + message + " client:" + client.getId());
                try {
                    client.send("welcome", "Welcome client " + client.getId() + "!");
                    client.send("welcome", "How are you today?");
                    client.send("welcome", "I hope you are doing well");
                    client.send("welcome", "I love her");
                    client.send("welcome", "This is indeed a love letter i a test");
                    client.send("welcome", "yeah!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            server.getEventHandler().registerTextChannel("pong", EventPriority.HIGH).onExecute(message -> {
                System.out.println("Event pong: " + message);
            });

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                }
            }, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

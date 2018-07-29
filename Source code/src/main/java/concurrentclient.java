import io.fire.core.client.FireIoClient;
import io.fire.core.common.eventmanager.enums.Event;


public class concurrentclient {
    
    public static void main(String[] args) {
        System.out.println("s");
        final FireIoClient client = new FireIoClient("localhost",80);
        client.connect();
        client.on(Event.CONNECT, eventPayload -> {
            for (int i = 0; i < 50; i++) {
                System.out.println("Sending: " + i);
                //Thread.sleep(3);
                client.send("a", "b");
            }
        });
    }
    
}

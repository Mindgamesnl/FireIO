import io.fire.core.client.FireIoClient;
import io.fire.core.server.FireIoServer;

import java.io.IOException;

public class TestClient {

    public static void main(String[] args) {
        FireIoClient client = new FireIoClient("localhost", 80);
        client.setPassword("welkom01");
        client.connect();
    }

}

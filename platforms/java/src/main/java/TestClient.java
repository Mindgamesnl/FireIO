import io.fire.core.client.FireIoClient;
import io.fire.core.server.FireIoServer;

import java.io.IOException;

public class TestClient {

    public static void main(String[] args) {
        FireIoClient server = new FireIoClient("localhost", 80);
    }

}

import io.fire.core.server.FireIoServer;

import java.io.IOException;

public class test {

    public static void main(String[] args) {
        try {
            FireIoServer server = new FireIoServer(80);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

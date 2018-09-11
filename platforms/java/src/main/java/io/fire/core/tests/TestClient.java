package io.fire.core.tests;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.superclasses.Client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestClient {

    public static void main(String[] args) {

        FireIoClient client = new FireIoClient("localhost", 80)
                .setPassword("testpassword1")
                .connect();

        client.on(Event.CONNECT, eventPayload -> System.out.println("Connected!"));
    }

}

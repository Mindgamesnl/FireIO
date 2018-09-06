package io.fire.core.tests;

import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.superclasses.Client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestServer {

    public static void main(String[] args) {

        FireIoServer server = null;
        try {
            server = new FireIoServer(80)
                    .setPassword("testpassword1")
                    .setRateLimiter(2, 10)

                    .on(Event.CONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        client.send("MOTD", "test");
                        System.out.println("Client connected");
                    })

                    .on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId() + " closed unexpectedly!");
                    })

                    .on(Event.DISCONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId() + " just disconnected");
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.onRequest("whoami", (client, request, response) -> {
            System.out.println(client.getId().toString() + " asked who it is! sending ip back");
            response.complete(new RequestString("You are: " + client.getInfo().getHostname()));
        });


        server.registerEndpoint("/time", req -> "The server time is: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        server.registerEndpoint("/hi/?name", req -> "Welcome to FireIO " + req.getVariable("name") + "!");

    }

}

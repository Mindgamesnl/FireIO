package io.fire.core.tests;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.packets.ReceivedText;
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

        client.on(Event.CONNECT, eventPayload -> {
            System.out.println("Connected!");
            for(int i = 0; i < 50; ++i) {
                //System.out.println("Sending packet " + i);
                client.send("channel", "i am message " + i);
            }
        });

        client.on("channel", eventPayload -> {
            ReceivedText text = (ReceivedText) eventPayload;
            System.out.println("Channel got: " +text.getString());
            //send hi back
            //text.getSender().send("channel", "well hi my love! :D");
        });
        client.on(Event.DISCONNECT, eventPayload -> System.out.println("Disconnected!"));
        client.on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> System.out.println("Connection died!!"));
    }

}

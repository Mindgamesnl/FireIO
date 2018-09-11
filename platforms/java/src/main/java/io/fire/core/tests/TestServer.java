package io.fire.core.tests;

import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.packets.ChannelMessagePacket;
import io.fire.core.common.packets.ReceivedText;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.balancingmodule.objects.BalancerConfiguration;
import io.fire.core.server.modules.client.superclasses.Client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TestServer {

    public static void main(String[] args) {

        FireIoServer server = null;
        try {
            server = new FireIoServer(new Random().nextInt(6969-6920) + 6920)
                    .setPassword("testpassword1")
                    .setRateLimiter(2, 5)

                    .on(Event.CONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println("A user connected via " + client.getConnectionType());
                    })

                    .on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId() + " closed unexpectedly! " + client.getConnectionType());
                    })

                    .on(Event.DISCONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId() + " just disconnected");
                    })

                    .on("channel", eventPayload -> {
                        ReceivedText text = (ReceivedText) eventPayload;
                        System.out.println("Channel got: " +text.getString());
                        //send hi back
                        text.getSender().send("channel", "well hi my love! :D");
                    });

            ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.onRequest("whoami", (client, request, response) -> {
            System.out.println(client.getId().toString() + " asked who it is! sending ip back");
            response.complete(new RequestString("You are: " + client.getInfo().getHostname()));
        });


        server.linkLoadBalancer(new BalancerConfiguration("localhost", 80, "testpassword2"));

        server.registerEndpoint("/time", req -> "The server time is: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        server.registerEndpoint("/hi/?name", req -> "Welcome to FireIO " + req.getVariable("name") + "!");

    }

}

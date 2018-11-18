package io.fire.core.tests;

import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpStatusCode;
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
            server = new FireIoServer(80)
                    .setPassword("testpassword1")
                    .setThreadPoolSize(22)
                    .setRateLimiter(100000, 1)

                    .on(Event.CONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        for(int i = 0; i < 50; ++i) {
                            //System.out.println("Sending packet " + i);
                            client.send("channel", "i am message " + i);
                        }
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
                        //text.getSender().send("channel", "well hi my love! :D");
                    });

            ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.onRequest("whoami", (client, request, response) -> {
            System.out.println(client.getId().toString() + " asked who it is! sending ip back");
            response.complete(new RequestString("You are: " + client.getInfo().getHostname()));
        });


      //  server.linkLoadBalancer(new BalancerConfiguration("localhost", 80, "testpassword2"));


        server.registerEndpoint("/api/v2/getplayer/?name", ((request, settings) -> {
            String username = request.getVariable("name");
            String data = "{\n" +
                    "  \"name\": \"usr\",\n" +
                    "  \"score\": 5,\n" +
                    "  \"kills\": 6,\n" +
                    "  \"coins\": 1,\n" +
                    "  \"online\": true\n" +
                    "}";
            data = data.replace("usr", username);

            settings.setContent(HttpContentType.JSON);

            return data;
        }));

        server.registerEndpoint("/time", (req, settings) -> {
            return "The server time is: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        });
        server.registerEndpoint("/hi/?name", (req, settings)  -> {
            return "Welcome to FireIO " + req.getVariable("name") + "!";
        });

        server.registerEndpoint("/hi", (req, settings)  -> {
            return "hoi";
        });
    }

}

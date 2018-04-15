import io.fire.core.common.packets.ChannelPacketPacket;
import io.fire.core.common.events.enums.Event;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.superclasses.Client;

import java.io.IOException;

public class testserver {

    public static void main(String[] args) {

        System.out.println("Starting server...");

        try {
            FireIoServer server = new FireIoServer(80)
                    .setPassword("testpassword1")

                    .on(Event.CONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId().toString() + " just connected!" +
                                " (ip: " + client.getInfo().getHostname() + ")" +
                                " (platform: " + client.getInfo().getPlatform() + ")" +
                                " (version: " + client.getTag("appversion") + ")");
                        client.send("MOTD", "test");
                    })

                    .on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId() + " closed unexpectedly!");
                    })

                    .on(Event.DISCONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId() + " just disconnected");
                    })

                    .on("cookie_jar", eventPayload -> {
                        ChannelPacketPacket receivedPacket = (ChannelPacketPacket) eventPayload;
                        CookieJar cookieJar = (CookieJar) receivedPacket.getPacket();

                        System.out.println("Received a cookie jar from : " + receivedPacket.getSender().getId() + ". The jar contains " + cookieJar.getAmount() + " cookies. The cookies type is: " + cookieJar.getType());

                        //thank the client for the cookies
                        receivedPacket.getSender().send("thanks", "thanks");
                    });

            //debug;
            server.getEventHandler().on(gl -> {
                if (gl.getIsEvent()) {
                    System.out.println("Debug: receved event " + gl.getChannel() + " with payload " + gl.getOriginalPayload());
                } else {
                    System.out.println("Debug: receved channel " + gl.getChannel() + " with payload " + gl.getOriginalPayload());
                }
            });

            server.broadcast("message", "welcome everybody!");

           //Client client = server.getClient(UUID.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"));
           //client.send("message", "well hi there! you are the best.");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

package io.fire.core.tests;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.request.interfaces.ClientRequest;
import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.packets.ReceivedText;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.superclasses.Client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TestClient {

    public static void main(String[] args) {

        FireIoClient client = new FireIoClient("localhost", 80)
                .setPassword("testpassword1")
                .connect();

        client.on(Event.CONNECT, eventPayload -> {
            System.out.println("Connected!");
            for(int i = 0; i < 50; ++i) {
                System.out.println("Sending packet " + i);
                client.send("channel", "i am message " + i);
            }

            //client.send("channel", "test of dit wel werkt here goes" + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi molestie tortor quis turpis blandit, eget congue ligula fringilla. Donec in varius turpis. Sed ullamcorper, sapien eget sollicitudin dictum, sapien lectus volutpat justo, quis fringilla dolor quam sit amet lorem. Phasellus a risus eget nibh imperdiet tristique. Nam congue finibus tortor at blandit. Suspendisse vulputate libero nec enim pharetra, sit amet pretium elit pellentesque. Fusce fringilla consequat vulputate. Integer sed lacinia diam. Nunc interdum, erat vel tincidunt venenatis, nulla purus fermentum velit, eget efficitur odio lorem eget arcu. Aenean enim justo, consectetur sed ullamcorper vel, euismod sed mi. Suspendisse vel elementum erat. Morbi urna ligula, iaculis et tristique venenatis, volutpat id felis. Cras quis mauris maximus, ullamcorper lorem nec, tristique mi. Sed congue, tellus vel elementum porttitor, arcu tellus congue risus, ut molestie nibh leo in purus. Sed turpis ligula, viverra consectetur finibus sit amet, faucibus at diam. Etiam magna ligula, consequat eget bibendum vel, lobortis sed est. Quisque hendrerit ultricies ligula, ac pharetra diam mattis scelerisque. Praesent pellentesque, dui vitae blandit tincidunt, augue diam volutpat lorem, non pellentesque dui massa id odio. Aliquam ullamcorper libero id velit molestie, quis consectetur nulla facilisis. Quisque tempus ligula elit, ac vehicula magna tristique a.");

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Requesting lorem");
                    client.request("lorem", null, r -> {
                        System.out.println(((RequestString) r).getString());
                        client.send("channel", "received the first one");
                    });
                    System.out.println("Requesting lorem");
                    client.request("lorem", null, r -> {
                        System.out.println(((RequestString) r).getString());
                        client.send("channel", "received the second one");
                    });
                }
            }, 5000);

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

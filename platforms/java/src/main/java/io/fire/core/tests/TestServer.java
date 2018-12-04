package io.fire.core.tests;

import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.enums.EventPriority;
import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.packets.PingPacket;
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

                    .on(Event.CONNECT, client -> {
                        Client client = (Client) eventPayload;
                        for(int i = 0; i < 50; ++i) {
                            System.out.println("Sending packet " + i);
                            client.send("channel", "i am message " + i);
                        }
                        System.out.println("A user connected via " + client.getConnectionType());
                    })

.on(Event.TIMED_OUT, client -> {
    System.out.println(client.getId() + " closed unexpectedly! " + client.getConnectionType());
})

                    .on(Event.DISCONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId() + " just disconnected");
                    });

                server.on("channel", (message) -> {
                    System.out.println("Channel got: " + message);
                });

                server.onPacket(CookieJar.class, "cookie_jar").onExecute((sender, packer) -> {
                    System.out.println(sender.getId() + " has send a ping packet on " + packer.getSendTime());
                });
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.onRequest("whoami", (client, request, response) -> {
            System.out.println(client.getId().toString() + " asked who it is! sending ip back");
            response.complete(new RequestString("You are: " + client.getInfo().getHostname()));
        });


        server.onRequest("lorem", (client, request, response) -> {
            System.out.println(client.getId().toString() + " requested lorem");
            response.complete(new RequestString("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum varius eros eget metus euismod, mollis bibendum lorem volutpat. Proin in viverra sem, non ultrices lacus. In accumsan, elit et consectetur viverra, ante elit commodo erat, ac lacinia risus nibh ac risus. Phasellus elit magna, sodales sed pretium ut, molestie eget ante. Proin lobortis, neque ac condimentum luctus, erat neque semper mauris, a venenatis leo tellus nec augue. Vivamus fermentum suscipit luctus. In eget facilisis est. Suspendisse nec elit tempor, molestie elit sed, sodales nulla. Integer volutpat imperdiet justo ut condimentum. Praesent dignissim tortor metus, in ornare erat aliquam et. Morbi vehicula erat vel enim volutpat, et eleifend libero ullamcorper.\n" +
                    "\n" +
                    "Nullam vestibulum, ante vitae tincidunt sodales, libero tortor aliquam ante, ac hendrerit lectus diam sed ante. Aenean sit amet rhoncus velit. Aenean maximus tellus quis diam bibendum, quis euismod risus laoreet. Nullam finibus quis nisl ac egestas. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed pharetra dolor a elit dignissim faucibus. Mauris consequat malesuada luctus. Etiam accumsan quam ut egestas fermentum. Integer volutpat rhoncus nisl a eleifend. Vestibulum tincidunt nisi dignissim turpis pharetra maximus.\n" +
                    "\n" +
                    "Quisque felis eros, imperdiet eu auctor vel, tincidunt et orci. Mauris tincidunt dignissim libero, vel sodales diam imperdiet non. Ut euismod mollis ante, ac molestie orci accumsan vel. Pellentesque semper semper viverra. Vestibulum ac mi augue. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Donec sodales at justo commodo euismod. Sed scelerisque nisi at diam bibendum, sed dictum libero ultrices. Sed volutpat eros euismod est varius tristique. Curabitur vitae accumsan massa, sit amet sagittis turpis.\n" +
                    "\n" +
                    "Aliquam erat volutpat. Praesent velit velit, porta eget urna sed, auctor eleifend nisi. In facilisis elementum magna, in vehicula justo cursus nec. Fusce consequat lobortis nulla maximus pharetra. Nunc rutrum lobortis imperdiet. Nulla euismod molestie iaculis. Praesent vitae eleifend eros, a ultrices sapien. Aenean facilisis neque ligula, at vehicula erat pulvinar vel. Vestibulum congue neque et nunc pharetra, vel pulvinar metus consectetur. Aenean feugiat justo et velit fringilla eleifend. Fusce gravida pellentesque nulla, ultrices euismod leo pellentesque id. Phasellus et justo facilisis, dignissim lacus eget, cursus mauris. Maecenas at sollicitudin sapien.\n" +
                    "\n" +
                    "Maecenas ut mauris in mi sollicitudin vestibulum porta ut magna. Duis id nunc egestas lacus tempus pretium. Mauris nisi mi, vulputate quis mauris nec, tempor dictum ante. Donec consectetur urna et lectus rutrum congue. Nam at erat lectus. Nulla facilisi. Ut vitae purus felis. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Sed fermentum enim libero, eget fermentum mauris feugiat a. Sed luctus lacus sed molestie fringilla. Sed eu quam et massa consectetur efficitur. Pellentesque facilisis mauris in ligula finibus, pulvinar pharetra augue rutrum. Pellentesque sit amet neque id lectus fringilla commodo. Sed id sodales tellus. Aliquam vitae massa at dui finibus laoreet quis vel sem.\n" +
                    "\n" +
                    "Nulla interdum aliquam ante nec scelerisque. Etiam non orci id massa elementum dapibus. Duis quis risus lacinia, maximus felis ac, lobortis ex. Mauris sed mi non diam tempus scelerisque vel nec mauris. Vestibulum at nunc dolor. Morbi laoreet posuere nisl, ut sollicitudin felis pretium sed. Suspendisse justo mi, accumsan eu justo id, ullamcorper volutpat quam. Nullam lacus erat, placerat et blandit sed, venenatis ac augue. Aenean magna augue, congue et ipsum a, gravida facilisis urna. Maecenas cursus lobortis nisi, quis tincidunt urna tempor finibus. Maecenas tincidunt, magna ut consectetur lobortis, nulla tellus feugiat lorem, et molestie felis purus nec mauris. Aenean viverra sapien eget dignissim commodo. Suspendisse elementum rhoncus augue ut laoreet. Quisque dictum, sem ut euismod vehicula, nunc odio semper erat, ac fermentum erat nibh non diam. Sed magna ante, finibus a urna rhoncus, commodo faucibus nisl. Maecenas venenatis leo arcu, non aliquam felis varius ut.\n" +
                    "\n" +
                    "Etiam eu venenatis tellus. Nullam tempor tortor ac felis aliquet tristique. In non semper urna. Nullam pulvinar, justo sed efficitur eleifend, leo ante semper ex, nec molestie quam mi in dolor. Cras ut venenatis diam. Suspendisse enim tortor, elementum ut interdum sed, condimentum id orci. Mauris diam erat, iaculis ut blandit vitae, malesuada non turpis. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Aenean at tellus tortor. Aenean at elit sed massa varius pretium tincidunt non arcu. Phasellus facilisis mattis nulla. Donec a leo quis ex efficitur bibendum. Praesent quis risus vel lorem ultrices pretium. Duis condimentum quis lorem eget tincidunt. Maecenas a quam egestas, vestibulum est sed, consectetur nunc. Curabitur fringilla hendrerit neque vel blandit.\n" +
                    "\n" +
                    "Vestibulum rutrum ex eget porta vestibulum. Nullam at lacus et felis interdum fringilla sed et mauris. Phasellus mattis urna id metus consequat, eu blandit lacus condimentum. Curabitur lacinia varius gravida. Pellentesque non nisl efficitur, ultricies ipsum at, rutrum urna. Duis convallis, eros ac tristique tempor, risus tortor molestie purus, venenatis auctor erat dui a risus. Proin malesuada ipsum ut est mollis, ac fermentum ante imperdiet. Maecenas ut lectus blandit, congue nunc id, molestie erat. Etiam ut mauris eros. Suspendisse sollicitudin tincidunt gravida.\n" +
                    "\n" +
                    "Sed egestas nec mi vitae eleifend. Cras hendrerit turpis sit amet orci lacinia semper. Mauris ac est bibendum, aliquet augue ut, pretium risus. Pellentesque nec magna arcu. Fusce in nunc id mauris rutrum dignissim. Praesent lacinia, turpis at feugiat pulvinar, neque turpis molestie neque, eget mollis elit turpis non diam. Integer malesuada suscipit porta.\n" +
                    "\n" +
                    "Vivamus a tellus eget tortor blandit sagittis. Aliquam scelerisque consectetur malesuada. Vestibulum dictum quam non sagittis placerat. Curabitur vitae vestibulum tortor. Pellentesque vehicula aliquam ornare. Ut cursus dui porta euismod sagittis. Integer vel tellus congue, interdum augue quis, lobortis dui. Quisque sollicitudin nibh lorem, sed dapibus urna sagittis sit amet. In at viverra ex. Suspendisse fringilla enim sit amet eleifend sollicitudin. Phasellus id felis at nibh interdum elementum. Duis aliquam molestie eleifend."));
            System.out.println("Sended data");
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

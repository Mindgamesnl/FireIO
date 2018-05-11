import io.fire.core.client.FireIoClient;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.body.RequestString;
import io.fire.core.common.packets.ReceivedText;

public class testclient {

    public static void main(String[] args) {

        FireIoClient client = new FireIoClient("localhost", 80)
                .setPassword("testpassword1")
                .setAutoReConnect(2000)
                .setParameter("appversion", "1.0-RELEASE")
                .connect();

        client.on(Event.CONNECT, a -> {
            System.out.println("Connected with the server!");

            //submit a non-blocking request for data
            client.request("whoami", null, response -> {
                String result = ((RequestString) response).getString();
                System.out.println("The server told me that i am: " + result);
            });

        })

                .on(Event.DISCONNECT, a -> {
                    System.out.println("Connection with the server has closed!");
                })

                .on("MOTD", payload -> {
                    String text = ((ReceivedText) payload).getString();
                    System.out.println("The message of the day is: " + text);

                    //send a cookie jar
                    client.send("cookie_jar", new CookieJar(5, "chocolate"));
                })

                .on("thanks", eventPayload -> {
                    System.out.println("The server thanked you for your cookies");
                });



        //debug;
        client.getEventHandler().on(gl -> {
            if (gl.getIsEvent()) {
                System.out.println("Debug: receved event " + gl.getChannel() + " with payload " + gl.getOriginalPayload());
            } else {
                System.out.println("Debug: receved channel " + gl.getChannel() + " with payload " + gl.getOriginalPayload());
            }
        });

        //keep alive and dont instantly die
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Integer.MAX_VALUE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).run();


    }

}

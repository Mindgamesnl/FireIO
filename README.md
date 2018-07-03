<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# Fire-IO
### The LIT java socket & rest framework
[![](https://jitpack.io/v/Mindgamesnl/FireIO.svg)](https://jitpack.io/#Mindgamesnl/FireIO) [![Build Status](https://travis-ci.org/Mindgamesnl/FireIO.svg?branch=master)](https://travis-ci.org/Mindgamesnl/FireIO) [![GitHub version](https://d25lcipzij17d.cloudfront.net/badge.svg?id=gh&type=6&v=1.2&x2=0)](https://github.com/Mindgamesnl/FireIO)

Fire-IO is a [lightning fast](https://github.com/Mindgamesnl/FireIO/blob/master/performance.md) and super simple networking framework to handle your connections, data, clients and requests.
Great for building api's and communication systems.


Features include:
 - Super simple event driven networking
 - Send objects or strings
 - Client manager
 - REST endpoints
 - Auto re-connect
 - Packet-loss prevention
 - Password protection for your network
 - Async networking
 - Android compatible
 - Thread pools for event execution
 - Custom rate limiting
 
Fire-IO is designed for server-to-server data transfer for real time updates, push notifications, logging, monitoring and promise like data requests.

# Information
Some other links and information:
 - [Technical notes](https://github.com/Mindgamesnl/FireIO/blob/master/technotes.md)
 - [Javadoc](https://cdn.rawgit.com/Mindgamesnl/FireIO/master/javadoc/)
 - [Contact](https://twitter.com/Mindgamesnl)
 - [Performance testing results](https://github.com/Mindgamesnl/FireIO/blob/master/performance.md)

FireIO depends on:
 - Java 8

# Example code

Here is a simple example setup with a server, client, a custom packet, two way data communication and a non blocking data request including a callback

##### Example server
```java
FireIoServer server = new FireIoServer(80)
        .setPassword("testpassword1") //OPTIONAL: password
        .setRateLimiter(10 , 20) //OPTIONAL: rate limit the endpoints, in this case, 10 requests every 20 seconds
        .setThreadPoolSize(1) //OPTIONAL: thread pool for event handling execution

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

server.broadcast("message", "welcome everybody!");

//simple request based endpoint
server.onRequest("whoami", (client, request, response) -> {
    System.out.println(client.getId().toString() + " asked who it is! sending ip back");
    response.complete(new RequestString("You are: " + client.getInfo().getHostname()));
});

//simple http rest endpoints, one clear example and one with a variable
//so calling http://localhost/time will return "The server time is: 2018-07-01 15:10:44"
server.registerEndpoint("/time", req -> {
    return "The server time is: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
});

//one with a variable, the path is set to /hi/?
//this will mean that ? will be a variable
//so calling http://localhost/hi/Mats
//will return Welcome to FireIO Mats!
server.registerEndpoint("/hi/?", req -> {
    return "Welcome to FireIO " + req.getVariable(0) + "!";
});


//Client client = server.getClient(UUID.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"));
//client.send("message", "well hi there! you are the best.");
```

##### Example client
```java
FireIoClient client = new FireIoClient("localhost", 80) //host & port
        .setPassword("testpassword1") //OPTIONAL: password
        .setAutoReConnect(2000) //OPTIONAL: auto-reconnect with timeout
        .setParameter("appversion", "1.0-RELEASE") //throw it meta-date in handshake
        .setThreadPoolSize(1) //OPTIONAL: thread pool for event handling execution
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
```

##### Example custom packet (object)
```java
public class CookieJar extends Packet {
    private int amount = 0;
    private String type;
}
```

##### Example requst data (object)
```java
public class RequestString implements RequestBody {
    private String string;
}
```

##### Output of examples
<p align="center">
  <img src="https://gyazo.com/7e9e7c11f0137d5f3fda944f587fd2f6.png" />
</p>

# Dependencies
##### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Mindgamesnl</groupId>
    <artifactId>FireIO</artifactId>
    <version>LATEST</version>
</dependency>
```

##### Gradle
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

```
dependencies {
    compile 'com.github.Mindgamesnl:FireIO:BUILD NUMBER'
}
```

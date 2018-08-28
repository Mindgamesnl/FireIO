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
    .setPassword("testpassword1")
    .setRateLimiter(2, 10)

.on(Event.CONNECT, eventPayload -> {
    Client client = (Client) eventPayload;
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

//simple request based endpoint
server.onRequest("whoami", (client, request, response) -> {
    System.out.println(client.getId().toString() + " asked who it is! sending ip back");
    response.complete(new RequestString("You are: " + client.getInfo().getHostname()));
});

//simple http rest endpoints, one clear example and one with a variable
server.registerEndpoint("/time", req -> {
    return "The server time is: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
});

//one with a variable, the path is set to /hi/?name
//this will mean that ?name will be a variable, example
server.registerEndpoint("/hi/?name", req -> {
    return "Welcome to FireIO " + req.getVariable("name") + "!";
});
```

##### Example client
```java
FireIoClient client = new FireIoClient("localhost", 80)
        .setPassword("testpassword1")
        .setAutoReConnect(2000)
        .setParameter("appversion", "1.0-RELEASE")
        .connect();

client.on(Event.CONNECT, a -> {
    System.out.println("Connected with the server!");
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

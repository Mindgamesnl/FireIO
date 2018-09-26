<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# Fire-IO [![](https://jitpack.io/v/Mindgamesnl/FireIO.svg)](https://jitpack.io/#Mindgamesnl/FireIO) [![Build Status](https://travis-ci.org/Mindgamesnl/FireIO.svg?branch=master)](https://travis-ci.org/Mindgamesnl/FireIO) [![GitHub version](https://d25lcipzij17d.cloudfront.net/badge.svg?id=gh&type=6&v=1.3.4&x2=0)](https://github.com/Mindgamesnl/FireIO)
Fire-IO is a [lightning fast](https://github.com/Mindgamesnl/FireIO/blob/master/performance.md) cross platform networking solution that allows you to connect and manage data from multiple platforms with one easy to use java server. The ultimate framework for your networking needs.

And all that for the sweet price of nothing.

### Platforms
 * **Java** Java server
 * **Java** Client
 * **JavaScript** Small web client
 * **PHP & Other** Can make use of socket, websocket or the Rest implementation
 * **Load Balancer** Fire-IO comes with a real time load balancer to scale your application.  

### Features
 * **Events** All event driven API
 * **Content types** Send objects, strings and more with build in (de)serialisation
 * **Web** Compatible with your web application via our REST service and automatic websocket implementation
 * **Data Loss Prevention** Automatically detect the failure of a packet and try to repair the data or resend it
 * **Auto Reconnect** Auto reconnect for all-clients
 * **Rate Limiter** Build in rate limiter for all your endpoints (rest, api and socket)
 * **Async** All networking is done in async at all times and events are correctly handled via pools, so no more worrying about response times.
 * **Requests** Handle requests from clients, assign a response to one specific piece of data
 * **Passwords** Password protection for your network, don't allow connections without a key
 * **Load Balancer** improve the reliability and response times of your applications by setting up load balancers to evenly spread the request and connections among your servers.
 
### Useful Links
 - [Technical notes](https://github.com/Mindgamesnl/FireIO/blob/master/documentation/technotes.md)
 - [Load Balancer Documentation](https://github.com/Mindgamesnl/FireIO/blob/master/documentation/loadbalancer.md)
 - [Javadoc](https://cdn.rawgit.com/Mindgamesnl/FireIO/master/platforms/java/javadoc/)
 - [Contact](https://twitter.com/Mindgamesnl)
 - [Performance testing results](https://github.com/Mindgamesnl/FireIO/blob/master/documentation/performance.md)
 - [Java Source Code](https://github.com/Mindgamesnl/FireIO/tree/master/platforms/java)
 - [Web Source Code](https://github.com/Mindgamesnl/FireIO/tree/master/platforms/web)

### FireIO depends on:
 - Java 8

# Example code

Here is a simple example setup with a server, client, a custom packet, two way data communication and a non blocking data request including a callback

### Example Java server
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

### Web JavaScript Client
```html
<script src="Fire-IO.js"></script>
<script>
    const client = new FireIoClient("localhost", 80);
    client.setPassword("testpassword1");
    client.setAutoReconnect(500);
    client.connect();
    
    client.on("connect", function () {
        console.log("connected!!");
        client.send("channel", "Hello world!");
    });
    
    client.on("channel", function (data) {
        console.log("The server said: " + data);
    });
    
    client.on("disconnect", function () {
        console.log("disconnected!!");
    });
</script>
```

### Example Java Client
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

### Example Custom Java packet (object)
```java
public class CookieJar extends Packet {
    private int amount = 0;
    private String type;
}
```


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
        maven { url 'https://jitpack.io' }
    }
}
```

```
dependencies {
    compile 'com.github.Mindgamesnl:FireIO:BUILD NUMBER'
}
```

<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# FireIo, a simple ASYNC java socket [![](https://jitpack.io/v/Mindgamesnl/FireIO.svg)](https://jitpack.io/#Mindgamesnl/FireIO) [![Build Status](https://travis-ci.org/Mindgamesnl/FireIO.svg?branch=master)](https://travis-ci.org/Mindgamesnl/FireIO) [![GitHub version](https://d25lcipzij17d.cloudfront.net/badge.svg?id=gh&type=6&v=1.0.1&x2=0)](https://github.com/Mindgamesnl/FireIO) [![Open Source Love](https://badges.frapsoft.com/os/v3/open-source.svg?v=102)](https://github.com/ellerbrock/open-source-badge/)

FireIO is a simple java socket solution with multi client support and easy to use networking channels

in layman’s terms, object thingy goes in, object thingy comes out

# Why fireio
FireIO is a simple socket system for server-to-server communication as a stand-alone replacement for systems like redis's pub-sub.

And i was super bored on the plane and thus made this

# Example server
```java
FireIoServer server = new FireIoServer(80)
        .setPassword("testpassword1") //OPTIONAL: password

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

//Client client = server.getClient(UUID.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"));
//client.send("message", "well hi there! you are the best.");
```

# Example client
```java
FireIoClient client = new FireIoClient("localhost", 80) //host & port
        .setPassword("testpassword1") //OPTIONAL: password
        .setAutoReConnect(2000) //OPTIONAL: auto-reconnect with timeout
        .setParameter("appversion", "1.0-RELEASE") //throw it meta-date in handshake
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

# Example custom packet (object)
```java
public class CookieJar extends Packet {
    private int amount = 0;
    private String type;
}

```

# Maven
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

# Gradle (yes, it's android compatible)
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

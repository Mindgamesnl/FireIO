<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# Fire-IO Event System
### packets
You can register a simple packet. The first argument is the class of the packet you wish to receive (you CAN handle multiple packets on one channel, you just have to register them all)
```java
main.onPacket(PingPacket.class, "my-channel").onExecute((sender, packer) -> {
    //handle packet send by sender
});
```
Besides this, you may also give an event priority.
You can use this to force the order of multiple executors.
these being `LOWEST`, `LOW`, `NORMAL`, `HIGH` and `HIGHEST`.
```java
main.onPacket(PingPacket.class, "my-channel", EventPriority.NORMAL).onExecute((sender, packer) -> {
    //handle packet send by sender
});
```
For clients or small platforms, it is also possible to leave out the sender and just handle the packet.
```java
main.onPacket(PingPacket.class, "my-channel").onExecute(packer -> {
    //handle packet send by sender
});
```

### events
Events now follow a similar system, but also have some changes.
An event also gives a client, but also an optional String as a message for what happened.
example
```java
main.on(Event.TIMED_OUT, client -> {
    System.out.println(client.getId() + " closed unexpectedly! " + client.getConnectionType());
});
```
or
```java
main.on(Event.TIMED_OUT, (client, message) -> {
    System.out.println(client.getId() + " closed unexpectedly! message=" + message);
})
```

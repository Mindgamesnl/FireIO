<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# Fire-IO Load Balancer
We've all been there. <br>You just sit and chill, until a message pops out of nowhere reminding you that the response times are crap and the service is down ONCE AGAIN.

By implementing a Fire-IO load balancer in front of you application you reducd.

## Setting up the balancer
<p align="center">
  <img src="http://static.craftmend.com/fireio/diagram.png" />
  
  ###### ^ Example diagram
</p>

You start up by configuring your Load Balancer server.<br >
To get started, clone and compile the latest Fire-IO version (preferably from master)<br />
`sudo java -cp <FIRE IO JAR PATH> io/fire/core/loadbalancer/startup/BalancerService`

After starting for the first time, a `server.properties` file will be created.<br />
This will be your main config file for the server.

The values do the following things:
 * **port** Is the port that the load balancer will attach to, this will also be the port that your CLIENT has to connect to.
 * **public_password** is the password your CLIENT uses to connect and authenticate with the Fire-IO servers attached to the loadbalancer
 * **private_password** is the private password your SERVERS will use to register at the load balancer
 * **ratelimit_per_session** is the amount of requests ONE CLIENT can do until they have reached their time out again
 * **ratelimit_session_timeout** is the time (in seconds) that there has to be total radio silence between ONE SPECIFIC client and the load balancer in order for the session to reset and accept new requests

It is possible to link multiple load balancers at the same time, just use different ports and repeat the steps for each of your balancers.

The load balancer will automatically remove an in active or dead Fire-IO server from the pool. This means that a client will never be send to a server that is unable to complete their request.

It is also possible to add/remove Fire-IO servers to your pool on-the-fly, no need to restart when up/down scaling your application/service. 

## Configuring the Fire-IO servers
In order for the Fire-IO servers to accept connections though the load balancer, they need to register.
You can do this by adding this line in your server application.

```java
server.linkLoadBalancer(new BalancerConfiguration("BALANCER HOST", PORT, "PRIVATE PASSWORD"));
```

You can register multiple load balancers at the same time.<br />
Please note that your FIre-IO server MAY NOT run on the same port as the load balancer.

## Important notes
 * The Fire-IO load balancer is **NOT** a proxy, it will direct clients to the next available server. This means that there is no mission-critical data in the load balancer at any time.
 * You can add and remove load balancers in real time. The network will ajust automatically and none of your current connectioins will be dropped.
 
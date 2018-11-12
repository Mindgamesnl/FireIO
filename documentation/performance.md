<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# Fire-IO Performance
Performance is really important for high scalable applications. FireIO is built to handle as many clients as quickly as possible. A few tests (including the code to run them for yourself) are available down below.

The setup:
 - One Fire-IO server with rate limiting disabled and a thread pool with the size of `16` threads
 - All tests ran on localhost
 - Tests are ran on a mid rage pc (quad core intel I5 with 8GB ram) running Ubuntu
 - Java 8
 - Socket testing is done with a socket test script (see below this page)
 - Restful testing is done with siege for Ubuntu (command: `siege --time 5s http://localhost:80`)
 
# Rest endpoints
Fire-IO has a build in web server to run restful API's.

The restful handler and service can handle up to `18940` requests per second!

Test results:
```text
Transactions:		       34503 hits
Availability:		      100.00 %
Elapsed time:		        4.65 secs
Data transferred:	       42.28 MB
Response time:		        0.00 secs
Transaction rate:	     7420.00 trans/sec
Throughput:		        9.09 MB/sec
Concurrency:		       23.63
Successful transactions:       34504
Failed transactions:	           0
Longest transaction:	        0.09
Shortest transaction:	        0.00
```

# Socket connections
For this test, there is a setup of ONE password protected Fire-IO server and 1000 clients. It uses the restful api for auth and token requesting, so besides 1000 sockets its also handeling 1000 rest requests in the background.

The goal is to connect as many clients as possible in one second, testing shows that connecting, authenticating and registering `1000` clients takes `470MS`!

####Test server
```java
public class RatedServer {
    public static void main(String[] args) {
        try {
            final int[] clients = {0};
            FireIoServer server = new FireIoServer(80)
                    .setPassword("testpassword1")
                    .setRateLimiter(1005, 1)
                    .setThreadPoolSize(16)

                    .on("go", a -> {
                        System.out.print("Clients at end: " + clients[0]);
                    })

                    .on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
                        clients[0]--;
                    })

                    .on(Event.CONNECT, eventPayload -> {
                        clients[0]++;
                    });

            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

            ses.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Clients: " + clients[0]);
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

####Test client
```java
public class RateClient {
    public static void main(String[] args) {
        List<FireIoClient> fireIoClientList = new ArrayList<>();
        ThreadPoolExecutor executor =
                (ThreadPoolExecutor) Executors.newFixedThreadPool(30);
        final int[] connected = {0};
        System.out.print("starting");
        Instant start = Instant.now();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Clients: " + connected[0]);
            }
        }, 0, 1, TimeUnit.SECONDS);
        for (int i = 1; i <= 1000; i++) {
            executor.submit(() -> {
                FireIoClient client = new FireIoClient("localhost", 80)
                        .setPassword("testpassword1")
                        .connect();

                client.on(Event.CONNECT, a -> {
                    connected[0]++;
                    if (connected[0] == 1000) {
                        Instant end = Instant.now();
                        System.out.println("Opened started 1000 clients in " + (Duration.between(start, end).getNano() / 1000000) + " milliseconds!");
                    }
                })

                .on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
                    connected[0]--;
                });
                fireIoClientList.add(client);
            });
        }
    }
}
```

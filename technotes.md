<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# Fire-IO Technical Notes
So.... you are a nerd too? like to tinker with thingamajigs and bits and bops? Well good for you.

So let's talk about the inner workings of Fire-IO, the juicy stuff, the good stuff, the fine machined code (ok, i'll stop)

# Socket Authentication & Handshake
The authentication process takes quite some steps to authenticate with the client token and password, before the actual socket gets opened, a REST request is made to the Fire-IO server (with the optional password).
ONLY if this request gets accepted (rate limiting, platform and the (optional) password) the server will respond with a token. That token is used to authenticate over the socket.

Once the socket is opened (empty) the client has to send the authentication token (received from the previous rest call) within 100MS, if the server does not receive a VALID token within that time, the connection gets terminated and the rate limiter gets called.

The steps:
 1. create client
 2. setup modules
 3. setup thread pools
 4. open http connection and request the token (optional: password)
 5. open socket
 6. send authentication header over socket
 7. wait for authentication validation from server
 8. request buffer size
 9. call `Event.CONNECT`
 
# Threading Model
#### Server
The Fire-IO server always uses at at least one thread to listen and handle connections.

#### Client
The Fire-IO client always uses one allocated thread to handle the IO listener for the socket client, this thread gets closed when the connection is also closed. 

#### Common
The events and requests are handled in a thread pool.
Ever triggered event instance is in it's own executed task in one of the threads. This makes it safe to make other API and DATABASE calls from within the socket events and requests.

The thread pool size can be changed using `.setThreadPoolSize(size)`, it takes a few milliseconds to update the thread pool, this should only been done when starting and creating a Fire-IO instance.

# Malformed Data Handling
Networking can be a bit tricky, some times there is a bit of data loss, it happens to the best of us.

Fire-IO has a few automatic fail safes build in to prevent this from happening.
 -  **End Signatures** every packet gets encoded to bytes, every byte stream ends with an ending signature, upon receiving data, Fire-IO looks for this end signature before it attempts to decode it. If it finds multiple it splits it, this prevents issues from streams of packets and compression some WIFI routers have.
 - **IOErrorHandling** In case of a critical error whilst trying to send a packet, it adds the received data to a queue to retry parsing the data when the server/client sends new instructions.
 - **Automatic Buffers** When trying to send an unusually large packet, Fire-IO server will notify all current and future clients that there is a enw maximum packet size in the network, all IO handlers will act accordingly and always be prepared for on the fly changes in packet buffers.
 - **Invalid REST calls** The restful server will always send data, once invalid data is received by the server it sends a error message to the client notifying about what happened, the server never responds with empty data. (unless the rate limiter kicked in)
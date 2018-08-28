<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# Fire-IO Technical Notes
So.... you are a nerd too? like to tinker with thingamajigs and bits and bops? Well good for you.

So let's talk about the inner workings of Fire-IO, the juicy stuff, the good stuff, the fine machined code (ok, i'll stop)

# Malformed Data Handling
Networking can be a bit tricky, some times there is a bit of data loss, it happens to the best of us.

Fire-IO has a few automatic fail safes build in to ensure the correct handling of your data amd transactions.
 - **Request Validation** Server requests can be used to request important data from the server or ask the server to do a database query, to prevent the server from getting overloaded and to ensure that all data will be handled propperly requests are handled in queues. The client can prompt multiple requests at once, but it only sends the first request. The client will send the server the follow up request when
    - There are no other requests queued
    - The server finished the previous request
    - The server denied the previous request
    - The server failed to handle the previous request
   
   this is to ensure there are no "stacked" or duplicate connections/requests at once.
   So when let's say, a database error occurs the following requests will be canceled since they will also fail due to the same proble, so the server outright cancels it.
   The server will only re evaluate the requests (and optional queue) when the client pushes a new successful request.
  
   This makes it safe to do external request (like database query's or lookups) directly from the request endpoint. 
 - **IOErrorHandling** In case of a critical error whilst trying to send a packet, it adds the received data to a queue to retry parsing the data when the server/client sends new instructions.
 - **Invalid REST calls** The restful server will always send data, once invalid data is received by the server it sends a error message to the client notifying about what happened, the server never responds with empty data. (unless the rate limiter kicked in)
 - **Data streams** To prevent data loss all traffic (even static packets) are separated in 64-Byte parts. Only the last part ends with a signature. Upon receiving data, FireIO saves it in an array (or adds it if there is already data). Only if the end signature is received, it links all the previous array's and parses them as one input stream. The stream cache clears when this process is finished, waiting for a new Byte stream for this process to start all over again. This allows for popper multi threading and encourages the streaming of data.
 - **Handling Network Compression** It's not uncommon to find Data Compression in modern virtual networks (and some hardware drivers), this poses a major threat to big data streams. To work around this issue, every packet is split into parts. Every 20 bytes there is a "packet indicator", if the packet size does NOT match up with the amount of indicators (and their patterns) it assumes that the packet got corrupted. This splicing also gives the client some time to catch up in the case of compression (where the data does not all arrive at the same time).
 
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

# End
That's it folks!

This is pretty much how Fire-IO works behind the API.
If you have any questions or concerns, feel free to contact me via [Twitter](https://twitter.com/Mindgamesnl), Lucky for you, I dont have a social life so I'll respond quickly.
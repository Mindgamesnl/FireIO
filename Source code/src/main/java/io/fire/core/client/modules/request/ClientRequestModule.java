package io.fire.core.client.modules.request;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.request.interfaces.ClientRequest;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.RequestBody;
import io.fire.core.common.packets.CancelRequestPacket;
import io.fire.core.common.packets.SubmitRequestPacket;

import java.io.IOException;
import java.util.*;

public class ClientRequestModule {

    //Map to hold pending request callbacks with theire id to execute when the server responds
    private Map<UUID, ClientRequest> ongoingRequests = new HashMap<>();
    private Queue<Packet> awaitingRequests = new LinkedList<>();
    private FireIoClient client;

    public ClientRequestModule(FireIoClient client) {
        this.client = client;
    }

    //create, register and send a request
    public void createRequest(String channel, RequestBody request, ClientRequest callback) {
        //create and set
        SubmitRequestPacket requestPacket = new SubmitRequestPacket();
        requestPacket.setId(channel);
        requestPacket.setPayload(request);
        requestPacket.setRequestId(UUID.randomUUID());

        //save the callback in memmory to execute when the server gives a response
        ongoingRequests.put(requestPacket.getRequestId(), callback);

        if (ongoingRequests.values().size() == 1) {
            //send request with body and id to the server
            try {
                client.getSocketModule().getConnection().emit(requestPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            awaitingRequests.add(requestPacket);
        }
    }

    //not handled by server, cancel
    public void cancel(UUID id) {
        //remove the callback since it wont get triggered again
        ongoingRequests.remove(id);

        Packet nextRequest = awaitingRequests.poll();
        if (nextRequest != null) {
            try {
                client.getSocketModule().getConnection().emit(nextRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //handle request with result
    public void handleRequestResponse(UUID id, RequestBody response) {
        //check if the request actually excists, prevents nullpointers and other weird behavior
        if (!ongoingRequests.containsKey(id)) return;

        //get and trigger the callback with the id with the response from the server
        ongoingRequests.get(id).call(response);

        //remove the callback since it wont get triggered again
        ongoingRequests.remove(id);

        Packet nextRequest = awaitingRequests.poll();
        if (nextRequest != null) {
            try {
                client.getSocketModule().getConnection().emit(nextRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

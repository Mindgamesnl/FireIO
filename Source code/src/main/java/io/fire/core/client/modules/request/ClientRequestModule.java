package io.fire.core.client.modules.request;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.request.interfaces.ClientRequest;
import io.fire.core.common.interfaces.RequestBody;
import io.fire.core.common.packets.SubmitRequestPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientRequestModule {

    private Map<UUID, ClientRequest> ongoingRequests = new HashMap<>();
    private FireIoClient client;

    public ClientRequestModule(FireIoClient client) {
        this.client = client;
    }

    public void createRequest(String channel, RequestBody request, ClientRequest callback) {
        SubmitRequestPacket requestPacket = new SubmitRequestPacket();
        requestPacket.setId(channel);
        requestPacket.setPayload(request);
        requestPacket.setRequestId(UUID.randomUUID());

        ongoingRequests.put(requestPacket.getRequestId(), callback);

        try {
            client.getSocketModule().getConnection().emit(requestPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRequestResponse(UUID id, RequestBody response) {
        if (!ongoingRequests.containsKey(id)) return;
        ongoingRequests.get(id).call(response);
        ongoingRequests.remove(id);
    }

}

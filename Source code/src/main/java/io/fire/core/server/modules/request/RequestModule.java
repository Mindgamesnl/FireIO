package io.fire.core.server.modules.request;

import io.fire.core.common.interfaces.RequestBody;
import io.fire.core.common.packets.CompleteRequestPacket;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.FireIoConnection;
import io.fire.core.server.modules.client.superclasses.Client;
import io.fire.core.server.modules.request.interfaces.RequestExecutor;
import io.fire.core.server.modules.request.objects.RequestResponse;

import java.io.IOException;
import java.util.*;

public class RequestModule {

    private Map<String, List<RequestExecutor>> requstExecutors = new HashMap<>();

    public RequestModule(FireIoServer server) {

    }

    public void trigger(String channel, RequestBody body, Client client, UUID uuid) {
        if (!requstExecutors.containsKey(channel)) return;
        for (RequestExecutor executor : requstExecutors.get(channel)) {
            RequestResponse requestResponse = new RequestResponse();
            requestResponse.setRequestId(uuid);

            requestResponse.setCompletableRequest(requestBody -> {

                FireIoConnection fireIoClient = (FireIoConnection) client;

                CompleteRequestPacket completeRequestPacket = new CompleteRequestPacket();

                completeRequestPacket.setRequestId(uuid);
                completeRequestPacket.setResult(requestBody);

                try {
                    fireIoClient.getHandler().emit(completeRequestPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            executor.onRequest(client, body, requestResponse);
        }
    }

    public void register(String channel, RequestExecutor executor) {
        if (!requstExecutors.containsKey(channel)) requstExecutors.put(channel, new ArrayList<>());
        requstExecutors.get(channel).add(executor);
    }

}

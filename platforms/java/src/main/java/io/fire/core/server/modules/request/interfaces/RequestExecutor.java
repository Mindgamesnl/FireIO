package io.fire.core.server.modules.request.interfaces;

import io.fire.core.common.interfaces.RequestBody;
import io.fire.core.server.modules.client.superclasses.Client;
import io.fire.core.server.modules.request.objects.RequestResponse;

public interface RequestExecutor {

    /**
     * request event
     * as a runnable to trigger evey time a request is received
     * request trigger containing who send it (client), what it asked (body) and a completable response function
     *
     * @param client
     * @param body
     * @param response
     */

    void onRequest(Client client, RequestBody body, RequestResponse response);

}

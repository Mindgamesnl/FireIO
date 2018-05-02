package io.fire.core.server.modules.request.interfaces;

import io.fire.core.common.interfaces.RequestBody;
import io.fire.core.server.modules.client.superclasses.Client;
import io.fire.core.server.modules.request.objects.RequestResponse;

public interface RequestExecutor {

    void onRequest(Client client, RequestBody body, RequestResponse response);

}

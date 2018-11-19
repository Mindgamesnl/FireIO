package io.fire.core.server.modules.request.interfaces;

import io.fire.core.common.interfaces.RequestBody;

public interface CompletableRequest {


    /**
     * completable request
     * used to finish (reply) to request from the client via the api with a request body as result
     *
     * @param requestBody
     */

    void complete(RequestBody requestBody);

}

package io.fire.core.client.modules.request.interfaces;

import io.fire.core.common.interfaces.RequestBody;

//Callback interface for requests
public interface ClientRequest {

    /**
     * callback interface for requests
     *
     * @param response
     */
    void call(RequestBody response);

}

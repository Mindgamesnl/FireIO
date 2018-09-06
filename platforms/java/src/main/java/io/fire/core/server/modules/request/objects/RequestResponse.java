package io.fire.core.server.modules.request.objects;

import io.fire.core.common.interfaces.RequestBody;
import io.fire.core.server.modules.request.interfaces.CompletableRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class RequestResponse {

    //request response
    //used by the api to reply to a request and handle it
    //containing the function and id of the request

    private CompletableRequest completableRequest;
    private UUID requestId;

    public void complete(RequestBody body) {
        completableRequest.complete(body);
    }

}

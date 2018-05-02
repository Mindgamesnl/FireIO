package io.fire.core.server.modules.request.interfaces;

import io.fire.core.common.interfaces.RequestBody;

public interface CompletableRequest {

    void complete(RequestBody requestBody);

}

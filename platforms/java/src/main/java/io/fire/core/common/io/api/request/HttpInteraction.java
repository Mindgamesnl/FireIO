package io.fire.core.common.io.api.request;

import io.fire.core.common.io.api.api.IncomingRequest;
import io.fire.core.common.io.api.api.ResponseSettings;

public interface HttpInteraction {

    String complete(IncomingRequest request, ResponseSettings settings);


}

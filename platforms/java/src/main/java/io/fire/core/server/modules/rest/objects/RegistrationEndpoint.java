package io.fire.core.server.modules.rest.objects;

import io.fire.core.server.modules.rest.interfaces.RestClientRegestration;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationEndpoint {
    private RestClientRegestration restClientRegestration;
}

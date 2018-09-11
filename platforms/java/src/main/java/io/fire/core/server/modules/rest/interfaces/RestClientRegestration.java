package io.fire.core.server.modules.rest.interfaces;

import io.fire.core.server.modules.rest.objects.RegistrationResult;

public interface RestClientRegestration {

    RegistrationResult onCall(String password);

}

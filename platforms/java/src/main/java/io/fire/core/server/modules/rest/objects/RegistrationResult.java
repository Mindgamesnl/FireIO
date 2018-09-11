package io.fire.core.server.modules.rest.objects;

import io.fire.core.server.modules.rest.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrationResult {
    private boolean isOk;
    private String result;
    private ContentType contentType;
}

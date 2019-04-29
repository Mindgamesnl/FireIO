package io.fire.core.server.modules.http.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ExecutableRoute {

    private Map<String, String> variables;
    private Route route;

}

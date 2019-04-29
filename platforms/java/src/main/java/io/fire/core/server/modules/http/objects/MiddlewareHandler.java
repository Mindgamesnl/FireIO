package io.fire.core.server.modules.http.objects;

import io.fire.core.server.modules.socket.objects.Connection;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
public class MiddlewareHandler {

    @Getter private Route route;
    @Setter @Getter private Boolean cancelled = false;
    @Getter private Connection connection;

    public MiddlewareHandler(Route route, Connection connection) {
        this.route = route;
        this.connection = connection;
    }
}

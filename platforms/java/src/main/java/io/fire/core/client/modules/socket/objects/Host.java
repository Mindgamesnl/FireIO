package io.fire.core.client.modules.socket.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Host {

    private String host;
    private int port;

}

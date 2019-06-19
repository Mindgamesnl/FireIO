package io.fire.core.client.modules.socket.objects;

import io.fire.core.common.io.socket.interfaces.Packet;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ClientDetails extends Packet {

    private UUID uuid = null;
    private String password = null;
    private Map<String, String> headers = new HashMap<>();

    public ClientDetails setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

}

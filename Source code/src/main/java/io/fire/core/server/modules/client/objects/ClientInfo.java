package io.fire.core.server.modules.client.objects;

import io.fire.core.common.interfaces.ClientMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {

    private String platform;
    private String hostname;
    private Map<String, String> arguments;
    private Map<String, ClientMeta> argumentsMeta;

}

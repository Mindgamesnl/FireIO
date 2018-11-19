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

    /**
     * client info object
     * acts as holder for client information
     */

    //platform, example: UBUNTU
    private String platform;
    //hostname, example: ipadress here lol
    private String hostname;
    //arguments and meta, used for custom authentication whilst connecting
    private Map<String, String> arguments;
    private Map<String, ClientMeta> argumentsMeta;

}

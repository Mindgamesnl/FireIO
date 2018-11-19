package io.fire.core.server.modules.balancingmodule.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalancerConfiguration {

    private String host;
    private int port;
    private String password = null;

    /**
     * Loadbalancer configuration, used to connect with a external loadbalancer
     *
     * @param host
     * @param port
     */
    public BalancerConfiguration(String host, int port) {
        this.host = host;
        this.port = port;
    }
}

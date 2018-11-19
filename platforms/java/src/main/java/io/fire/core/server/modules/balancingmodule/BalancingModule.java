package io.fire.core.server.modules.balancingmodule;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.balancingmodule.objects.BalancedConnection;
import io.fire.core.server.modules.balancingmodule.objects.BalancerConfiguration;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class BalancingModule {

    private List<BalancedConnection> balancers = new ArrayList<>();


    /**
     * Register a loadbalancer
     *
     * @param config
     * @param main
     */
    public void register(BalancerConfiguration config, FireIoServer main) {
        balancers.add(new BalancedConnection(main, config.getHost(), config.getPort(), config.getPassword()));
    }

}

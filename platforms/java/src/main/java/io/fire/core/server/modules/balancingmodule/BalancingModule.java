package io.fire.core.server.modules.balancingmodule;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.balancingmodule.objects.BalancedConnection;
import io.fire.core.server.modules.balancingmodule.objects.BalancerConfiguration;

import java.util.ArrayList;
import java.util.List;

public class BalancingModule {

    private List<BalancedConnection> balancers = new ArrayList<>();

    public BalancingModule(FireIoServer server) {

    }

    public void register(BalancerConfiguration config, FireIoServer main) {
        balancers.add(new BalancedConnection(main, config.getHost(), config.getPort(), config.getPassword()));
    }

}

package io.fire.core.loadbalancer;

import io.fire.core.loadbalancer.rest.RestHandlers;
import io.fire.core.loadbalancer.servermanager.ServerManager;
import io.fire.core.loadbalancer.socket.SocketHandler;
import io.fire.core.server.FireIoServer;
import lombok.Getter;

import java.io.IOException;

public class FireIoBalancer {

    @Getter private FireIoServer balancingServer;

    @Getter private RestHandlers restModule;
    @Getter private ServerManager serverManager;
    @Getter private SocketHandler socketHandler;

    public FireIoBalancer(int port) throws IOException {
        //start server to handle requests
        balancingServer = new FireIoServer(port);

        restModule = new RestHandlers(this);
        serverManager = new ServerManager(this);
        socketHandler = new SocketHandler(this);
    }

    public FireIoBalancer setPublicPassword(String pswd) {
        this.restModule.setClientPassword(pswd);
        return this;
    }

    public FireIoBalancer setNetworkPassword(String pswd) {
        this.restModule.setServerPassword(pswd);
        return this;
    }
}

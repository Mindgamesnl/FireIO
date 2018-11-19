package io.fire.core.loadbalancer;

import io.fire.core.loadbalancer.config.ConfigFile;
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


    /**
     * Setup and initiate the FireIoBalancer based of a Config File
     *
     * @param configFile
     * @throws IOException
     */
    public FireIoBalancer(ConfigFile configFile) throws IOException {
        //load from config
        int port = Integer.valueOf(configFile.getString("port"));

        //start server to handle requests
        balancingServer = new FireIoServer(port);
        balancingServer.setRateLimiter(configFile.getInt("ratelimit_per_session"), configFile.getInt("ratelimit_session_timeout"));

        serverManager = new ServerManager(this);
        socketHandler = new SocketHandler(this);
        restModule = new RestHandlers(this);
        setPublicPassword(configFile.getString("public_password"));
        setNetworkPassword(configFile.getString("private_password"));
    }


    /**
     * Set public password, for clients to connect to
     *
     * @param pswd
     * @return
     */
    public FireIoBalancer setPublicPassword(String pswd) {
        this.restModule.setClientPassword(pswd);
        return this;
    }


    /**
     * Set private password, for servers to join the network
     *
     * @param pswd
     * @return
     */
    public FireIoBalancer setNetworkPassword(String pswd) {
        this.restModule.setServerPassword(pswd);
        return this;
    }
}

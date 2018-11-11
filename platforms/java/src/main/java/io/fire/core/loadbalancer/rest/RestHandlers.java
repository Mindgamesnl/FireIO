package io.fire.core.loadbalancer.rest;

import io.fire.core.common.io.api.request.HttpInteraction;
import io.fire.core.common.objects.VersionInfo;
import io.fire.core.loadbalancer.FireIoBalancer;
import io.fire.core.loadbalancer.servermanager.objects.FireIoNode;
import lombok.Setter;

import java.util.UUID;

public class RestHandlers {

    @Setter private String clientPassword = null;
    @Setter private String serverPassword = null;

    public RestHandlers(FireIoBalancer balancer) {

        //change "authentication" endpoint to allow redirects
        HttpInteraction login = ((request, settings) -> {
            if (clientPassword != null) {
                if (!clientPassword.equals(request.getVariable("password"))) {
                    return "fail-auth";
                }
            }

            FireIoNode node = balancer.getServerManager().getAvailableServer();
            if (node == null) {
                return "No server..";
            } else {
                settings.setHeader("Location", node.getHost() + ":" + node.getPort());
            }
            return "redirecting...";
        });

        balancer.getBalancingServer().registerEndpoint("/fireio/register", login);
        balancer.getBalancingServer().registerEndpoint("/fireio/register/?password", login);


        //auth adress for servers
        balancer.getBalancingServer().registerEndpoint("/fireio/balancer/list",  (request, settings) -> {
            final String[] out = {""};
            balancer.getServerManager().getNodes().values().forEach(n -> {
                out[0] += "id="+n.getUuid().toString() + ",state="+n.getState() +",clients="+n.getConnections()+",httpinteractions="+n.getRestInteractions()+"\n";
            });
            return out[0];
        });

        balancer.getBalancingServer().registerEndpoint("/", (request, settings) -> {
            FireIoNode n = balancer.getServerManager().getAvailableEndpoint();
            if (n == null) {
                return "no-server-available";
            }
            n.restInteractions++;
            return "http-redirect:http://" + n.getHost() + ":" + n.getPort() + request.getUrl();
        });

        //auth adress for servers
        balancer.getBalancingServer().registerEndpoint("/fireio/balancer/register/?port/?password",  (request, settings) -> {
            if (serverPassword != null) if (!serverPassword.equals(request.getVariable("password"))) return "fail-auth";
            //do shit
            UUID id = balancer.getBalancingServer().getClientModule().registerConnection().getId();
            String out = id.toString() + "INFO:" + new VersionInfo().toString();
            //register new expecting node
            FireIoNode node = balancer.getServerManager().create(id);
            node.setPort(Integer.valueOf(request.getVariable("port")));
            node.setHost(request.getAdress().getHostName());
            return out;
        });
    }

}

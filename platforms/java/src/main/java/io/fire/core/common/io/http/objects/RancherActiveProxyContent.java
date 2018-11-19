package io.fire.core.common.io.http.objects;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;

@Data
@NoArgsConstructor
public class RancherActiveProxyContent {


    /**
     * Rancher Active Proxy header data for IP-Address corrections
     */
    private int forwardedPort = 80;
    private String realIp = "0.0.0.0";
    private boolean forwardedSsl = false;
    private String forwardedProtocol = "http";

}

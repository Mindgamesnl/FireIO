package io.fire.core.common.io.http.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
public class ConnectionInfo {

    @Getter private int proxyForwardedPort = 80;
    @Getter private String ipAddress = "0.0.0.0";
    @Getter private boolean proxyForwardedSsl = false;
    @Getter private String proxyForwardedProtocol = "http";
    @Getter private boolean isProxy = false;

}

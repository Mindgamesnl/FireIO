package io.fire.core.common.io.api.request;

import io.fire.core.common.io.IoManager;
import io.fire.core.common.io.http.objects.HttpContent;
import io.fire.core.server.FireIoServer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@Data
@AllArgsConstructor
public class PendingRequest {

    private IoManager manager;
    private HttpContent headers;
    private SocketChannel socketChannel;

    public void finish(HttpContent response) {
        try {
            socketChannel.write(response.getBuffer());
            manager.getServer().getSocketModule().getAsyncNetworkService().getSelectorHandler().getReferences().remove(socketChannel.socket().getRemoteSocketAddress());
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

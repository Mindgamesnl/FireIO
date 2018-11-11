package io.fire.core.common.io.api.request;

import io.fire.core.common.io.IoManager;
import io.fire.core.common.io.http.objects.HttpContent;
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
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

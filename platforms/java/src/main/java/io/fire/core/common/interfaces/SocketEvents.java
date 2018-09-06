package io.fire.core.common.interfaces;

import io.fire.core.common.io.IoManager;
import io.fire.core.common.io.objects.WebSocketTransaction;

public interface SocketEvents {

    void onPacket(Packet packet);
    void onWebsocketPacket(WebSocketTransaction webSocketTransaction) throws Exception;
    void onClose();
    void onOpen();
    IoManager getIoManager();

}

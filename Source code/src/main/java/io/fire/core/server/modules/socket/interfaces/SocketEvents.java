package io.fire.core.server.modules.socket.interfaces;

import io.fire.core.common.interfaces.Packet;

public interface SocketEvents {

    void onPacket(Packet packet);
    void onClose();
    void onOpen();

}

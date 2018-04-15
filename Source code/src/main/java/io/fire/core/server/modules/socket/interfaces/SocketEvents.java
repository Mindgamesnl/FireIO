package io.fire.core.server.modules.socket.interfaces;

import io.fire.core.common.interfaces.Packet;

public interface SocketEvents {

    public void onPacket(Packet packet);
    public void onClose();
    public void onOpen();

}

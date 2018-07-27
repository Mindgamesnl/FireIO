package io.fire.core.common.interfaces;

public interface SocketEvents {

    void onPacket(Packet packet);
    void onClose();
    void onOpen();

}

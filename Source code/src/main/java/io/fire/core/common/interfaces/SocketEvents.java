package io.fire.core.common.interfaces;

import io.fire.core.common.io.IoManager;

public interface SocketEvents {

    void onPacket(Packet packet);
    void onClose();
    void onOpen();
    IoManager getIoManager();

}

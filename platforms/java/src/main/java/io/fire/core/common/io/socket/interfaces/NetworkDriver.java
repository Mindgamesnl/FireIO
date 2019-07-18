package io.fire.core.common.io.socket.interfaces;

public interface NetworkDriver {

    void onError();
    void onOpen();
    void onClose();
    void onData(byte[] data);

}

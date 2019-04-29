package io.fire.core.server.modules.socket.interfaces;

public interface NetworkDriver {

    void onError();
    void onOpen();
    void onClose();
    void onData(byte[] data, Integer length);

}

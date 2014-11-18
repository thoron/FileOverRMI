package com.thoron.rmi.app.statics;

import java.io.Serializable;

public interface RemoteChunk extends Serializable {
    public byte[] getData();

    public int getNumberOfRead();
}

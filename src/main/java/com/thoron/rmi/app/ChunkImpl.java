package com.thoron.rmi.app;

import com.thoron.rmi.app.statics.RemoteChunk;

public class ChunkImpl implements RemoteChunk {

    private static final long serialVersionUID = 7104245215243860431L;

    private byte[] data;

    public ChunkImpl(byte[] data) {
        this(data, data.length);
    }

    public ChunkImpl(byte[] data, int noRead) {
        byte[] a = new byte[noRead];
        System.arraycopy(data, 0, a, 0, noRead);
        this.data = a;
    }

    public byte[] getData() {
        return data;
    }

    public int getNumberOfRead() {
        return this.data.length;
    }
}

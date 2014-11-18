package com.thoron.rmi.app;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;

import com.thoron.rmi.app.statics.RemoteChunk;
import com.thoron.rmi.app.statics.RemoteFileTransfer;

public class TransferIntermediate implements RemoteFileTransfer {

    transient private File file;

    public TransferIntermediate() {
    }

    public TransferIntermediate(File f) {
        setFileForTransfer(f);
    }

    public void setFileForTransfer(File file) {
        this.file = file;
    }

    public RemoteChunk getChunk(int offset, byte[] dest) throws RemoteException {
        System.gc();
        return getChunkFromAccessFile(offset, dest);
    }

    private RemoteChunk getChunkFromAccessFile(int offset, byte[] dest) {
        int noRead = -1;
        // Open the file for both reading and writing
        try (RandomAccessFile rand = new RandomAccessFile(file, READ_MODE);){
            rand.seek(offset); // Seek to start point of file
            noRead = rand.read(dest, 0, dest.length);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return noRead == -1 ? null : new ChunkImpl(dest, noRead);
    }

    public RMIFile getEntireFile() {
        try {
            return new RMIFile(file, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean ping() throws RemoteException {
        return true;
    }
}

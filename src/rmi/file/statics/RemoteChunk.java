package rmi.file.statics;

import java.io.Serializable;

public interface RemoteChunk extends Serializable {
	public byte[] getData();
	public int getNumberOfRead();
}

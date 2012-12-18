package rmi.file.statics;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

import rmi.file.RMIFile;

public interface RemoteFileTransfer extends Remote{
	
	/**
	 * For testing only
	 */
	public static final String SERVIC_STUB = "REMOTE_TESTING_FILE_STUB11";
	
	public static final String READ_MODE = "r";
	public static final String READWRITE_MODE = "rw";
	
	public void setFileForTransfer(File file) throws RemoteException;
	public RemoteChunk getChunk(int offset,  byte[] dest) throws RemoteException;
	public RMIFile getEntireFile() throws RemoteException;
	public boolean ping() throws RemoteException;
}

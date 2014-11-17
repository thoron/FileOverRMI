package com.thoron.rmi.app;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;

import com.thoron.rmi.app.statics.RemoteChunk;
import com.thoron.rmi.app.statics.RemoteFileTransfer;

public class DataWrapper {
	public final static String FILE_NAME = "remote-file_";
	private int chunkSize;
	private RemoteFileTransfer transferConnection;
	private String fileExtension;

	public DataWrapper(File f, int chunkSize, String fileExtension) {
		// Connect
		this.chunkSize = chunkSize;
		this.transferConnection = new TransferIntermediate();
		if (fileExtension == null) {
			int index = f.getName().lastIndexOf('.');
			if (index != -1) {
				this.fileExtension = f.getName().substring(index);
			} else {
				this.fileExtension = "tmp";
			}
		} else {
			this.fileExtension = fileExtension;
		}
		this.fileExtension = (this.fileExtension.startsWith(".") ? "" : ".")
				+ this.fileExtension;
		try {
			transferConnection.setFileForTransfer(f);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DataWrapper(RemoteFileTransfer t, int chunkSize, String fileExtension) {
		// Connect
		this.chunkSize = chunkSize;
		this.transferConnection = t;
		this.fileExtension = fileExtension == null ? "tmp" : fileExtension;
		this.fileExtension = (this.fileExtension.startsWith(".") ? "" : ".")
				+ this.fileExtension;
	}

	public File getFile(transferType t, File dest) {
		File f = null;
		switch (t) {
		case CHUNK:
			f = chunkTransfer();
			f.renameTo(dest == null ? f : dest);
			break;
		case COMPLETE:
			try {
				f = transferConnection.getEntireFile().getFile();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			f.renameTo(dest == null ? f : dest);
			break;
		default:
			break;
		}
		return f;
	}

	private File chunkTransfer() {
		File newFile = null;
		try {
			newFile = File.createTempFile(FILE_NAME, fileExtension);
		} catch (IOException e1) {
			return null;
		}
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(newFile, "rw");
			byte[] b = new byte[chunkSize];
			RemoteChunk chunk;
			int o = 0;
			// TODO: might be better to allocate array on service side
			while ((chunk = transferConnection.getChunk(o, b)) != null) {
				file.seek(o);
				file.write(chunk.getData(), 0, chunk.getNumberOfRead());
				o += chunk.getNumberOfRead();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
				}
			}
		}
		return newFile;
	}

	public enum transferType {
		CHUNK, COMPLETE;
	}
}

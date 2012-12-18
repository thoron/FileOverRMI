package rmi.file;

import java.io.File;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import rmi.file.statics.RemoteFileTransfer;

public class Server {
	public static void main(String[] args) throws UnknownHostException,
			RemoteException, AlreadyBoundException {
		System.out.println("Running file over RMI test server @"
				+ java.net.InetAddress.getLocalHost());

		System.out.println("Creating files..");
		File f = new File(Utils.TEST_FILE_SMALL);
		if (!f.exists()) {
			Utils.writeSmallFile(f);
		}
		File g = new File(Utils.TEST_FILE_MEDIUM);
		if (!g.exists()) {
			Utils.writeMediumFile(g);
		}
		File l = new File(Utils.TEST_FILE_LARGE);
		if (!l.exists()) {
			Utils.writeLargeFile(l);
		}
		TransferIntermediate t = new TransferIntermediate(f);
		System.setSecurityManager(null);
		RemoteFileTransfer stub = (RemoteFileTransfer) UnicastRemoteObject
				.exportObject((RemoteFileTransfer) t, 8001);
		Registry registry = null;
		try {
			registry = LocateRegistry.createRegistry(8001);
		} catch (RemoteException e) {
			registry = LocateRegistry.getRegistry(8001);
		}
		registry.bind(RemoteFileTransfer.SERVIC_STUB, stub);
		System.out.println("Ready!");
	}
}

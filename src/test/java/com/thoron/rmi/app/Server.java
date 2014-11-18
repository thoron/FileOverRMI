package com.thoron.rmi.app;

import java.io.File;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.thoron.rmi.app.statics.RemoteFileTransfer;

public class Server implements Runnable {
    private Thread serverThread;
    private File fs;
    private File fm;
    private File fl;

    public static void main(String[] args) throws UnknownHostException, RemoteException, AlreadyBoundException {
        Server server = new Server();
        server.startServer(false);
    }

    public Server() {
        this(true);
    }

    public Server(boolean createFiles) {
        if (createFiles) {
            System.out.println("Creating files..");
            fs = new File(Utils.TEST_FILE_SMALL);
            if (!fs.exists()) {
                Utils.writeSmallFile(fs);
            }
            fm = new File(Utils.TEST_FILE_MEDIUM);
            if (!fm.exists()) {
                Utils.writeMediumFile(fm);
            }
            fl = new File(Utils.TEST_FILE_LARGE);
            if (!fl.exists()) {
                Utils.writeLargeFile(fl);
            }
        }
        System.setSecurityManager(null);
        serverThread = null;

    }

    public Thread startServer(boolean runInbackground) {
        if (runInbackground) {
            serverThread = new Thread(this);
            serverThread.start();
            return serverThread;
        } else {
            run();
        }
        return null;
    }

    public void run() {
        try {
            System.out.println("Running file over RMI test server @" + java.net.InetAddress.getLocalHost());
            TransferIntermediate t = new TransferIntermediate(fs);
            Registry registry = null;
            RemoteFileTransfer stub = (RemoteFileTransfer) UnicastRemoteObject.exportObject((RemoteFileTransfer) t,
                    8001);
            try {
                registry = LocateRegistry.createRegistry(8001);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(8001);
            }
            registry.bind(RemoteFileTransfer.SERVICE_STUB, stub);
            System.out.println("Ready!");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

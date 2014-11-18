package com.thoron.rmi.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.thoron.rmi.app.statics.RemoteFileTransfer;

public class DataWrapperTest {
    private static final int REGISTRY_PORT = 8001;

    private static final String REGISTRY_HOST_NAME = "salt.cs.umu.se";

    @Rule
    public TestName testName = new TestName();

    private long startTime;

    @BeforeClass
    public static void oneTimeSetUp() {
        File f = new File(Utils.TEST_FILE_SMALL);
        if (!f.exists()) {
            Utils.writeSmallFile(f);
        }
        File l = new File(Utils.TEST_FILE_LARGE);
        if (!l.exists()) {
            Utils.writeLargeFile(l);
        }
        File g = new File(Utils.TEST_FILE_MEDIUM);
        if (!g.exists()) {
            Utils.writeMediumFile(g);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        new File(Utils.TEST_FILE_SMALL).delete();
        new File(Utils.TEST_FILE_MEDIUM).delete();
        new File(Utils.TEST_FILE_LARGE).delete();
    }

    @Before
    public void setUp() {
        System.out.println("Running test: " + testName.getMethodName());
        startTime = System.currentTimeMillis();
    }

    @After
    public void tearDown() {
        System.out.println("Test: '" + testName.getMethodName() + "' time: " + getRunTimeData(startTime));
    }

    @Test
    public void testSingleTransfer() {
        File f = new File(Utils.TEST_FILE_SMALL);
        DataWrapper wrapper = new DataWrapper(f, 10);
        File wr = wrapper.getFile(DataWrapper.transferType.CHUNK);
        assertEquals(f.length(), wr.length());
        assertContentSame(f, wr);
        assertTrue(wr.delete());
    }

    // @Test
    public void majorTestChunk() throws AccessException, RemoteException, AlreadyBoundException, NotBoundException {
        File f = new File(Utils.TEST_FILE_SMALL);
        TransferIntermediate t = new TransferIntermediate(f);
        System.setSecurityManager(null);
        RemoteFileTransfer stub = (RemoteFileTransfer) UnicastRemoteObject.exportObject((RemoteFileTransfer) t,
                REGISTRY_PORT);
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(REGISTRY_PORT);
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(REGISTRY_PORT);
        }
        registry.bind(RemoteFileTransfer.SERVICE_STUB, stub);

        Registry registry2 = LocateRegistry.getRegistry(null, REGISTRY_PORT);
        RemoteFileTransfer rt = (RemoteFileTransfer) registry2.lookup(RemoteFileTransfer.SERVICE_STUB);
        DataWrapper wrapper = new DataWrapper(rt, 10);
        File tmp = wrapper.getFile(DataWrapper.transferType.CHUNK);
        assertEquals(f.length(), tmp.length());
        assertContentSame(f, tmp);
        assertTrue(tmp.delete());
        f = new File(Utils.TEST_FILE_LARGE);
        t.setFileForTransfer(f);

        wrapper = new DataWrapper(rt, 1000);
        tmp = wrapper.getFile(DataWrapper.transferType.CHUNK);
        assertEquals(f.length(), tmp.length());
        assertContentSame(f, tmp);
        assertTrue(tmp.delete());
        registry.unbind(RemoteFileTransfer.SERVICE_STUB);
    }

    // @Test
    public void majorTestComplete() throws AccessException, RemoteException, AlreadyBoundException, NotBoundException {
        File f = new File(Utils.TEST_FILE_SMALL);
        TransferIntermediate t = new TransferIntermediate(f);
        System.setSecurityManager(null);
        RemoteFileTransfer stub = (RemoteFileTransfer) UnicastRemoteObject.exportObject((RemoteFileTransfer) t,
                REGISTRY_PORT);
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(REGISTRY_PORT);
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(REGISTRY_PORT);
        }
        registry.bind(RemoteFileTransfer.SERVICE_STUB, stub);

        Registry registry2 = LocateRegistry.getRegistry(null, REGISTRY_PORT);
        RemoteFileTransfer rt = (RemoteFileTransfer) registry2.lookup(RemoteFileTransfer.SERVICE_STUB);
        DataWrapper wrapper = new DataWrapper(rt, 10);
        File tmp = wrapper.getFile(DataWrapper.transferType.COMPLETE);
        assertEquals(f.length(), tmp.length());
        assertContentSame(f, tmp);
        assertTrue(tmp.delete());
        f = new File(Utils.TEST_FILE_LARGE);
        t.setFileForTransfer(f);

        wrapper = new DataWrapper(rt, 1000);
        tmp = wrapper.getFile(DataWrapper.transferType.COMPLETE);
        assertEquals(f.length(), tmp.length());
        assertContentSame(f, tmp);
        assertTrue(tmp.delete());

        registry.unbind(RemoteFileTransfer.SERVICE_STUB);
    }

    @Test
    public void testContentChunk() {
        File f = new File(Utils.TEST_FILE_SMALL);
        DataWrapper wrapper = new DataWrapper(f, 17);
        File wr = wrapper.getFile(DataWrapper.transferType.CHUNK);
        assertEquals(f.length(), wr.length());
        assertContentSame(f, wr);
        assertTrue(wr.delete());
    }

    @Test
    public void testRemoteContentChunk() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(REGISTRY_HOST_NAME, REGISTRY_PORT);
        RemoteFileTransfer rt = (RemoteFileTransfer) registry.lookup(RemoteFileTransfer.SERVICE_STUB);
        rt.ping();
        File f = new File(Utils.TEST_FILE_SMALL);
        rt.setFileForTransfer(f);
        DataWrapper wrapper = new DataWrapper(rt, 17);
        File wr = wrapper.getFile(DataWrapper.transferType.CHUNK);
        assertEquals(f.length(), wr.length());
        assertContentSame(f, wr);
        assertTrue(wr.delete());
    }

    @Test
    public void testRemoteLargeContentChunk() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(REGISTRY_HOST_NAME, REGISTRY_PORT);
        RemoteFileTransfer rt = (RemoteFileTransfer) registry.lookup(RemoteFileTransfer.SERVICE_STUB);
        rt.ping();
        File f = new File(Utils.TEST_FILE_LARGE);
        rt.setFileForTransfer(f);
        DataWrapper wrapper = new DataWrapper(rt, 100);
        File wr = wrapper.getFile(DataWrapper.transferType.CHUNK);
        assertEquals(f.length(), wr.length());
        assertContentSame(f, wr);
        assertTrue(wr.delete());
    }

    @Test
    public void testRemoteContentComplete() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(REGISTRY_HOST_NAME, REGISTRY_PORT);
        RemoteFileTransfer rt = (RemoteFileTransfer) registry.lookup(RemoteFileTransfer.SERVICE_STUB);
        rt.ping();
        File f = new File(Utils.TEST_FILE_SMALL);
        rt.setFileForTransfer(f);
        DataWrapper wrapper = new DataWrapper(rt, 17);
        File wr = wrapper.getFile(DataWrapper.transferType.COMPLETE);
        assertEquals(f.length(), wr.length());
        assertContentSame(f, wr);
        assertTrue(wr.delete());
    }

    @Test
    public void testRemoteLargeContentComplete() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(REGISTRY_HOST_NAME, REGISTRY_PORT);
        RemoteFileTransfer rt = (RemoteFileTransfer) registry.lookup(RemoteFileTransfer.SERVICE_STUB);
        rt.ping();
        File f = new File(Utils.TEST_FILE_LARGE);
        rt.setFileForTransfer(f);
        DataWrapper wrapper = new DataWrapper(rt, 100);
        File wr = wrapper.getFile(DataWrapper.transferType.CHUNK);
        assertEquals(f.length(), wr.length());
        assertContentSame(f, wr);
        assertTrue(wr.delete());
    }

    @Test
    public void testLargeFileChunk() {
        File f = new File(Utils.TEST_FILE_LARGE);
        DataWrapper wrapper = new DataWrapper(f, 2000);
        File wr = wrapper.getFile(DataWrapper.transferType.CHUNK);
        assertEquals(f.length(), wr.length());
        assertContentSame(f, wr);
        assertTrue(wr.delete());
    }

    @Test
    public void testLargeFileComplete() {
        File f = new File(Utils.TEST_FILE_LARGE);
        DataWrapper wrapper = new DataWrapper(f, 2000);
        File wr = wrapper.getFile(DataWrapper.transferType.COMPLETE);
        assertEquals(f.length(), wr.length());
        assertContentSame(f, wr);
        assertTrue(wr.delete());

    }

    public static void assertContentSame(File exp, File act) {
        try {
            assertTrue(FileUtils.contentEquals(exp, act));
        } catch (IOException e) {
            fail();
        }
    }

    private static String getRunTimeData(long startTime) {
        // Some runtime data
        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        int elapsedTimeSec = (int) ((elapsedTimeMillis / 1000F) % 60);
        int elapsedTimeMin = (int) ((elapsedTimeMillis / (60 * 1000F)));

        String data = elapsedTimeMin + " Minutes " + elapsedTimeSec + " Seconds "
                + (elapsedTimeMillis % 1000) + " Milliseconds";
        return data;
    }
}

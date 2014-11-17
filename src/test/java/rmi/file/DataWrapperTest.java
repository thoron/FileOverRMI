package rmi.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import rmi.file.statics.RemoteFileTransfer;

public class DataWrapperTest {
	@Rule
	public TestName testName = new TestName();

	private long startTime;
	private static Vector<String> times = new Vector<String>();

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
		for (String s : times) {
			System.out.println(s);
		}
	}

	@Before
	public void setUp() {
		startTime = System.currentTimeMillis();
	}

	@After
	public void tearDown() {
		times.add("Test: '" + testName.getMethodName() + "' time: "
				+ getRunTimeData(startTime));
	}

	@Test
	public void testSingleTransfer() {
		File f = new File(Utils.TEST_FILE_SMALL);
		DataWrapper wrapper = new DataWrapper(f, 10, null);
		File wr = wrapper.getFile(DataWrapper.transferType.CHUNK, null);
		assertEquals(f.length(), wr.length());
		assertContentSame(f, wr);
		assertTrue(wr.delete());
	}

	@Test
	public void majorTestChunk() throws AccessException, RemoteException,
			AlreadyBoundException, NotBoundException {
		File f = new File(Utils.TEST_FILE_SMALL);
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

		Registry registry2 = LocateRegistry.getRegistry(null, 8001);
		RemoteFileTransfer rt = (RemoteFileTransfer) registry2
				.lookup(RemoteFileTransfer.SERVIC_STUB);
		DataWrapper wrapper = new DataWrapper(rt, 10, null);
		File tmp = wrapper.getFile(DataWrapper.transferType.CHUNK, null);
		assertEquals(f.length(), tmp.length());
		assertContentSame(f, tmp);
		assertTrue(tmp.delete());
		f = new File(Utils.TEST_FILE_LARGE);
		t.setFileForTransfer(f);

		wrapper = new DataWrapper(rt, 1000, null);
		tmp = wrapper.getFile(DataWrapper.transferType.CHUNK, null);
		assertEquals(f.length(), tmp.length());
		assertContentSame(f, tmp);
		assertTrue(tmp.delete());
		registry.unbind(RemoteFileTransfer.SERVIC_STUB);
	}

	@Test
	public void majorTestComplete() throws AccessException, RemoteException,
			AlreadyBoundException, NotBoundException {
		File f = new File(Utils.TEST_FILE_SMALL);
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

		Registry registry2 = LocateRegistry.getRegistry(null, 8001);
		RemoteFileTransfer rt = (RemoteFileTransfer) registry2
				.lookup(RemoteFileTransfer.SERVIC_STUB);
		DataWrapper wrapper = new DataWrapper(rt, 10, null);
		File tmp = wrapper.getFile(DataWrapper.transferType.COMPLETE, null);
		assertEquals(f.length(), tmp.length());
		assertContentSame(f, tmp);
		assertTrue(tmp.delete());
		f = new File(Utils.TEST_FILE_LARGE);
		t.setFileForTransfer(f);

		wrapper = new DataWrapper(rt, 1000, null);
		tmp = wrapper.getFile(DataWrapper.transferType.COMPLETE, null);
		assertEquals(f.length(), tmp.length());
		assertContentSame(f, tmp);
		assertTrue(tmp.delete());

		registry.unbind(RemoteFileTransfer.SERVIC_STUB);
	}

	@Test
	public void testContentChunk() {
		File f = new File(Utils.TEST_FILE_SMALL);
		DataWrapper wrapper = new DataWrapper(f, 17, null);
		File wr = wrapper.getFile(DataWrapper.transferType.CHUNK, null);
		assertEquals(f.length(), wr.length());
		assertContentSame(f, wr);
		assertTrue(wr.delete());
	}

	@Test
	public void testRemoteContentChunk() throws RemoteException,
			NotBoundException {
		String address = "salt.cs.umu.se";
		Registry registry = LocateRegistry.getRegistry(address, 8001);
		RemoteFileTransfer rt = (RemoteFileTransfer) registry
				.lookup(RemoteFileTransfer.SERVIC_STUB);
		rt.ping();
		File f = new File(Utils.TEST_FILE_SMALL);
		rt.setFileForTransfer(f);
		DataWrapper wrapper = new DataWrapper(rt, 17, null);
		File wr = wrapper.getFile(DataWrapper.transferType.CHUNK, null);
		assertEquals(f.length(), wr.length());
		assertContentSame(f, wr);
		assertTrue(wr.delete());
	}

	@Test
	public void testRemoteLargeContentChunk() throws RemoteException,
			NotBoundException {
		String address = "salt.cs.umu.se";
		Registry registry = LocateRegistry.getRegistry(address, 8001);
		RemoteFileTransfer rt = (RemoteFileTransfer) registry
				.lookup(RemoteFileTransfer.SERVIC_STUB);
		rt.ping();
		File f = new File(Utils.TEST_FILE_LARGE);
		rt.setFileForTransfer(f);
		DataWrapper wrapper = new DataWrapper(rt, 100, null);
		File wr = wrapper.getFile(DataWrapper.transferType.CHUNK, null);
		assertEquals(f.length(), wr.length());
		assertContentSame(f, wr);
		assertTrue(wr.delete());
	}

	@Test
	public void testRemoteContentComplete() throws RemoteException,
			NotBoundException {
		String address = "salt.cs.umu.se";
		Registry registry = LocateRegistry.getRegistry(address, 8001);
		RemoteFileTransfer rt = (RemoteFileTransfer) registry
				.lookup(RemoteFileTransfer.SERVIC_STUB);
		rt.ping();
		File f = new File(Utils.TEST_FILE_SMALL);
		rt.setFileForTransfer(f);
		DataWrapper wrapper = new DataWrapper(rt, 17, null);
		File wr = wrapper.getFile(DataWrapper.transferType.COMPLETE, null);
		assertEquals(f.length(), wr.length());
		assertContentSame(f, wr);
		assertTrue(wr.delete());
	}

	@Test
	public void testRemoteLargeContentComplete() throws RemoteException,
			NotBoundException {
		String address = "salt.cs.umu.se";
		Registry registry = LocateRegistry.getRegistry(address, 8001);
		RemoteFileTransfer rt = (RemoteFileTransfer) registry
				.lookup(RemoteFileTransfer.SERVIC_STUB);
		rt.ping();
		File f = new File(Utils.TEST_FILE_LARGE);
		rt.setFileForTransfer(f);
		DataWrapper wrapper = new DataWrapper(rt, 100, null);
		File wr = wrapper.getFile(DataWrapper.transferType.CHUNK, null);
		assertEquals(f.length(), wr.length());
		assertContentSame(f, wr);
		assertTrue(wr.delete());
	}

	@Test
	public void testLargeFileChunk() {
		File f = new File(Utils.TEST_FILE_LARGE);
		DataWrapper wrapper = new DataWrapper(f, 2000, null);
		File wr = wrapper.getFile(DataWrapper.transferType.CHUNK, null);
		assertEquals(f.length(), wr.length());
		assertContentSame(f, wr);
		assertTrue(wr.delete());

	}

	@Test
	public void testLargeFileComplete() {
		File f = new File(Utils.TEST_FILE_LARGE);
		DataWrapper wrapper = new DataWrapper(f, 2000, null);
		File wr = wrapper.getFile(DataWrapper.transferType.COMPLETE, null);
		assertEquals(f.length(), wr.length());
		assertContentSame(f, wr);
		assertTrue(wr.delete());

	}

	public void assertContentSame(File exp, File act) {
		String line;
		BufferedReader in1 = null, in2 = null;
		FileInputStream fin1 = null, fin2 = null;
		try {
			fin1 = new FileInputStream(exp);
			fin2 = new FileInputStream(act);
			in1 = new BufferedReader(new InputStreamReader(fin1));
			in2 = new BufferedReader(new InputStreamReader(fin2));
			while ((line = in1.readLine()) != null) {
				String line2 = in2.readLine();
				assertEquals(line, line2);
			}
		} catch (Exception e) {
			fail("Streams do not match");
		} finally {
			if (in1 != null) {
				try {
					in1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in2 != null) {
				try {
					in2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fin1 != null) {
				try {
					fin1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fin2 != null) {
				try {
					fin2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String getRunTimeData(long startTime) {
		// Some runtime data
		long elapsedTimeMillis = System.currentTimeMillis() - startTime;
		int elapsedTimeSec = (int) ((elapsedTimeMillis / 1000F) % 60);
		int elapsedTimeMin = (int) ((elapsedTimeMillis / (60 * 1000F)) % 60);
		int elapsedTimeHour = (int) ((elapsedTimeMillis / (60 * 60 * 1000F)) % 24);
		int elapsedTimeDays = (int) (elapsedTimeMillis / (24 * 60 * 60 * 1000F));
		String hours = elapsedTimeHour > 0 ? elapsedTimeHour + " Hours " : "";
		String days = elapsedTimeDays > 0 ? elapsedTimeDays + " Days " : "";

		String data = days + hours + elapsedTimeMin + " Minutes "
				+ elapsedTimeSec + " Seconds " + (elapsedTimeMillis % 1000)
				+ " Milliseconds";
		return data;
	}

	@SuppressWarnings("unused")
	private void printContent(File f) {
		String thisLine;
		try {
			FileInputStream fin = new FileInputStream(f);
			// JDK1.1+
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fin));
			while ((thisLine = br.readLine()) != null) {
				System.out.println(thisLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

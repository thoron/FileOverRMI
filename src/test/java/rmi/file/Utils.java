package rmi.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {
	public static final String TEST_FILE_SMALL = "/tmp/datawrapperTestFileSmall";
	public static final String TEST_FILE_MEDIUM = "/tmp/datawrapperTestFileMedium";
	public static final String TEST_FILE_LARGE = "/tmp/datawrapperTestFileLagre";
	public static final int NO_TEST_ROWS = 32;
	public static final int NO_TEST_ROWS_LARGE = 1000000;
	public static final int NO_TEST_COL_LARGE = 100;

	public static void writeSmallFile(File f) {
		BufferedWriter out = null;
		try {
			// Create file
			FileWriter fstream = new FileWriter(f);
			out = new BufferedWriter(fstream);
			for (int i = 1; i < NO_TEST_ROWS; i++) {
				out.write('A');
				out.newLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeMediumFile(File f) {
		BufferedWriter out = null;
		try {
			// Create file
			FileWriter fstream = new FileWriter(f);
			out = new BufferedWriter(fstream);
			int col = 0;
			for (int i = 0; i < (NO_TEST_ROWS_LARGE / 2); i++) {
				out.write('A');
				if (col++ == (NO_TEST_COL_LARGE / 2)) {
					out.newLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeLargeFile(File f) {
		BufferedWriter out = null;
		try {
			// Create file
			FileWriter fstream = new FileWriter(f);
			out = new BufferedWriter(fstream);
			int col = 0;
			for (int i = 0; i < NO_TEST_ROWS_LARGE; i++) {
				out.write('A');
				if (col++ == NO_TEST_COL_LARGE) {
					out.newLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

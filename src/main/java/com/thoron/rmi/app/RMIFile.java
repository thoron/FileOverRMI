package com.thoron.rmi.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;

/**
 * File wrapper for sending data via RMI. <br />
 * <br />
 * <b><tt>CAN NOT BE CHANGED:</tt></b><br />
 * Without re-deploying
 * 
 * @author bob
 * 
 */
public class RMIFile implements Serializable {
    private static final long serialVersionUID = 7431389691750459986L;
    private final String fileExtension;
    private volatile byte[] data;
    /**
     * All created file will start with this naming.
     */
    public final static String FILE_NAME = "remote-file_";

    /**
     * Stores file byte data and extension(if any), enabling recreation of file
     * content on remote host.
     * 
     * @param file
     *            the file to be sent.
     * @throws IOException
     *             if byte read from file fails.
     */
    public RMIFile(File file, String fileExtension) throws IOException {
        if (fileExtension == null) {
            String ext = FilenameUtils.getExtension(file.getName());
            this.fileExtension = ext.isEmpty() || ext.startsWith(".") ? ext : "." + ext;
        } else {
            this.fileExtension = fileExtension;
        }
        readByteData(file);
    }

    /**
     * Creates file from byte data and returns {@link File} reference to newly
     * created file.
     * 
     * @return the file created or null upon failure
     */
    public final File getFile() {
        File newFile = null;
        try {
            newFile = File.createTempFile(FILE_NAME, fileExtension);
        } catch (IOException e1) {
            return null;
        } 
        try (FileOutputStream out = new FileOutputStream(newFile)) {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            // Free memory
            markMemoryAsGCR();
        }
        return newFile;
    }

    /**
     * Marks memory as GC available and runs {@link System#gc()}. Method called
     * by default when inflating byte data as File.
     */
    public void markMemoryAsGCR() {
        if (data != null) {
            data = null;
            System.gc();
        }
    }

    private void readByteData(File file) throws IOException {
        data = new byte[(int) (file.length())];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
        }
    }
}

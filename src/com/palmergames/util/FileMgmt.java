// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Iterator;
import java.util.List;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public class FileMgmt
{
    public static boolean checkOrCreateFolder(final String folderPath) {
        final File file = new File(folderPath);
        return file.exists() || file.mkdirs() || file.isDirectory();
    }
    
    public static boolean checkOrCreateFolders(final String... folders) {
        for (final String folder : folders) {
            if (!checkOrCreateFolder(folder)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean checkOrCreateFile(final String filePath) {
        final File file = new File(filePath);
        if (!checkOrCreateFolder(file.getParentFile().getPath())) {
            return false;
        }
        try {
            return file.exists() || file.createNewFile();
        }
        catch (IOException e) {
            return false;
        }
    }
    
    public static boolean checkOrCreateFiles(final String... files) {
        for (final String file : files) {
            if (!checkOrCreateFile(file)) {
                return false;
            }
        }
        return true;
    }
    
    public static void copyDirectory(final File sourceLocation, final File targetLocation) throws IOException {
        synchronized (sourceLocation) {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdir();
                }
                final String[] list;
                final String[] children = list = sourceLocation.list();
                for (final String aChildren : list) {
                    copyDirectory(new File(sourceLocation, aChildren), new File(targetLocation, aChildren));
                }
            }
            else {
                final OutputStream out = new FileOutputStream(targetLocation);
                try {
                    final InputStream in = new FileInputStream(sourceLocation);
                    final byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
                catch (IOException ex) {
                    System.out.println("Error: Could not access: " + sourceLocation);
                }
                out.close();
            }
        }
    }
    
    public static File unpackResourceFile(final String filePath, final String resource, final String defaultRes) {
        final File file = new File(filePath);
        if (file.exists()) {
            return file;
        }
        checkOrCreateFile(filePath);
        try {
            final String resString = convertStreamToString("/" + resource);
            stringToFile(resString, filePath);
        }
        catch (IOException e2) {
            try {
                final String resString = convertStreamToString("/" + defaultRes);
                stringToFile(resString, filePath);
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return file;
    }
    
    public static String convertStreamToString(final String name) throws IOException {
        if (name != null) {
            final Writer writer = new StringWriter();
            final InputStream is = FileMgmt.class.getResourceAsStream(name);
            final char[] buffer = new char[1024];
            try {
                final Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            }
            catch (IOException e) {
                System.out.println("Exception ");
                try {
                    is.close();
                }
                catch (NullPointerException e2) {
                    throw new IOException();
                }
            }
            finally {
                try {
                    is.close();
                }
                catch (NullPointerException e3) {
                    throw new IOException();
                }
            }
            return writer.toString();
        }
        return "";
    }
    
    public static String convertFileToString(final File file) {
        if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
            final Writer writer = new StringWriter();
            final char[] buffer = new char[1024];
            try {
                final InputStream is = new FileInputStream(file);
                try {
                    final Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    int n;
                    while ((n = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, n);
                    }
                    reader.close();
                    is.close();
                }
                catch (Throwable t) {
                    try {
                        is.close();
                    }
                    catch (Throwable exception) {
                        t.addSuppressed(exception);
                    }
                    throw t;
                }
            }
            catch (IOException e) {
                System.out.println("Exception ");
            }
            return writer.toString();
        }
        return "";
    }
    
    public static void stringToFile(final String source, final String FileName) {
        if (source != null) {
            stringToFile(source, new File(FileName));
        }
    }
    
    public static void stringToFile(final String source, final File file) {
        try {
            final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            out.write(source);
            out.close();
        }
        catch (IOException e) {
            System.out.println("Exception ");
        }
    }
    
    public static boolean listToFile(final List<String> source, final String targetLocation) {
        try {
            final File file = new File(targetLocation);
            final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            for (final String aSource : source) {
                out.write(aSource + System.getProperty("line.separator"));
            }
            out.close();
            return true;
        }
        catch (IOException e) {
            System.out.println("Exception ");
            return false;
        }
    }
    
    public static void moveFile(final File sourceFile, final String targetLocation) {
        synchronized (sourceFile) {
            if (sourceFile.isFile()) {
                final File f = new File(sourceFile.getParent() + File.separator + targetLocation + File.separator + sourceFile.getName());
                if (f.exists() && f.isFile()) {
                    f.delete();
                }
                sourceFile.renameTo(new File(sourceFile.getParent() + File.separator + targetLocation, sourceFile.getName()));
            }
        }
    }
    
    public static void zipDirectories(final File destination, final File... sourceFolders) throws IOException {
        synchronized (sourceFolders) {
            final ZipOutputStream output = new ZipOutputStream(new FileOutputStream(destination), StandardCharsets.UTF_8);
            for (final File sourceFolder : sourceFolders) {
                recursiveZipDirectory(sourceFolder, output);
            }
            output.close();
        }
    }
    
    public static void recursiveZipDirectory(final File sourceFolder, final ZipOutputStream zipStream) throws IOException {
        synchronized (sourceFolder) {
            final String[] dirList = sourceFolder.list();
            final byte[] readBuffer = new byte[2156];
            for (final String aDirList : dirList) {
                final File f = new File(sourceFolder, aDirList);
                if (f.isDirectory()) {
                    recursiveZipDirectory(f, zipStream);
                }
                else if (f.isFile() && f.canRead()) {
                    final FileInputStream input = new FileInputStream(f);
                    final ZipEntry anEntry = new ZipEntry(f.getPath());
                    zipStream.putNextEntry(anEntry);
                    int bytesIn;
                    while ((bytesIn = input.read(readBuffer)) != -1) {
                        zipStream.write(readBuffer, 0, bytesIn);
                    }
                    input.close();
                }
            }
        }
    }
    
    public static void deleteFile(final File file) {
        synchronized (file) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children != null) {
                    for (final File child : children) {
                        deleteFile(child);
                    }
                }
                children = file.listFiles();
                if ((children == null || children.length == 0) && !file.delete()) {
                    System.out.println("Error: Could not delete folder: " + file.getPath());
                }
            }
            else if (file.isFile() && !file.delete()) {
                System.out.println("Error: Could not delete file: " + file.getPath());
            }
        }
    }
    
    public static void deleteOldBackups(final File backupsDir, final long deleteAfter) {
        synchronized (backupsDir) {
            final TreeSet<Long> deleted = new TreeSet<Long>();
            if (backupsDir.isDirectory()) {
                final File[] children = backupsDir.listFiles();
                if (children != null) {
                    for (final File child : children) {
                        try {
                            String filename = child.getName();
                            if (child.isFile() && filename.contains(".")) {
                                filename = filename.split("\\.")[0];
                            }
                            final String[] tokens = filename.split(" ");
                            final String lastToken = tokens[tokens.length - 1];
                            final long timeMade = Long.parseLong(lastToken);
                            if (timeMade >= 0L) {
                                final long age = System.currentTimeMillis() - timeMade;
                                if (age >= deleteAfter) {
                                    deleteFile(child);
                                    deleted.add(age);
                                }
                            }
                        }
                        catch (Exception ex) {}
                    }
                }
            }
            if (deleted.size() > 0) {
                System.out.println(String.format("[Towny] Deleting %d Old Backups (%s).", deleted.size(), (deleted.size() > 1) ? String.format("%d-%d days old", TimeUnit.MILLISECONDS.toDays(deleted.first()), TimeUnit.MILLISECONDS.toDays(deleted.last())) : String.format("%d days old", TimeUnit.MILLISECONDS.toDays(deleted.first()))));
            }
        }
    }
    
    public static synchronized void deleteUnusedFiles(final File residentDir, final Set<String> fileNames) {
        synchronized (residentDir) {
            int count = 0;
            if (residentDir.isDirectory()) {
                final File[] children = residentDir.listFiles();
                if (children != null) {
                    for (final File child : children) {
                        try {
                            String filename = child.getName();
                            if (child.isFile()) {
                                if (filename.contains(".txt")) {
                                    filename = filename.split("\\.txt")[0];
                                }
                                if (!fileNames.contains(filename.toLowerCase())) {
                                    deleteFile(child);
                                    ++count;
                                }
                            }
                        }
                        catch (Exception ex) {}
                    }
                    if (count > 0) {
                        System.out.println(String.format("[Towny] Deleted %d old files.", count));
                    }
                }
            }
        }
    }
    
    @Deprecated
    public static String fileSeparator() {
        return System.getProperty("file.separator");
    }
}

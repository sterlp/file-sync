package org.sterl.filesync.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import org.sterl.filesync.sync.activity.DeleteFileVisitorBA;

public class FileUtil {
    public static boolean isSameFile(File source, File destination) throws IOException {
        return isSameFile(source.toPath(), destination.toPath());
    }
    /**
     * Checks the size and the last modification time.
     */
    public static boolean isSameFile(Path source, Path destination) throws IOException {
        if (Files.exists(source) && Files.exists(destination)) {
            final BasicFileAttributes sourceAttributes = Files.getFileAttributeView(source, BasicFileAttributeView.class).readAttributes();
            final BasicFileAttributes destinationAttributes = Files.getFileAttributeView(destination, BasicFileAttributeView.class).readAttributes();
            
            return sourceAttributes.size() == destinationAttributes.size() 
                    && sourceAttributes.lastModifiedTime().compareTo(destinationAttributes.lastModifiedTime()) == 0;
        } else {
            return false;
        }
    }

    /**
     * The real delete which just deletes stuff and returns the count of deleted resources.
     * 
     * @throws IOException if the delete fails
     * @return count of deleted resources (files and directories)
     */
    public static long delete(Path path) throws IOException {
        long result = 0;
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                DeleteFileVisitorBA deleteFileVisitorBA = new DeleteFileVisitorBA();
                Files.walkFileTree(path, deleteFileVisitorBA);
                result = deleteFileVisitorBA.getDeleteCount();
            } else {
                Files.delete(path);
                result = 1;
            }
        }
        return result;
    }
    /**
     * The real delete which just deletes stuff and returns the count of deleted resources.
     * 
     * @throws IOException if the delete fails
     * @return count of deleted resources (files and directories)
     */
    public static long delete(File file) throws IOException {
        long result = 0;
        if (file != null && file.exists()) {
            if (file.isFile()) {
                file.delete();
                result = 1;
            } else {
                result = delete(file.toPath());
            }
        }
        return result;
    }
    /**
     * Deletes the content of the given directory and recreates an empty one. 
     */
    public static void createEmpty(Path path) throws IOException {
        delete(path);
        Files.createDirectories(path);
    }
    public static void writeToFile(Path file, String string) throws FileNotFoundException, IOException {
        writeToFile(file.toFile(), string);
    }
    public static void writeToFile(File file, String string) throws FileNotFoundException, IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(string.getBytes());
        }
    }
}

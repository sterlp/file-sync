package org.sterl.filesync.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import org.sterl.filesync.sync.activity.DeleteFileVisitorBA;

public class FileUtil {
    
    public static void assertDirectory(Path directory) {
        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("Directory not found: " + directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
    }
    public static void assertWriteableDirectory(Path directory) {
        assertDirectory(directory);
        if(!Files.isWritable(directory)) {
            throw new IllegalArgumentException("Cannot write to: " + directory);
        }
    }

    public static boolean hasFileChanged(File source, File destination, int maxTimeDiff) throws IOException {
        if (source == null || destination == null) return false;
        return hasFileChanged(source.toPath(), destination.toPath(), maxTimeDiff);
    }

    public static boolean hasFileChanged(File source, File destination) throws IOException {
        if (source == null || destination == null) return false;
        return hasFileChanged(source.toPath(), destination.toPath());
    }
    
    public static boolean hasFileChanged(Path source, Path destination) throws IOException {
        return hasFileChanged(source, destination, 0);
    }

    /**
     * Checks the size and the last modification time.
     */
    public static boolean hasFileChanged(Path source, Path destination, long maxTimeDiff) throws IOException {
        if (source == null || destination == null) return false;
        if (Files.exists(source) && Files.exists(destination)) {
            final BasicFileAttributes sourceAttributes = Files
                    .getFileAttributeView(source, BasicFileAttributeView.class).readAttributes();
            final BasicFileAttributes destinationAttributes = Files
                    .getFileAttributeView(destination, BasicFileAttributeView.class).readAttributes();

            return hasFileChanged(sourceAttributes, destinationAttributes, maxTimeDiff);
        } else {
            return false;
        }
    }

    public static boolean hasFileChanged(final BasicFileAttributes sourceAttributes,
            final BasicFileAttributes destinationAttributes, long maxTimeDiff) {
        return sourceAttributes.size() == destinationAttributes.size()
                && Math.abs(sourceAttributes.lastModifiedTime().toMillis()
                        - destinationAttributes.lastModifiedTime().toMillis()) <= maxTimeDiff;
    }

    /**
     * The real delete which just deletes stuff and returns the count of deleted
     * resources.
     * 
     * @throws IOException if the delete fails
     * @return count of deleted resources (files and directories)
     */
    public static int delete(Path path) throws IOException {
        int result = 0;
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
     * The real delete which just deletes stuff and returns the count of deleted
     * resources.
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
    public static void clearDirectory(Path path) throws IOException {
        // https://bugs.openjdk.java.net/browse/JDK-8029608
        // sometimes I hate windows -- delete create works fine on any OS despite
        // Windows
        if (path != null && Files.exists(path)) {
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (!path.equals(dir)) {
                            Files.delete(dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }

    public static File appendToFile(Path file, String string) throws FileNotFoundException, IOException {
        return appendToFile(file.toFile(), string);
    }

    public static File appendToFile(File file, String string) throws FileNotFoundException, IOException {
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.append(string);
        }
        return file;
    }
    

    /**
     * Creates the given directory if not already present.
     */
    public static boolean createDirectoryIfNeeded(Path dir) throws IOException {
        if (dir != null && !Files.exists(dir)) {
            Files.createDirectories(dir);
            return true;
        }
        return false;
    }
}

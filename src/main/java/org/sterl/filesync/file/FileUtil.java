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
import java.util.function.IntPredicate;

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
    /**
     * @deprecated wrong
     */
    @Deprecated
    public static boolean hasFileChanged(File source, File destination, int maxTimeDiff) throws IOException {
        if (source == null || destination == null) return false;
        return hasFileChanged(source.toPath(), destination.toPath(), maxTimeDiff);
    }
    /**
     * @deprecated wrong
     */
    @Deprecated
    public static boolean hasFileChanged(File source, File destination) throws IOException {
        if (source == null || destination == null) return false;
        return hasFileChanged(source.toPath(), destination.toPath());
    }
    /**
     * @deprecated wrong
     */
    @Deprecated
    public static boolean hasFileChanged(Path source, Path destination) throws IOException {
        return hasFileChanged(source, destination, 0);
    }

    /**
     * @deprecated wrong
     */
    @Deprecated
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
     * @return 0 same, > 0 source newer, otherwise destination newer
     */
    public static long compareModifiedTime(BasicFileAttributes sourceAttributes, 
            BasicFileAttributes destinationAttributes) {
        return sourceAttributes.lastModifiedTime().toMillis()
                - destinationAttributes.lastModifiedTime().toMillis();
    }

    /**
     * @return 0 same, > 0 source newer, otherwise destination newer
     */
    public static long compareModifiedTime(BasicFileAttributes sourceAttributes, Path destinationFile) throws IOException {
        if (Files.exists(destinationFile)) {
            BasicFileAttributes destinationAttributes = Files
                    .getFileAttributeView(destinationFile, BasicFileAttributeView.class)
                    .readAttributes();
            return compareModifiedTime(sourceAttributes, destinationAttributes);
        }
        return sourceAttributes.lastModifiedTime().toMillis();
    }
    /**
     * @return 0 same, > 0 source newer, otherwise destination newer
     */
    public static long compareModifiedTime(Path source, Path destination) throws IOException {
        if (Files.exists(source)) {
            BasicFileAttributes sa = Files
                    .getFileAttributeView(source, BasicFileAttributeView.class)
                    .readAttributes();

            return compareModifiedTime(sa, destination);
        } else if (Files.exists(destination)) {
            return -1;
        } else {
            return 0; // neither exists
        }
    }
    /**
     * @return 0 same, > 0 source newer, otherwise destination newer
     */
    public static long compareModifiedTime(File source, File destination) throws IOException {
        return compareModifiedTime(source.toPath(), destination.toPath());
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
        } else {
            createDirectoryIfNeeded(path);
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

package org.sterl.filesync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.sterl.filesync.config.FileSyncConfig;
import org.sterl.filesync.file.FileUtil;

public class TestDataGenerator {

    public final Path sourceDir = new File("./src/test/resources/simpleSync/source").toPath();
    public final Path destinationDir = new File("./src/test/resources/simpleSync/destination").toPath();
    
    public final Path source_f1 = sourceDir.resolve("f1.txt");
    public final Path source_a_f1 = sourceDir.resolve("a/f1.txt");
    public final Path source_a_b_f1 = sourceDir.resolve("a/b/file.txt");
    
    public final Path destination_f1 = destinationDir.resolve("f1.txt");
    
    public FileSyncConfig toConfig() {
        return FileSyncConfig.of(sourceDir, destinationDir);
    }

    /**
     * <pre>
     * |- source
     * |  |- f1.txt
     * |  |- a
     * |  |  |- f1.txt
     * |  |  |- b
     * |  |  |  |- file.txt
     * |- destination
     *    |- f1.txt
     * </pre>
     */
    public void reset() throws IOException {
        clean();
        Files.createDirectories(sourceDir.resolve("a/b"));

        FileUtil.appendToFile(source_f1, UUID.randomUUID().toString());
        FileUtil.appendToFile(source_a_f1, UUID.randomUUID().toString());
        FileUtil.appendToFile(source_a_b_f1, UUID.randomUUID().toString());
    }
    
    public void clean() throws IOException {
        FileUtil.clearDirectory(destinationDir);
        FileUtil.clearDirectory(sourceDir);
    }
    
    public String readSourceFile(String path) throws IOException {
        return Files.readString(sourceDir.resolve(path));
    }
    public String readDestinationFile(String path) throws IOException {
        return Files.readString(destinationDir.resolve(path));
    }
    public File writeSourceFile(String path, String content) throws IOException {
        Path f = sourceDir.resolve(path);
        return writeFile(content, f);
    }
    public File writeDestinationFile(String path, String content) throws IOException {
        Path f = destinationDir.resolve(path);
        return writeFile(content, f);
    }

    private File writeFile(String content, Path f) throws IOException, FileNotFoundException {
        FileUtil.createDirectoryIfNeeded(f.getParent());
        final File result = f.toFile();
        try (FileWriter writer = new FileWriter(result, false)) {
            writer.write(content);
        }
        result.deleteOnExit();
        return result;
    }
}

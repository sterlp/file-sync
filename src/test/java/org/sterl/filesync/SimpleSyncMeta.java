package org.sterl.filesync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.sterl.filesync.file.FileUtil;

public class SimpleSyncMeta {

    public final Path sourceDir = new File("./src/test/resources/simpleSync/source").toPath();
    public final Path destinationDir = new File("./src/test/resources/simpleSync/destination").toPath();
    
    public final Path source_f1 = sourceDir.resolve("f1.txt");
    public final Path source_a_f1 = sourceDir.resolve("a/f1.txt");
    public final Path source_a_b_f1 = sourceDir.resolve("a/b/file.txt");
    
    public final Path destination_f1 = destinationDir.resolve("f1.txt");
    
    public void clean() throws IOException {
        FileUtil.createEmpty(destinationDir);
        FileUtil.createEmpty(sourceDir);
        Files.createDirectories(sourceDir.resolve("a/b"));
        
        FileUtil.writeToFile(source_f1, UUID.randomUUID().toString());
        FileUtil.writeToFile(source_a_f1, UUID.randomUUID().toString());
        FileUtil.writeToFile(source_a_b_f1, UUID.randomUUID().toString());
    }
}

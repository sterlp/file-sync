package org.sterl.filesync.copy.actvity;

import java.io.IOException;
import java.nio.file.Path;

public interface CopyStrategy {
    boolean created(Path path) throws IOException;
    boolean changed(Path path) throws IOException;
    boolean deleted(Path path) throws IOException;

    Path getSourceDir();
    Path getDestinationDir();
}

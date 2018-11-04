package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sterl.filesync.file.FileUtil;

/**
 * Removes any orphan files and directories found in the slave directory.
 */
class RemoveOrphanBA implements Callable<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrphanBA.class);
    /** The directory which should be clean from stuff which aren't found in the master */
    private final Path toVerfyDir;
    /** The directory which represents all files which should be in the verify directory */
    private final Path masterDir;
    /** List of paths which can be ignored during the check */
    private final List<Path> existsInMaster = new ArrayList<>();

    RemoveOrphanBA(Path toVerfyDir, Path masterDir, List<Path> existsInMaster) {
        super();
        this.toVerfyDir = toVerfyDir;
        this.masterDir = masterDir;
        Objects.requireNonNull(toVerfyDir, "To verify directory cannot be null.");
        Objects.requireNonNull(masterDir, "Master directory cannot be null.");
        if (existsInMaster != null && !existsInMaster.isEmpty()) {
            this.existsInMaster.addAll(existsInMaster);
        }
    }

    @Override
    public Long call() throws IOException {
        Iterator<Path> paths = Files.list(toVerfyDir).iterator();
        long result = 0;
        while (paths.hasNext()) {
            Path toCheck = paths.next();
            if (!existsInMaster.remove(toCheck)) { // only if we should check it ...
                result += checkAndRemove(toCheck);
            }
        }
        return result;
    }
    
    private long checkAndRemove(Path path) throws IOException {
        final Path masterPath = masterDir.resolve(toVerfyDir.relativize(path));
        long result = 0;
        if (!Files.exists(masterPath)) {
            // if not found in the master we have to delete it ...
            result = FileUtil.delete(path);
            LOGGER.debug("Orphan path {} found and deleted {} element(s).", path, result);
        }
        return result;
    }
}

package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sterl.filesync.file.FileUtil;

/**
 * Removes any orphan files and directories found in the slave directory.
 */
class RemoveOrphanBA extends AbstractDirectorySync {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrphanBA.class);
    /** The directory which represents all files which should be in the verify directory */
    private final Path masterDir;
    /** List of paths which can be ignored during the check */
    private final List<String> ignoreList = new ArrayList<>();

    RemoveOrphanBA(Path toVerfyDir, Path masterDir, List<String> ignoreList) {
        super(toVerfyDir, ignoreList);
        this.masterDir = masterDir;
        Objects.requireNonNull(toVerfyDir, "To verify directory cannot be null.");
        Objects.requireNonNull(masterDir, "Master directory cannot be null.");
        if (ignoreList != null && !ignoreList.isEmpty()) {
            this.ignoreList.addAll(ignoreList);
        }
    }

    @Override
    protected int handle(Path path) throws IOException {
        final Path masterPath = masterDir.resolve(sourceDir.relativize(path));
        int result = 0;
        if (!Files.exists(masterPath)) {
            // if not found in the master we have to delete it ...
            result = FileUtil.delete(path);
            LOGGER.debug("Orphan path {} found and deleted {} element(s).", path, result);
        }
        return result;
    }
}

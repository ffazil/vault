package com.tracebucket.vault;

import java.util.Optional;
import java.util.UUID;

public interface FileStorage {
	Optional<FilePointer> findFile(UUID uuid);
}

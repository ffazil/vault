package com.tracebucket.vault;

import org.apache.tika.mime.MimeTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public interface FileStorage {
	Optional<FilePointer> findFile(UUID uuid);
	UUID saveFile(MultipartFile file) throws IOException, MimeTypeException;
}

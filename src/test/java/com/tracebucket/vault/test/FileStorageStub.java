package com.tracebucket.vault.test;

import com.tracebucket.vault.FilePointer;
import com.tracebucket.vault.FileStorage;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


@Component
@Profile("test")
public class FileStorageStub implements FileStorage {

	private static final Logger log = LoggerFactory.getLogger(FileStorageStub.class);

	public Optional<FilePointer> findFile(UUID uuid) {
		log.debug("Downloading {}", uuid);
		if (uuid.equals(FileExamples.TXT_FILE_UUID)) {
			return Optional.of(FileExamples.TXT_FILE);
		}
		return Optional.empty();
	}

    @Override
    public UUID saveFile(MultipartFile file) throws IOException, MimeTypeException {
        return null;
    }

	@Override
	public UUID saveFile(File file) throws IOException, MimeTypeException {
		return null;
	}

}

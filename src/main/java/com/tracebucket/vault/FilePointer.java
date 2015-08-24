package com.tracebucket.vault;

import com.google.common.net.MediaType;
import org.apache.tika.mime.MimeTypeException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;

public interface FilePointer {

	InputStream open();

	long getSize();

	String getOriginalName();

	String getEtag();

	Optional<MediaType> getMediaType();

	boolean matchesEtag(String requestEtag);

	Instant getLastModified();

	boolean modifiedAfter(Instant isModifiedSince);

    String getFileExtension() throws IOException, MimeTypeException;

}

package com.tracebucket.vault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

@RestController
@RequestMapping("/download")
public class DownloadController {

	private final FileStorage storage;

    @Autowired
	public DownloadController(FileStorage storage) {
		this.storage = storage;
	}

	@RequestMapping(method = {GET, HEAD}, value = "/{uuid}")
	public ResponseEntity<Resource> redirect(
			HttpMethod method,
			@PathVariable UUID uuid,
			@RequestHeader(IF_NONE_MATCH) Optional<String> requestEtagOpt,
			@RequestHeader(IF_MODIFIED_SINCE) Optional<Date> ifModifiedSinceOpt,
			HttpServletRequest request
			) {
		String contextPath = extractContextPath(request);
		return findExistingFile(method, uuid, contextPath)
				.map(file -> file.redirect(requestEtagOpt, ifModifiedSinceOpt))
				.orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
	}

	@RequestMapping(method = {GET, HEAD}, value = "/{uuid}/{filename}")
	public ResponseEntity<Resource> download(
			HttpMethod method,
			@PathVariable UUID uuid,
			@RequestHeader(IF_NONE_MATCH) Optional<String> requestEtagOpt,
			@RequestHeader(IF_MODIFIED_SINCE) Optional<Date> ifModifiedSinceOpt,
			HttpServletRequest request
			) {
		String contextPath = extractContextPath(request);

		return findExistingFile(method, uuid, contextPath)
				.map(file -> file.handle(requestEtagOpt, ifModifiedSinceOpt))
				.orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
	}

	private String extractContextPath(HttpServletRequest request) {
		String protocol =  (request.getHeader("x-forwarded-proto") == null || request.getHeader("x-forwarded-proto").isEmpty())? "":request.getHeader("x-forwarded-proto");
		String host =  (request.getHeader("x-forwarded-host") == null || request.getHeader("x-forwarded-host").isEmpty())? "":request.getHeader("x-forwarded-host");
		String prefix = (request.getHeader("x-forwarded-prefix") == null || request.getHeader("x-forwarded-prefix").isEmpty())? "":request.getHeader("x-forwarded-prefix");

		String contextPath = null;
		if(!(protocol.isEmpty() && host.isEmpty() && prefix.isEmpty()))
			//Append forwarded prefix in case of proxied request
			contextPath = prefix + request.getContextPath();
		else
			contextPath = request.getContextPath();
		return contextPath;
	}

	private Optional<ExistingFile> findExistingFile(HttpMethod method, @PathVariable UUID uuid, String contextPath) {
		return storage
				.findFile(uuid)
				.map(pointer -> new ExistingFile(contextPath, method, pointer, uuid));
	}

}

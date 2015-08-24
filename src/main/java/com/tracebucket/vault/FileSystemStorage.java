package com.tracebucket.vault;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;

@Component
public class FileSystemStorage implements FileStorage {

	private static final Logger log = LoggerFactory.getLogger(FileSystemStorage.class);

    private static final String dbName = "VAULT_DB";

    @Value("${vault.baseDirectory}")
    private String baseDirectory;

    @Value("${vault.dbFile}")
    private String dbFile;

    private ConcurrentNavigableMap<String, String> register = null;


    public FileSystemStorage(){
        // configure and open database using builder pattern.
        // all options are available with code auto-completion.
/*        DB db = DBMaker.fileDB(new File(dbFile))
                .closeOnJvmShutdown()
                .make();

        // open existing an collection (or create new)
        register = db.treeMap(dbName);*/
    }

	@Override
	public Optional<FilePointer> findFile(UUID uuid) {
		log.debug("Downloading {}", uuid);
		if(register.containsKey(uuid.toString())){
            String filename = register.get(uuid.toString());
            final File file = new File(filename);
            final FileSystemPointer pointer = new FileSystemPointer(file);
            return Optional.of(pointer);
        }
        else {
            return Optional.empty();
        }


	}

    @Override
    public UUID saveFile(MultipartFile file) throws IOException, MimeTypeException{
        UUID uuid = UUID.randomUUID();
        String filename = uuid.toString() + FileSystemPointer.getFileExtension(new BufferedInputStream(file.getInputStream()));
        try {
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(findAbsoultPath(filename))));
            stream.write(file.getBytes());
            stream.close();
            register.put(uuid.toString(), findAbsoultPath(filename));
            return uuid;
        } catch (Exception e) {
            log.error("You failed to upload " + filename + " => " + e.getMessage());
            return null;
        }
    }

    public static String getFileExtension(InputStream inputStream) throws IOException, MimeTypeException {
        TikaConfig config = TikaConfig.getDefaultConfig();
        MediaType mediaType = config.getMimeRepository().detect(inputStream, new Metadata());
        MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());
        return mimeType.getExtension();
    }

    @PostConstruct
    private void postConstruct() throws IOException{
        if(this.baseDirectory != null) {
            if(!Files.exists(Paths.get(this.baseDirectory))) {
                Files.createDirectories(Paths.get(this.baseDirectory));
            }
        }
        if(this.dbFile != null) {
            File dbFile = new File(this.dbFile);
            if(!dbFile.getParentFile().exists()) {
                if(!dbFile.getParentFile().mkdirs()) {
                    throw new IOException("Could Not Create DB File");
                }
            }
            DB db = DBMaker.fileDB(new File(this.dbFile))
                    .closeOnJvmShutdown()
                    .make();

            // open existing an collection (or create new)
            register = db.treeMap(dbName);
        }
    }

    private String findAbsoultPath(String filename){
        return baseDirectory + "/" + filename;
    }


}

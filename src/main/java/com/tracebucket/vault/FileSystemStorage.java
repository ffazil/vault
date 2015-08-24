package com.tracebucket.vault;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
        DB db = DBMaker.fileDB(new File(dbFile))
                .closeOnJvmShutdown()
                .make();

        // open existing an collection (or create new)
        register = db.treeMap(dbName);
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
    public UUID saveFile(String filename, byte[] content) {


            try {
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(findAbsoultPath(filename))));
                stream.write(content);
                stream.close();
                UUID uuid = UUID.randomUUID();
                register.put(uuid.toString(), findAbsoultPath(filename));
                return uuid;
            } catch (Exception e) {
                log.error("You failed to upload " + filename + " => " + e.getMessage());
                return null;
            }
    }

    private String findAbsoultPath(String filename){
        return baseDirectory + "/" + filename;
    }


}


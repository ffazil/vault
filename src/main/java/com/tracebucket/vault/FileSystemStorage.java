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
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;

@Component
public class FileSystemStorage implements FileStorage {

	private static final Logger log = LoggerFactory.getLogger(FileSystemStorage.class);

    private static final String dbName = "VAULT_DB";

    @Value("${vault.baseDirectory:null}")
    private String baseDirectory;

    @Value("${vault.dbFile:null}")
    private String dbFile;

    @Value("${user.home}")
    private String userHome;

    DB db = null;

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
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(findAbsolutePath(filename))));
            stream.write(file.getBytes());
            stream.close();
            register.put(uuid.toString(), findAbsolutePath(filename));
            db.commit();
            return uuid;
        } catch (Exception e) {
            log.error("You failed to upload " + filename + " => " + e.getMessage());
            db.rollback();
            return null;
        }
    }

    @Override
    public UUID saveFile(File file) throws IOException, MimeTypeException {
        UUID uuid = UUID.randomUUID();
        String targetFileName = uuid.toString() + FileSystemPointer.getFileExtension(new BufferedInputStream(new FileInputStream(file.getAbsolutePath())));
        try {
            FileInputStream inputStream = new FileInputStream(file);
            FileChannel inChannel = inputStream.getChannel();

            File target = new File(findAbsolutePath(targetFileName));
            FileOutputStream outputStream = new FileOutputStream(target);
            FileChannel outChannel = outputStream.getChannel();

            inChannel.transferTo(0, file.length(), outChannel);

            inputStream.close();
            outputStream.close();

            register.put(uuid.toString(), findAbsolutePath(targetFileName));
            db.commit();
            return uuid;
        } catch (Exception e) {
            log.error("You failed to upload " + targetFileName + " => " + e.getMessage());
            db.rollback();
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
        if(this.baseDirectory != null && !this.baseDirectory.equals("null")) {
            try {
                if (!Files.exists(Paths.get(this.baseDirectory))) {
                    Files.createDirectories(Paths.get(this.baseDirectory));
                }
            } catch (Exception e) {
                log.info("Could Not Create Vault File Directory From Path Specified In Properties Files, Hence Creating It In Home Directory");
                if (!Files.exists(Paths.get(this.userHome+"/vault/files"))) {
                    Files.createDirectories(Paths.get(this.userHome+"/vault/files"));
                }
            }
        } else if(this.baseDirectory != null && this.baseDirectory.equals("null")) {
            log.info("Could Not Load Vault File Directory Path From Properties Files, Hence Creating It In Home Directory");
            if (!Files.exists(Paths.get(this.userHome+"/vault/files"))) {
                Files.createDirectories(Paths.get(this.userHome+"/vault/files"));
            }
        }
        if(this.dbFile != null && !this.dbFile.equals("null")) {
            try {
                File dbFile = new File(this.dbFile);
                if (!dbFile.getParentFile().exists()) {
                    if (!dbFile.getParentFile().mkdirs()) {
                        throw new IOException("Could Not Create DB File");
                    }
                }
                db = DBMaker.fileDB(new File(this.dbFile))
                        .closeOnJvmShutdown()
                        .fileLockDisable()
                        .fileLockHeartbeatEnable()
                        .make();

                // open an existing collection (or create new)
                register = db.treeMap(dbName);
            } catch (Exception e) {
                log.info("Could Not Create Vault DB Directory From Path Specified In Properties Files, Hence Creating It In Home Directory");
                File dbFile = new File(this.userHome+"/vault/db");
                if (!dbFile.getParentFile().exists()) {
                    if (!dbFile.getParentFile().mkdirs()) {
                        throw new IOException("Could Not Create DB File");
                    }
                }
                db = DBMaker.fileDB(new File(this.userHome+"/vault/db"))
                        .closeOnJvmShutdown()
                        .fileLockDisable()
                        .fileLockHeartbeatEnable()
                        .make();

                // open existing an collection (or create new)
                register = db.treeMap(dbName);
            }
        } else if(this.dbFile != null && this.dbFile.equals("null")) {
            log.info("Could Not Load Vault DB Path From Properties Files, Hence Creating It In Home Directory");
            File dbFile = new File(this.userHome+"/vault/db");
            if (!dbFile.getParentFile().exists()) {
                if (!dbFile.getParentFile().mkdirs()) {
                    throw new IOException("Could Not Create DB File");
                }
            }
            db = DBMaker.fileDB(new File(this.userHome+"/vault/db"))
                    .closeOnJvmShutdown()
                    .fileLockDisable()
                    .fileLockHeartbeatEnable()
                    .make();

            // open existing an collection (or create new)
            register = db.treeMap(dbName);
        }
    }

    private String findAbsolutePath(String filename){
        if(this.baseDirectory != null && !this.baseDirectory.equals("null")) {
            return baseDirectory + "/" + filename;
        } else if(this.baseDirectory != null && this.baseDirectory.equals("null")) {
            return this.userHome+"/vault/files/" + filename;
        }
        return null;
    }
}


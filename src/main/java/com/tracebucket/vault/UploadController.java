package com.tracebucket.vault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author ffazil
 * @since 22/08/15
 */
@Controller
@RequestMapping("/upload")
public class UploadController {

    private final FileStorage storage;

    @Autowired
    public UploadController(FileStorage storage) {
        this.storage = storage;
    }

    @RequestMapping(method= RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

    @RequestMapping(method=RequestMethod.POST)
    public @ResponseBody String handleFileUpload(@RequestParam("name") String name,
                                                 @RequestParam("file") MultipartFile file){
        UUID filePointer = null;
        if (!file.isEmpty()) {
            try {
                filePointer = storage.saveFile(name, file.getBytes());
                return filePointer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

            return "";

    }
}

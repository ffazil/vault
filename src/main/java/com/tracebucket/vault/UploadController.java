package com.tracebucket.vault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
    public @ResponseBody String handleFileUpload(@RequestParam("file") MultipartFile file) throws Exception {
        UUID filePointer = null;
        if (!file.isEmpty()) {
            try {
                filePointer = storage.saveFile(file);
                return filePointer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

            return "";

    }

    @RequestMapping("uploadError")
    public ResponseEntity onUploadError(HttpServletRequest request, Exception e) {
        return new ResponseEntity(HttpStatus.PRECONDITION_REQUIRED);
    }
}

package com.petcare.controller;
import com.petcare.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    /** Mock OSS upload - saves to local /tmp/petcare/ */
    @PostMapping
    public Result<?> upload(@RequestParam("file") MultipartFile file, HttpServletRequest req) {
        try {
            String dir = System.getProperty("java.io.tmpdir") + "/petcare/";
            new File(dir).mkdirs();
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            file.transferTo(new File(dir + filename));
            Map<String, String> data = new HashMap<>();
            data.put("url", "/uploads/" + filename);
            data.put("filename", filename);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("Upload failed: " + e.getMessage());
        }
    }
}

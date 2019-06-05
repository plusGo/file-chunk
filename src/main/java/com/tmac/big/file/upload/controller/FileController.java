package com.tmac.big.file.upload.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FileController {
    @Value("${application.web.upload-path}")
    private String webUploadPath;

    @PostMapping(value = "/mkfile")
    public Map<String, String> mkfile(@RequestParam final String context,
                                      @RequestParam final String fileName,
                                      @RequestParam final Integer chunks) {
        final List<String> fpathList = new ArrayList<>();

        for (int i = 1; i < chunks + 1; i++) {
            fpathList.add(webUploadPath + "/" + context + "/" + i);
        }
        String[] fpaths = fpathList.toArray(new String[0]);
        mergeFiles(fpaths, webUploadPath + "/" + fileName);

        return new HashMap<>();
    }

    @PostMapping(value = "/fileUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> fileUpload(@RequestParam(value = "file") MultipartFile file,
                                          final HttpServletRequest request) {
        final String context = request.getParameter("context");
        final String chunk = request.getParameter("chunk");
        if (!file.isEmpty()) {
            try {
                File file1 = new File(webUploadPath + "/" + context);
                if (!file1.exists()) {
                    boolean mkdir = file1.mkdir();
                }
                File dest = new File(webUploadPath + "/" + context, chunk);

                file.transferTo(dest);
                return new HashMap<>();
            } catch (IOException exception) {
                exception.printStackTrace();
                throw new RuntimeException("upload failed");
            }

        }
        return null;
    }

    public static boolean mergeFiles(String[] fpaths, String resultPath) {
        if (fpaths == null || fpaths.length < 1 || StringUtils.isEmpty(resultPath)) {
            return false;
        }
        if (fpaths.length == 1) {
            return new File(fpaths[0]).renameTo(new File(resultPath));
        }

        File[] files = new File[fpaths.length];
        for (int i = 0; i < fpaths.length; i++) {
            files[i] = new File(fpaths[i]);
            if (StringUtils.isEmpty(fpaths[i]) || !files[i].exists() || !files[i].isFile()) {
                return false;
            }
        }

        File resultFile = new File(resultPath);

        try {
            int bufSize = 1024;
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(resultFile));
            byte[] buffer = new byte[bufSize];

            for (int i = 0; i < fpaths.length; i++) {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(files[i]));
                int readcount;
                while ((readcount = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readcount);
                }
                inputStream.close();
            }
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

//        for (int i = 0; i < fpaths.length; i++) {
//            files[i].delete();
//        }

        return true;
    }
}

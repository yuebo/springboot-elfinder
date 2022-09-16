package cn.kong.web.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Controller
public class IndexController {
    @Value("${UPLOAD_PATH:/tmp/upload}")
    private String root;
    @GetMapping("/")
    public String index(){
        return "redirect:index.html";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam(name = "file") MultipartFile file) throws Exception{
        File parent = new File(root,"upload");
        FileUtils.forceMkdir(parent);
        try(OutputStream outputStream=new FileOutputStream(new File(parent,file.getOriginalFilename()))) {
            IOUtils.copy(file.getInputStream(),outputStream);
        }
        return "ok";
    }
}

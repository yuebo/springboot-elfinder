package cn.kong.web.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Controller
public class IndexController {
    @GetMapping("/")
    public String index(){
        return "index";
    }

    @PostMapping("/")
    @ResponseBody
    public String upload(@RequestParam(name = "file") MultipartFile file) throws Exception{

        try(OutputStream outputStream=new FileOutputStream(new File("/tmp/app",file.getOriginalFilename()))) {
            IOUtils.copy(file.getInputStream(),outputStream);
        }
        return "ok";
    }
}

package com.pinyougou.manager.controller;

import com.pinyougou.pojo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

//    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL="http://192.168.25.133/";

    //进行文件上传
    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        try {
            //1.得到文件后缀名
            //1.1得到上传的文件名
            String fileName = file.getOriginalFilename();
            //1.2得到上传文件名的后缀名
            String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
            //2.调用文件上传方法进行上传
            util.FastDFSClient client = new util.FastDFSClient("classpath:config/fdfs_client.conf");
            // 3.进行文件上传，返回文件路径
            String fileId = client.uploadFile(file.getBytes(), extName);
            System.out.println("FILE_SERVER_URL="+FILE_SERVER_URL);
            String url = FILE_SERVER_URL + fileId;//图片完整地址
            System.out.println("url=="+url);
            // 4.将上传成功后的 url 地址随着 Result 对象发到客户端
            return new Result(true, url);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"上传失败！");
        }

    }

}

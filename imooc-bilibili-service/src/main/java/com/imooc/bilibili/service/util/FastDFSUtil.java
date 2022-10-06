package com.imooc.bilibili.service.util;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.imooc.bilibili.domain.exception.ConditionalException;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.ConnectException;
import java.util.*;

@Component
public class FastDFSUtil {
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String,String>redisTemplate;

    public static final String PATH_KEY = "path-key:";
    public static final String UPLOAD_SIZE_KEY = "uploaded-size-key:";
    public static final String UPLOAD_NO_KEY = "uploaded-no-key:";
    public static final String DEFAULT_GROUP = "group1";
    public static final Integer SLICE_SIZE = 1024 * 1024 * 2;
    public String getFileType(MultipartFile file){
        if(file==null){
            throw new ConditionalException("非法文件！");
        }
        //根据文件获取文件类型，读取file的最后一个参数
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index+1);
    }
    //上传
    public String uploadCommonFile(MultipartFile file) throws IOException {
        Set<MetaData>metaData = new HashSet<>();
        String fileType = this.getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaData);
        return storePath.getPath();
    }


    public String uploadCommonFile(File file, String fileType) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>();
        StorePath storePath = fastFileStorageClient.uploadFile(new FileInputStream(file),
                file.length(), fileType, metaDataSet);
        return storePath.getPath();
    }

    //断点续传的功能
    public String uploadAppendFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    //不会重复已经上传的
    public void modifyAppendFile(MultipartFile file,String filePath,long offset) throws IOException {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP,filePath,file.getInputStream(),file.getSize(),offset);
    }

    //分片上传，及那个path,uploadNo,uploadSize存到我们的redis
    public String uploadFileBySlices(MultipartFile file,String fileMD5,Integer slice,Integer totalSlice) throws IOException {
        if(file==null || slice==null || totalSlice == null){
            throw new ConditionalException("参数异常");
        }
        String pathKey = PATH_KEY + fileMD5;
        String uploadNoKey = UPLOAD_NO_KEY + fileMD5;
        String uploadSizeKey = UPLOAD_SIZE_KEY + fileMD5;
        //获取上传大小
        String uploadSizeStr = redisTemplate.opsForValue().get(uploadSizeKey);
        Long uploadedSize = 0L;
        if(!StringUtils.isNullOrEmpty(uploadSizeStr)){
            uploadedSize = Long.valueOf(uploadSizeStr);
        }
        String fileType = this.getFileType(file);
        //判断上传的是第一个分片还是其他分片
        if(slice==1){
            String path = this.uploadAppendFile(file);
            if(StringUtils.isNullOrEmpty(path)){
                throw new ConditionalException("上传失败! ");
            }
            redisTemplate.opsForValue().set(pathKey,path);
            redisTemplate.opsForValue().set(uploadNoKey,"1");
        }else{
            String filePath = redisTemplate.opsForValue().get(pathKey);
            if (StringUtils.isNullOrEmpty(filePath)){
                throw new ConnectException("上传失败!");
            }
            this.modifyAppendFile(file,filePath,uploadedSize);
            //更新分片
            redisTemplate.opsForValue().increment(uploadNoKey);
        }
        //更新上传大小
        uploadedSize +=file.getSize();
        redisTemplate.opsForValue().set(uploadSizeKey,String.valueOf(uploadedSize));
        //判断是否已经上传完,上传完后清除redis里面的key值
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if(uploadedNo.equals(totalSlice)){
            resultPath = redisTemplate.opsForValue().get(pathKey);
            List<String> list = Arrays.asList(uploadNoKey, pathKey, uploadSizeKey);
            redisTemplate.delete(list);
        }
        return resultPath;
    }


    //切片处理

    /**
     * 指定文件切成多少片，那么需要用到IO流的关系，我们先读取文件的大小
     * 确定好每次读取多少，比如SLICE_SIZE=1024*2，在指定我们写入的路径path
     *
     */
    public void convertFileToSlice(MultipartFile multipartFile) throws IOException {
        String fileType = this.getFileType(multipartFile);
        File file =this.multipartFileToFile(multipartFile);
        long fileLength = file.length();
        int count = 1;
        for(int i = 0;i<fileLength;i+=SLICE_SIZE){
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
            randomAccessFile.seek(i);
            byte[]bytes = new byte[SLICE_SIZE];
            int len = randomAccessFile.read();
            String path = "E:\\WorkSpace\\tmp\\tmpfile"+count+"."+fileType;
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes,0,len);
            fos.close();
            randomAccessFile.close();
            count++;
        }
        file.delete();//删除临时文件
    }


    public File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String[]fileName = originalFilename.split("\\.");
        File file = File.createTempFile(fileName[0],"."+fileName[1]);
        multipartFile.transferTo(file);
        return file;
    }
    //删除
    public void deleteFile(String filePath){
        fastFileStorageClient.deleteFile(filePath);
    }

    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response,String path) throws Exception {
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP,path);
        long totalFileSize = fileInfo.getFileSize();
        String url = httpFdfsStorageAddr+path;
        //获取请求头中的信息
        Enumeration<String>headerNames = request.getHeaderNames();//枚举类
        Map<String,Object>headers = new HashMap<>();
        while (headerNames.hasMoreElements()){
            String header = headerNames.nextElement();
            headers.put(header,request.getHeader(header));
        }
        String rangeStr = request.getHeader("range");
        String[]range;
        if(StringUtils.isNullOrEmpty(rangeStr)){
            rangeStr = "bytes=0-"+(totalFileSize-1);
        }
        range = rangeStr.split("bytes=|-");
        //范围的开始与结束
        long begin = 0;
        if(range.length >=2){
            begin = Long.parseLong(range[1]);
        }
        long end = totalFileSize -1;
        if(range.length >= 3){
            end = Long.parseLong(range[2]);
        }
        long len = (end-begin)+1;
        String contentRange = "bytes"+begin+"-"+end+"/"+totalFileSize;
        response.setHeader("Content-Range",contentRange);
        response.setHeader("Accept-Ranges","bytes");
        response.setHeader("Content-Type","video/mp4");
        response.setContentLength((int)len);
        HttpUtil.get(url,headers,response);
    }

    public void downLoadFile(String url, String localPath) {
        fastFileStorageClient.downloadFile(DEFAULT_GROUP, url,
                new DownloadCallback<String>() {
                    @Override
                    public String recv(InputStream ins) throws IOException {
                        File file = new File(localPath);
                        OutputStream os = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = ins.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.close();
                        ins.close();
                        return "success";
                    }
                });

    }
}

package com.imooc.bilibili.controller;

import com.imooc.bilibili.controller.support.UserSupport;
import com.imooc.bilibili.domain.Danmu;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DanmuApi {
    @Autowired
    private UserSupport userSupport;

    @Autowired
    private DanmuService danmuService;
    @GetMapping("/danmus")
    public JsonResponse<List<Danmu>>getDanmus(@RequestParam Long videoId,
                                               String startTime,
                                                String endTime) throws Exception {
        List<Danmu>list;
        try{
            userSupport.getCurrentUserId();
            list = danmuService.getDanmus(videoId,startTime,endTime);
        }catch(Exception ignored){
            list = danmuService.getDanmus(videoId,null,null);
        }
        return new JsonResponse<>(list);
    }
}

package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.DanmuDao;
import com.imooc.bilibili.domain.Danmu;
import com.mysql.jdbc.StringUtils;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DanmuService {

    @Autowired
    private DanmuDao danmuDao;

    @Autowired
    private RedisTemplate<String,String>redisTemplate;

    public static final String DANMU_KEY = "danmu-video-";

    public void addDanmu(Danmu danmu){
        danmuDao.addDanmu(danmu);
    }

    @Async
    public void asyncAddDanmu(Danmu danmu){
        danmuDao.addDanmu(danmu);
    }

    /**
     * 查询策略，应优先查询redis的弹幕数据
     * 如果reids没有在查询数据库
     * @param videoId
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Danmu>getDanmus(Long videoId,
                                String startTime,
                                String endTime) throws Exception {
        String key = DANMU_KEY + videoId;
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu>list;
        if(!StringUtils.isNullOrEmpty(value)){
            //通过redis获取到对应的list
            list = JSONArray.parseArray(value,Danmu.class);
            if(!StringUtils.isNullOrEmpty(startTime) && !StringUtils.isNullOrEmpty(endTime)){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = sdf.parse(startTime);
                Date endDate = sdf.parse(endTime);
                List<Danmu>childList = new ArrayList<>();
                for(Danmu danmu:list){
                    Date createTime = danmu.getCreateTime();
                    if(createTime.after(startDate) && createTime.before(endDate)){
                        childList.add(danmu);
                    }
                }
                list = childList;
            }
        }else{
            Map<String,Object>params = new HashMap<>();
            params.put("videoId",videoId);
            params.put("startTime",startTime);
            params.put("endTime",endTime);
            list = danmuDao.getDanmus(params);
            redisTemplate.opsForValue().set(key,JSONObject.toJSONString(list));
        }
        return list;
    }

    public void addDanmusToRedis(Danmu danmu) {
        String key = DANMU_KEY+danmu.getVideoId();
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu>list = new ArrayList<>();
        if(!StringUtils.isNullOrEmpty(value)){
            list = JSONArray.parseArray(value,Danmu.class);
        }
        list.add(danmu);
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(danmu));
    }
}

package com.imooc.bilibili.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RestFulDemo {
    private final Map<Integer, Map<String,Object>>dataMap;
    public RestFulDemo(){
        //初始化
        dataMap = new HashMap<>();
        for(int i  = 0;i<3;i++){
            Map<String,Object> data = new HashMap<>();
            data.put("id",i);
            data.put("name","name"+i);
            dataMap.put(i,data);
        }
    }

    /**
     * 查询操作
     * @param id
     * @return
     */
    @GetMapping("/objects/{id}")
    public Map<String,Object>getRest(@PathVariable("id") Integer id){
        return dataMap.get(id);
    }

    /**
     * 删除操作
     * @param id
     * @return
     */
    @DeleteMapping("/objects/{id}")
    public String deleteRest(@PathVariable("id") Integer id){
        dataMap.remove(id);
        return "delete success";
    }

    /**
     * 新增操作
     * @param data
     * @return
     */
    @PostMapping("/objects")
    public String postRest(@RequestBody Map<String,Object>data){
        Integer[]array = dataMap.keySet().toArray(new Integer[0]);
        Arrays.sort(array);
        int nextInt = array[array.length-1]+1;
        dataMap.put(nextInt,data);
        return "post success";
    }

    @PutMapping("/objects")
    public String putRest(@RequestBody Map<String,Object>data){
        //找出修改的id字段
        Integer id = Integer.valueOf(String.valueOf(data.get("id")));
        Map<String,Object>containMap = dataMap.get(id);
        if(containMap==null){
            //进行新增操作
            Integer[]array = dataMap.keySet().toArray(new Integer[0]);
            Arrays.sort(array);
            int nextInt = array[array.length-1]+1;
            dataMap.put(nextInt,data);
        }else{
            dataMap.put(id,data);
        }
        return "put success";
    }
}

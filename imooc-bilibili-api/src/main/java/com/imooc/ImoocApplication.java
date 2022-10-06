package com.imooc;

import com.imooc.bilibili.service.websocket.WebSocketService;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@EnableFeignClients(basePackages = "com.imooc.bilibili.service.feign")
@EnableHystrix
public class ImoocApplication {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(ImoocApplication.class, args);
        WebSocketService.setApplicationContext(app);
    }
//    @Bean
//    public RestHighLevelClient client(){
//        return new RestHighLevelClient(RestClient.builder(
//                HttpHost.create("http://192.168.117.130:9200")));
//    }
}

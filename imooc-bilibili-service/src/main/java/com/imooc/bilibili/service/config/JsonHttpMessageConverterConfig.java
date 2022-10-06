package com.imooc.bilibili.service.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.Collections;

@Configuration
public class JsonHttpMessageConverterConfig {

    @Bean
    @Primary
    public HttpMessageConverters jsonHttpMessageConverter(){
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //配置json序列化
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.MapSortField,
                SerializerFeature.DisableCircularReferenceDetect//禁止循环引用
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //使用feign进行微服务之间的接口调用，需要加上该配置
        fastConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpMessageConverters(fastConverter);
    }
}

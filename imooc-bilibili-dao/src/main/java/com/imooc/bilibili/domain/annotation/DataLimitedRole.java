package com.imooc.bilibili.domain.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Component
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface DataLimitedRole {


}

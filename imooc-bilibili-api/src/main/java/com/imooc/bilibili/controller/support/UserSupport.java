package com.imooc.bilibili.controller.support;

import com.imooc.bilibili.domain.exception.ConditionalException;
import com.imooc.bilibili.service.util.TokenUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserSupport {
    //获取请求头中信息
    public Long getCurrentUserId(){
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = requestAttributes.getRequest().getHeader("token");
        Long userId = TokenUtils.verifyToken(token);
        if(userId < 0){
            throw new ConditionalException("非法用户！");
        }
        return userId;
    }
}

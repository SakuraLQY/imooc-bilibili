package com.imooc.bilibili.controller.aspect;

import com.imooc.bilibili.controller.support.UserSupport;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.annotation.ApiLimitedRole;
import com.imooc.bilibili.domain.auth.UserRole;
import com.imooc.bilibili.domain.constants.AuthRoleConstant;
import com.imooc.bilibili.domain.exception.ConditionalException;
import com.imooc.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class DataLimitedRoleAspect {
    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.imooc.bilibili.domain.annotation.DataLimitedRole)")
    public void check(){
    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint){
        Long userId = userSupport.getCurrentUserId();
        //获取用户权限集合
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //获取到权限限制集合
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if(arg instanceof UserMoment){
            UserMoment userMoment = (UserMoment)arg;
            String type = userMoment.getType();
            if(roleCodeSet.contains(AuthRoleConstant.ROLE_LV1 )&& !"0".equals(type)){
                throw new ConditionalException("参数异常");
               }
            }
        }
    }
}

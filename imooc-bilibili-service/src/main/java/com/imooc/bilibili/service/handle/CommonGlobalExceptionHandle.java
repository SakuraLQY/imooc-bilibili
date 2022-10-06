package com.imooc.bilibili.service.handle;

import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.exception.ConditionalException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandle {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonResponse<String>commonExceptionHandle(HttpServletRequest request, Exception e){
        String errorMsg = e.getMessage();
        if(e instanceof ConditionalException){
            //如果是条件异常
            String errorCode = ((ConditionalException) e).getCode();
            return new JsonResponse<>(errorCode,errorMsg);
        }else {
            return new JsonResponse<>("500", errorMsg);
        }
    }
}

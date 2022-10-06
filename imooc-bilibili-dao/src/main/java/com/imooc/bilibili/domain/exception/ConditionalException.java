package com.imooc.bilibili.domain.exception;

/**
 * 自定义异常
 */
public class ConditionalException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    private String code;


    public ConditionalException(String code,String name){
        super(name);
        this.code = code;
    }


    /**
     * 自定义异常返回
     * @param name
     */
    public ConditionalException(String name){
        super(name);
        code = "500";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}

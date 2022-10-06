package com.imooc.bilibili.domain;

public class JsonResponse<T> {
    private String code;
    private String msg;
    private T data;

    public JsonResponse(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public JsonResponse(T data){
        this.data = data;
        msg = "成功";
        code = "0";
    }

    /**
     * 成功方法,不需要返回给前端的时候
     * @return
     */
    public static JsonResponse<String> success (){
        return new JsonResponse<>(null);
    }

    /**
     * 根据编码返回成功
     * @param code
     * @param msg
     * @return
     */
    public static JsonResponse<String> success(String code,String msg){
        return new JsonResponse<>(code,msg);
    }

    /**
     * 默认失败的状态码和信息
     * @return
     */
    public static JsonResponse<String>fail(){
        return new JsonResponse<>("1","失败");

    }

    /**
     * 返回特定的状态码和提示信息
     * @param code
     * @param msg
     * @return
     */
    public static JsonResponse<String>fail(String code,String msg){
        return new JsonResponse<>(code,msg);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

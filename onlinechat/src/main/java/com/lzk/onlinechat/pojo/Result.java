package com.lzk.onlinechat.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author lzk
 * @Email 1801290586@qq.com
 * @Description <类说明>
 * @Date 15:21 2022/6/12
 */
@Data
/**
 * 结果统一封装
 */
public class Result  implements Serializable {
    private int code;
    private String msg;
    private Object data;

    //    成功
    public static Result succ(Object data){
        return Result.succ(200,"ok",data);
    }

    public static Result succ(int code,String msg,Object data){
        Result r= new Result();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
    //    失败
    public static Result fail(String msg){
        return Result.fail(msg,null);
    }
    public static Result fail(String msg,Object data){
        return Result.fail(400,msg,data);
    }
    public static Result fail(int code,String msg,Object data){
        Result r= new Result();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}

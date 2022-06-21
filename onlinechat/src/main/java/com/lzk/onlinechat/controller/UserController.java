package com.lzk.onlinechat.controller;

import com.lzk.onlinechat.pojo.Result;
import com.lzk.onlinechat.pojo.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lzk
 * @Email 1801290586@qq.com
 * @Description <类说明>
 * @Date 15:08 2022/6/12
 */
@RestController
@RequestMapping("/user")
public class UserController {
    //所有用户信息
    private static Map<Integer,User>allUser;

    //在线的用户
    private static Map<Integer,User> onlineUser;

    static {
        onlineUser=new ConcurrentHashMap<>();
        allUser=new ConcurrentHashMap<>();
        //自定义三个用户
        allUser.put(1,new User(1,"娴娴","https://s1.ax1x.com/2022/06/12/X2CkdI.jpg"));
        allUser.put(2,new User(2,"扬扬","https://s1.ax1x.com/2022/06/12/X2CAot.jpg"));
        allUser.put(3,new User(3,"萌萌","https://s1.ax1x.com/2022/06/12/X2CFeA.jpg"));
    }

    //获取用户信息
    @RequestMapping(value = "/getYourInfo/{uid}")
    public Result getYourInfo(@PathVariable("uid") int uid){
        //添加到在线用户列表
        if(!onlineUser.containsKey(uid)){
            onlineUser.put(uid,allUser.get(uid));
        }
        return Result.succ(allUser.get(uid));
    }

    //获取在线用户
    @RequestMapping("/getUserList")
    public Result getUserList(){
        return Result.succ(onlineUser.values());
    }

}

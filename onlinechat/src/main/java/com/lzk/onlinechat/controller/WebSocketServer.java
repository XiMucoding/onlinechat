package com.lzk.onlinechat.controller;

/**
 * @Author lzk
 * @Email 1801290586@qq.com
 * @Description <类说明>
 * @Date 15:56 2022/6/12
 */

import com.alibaba.fastjson.JSON;
import com.lzk.onlinechat.pojo.Message;
import com.lzk.onlinechat.pojo.Result;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/wechat/{uid}")
public class WebSocketServer {
    /**
     * 记录在线的用户数
     */
    private static AtomicInteger onlineUserNum=new AtomicInteger(0);

    /**
     * 存储连接该服务的用户（客户端）对应的WebSocketServer （uid,WebSocketServer）
     */
    private static Map<Integer,WebSocketServer> webSocketMap=new ConcurrentHashMap<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 当前连接进行用户的uid
     */
    private int uid;

    /**
     * 连接成功后的回调函数
     * @param uid
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("uid")int uid,Session session){
        //获取当前的session、uid
        this.session=session;
        this.uid=uid;
        //存储客户端对应的websocket
        if (!webSocketMap.containsKey(uid)){
            //判断这里还应该查一下数据库，但是我这里比较潦草就没做
            //还未连接过
            webSocketMap.put(uid,this);
            //在线人数+1
            onlineUserNum.incrementAndGet();
        }else{
            //已经连接过，记录新的websocket
            webSocketMap.replace(uid,this);
        }
        System.out.println("用户id:"+uid+"建立连接！");
    }

    /**
     * 连接失败后的回调函数
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("用户:"+this.uid+"连接失败,原因:"+error.getMessage());
        error.printStackTrace();
    }

    /**
     * 前提：成功建立连接
     *      发送过来的消息按如下的机制推送到接收的客户端
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message,Session session){
        System.out.println(message);
        if(message.isEmpty()||message==null){
            //消息不正常，不处理
            return;
        }
        //初始化消息的格式 json->自己定义的消息体
        Message fromMessage = JSON.parseObject(message,Message.class);
        if(!webSocketMap.containsKey(fromMessage.getToUid())){
            System.out.println("要接收的用户不在线，暂存数据库，等该用户上线在获取！");
            return;
        }
        //在线则直接推送数据到接收端客户端
        WebSocketServer webSocketServer = webSocketMap.get(fromMessage.getToUid());
        webSocketServer.sendMessage(message);
    }

    /**
     * 推送消息到客户端
     * @param message
     */
    public void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接关闭后的回调函数
     */
    @OnClose
    public void onClose(){
        if (webSocketMap.containsKey(uid)){
            webSocketMap.remove(uid);
            //在线人数-1
            onlineUserNum.decrementAndGet();
        }
    }

}


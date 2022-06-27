package com.lzk.onlinechat.netty;

import com.alibaba.fastjson.JSON;
import com.lzk.onlinechat.pojo.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lzk
 * @Email 1801290586@qq.com
 * @Description <类说明>
 * @Date 0:38 2022/6/27
 */
//这里 TextWebSocketFrame 类型，表示一个文本帧(frame)
@Component
@ChannelHandler.Sharable
public class MyTextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    //记录客户端和channel的绑定
    private static Map<Integer, Channel> channelMap=new ConcurrentHashMap<Integer, Channel>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        //将发过来的内容进行解析成 自定义的Message
        Message message = JSON.parseObject(textWebSocketFrame.text(), Message.class);
        //绑定对应用户和channel
        if (!channelMap.containsKey(message.getFromUid())){
            channelMap.put(message.getFromUid(),channelHandlerContext.channel());
        }else{
            channelMap.replace(message.getFromUid(),channelHandlerContext.channel());
        }
        //发送给对应的客户端对应的channel
        if(channelMap.containsKey(message.getToUid())){
            //因为连接成功会发送一次注册消息（注册消息message.getToUid()== message.getFromUid()）
            if(message.getToUid()!= message.getFromUid()){
                //不能重用之前的textWebSocketFrame
                channelMap.get(message.getToUid()).writeAndFlush(new TextWebSocketFrame(textWebSocketFrame.text()));
            }
        }else{
            //该用户暂未在线，先将消息存进数据库（这里没实现）
            System.out.println("该用户暂未在线，先将消息存进数据库");
        }
        //计数-1（计数法来控制回收内存）
        channelHandlerContext.fireChannelRead(textWebSocketFrame.retain());
    }
}

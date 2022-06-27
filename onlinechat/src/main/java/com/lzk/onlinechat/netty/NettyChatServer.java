package com.lzk.onlinechat.netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @Author lzk
 * @Email 1801290586@qq.com
 * @Description <类说明>
 * @Date 0:16 2022/6/27
 */
@Component("NettyChatServer")
public class NettyChatServer {
    //主线程池：处理连接请求
    private static NioEventLoopGroup boss = new NioEventLoopGroup(2);
    //工作线程池：接收主线程发过来的任务，完成实际的工作
    private static NioEventLoopGroup worker = new NioEventLoopGroup(6);
    //创建一个服务器端的启动对象
    ServerBootstrap serverBootstrap=null;

    @Autowired
    //自定义handler、处理客户端发送过来的消息进行转发等逻辑
    MyTextWebSocketFrameHandler myTextWebSocketFrameHandler = new MyTextWebSocketFrameHandler();

    public void run() {
        serverBootstrap= new ServerBootstrap().group(boss, worker)
                .channel(NioServerSocketChannel.class)
                //连接的最大线程数
                .option(ChannelOption.SO_BACKLOG, 128)
                //长连接，心跳机制
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //因为基于http协议，使用http的编码和解码器
                        nioSocketChannel.pipeline().addLast(new HttpServerCodec());
                        //是以块方式写，添加ChunkedWriteHandler处理器
                        nioSocketChannel.pipeline().addLast(new ChunkedWriteHandler());
                        /**
                         * 说明
                         *   1. http数据在传输过程中是分段, HttpObjectAggregator ，就是可以将多个段聚合
                         *   2. 这就就是为什么，当浏览器发送大量数据时，就会发出多次http请求
                         */
                        nioSocketChannel.pipeline().addLast(new HttpObjectAggregator(8192));
                        /**
                         * 说明
                         *    1. 对应websocket ，它的数据是以帧(frame,基于TCP)形式传递
                         *    2. 可以看到WebSocketFrame下面有六个子类
                         *    3. 浏览器请求时 ws://localhost:8888/wechat 表示请求的uri
                         *    4. WebSocketServerProtocolHandler 核心功能是将 http协议升级为 ws协议 , 保持长连接
                         *    5. 是通过一个 状态码 101
                         */
                        nioSocketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/wechat"));
                        //自定义handler、处理客户端发送过来的消息进行转发等逻辑
                        nioSocketChannel.pipeline().addLast(myTextWebSocketFrameHandler);
                    }
                });
        //server监听接口
        try {
            ChannelFuture channelfuture = serverBootstrap.bind(8888).sync();
            // 添加注册监听，监控关心的事件，当异步结束后就会回调监听逻辑
            channelfuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()){
                        System.out.println("监听端口8888成功");
                    }else{
                        System.out.println("监听端口8888失败");
                    }
                }
            });
            //关闭通道和关闭连接池(不是真正关闭，只是设置为关闭状态)
            channelfuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //EventLoop停止接收任务、任务结束完毕停掉线程池
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

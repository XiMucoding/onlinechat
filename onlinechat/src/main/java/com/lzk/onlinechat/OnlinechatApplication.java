package com.lzk.onlinechat;

import com.lzk.onlinechat.netty.NettyChatServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class OnlinechatApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OnlinechatApplication.class, args);
        NettyChatServer nettyChatServer = (NettyChatServer)context.getBean("NettyChatServer");
        nettyChatServer.run();
    }

}

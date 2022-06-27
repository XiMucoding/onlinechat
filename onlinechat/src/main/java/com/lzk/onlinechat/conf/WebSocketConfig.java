package com.lzk.onlinechat.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @Author lzk
 * @Email 1801290586@qq.com
 * @Description <类说明>
 * @Date 15:37 2022/6/12
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注入ServerEndpointExporter,该Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
    
}


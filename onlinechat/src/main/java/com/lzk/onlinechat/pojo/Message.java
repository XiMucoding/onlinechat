package com.lzk.onlinechat.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author lzk
 * @Email 1801290586@qq.com
 * @Description <类说明>自定义消息体
 * @Date 10:56 2022/6/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Message {
    /**
     * 发送出消息的uid
     */
    private int FromUid;
    /**
     * 接收消息的uid
     */
    private int ToUid;
    /**
     * 消息内容
     */
    private String msg;
    /**
     * 发送时间
     */
    private String time;
}

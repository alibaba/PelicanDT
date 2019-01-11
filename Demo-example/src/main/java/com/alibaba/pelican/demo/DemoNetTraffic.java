package com.alibaba.pelican.demo;

import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.utils.MemUtils;
import com.alibaba.pelican.chaos.client.utils.NetTrafficUtils;
import com.alibaba.pelican.deployment.element.impl.AbstractElement;

import java.util.Map;

/**
 * @author moyun@middleware
 */
public class DemoNetTraffic {

    public static void main(String[] args) {

        //服务器IP
        String ip = "39.96.172.140";
        //服务器用户名
        String userName = "root";
        //服务器登录密码
        String password = "Hello1234";
        RemoteCmdClientConfig config = new RemoteCmdClientConfig();
        config.setIp(ip);
        config.setUserName(userName);
        config.setPassword(password);

        RemoteCmdClient client = new RemoteCmdClient(config);
        //内存比例
        int delayTime = 1000;
        //持续时间
        int delaySecond = 10;
        //通过该API控制网络延时
        NetTrafficUtils.setNetworkDelay(client, delayTime, delaySecond);
    }
}

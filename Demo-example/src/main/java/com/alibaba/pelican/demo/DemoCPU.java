package com.alibaba.pelican.demo;

import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.utils.CpuUtils;

/**
 * @author moyun@middleware
 */
public class DemoCPU {

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
        //CPU占用比例
        int percent = 70;
        //持续时间
        int delayMinutes = 3;
        CpuUtils.adjustCpuUsage(client, percent, delayMinutes);
    }
}

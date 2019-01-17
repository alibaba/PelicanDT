package com.alibaba.pelican.demo;

import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.utils.CpuUtils;
import com.alibaba.pelican.chaos.client.utils.MemUtils;

/**
 * @author moyun@middleware
 */
public class DemoMem {

    public static void main(String[] args) {

        //服务器IP
        String ip = "";
        //服务器用户名
        String userName = "";
        //服务器登录密码
        String password = "";
        RemoteCmdClientConfig config = new RemoteCmdClientConfig();
        config.setIp(ip);
        config.setUserName(userName);
        config.setPassword(password);

        RemoteCmdClient client = new RemoteCmdClient(config);
        //内存占用单位为M
        int percent = 6144;
        //持续时间
        int delayMinutes = 3;
        MemUtils.adjustMemUsage(client, percent, delayMinutes);
    }
}

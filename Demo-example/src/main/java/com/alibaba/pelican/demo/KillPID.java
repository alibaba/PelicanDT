package com.alibaba.pelican.demo;

import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.utils.CpuUtils;

/**
 * @author moyun@middleware
 */
public class KillPID {

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

        client.killProcess("vmstat");
    }
}

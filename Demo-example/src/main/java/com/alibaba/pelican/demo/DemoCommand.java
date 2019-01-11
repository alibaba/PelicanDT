package com.alibaba.pelican.demo;

import com.alibaba.pelican.chaos.client.RemoteCmd;
import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.RemoteCmdResult;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.utils.CpuUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author moyun@middleware
 */
@Slf4j
public class DemoCommand {

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
        RemoteCmdResult remoteCmdResult = client.execCmdWithPTY(new RemoteCmd("pwd"));
        log.info(remoteCmdResult.getStdInfo());
    }
}

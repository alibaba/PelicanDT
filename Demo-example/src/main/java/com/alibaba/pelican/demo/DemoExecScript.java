package com.alibaba.pelican.demo;

import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.RemoteCmdResult;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.utils.CpuUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author moyun@middleware
 */
public class DemoExecScript {

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
        RemoteCmdResult result = client.scpAndExecScript("demo.sh");
        System.out.println(result.getStdInfo());
    }
}

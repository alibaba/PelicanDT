/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.pelican.chaos.client;

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author moyun@middleware
 */
@Ignore
@Slf4j
public class TestRemoteCmdClient {

    @Test
    public void testRemoteCmdClient() {
        //ECS可公网访问的IP
        String ip = "";
        //ECS用户名，一般为root
        String userName = "";
        //ECS登录密码
        String password = "";

        //初始化客户端
        RemoteCmdClientConfig remoteCmdClientConfig = new RemoteCmdClientConfig();
        remoteCmdClientConfig.setIp(ip);
        remoteCmdClientConfig.setUserName(userName);
        remoteCmdClientConfig.setPassword(password);
        RemoteCmdClient client = new RemoteCmdClient(remoteCmdClientConfig);

        //执行ls命令
        RemoteCmdResult resultInfo = client.execCmdWithPTY(new RemoteCmd("ls"));
        log.info(resultInfo.getStdInfo());
    }


}

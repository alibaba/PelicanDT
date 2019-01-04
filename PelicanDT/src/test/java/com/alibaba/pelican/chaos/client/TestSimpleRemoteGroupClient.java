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

import com.alibaba.pelican.chaos.client.group.RemoteCmdClientGroup;
import com.alibaba.pelican.deployment.element.impl.AbstractElement;
import com.alibaba.pelican.deployment.junit.AbstractJUnit4PelicanTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author moyun@middleware
 */
@Ignore
@Slf4j
public class TestSimpleRemoteGroupClient extends AbstractJUnit4PelicanTests {

    @Test
    public void test() {
        Set<RemoteCmdClientConfig> connectUnits = new TreeSet<RemoteCmdClientConfig>();
        Map<String, String> params = ((AbstractElement) this.getTestProject()).getVariables();
        RemoteCmdClientConfig remoteCmdClientConfig = new RemoteCmdClientConfig();
        remoteCmdClientConfig.setPassword(params.get("password"));
        remoteCmdClientConfig.setUserName(params.get("userName"));
        remoteCmdClientConfig.setIp(params.get("ip"));
        connectUnits.add(remoteCmdClientConfig);

        RemoteCmdClientGroup commandExecutor = new RemoteCmdClientGroup(connectUnits);
        RemoteCmd remoteCommand = new RemoteCmd();
        remoteCommand.addCmd("mkdir moyun");
        Map<String, RemoteCmdResult> resultInfo = commandExecutor.execCmdWithPTY(remoteCommand);
    }

}

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
package com.alibaba.pelican.chaos.client.utils;

import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.deployment.element.impl.AbstractElement;
import com.alibaba.pelican.deployment.junit.AbstractJUnit4PelicanTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

/**
 * @author moyun@middleware
 */
@Ignore
@Slf4j
public class TestNetAccessUtils extends AbstractJUnit4PelicanTests {

    @Test
    public void testIPBlock() {
        Map<String, String> params = ((AbstractElement) this.getTestProject()).getVariables();
        RemoteCmdClientConfig connectUnit = new RemoteCmdClientConfig();
        connectUnit.setPassword(params.get("password"));
        connectUnit.setUserName(params.get("userName"));
        connectUnit.setIp(params.get("ip"));
        RemoteCmdClient commandExecutor = new RemoteCmdClient(connectUnit);
        NetAccessUtils.blockIPInput(commandExecutor, "124.115.0.199", 60);
    }

    @Test
    public void testPortBlock() {
        Map<String, String> params = ((AbstractElement) this.getTestProject()).getVariables();
        RemoteCmdClientConfig connectUnit = new RemoteCmdClientConfig();
        connectUnit.setPassword(params.get("password"));
        connectUnit.setUserName(params.get("userName"));
        connectUnit.setIp(params.get("ip"));
        RemoteCmdClient commandExecutor = new RemoteCmdClient(connectUnit);
        NetAccessUtils.blockPortProtocol(commandExecutor, "8080", "tcp", 60);
    }


}

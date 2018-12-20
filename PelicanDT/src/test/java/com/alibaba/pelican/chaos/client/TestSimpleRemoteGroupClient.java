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

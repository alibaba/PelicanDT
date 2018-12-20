package com.alibaba.pelican.deployment;

import com.alibaba.pelican.chaos.client.RemoteCmd;
import com.alibaba.pelican.chaos.client.RemoteCmdResult;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.deployment.junit.AbstractJUnit4PelicanTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;



/**
 * @author moyun@middleware
 */
@Ignore
@Slf4j
public class TestApplication extends AbstractJUnit4PelicanTests {

	@Test
	public void test() {
		RemoteCmdClient remoteCmdClient = this.getTestProject().getMachineById("demo1Machine").getRemoteCmdClient();
		RemoteCmd remoteCommand = new RemoteCmd();
		remoteCommand.addCmd("ls -la");
		RemoteCmdResult resultInfo = remoteCmdClient.execCmdWithPTY(remoteCommand);
		log.info(resultInfo.getStdInfo());
		log.info("hello");
	}

}

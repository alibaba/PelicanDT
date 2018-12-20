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

package com.alibaba.pelican.chaos.client.cmd;

import com.alibaba.pelican.chaos.client.cmd.event.CmdEvent;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author moyun@middleware
 */
@Slf4j
public class KillPIDCmdAction extends AbstractCmdAction {

    public static String NAME = CmdConstant.KILL_PROCESS;
    private String prefix = "ps -ef";
    private String suffix = "| grep -v grep | awk '{print $2}' | xargs kill -9";

    public KillPIDCmdAction() {
        super();
        super.cmd = prefix;
    }

    @Override
    public void doAction(CmdEvent event) {
        RemoteCmdClient client = event.getSourceClient();
        String commandStr = super.getExecCmd(event);
        commandStr = commandStr + suffix;
        String result = client.execCmdGetString(RemoteCmdFactory.getCmd(commandStr));
        event.setResult(result);
        event.setSuccessful(true);
    }

    @Override
    public String getActionName() {
        return NAME;
    }

}

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

import com.alibaba.pelican.chaos.client.RemoteCmdResult;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.cmd.event.CmdEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


/**
 * @author moyun@middleware
 */
@Slf4j
public class JpsCmdAction extends AbstractCmdAction {
    public static String NAME = CmdConstant.JPS;
    public static String prefix = "jps";

    public JpsCmdAction() {
        super();
        super.cmd = prefix;
    }

    @Override
    public void doAction(CmdEvent event) {
        boolean start = false;
        Map<String, String> results = new HashMap<String, String>();
        List<String> ress = new ArrayList<String>();
        RemoteCmdClient client = event.getSourceClient();
        RemoteCmdResult result = client.execCmdWithPTY(RemoteCmdFactory.getCmd(getExecCmd(event)));
        String resStr = result.getStdInfo();

        event.setResult(resStr);
    }

    public String getCmd(CmdEvent event) {
        return "";
    }

    private boolean isCmdLine(String item) {
        return StringUtils.isNotBlank(item) && item.contains("[") && item.contains("]");
    }

    @Override
    public String getActionName() {
        return NAME;
    }
}

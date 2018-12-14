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

package com.alibaba.pelican.chaos.client.unit;

import com.alibaba.pelican.chaos.client.RemoteCmd;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

/**
 * @author moyun@middleware
 */
@Slf4j
public final class MemUtils {

    private MemUtils() {

    }

    public synchronized static boolean startMemCtrlAgent(RemoteCmdClient client, String useageString) {
        if (StringUtils.isBlank(useageString)) {
            return false;
        }

        String scriptName = "mem_useage.sh";
        String memPID = client.getPID(scriptName);

        if (!memPID.isEmpty()) {
            log.error("Memory useage agent is running, can not start another one.");
            return false;
        }

        String scriptDir = client.getDefaultDir() + "/" + "scripts/";

        InputStream is = MemUtils.class.getResourceAsStream("/" + scriptName);
        if (!client.createFile(is, scriptName, scriptDir)) {
            return false;
        }

        RemoteCmd command = new RemoteCmd();
        command.addCmd(String.format("cd %s", scriptDir));
        command.addCmd(String.format("chmod +x %s", scriptName));
        command.addCmd(String.format("sudo /bin/sh -c \"nohup ./%s %s 5 once &\"", scriptName, useageString));
        client.execCmdWithPTY(command);
        return true;
    }
}

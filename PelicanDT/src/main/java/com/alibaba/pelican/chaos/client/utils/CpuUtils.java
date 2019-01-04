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

import com.alibaba.pelican.chaos.client.RemoteCmd;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

/**
 * @author moyun@middleware
 */
@Slf4j
public final class CpuUtils {

    private CpuUtils() {

    }

    private static final String CLONE_SCRIPT = "#! /usr/bin/expect\nset srcpath [lindex $argv 0]\nset dstpath [lindex $argv 1]\nset password [lindex $argv 2]\nspawn %s $srcpath $dstpath\nexpect {\n\"yes/no\" { send \"yes\\r\"; exp_continue}\n\"password:\" { send \"$password\\r\"}\n}\nexpect eof\nexit";

    public synchronized static boolean adjustCpuUsage(RemoteCmdClient client, int percent, int delayMinutes) {
        String useageString = percent + ":" + delayMinutes;
        if (StringUtils.equals(useageString, "0:0")) {
            return false;
        }

        String scriptName = "cpu.sh";
        String cpuPID = client.getPID(scriptName);

        if (StringUtils.isNotBlank(cpuPID)) {
            log.error("CPU usage agent is running, can not start another one.");
            return false;
        }

        String scriptDir = client.getDefaultDir() + "/" + "scripts/";

        InputStream is = CpuUtils.class.getResourceAsStream("/" + scriptName);
        if (!client.createFile(is, scriptName, scriptDir)) {
            return false;
        }

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("cd %s", scriptDir));
        remoteCmd.addCmd(String.format("dos2unix %s", scriptName));
        remoteCmd.addCmd(String.format("chmod +x %s", scriptName));
        remoteCmd.addCmd(String.format("nohup ./%s %s 1 once &", scriptName, useageString));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

}

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
import com.alibaba.pelican.chaos.client.RemoteCmdResult;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author moyun@middleware
 */
@Slf4j
public class NetTrafficUtils {

    private String nicName;

    private void getNICName(RemoteCmdClient client) {
        String ip = client.getIp();
        String cmdString = "for nic in `/sbin/ifconfig | grep encap | awk -F \" \" '{ print $1 }'`; do if [ ! -z \"`/sbin/ifconfig $nic | grep -w %s`\" ]; then echo $nic; fi; done";
        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format(cmdString, ip));
        RemoteCmdResult result = client.execCmdWithPTY(remoteCmd);
        String stdInfo = result.getStdInfo();
        if (stdInfo.equals("")) {
            cmdString = "for nic in `/sbin/ifconfig | grep flags | awk -F \":\" '{ print $1 }'`; do if [ ! -z \"`/sbin/ifconfig $nic | grep -w %s`\" ]; then echo $nic; fi; done";
            RemoteCmd remoteCmd2 = new RemoteCmd();
            remoteCmd2.addCmd(String.format(cmdString, ip));
            result = client.execCmdWithPTY(remoteCmd2);
            stdInfo = result.getStdInfo();
        }
        this.nicName = stdInfo.replaceAll("\n", "").replaceAll("\r", "");
    }

    private boolean doSetNetWorkDelay(RemoteCmdClient client, int milliseconds, int delayMinute, int triggerMinute) {
        if (milliseconds > 10000 || milliseconds <= 0 || delayMinute < 0 || triggerMinute < 0) {
            log.error("The value is invalid, valid range is (0, 10000]");
            return false;
        }
        if (nicName == null) {
            getNICName(client);
        }

        String cmdString = String.format("*/1 * * * * crontab -u root -r; sleep %d; "
                        + "/sbin/tc qdisc add dev %s root netem delay %dms;"
                        + "if [ \\$? -ne 0 ]; "
                        + "then /sbin/tc qdisc change dev %s root netem delay %dms;"
                        + "fi;"
                        + "sleep %d; /sbin/tc qdisc del dev %s root;",
                triggerMinute * 60, nicName, milliseconds,
                nicName, milliseconds, delayMinute * 60, nicName);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/task", cmdString));
        remoteCmd.addCmd("sudo crontab -u root -r");
        remoteCmd.addCmd(String.format("sudo crontab task"));
        remoteCmd.addCmd("rm -rf ~/task");
        client.execCmdWithPTY(remoteCmd);

        log.info(String.format("Set network delay to %dms, dalay time %s minutes, trigger after %d minutes.",
                milliseconds, delayMinute, triggerMinute));
        return true;
    }

    private boolean doSetNetWorkDelay(RemoteCmdClient client, int milliseconds, int delaySecond) {
        if (milliseconds > 10000 || milliseconds <= 0 || delaySecond < 0) {
            log.error("The value is invalid, valid range is (0, 10000]");
            return false;
        }
        if (nicName == null) {
            getNICName(client);
        }

        String cmdString = String.format("#\\!/bin/sh\n"
                        + "function action()\n"
                        + "{\n"
                        + "/sbin/tc qdisc add dev %s root netem delay %dms\n"
                        + "if [ \\$? -ne 0 ]\n"
                        + "then\n"
                        + "/sbin/tc qdisc change dev %s root netem delay %dms\n"
                        + "fi\n"
                        + "sleep %d\n"
                        + "/sbin/tc qdisc del dev %s root\n"
                        + "}\n"
                        + "action &",
                nicName, milliseconds,
                nicName, milliseconds, delaySecond, nicName);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/task.sh", cmdString));
        remoteCmd.addCmd(String.format("chmod +x ~/task.sh"));
        remoteCmd.addCmd(String.format("sudo ~/task.sh"));
        remoteCmd.addCmd("rm -rf ~/task.sh");
        client.execCmdWithPTY(remoteCmd);

        log.info(String.format("Set network delay to %dms, dalay time %s seconds.",
                milliseconds, delaySecond));
        return true;
    }

    private boolean doSetPackageLoss(RemoteCmdClient client, int percent, int delayMinute, int triggerMinute) {
        if (percent > 50 || percent <= 0 || delayMinute < 0 || triggerMinute < 0) {
            log.error("The value is invalid, valid range is (0%, 50%]");
            return false;
        }
        if (nicName == null) {
            getNICName(client);
        }

        String cmdString = String.format("*/1 * * * * crontab -u root -r; sleep %d; "
                        + "/sbin/tc qdisc add dev %s root netem loss %d;"
                        + "if [ \\$? -ne 0 ]; "
                        + "then /sbin/tc qdisc change dev %s root netem loss %d;"
                        + "fi;"
                        + "sleep %d; /sbin/tc qdisc del dev %s root;",
                triggerMinute * 60, nicName, percent,
                nicName, percent, delayMinute * 60, nicName);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/task", cmdString));
        remoteCmd.addCmd("sudo crontab -u root -r");
        remoteCmd.addCmd(String.format("sudo crontab task"));
        remoteCmd.addCmd("rm -rf ~/task");
        client.execCmdWithPTY(remoteCmd);

        log.info(String.format("Set package loss to %d%%, dalay time %s minutes, trigger after %d minutes.",
                percent, delayMinute, triggerMinute));
        return true;
    }

    private boolean doSetPackageLoss(RemoteCmdClient client, int percent, int delaySecond) {
        if (percent > 50 || percent <= 0 || delaySecond < 0) {
            log.error("The value is invalid, valid range is (0%, 50%]");
            return false;
        }
        if (nicName == null) {
            getNICName(client);
        }

        String cmdString = String.format("#\\!/bin/bash\n"
                        + "function action()\n"
                        + "{\n"
                        + "/sbin/tc qdisc add dev %s root netem loss %d\n"
                        + "if [ \\$? -ne 0 ]\n"
                        + "then \n"
                        + "/sbin/tc qdisc change dev %s root netem loss %d\n"
                        + "fi\n"
                        + "sleep %d\n"
                        + "/sbin/tc qdisc del dev %s root\n"
                        + "}\n"
                        + "action &",
                nicName, percent,
                nicName, percent, delaySecond, nicName);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/task2.sh", cmdString));
        remoteCmd.addCmd("chmod +x ~/task2.sh");
        remoteCmd.addCmd(String.format("sudo ~/task2.sh"));
        remoteCmd.addCmd("rm -rf ~/task2.sh");
        client.execCmdWithPTY(remoteCmd);

        log.info(String.format("Set package loss to %d%%, dalay time %s second.",
                percent, delaySecond));
        return true;
    }

    public boolean setPackageLoss(RemoteCmdClient client, int percent, int delayMinute) {

        return doSetPackageLoss(client, percent, delayMinute, 0);
    }

    public boolean setPackageLoss(RemoteCmdClient client, int percent, int delayMinute, int triggerMinute) {

        return doSetPackageLoss(client, percent, delayMinute, triggerMinute);
    }

    public boolean setPackageLossInSecond(RemoteCmdClient client, int percent, int delaySecond) {

        return doSetPackageLoss(client, percent, delaySecond);
    }

    public boolean setNetworkDelay(RemoteCmdClient client, int milliseconds, int delayMinute) {

        return doSetNetWorkDelay(client, milliseconds, delayMinute, 0);
    }

    public boolean setNetworkDelay(RemoteCmdClient client, int milliseconds, int delayMinute, int triggerMinute) {

        return doSetNetWorkDelay(client, milliseconds, delayMinute, triggerMinute);
    }

    public boolean setNetworkDelayInSeconds(RemoteCmdClient client, int milliseconds, int delaySecond) {

        return doSetNetWorkDelay(client, milliseconds, delaySecond);
    }

    public void clearNetworkDelay(RemoteCmdClient client) {
        clearTrafficCtrl(client);
        log.info(String.format("Clear network delay."));
        return;
    }

    public void clearPackageLoss(RemoteCmdClient client) {
        clearTrafficCtrl(client);
        log.info(String.format("Clear package loss."));
        return;
    }

    private void clearTrafficCtrl(RemoteCmdClient client) {
        if (nicName == null) {
            getNICName(client);
        }
        String cmdString = "sudo /sbin/tc qdisc del dev %s root";
        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format(cmdString, nicName));
        for (int i = 0; i < 10; i++) {
            client.execCmdWithPTY(remoteCmd);
        }
        return;
    }


}

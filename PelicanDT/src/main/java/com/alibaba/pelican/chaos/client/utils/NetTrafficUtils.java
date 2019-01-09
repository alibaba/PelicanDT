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

    private static boolean doSetNetWorkDelay(RemoteCmdClient client, String networkCard, int delayTime, int delaySecond, int triggerSecond) {
        if (delayTime > 10000 || delayTime <= 0 || delaySecond < 0) {
            log.error("The value is invalid, valid range is (0, 10000]");
            return false;
        }

        String cmdString = String.format("#\\!/bin/sh\n"
                        + "function action()\n"
                        + "{\n"
                        + "sleep %d\n"
                        + "/sbin/tc qdisc add dev %s root netem delay %dms\n"
                        + "if [ \\$? -ne 0 ]\n"
                        + "then\n"
                        + "/sbin/tc qdisc change dev %s root netem delay %dms\n"
                        + "fi\n"
                        + "sleep %d\n"
                        + "/sbin/tc qdisc del dev %s root\n"
                        + "}\n"
                        + "action &",
                triggerSecond, networkCard, delayTime,
                networkCard, delayTime, delaySecond, networkCard);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/task.sh", cmdString));
        remoteCmd.addCmd(String.format("chmod +x ~/task.sh"));
        remoteCmd.addCmd(String.format("sudo ~/task.sh"));
        remoteCmd.addCmd("rm -rf ~/task.sh");
        RemoteCmdResult remoteCmdResult = client.execCmdWithPTY(remoteCmd);
        log.info(remoteCmdResult.getStdInfo());
        log.info(String.format("Set network delay to %dms, dalay time %s seconds.",
                delayTime, delaySecond));
        return true;
    }

    private static boolean doSetPackageLoss(RemoteCmdClient client, String networkCard, int percent, int delaySecond, int triggerSecond) {
        if (percent > 50 || percent <= 0 || delaySecond < 0) {
            log.error("The value is invalid, valid range is (0%, 50%]");
            return false;
        }

        String cmdString = String.format("#\\!/bin/bash\n"
                        + "function action()\n"
                        + "{\n"
                        + "sleep %d;\n"
                        + "/sbin/tc qdisc add dev %s root netem loss %d\n"
                        + "if [ \\$? -ne 0 ]\n"
                        + "then \n"
                        + "/sbin/tc qdisc change dev %s root netem loss %d\n"
                        + "fi\n"
                        + "sleep %d\n"
                        + "/sbin/tc qdisc del dev %s root\n"
                        + "}\n"
                        + "action &",
                triggerSecond, networkCard, percent,
                networkCard, percent, delaySecond, networkCard);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/task2.sh", cmdString));
        remoteCmd.addCmd("chmod +x ~/task2.sh");
        remoteCmd.addCmd(String.format("sudo ~/task2.sh"));
        remoteCmd.addCmd("rm -rf ~/task2.sh");
        RemoteCmdResult remoteCmdResult = client.execCmdWithPTY(remoteCmd);
        log.info(remoteCmdResult.getStdInfo());
        log.info(String.format("Set package loss to %d%%, dalay time %s second.",
                percent, delaySecond));
        return true;
    }

    /**
     * default networkCard: eth0
     */
    public static boolean setPackageLoss(RemoteCmdClient client, int percent, int delaySecond) {

        return setPackageLoss(client, "eth0", percent, delaySecond);
    }

    public static boolean setPackageLoss(RemoteCmdClient client, String networkCard, int percent, int delaySecond) {

        return setPackageLoss(client, networkCard, percent, delaySecond, 0);
    }

    public static boolean setPackageLoss(RemoteCmdClient client, String networkCard, int percent, int delaySecond, int triggerSecond) {
        return doSetPackageLoss(client, networkCard, percent, delaySecond, triggerSecond);
    }

    public static boolean setPackageLoss(RemoteCmdClient client, int percent, int delaySecond, int triggerSecond) {

        return setPackageLoss(client, "eth0", percent, delaySecond, triggerSecond);
    }

    public static boolean setNetworkDelay(RemoteCmdClient client, int delayTime, int delaySecond) {

        return setNetworkDelay(client, "eth0", delayTime, delaySecond);
    }

    public static boolean setNetworkDelay(RemoteCmdClient client, String networkCard, int delayTime, int delaySecond) {

        return setNetworkDelay(client, networkCard, delayTime, delaySecond, 0);
    }

    public static boolean setNetworkDelay(RemoteCmdClient client, int delayTime, int delaySecond, int triggerSecond) {
        return setNetworkDelay(client, "eth0", delayTime, delaySecond, triggerSecond);
    }

    public static boolean setNetworkDelay(RemoteCmdClient client, String networkCard, int delayTime, int delaySecond, int triggerSecond) {

        return doSetNetWorkDelay(client, networkCard, delayTime, delaySecond, triggerSecond);
    }

    public static void clearNetworkDelay(RemoteCmdClient client) {
        clearNetTraffic(client, "eth0");
        log.info(String.format("Clear network delay."));
        return;
    }

    public static void clearNetworkDelay(RemoteCmdClient client, String networkCard) {
        clearNetTraffic(client, networkCard);
        log.info(String.format("Clear network delay."));
        return;
    }

    public static void clearPackageLoss(RemoteCmdClient client) {
        clearNetTraffic(client, "eth0");
        log.info(String.format("Clear package loss."));
        return;
    }

    public static void clearPackageLoss(RemoteCmdClient client, String networkCard) {
        clearNetTraffic(client, networkCard);
        log.info(String.format("Clear package loss."));
        return;
    }

    private static void clearNetTraffic(RemoteCmdClient client, String networkCard) {
        String cmdString = "sudo /sbin/tc qdisc del dev %s root";
        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format(cmdString, networkCard));
        for (int i = 0; i < 10; i++) {
            client.execCmdWithPTY(remoteCmd);
        }
        return;
    }


}

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

import java.util.Arrays;
import java.util.List;

/**
 * @author moyun@middleware
 */
@Slf4j
public class NetAccessUtils {

    private static final int INPUT = 1;
    private static final int OUTPUT = 2;
    private static final int ALL = 3;

    private static boolean doBlockIP(RemoteCmdClient client, List<String> ipList, int delaySecond, int triggerSecond, int type) {
        if (delaySecond < 0) {
            log.error("The delaySecond value is invalid.");
            return false;
        }

        String cmd = "";
        if (type >= OUTPUT) {
            for (String ip : ipList) {
                cmd += String.format("/sbin/iptables -A OUTPUT -d %s -j DROP;\n", ip);
            }
        }
        if (type == INPUT || type == ALL) {
            for (String ip : ipList) {
                cmd += String.format("/sbin/iptables -A INPUT -s %s -j DROP;\n", ip);
            }
        }

        String cmdString = String.format("#\\!/bin/sh\n function action() {\nsleep %d;\n %s \n sleep %d;\n /sbin/iptables -F \n}\n action &",
                triggerSecond, cmd, delaySecond);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/block_ip_task.sh", cmdString));
        remoteCmd.addCmd(String.format("chmod +x ~/block_ip_task.sh"));
        remoteCmd.addCmd(String.format("sudo ~/block_ip_task.sh"));
        remoteCmd.addCmd("rm -rf ~/block_ip_task.sh");
        RemoteCmdResult remoteCmdResult = client.execCmdWithPTY(remoteCmd);
        log.info(remoteCmdResult.getStdInfo());
        for (String ip : ipList) {
            log.info(String.format("Block ip %s, dalay time %s seconds, "
                            + "type %d.",
                    ip, delaySecond, type));
        }
        return true;
    }

    private static boolean doBlockPort(RemoteCmdClient client, String port, String protcol, int delaySecond, int triggerSecond) {
        if (delaySecond < 0) {
            log.error("The delaySecond value is invalid.");
            return false;
        }

        String cmd = "";
        cmd += String.format("sudo /sbin/iptables -A INPUT -p %s --dport %s -j DROP;\n", protcol, port);
        cmd += String.format("sudo /sbin/iptables -A OUTPUT -p %s --sport %s -j DROP;\n", protcol, port);
        String cmdString = String.format("#\\!/bin/sh\n function action()\n {\n sleep %d;\n %s \n sleep %d;\n /sbin/iptables -F\n }\n action &",
                triggerSecond, cmd, delaySecond);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/block_port_task.sh", cmdString));
        remoteCmd.addCmd(String.format("chmod +x ~/block_port_task.sh"));
        remoteCmd.addCmd(String.format("sudo ~/block_port_task.sh"));
        remoteCmd.addCmd("rm -rf ~/block_port_task.sh");
        RemoteCmdResult remoteCmdResult =client.execCmdWithPTY(remoteCmd);
        log.info(remoteCmdResult.getStdInfo());
        log.info(String.format("Block port %s protcol %s, dalay time %d seconds.", port, protcol, delaySecond));
        return true;
    }

    public static boolean blockIP(RemoteCmdClient client, String ip, int delaySecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, 0, ALL);
    }

    public static boolean blockIPInput(RemoteCmdClient client, String ip, int delaySecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, 0, INPUT);
    }

    public static boolean blockIPInput(RemoteCmdClient client, List<String> ipList, int delaySecond) {

        return doBlockIP(client, ipList, delaySecond, 0, INPUT);
    }

    public static boolean blockIPOutput(RemoteCmdClient client, String ip, int delaySecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, 0, OUTPUT);
    }

    public static boolean blockIPOutput(RemoteCmdClient client, List<String> ipList, int delaySecond) {

        return doBlockIP(client, ipList, delaySecond, 0, OUTPUT);
    }

    public static boolean blockIP(RemoteCmdClient client, String ip, int delaySecond, int triggerSecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, triggerSecond, ALL);
    }

    public static boolean blockIP(RemoteCmdClient client, List<String> ipList, int delaySecond, int triggerSecond) {

        return doBlockIP(client, ipList, delaySecond, triggerSecond, ALL);
    }

    public static boolean blockIPInput(RemoteCmdClient client, String ip, int delaySecond, int triggerSecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, triggerSecond, INPUT);
    }

    public static boolean blockIPInput(RemoteCmdClient client, List<String> ipList, int delaySecond, int triggerSecond) {

        return doBlockIP(client, ipList, delaySecond, triggerSecond, INPUT);
    }

    public static boolean blockIPOutput(RemoteCmdClient client, String ip, int delaySecond, int triggerSecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, triggerSecond, OUTPUT);
    }

    public static boolean blockIPOutput(RemoteCmdClient client, List<String> ipList, int delaySecond, int triggerSecond) {

        return doBlockIP(client, ipList, delaySecond, triggerSecond, OUTPUT);
    }

    public static boolean blockPortProtocol(RemoteCmdClient client, String port, String protcol, int delaySecond) {

        return doBlockPort(client, port, protcol, delaySecond, 0);
    }

    public static boolean blockPortProtocol(RemoteCmdClient client, String port, String protcol, int delaySecond, int triggerSecond) {

        return doBlockPort(client, port, protcol, delaySecond, triggerSecond);
    }

    public static boolean unblockIP(RemoteCmdClient client, String ip) {

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D OUTPUT -d %s -j DROP", ip));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D INPUT -s %s -j DROP", ip));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

    public static boolean unblockIPInput(RemoteCmdClient client, String ip) {

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D INPUT -s %s -j DROP", ip));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

    public static boolean unblockIPOutput(RemoteCmdClient client, String ip) {

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D OUTPUT -d %s -j DROP", ip));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

    public static boolean unblockPortProtcol(RemoteCmdClient client, String port, String protcol) {
        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D INPUT -p %s --dport %s -j DROP",
                port, protcol));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D OUTPUT -p %s --sport %s -j DROP",
                port, protcol));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

    public static void clearAll(RemoteCmdClient client) {
        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -F"));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return;
    }

}

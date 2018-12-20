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

    private boolean doBlockIP(RemoteCmdClient client, List<String> ipList, int delayMinute, int triggerMinute, int type) {
        if (delayMinute < 0 || triggerMinute < 0) {
            log.error("The delayMinute or triggerMinute value is invalid.");
            return false;
        }

        String cmd = "";
        if (type >= OUTPUT) {
            for (String ip : ipList) {
                cmd += String.format("/sbin/iptables -A OUTPUT -d %s -j DROP;", ip);
            }
        }
        if (type == INPUT || type == ALL) {
            for (String ip : ipList) {
                cmd += String.format("/sbin/iptables -A INPUT -s %s -j DROP;", ip);
            }
        }
        String cmdString = String.format("*/1 * * * * crontab -u root -r; sleep %d; %s"
                        + "sleep %d; /sbin/iptables -F",
                triggerMinute * 60, cmd, delayMinute * 60);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/task", cmdString));
        remoteCmd.addCmd("sudo crontab -u root -r");
        remoteCmd.addCmd(String.format("sudo crontab task"));
        remoteCmd.addCmd("rm -rf ~/task");
        client.execCmdWithPTY(remoteCmd);
        for (String ip : ipList) {
            log.info(String.format("Block ip %s, dalay time %s minutes, "
                            + "trigger after %d minutes, type %d.",
                    ip, delayMinute, triggerMinute, type));
        }
        return true;
    }

    private boolean doBlockIP(RemoteCmdClient client, List<String> ipList, int delaySecond, int type) {
        if (delaySecond < 0) {
            log.error("The delaySecond value is invalid.");
            return false;
        }

        String cmd = "";
        if (type >= OUTPUT) {
            for (String ip : ipList) {
                cmd += String.format("/sbin/iptables -A OUTPUT -d %s -j DROP;", ip);
            }
        }
        if (type == INPUT || type == ALL) {
            for (String ip : ipList) {
                cmd += String.format("/sbin/iptables -A INPUT -s %s -j DROP;", ip);
            }
        }

        String cmdString = String.format("#\\!/bin/sh\n"
                        + "function action()\n"
                        + "{\n"
                        + cmd
                        + "sleep %d\n"
                        + "/sbin/iptables -F\n"
                        + "}\n"
                        + "action &",
                delaySecond);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/block_ip_task.sh", cmdString));
        remoteCmd.addCmd(String.format("chmod +x ~/block_ip_task.sh"));
        remoteCmd.addCmd(String.format("sudo ~/block_ip_task.sh"));
        remoteCmd.addCmd("rm -rf ~/block_ip_task.sh");
        client.execCmdWithPTY(remoteCmd);

        for (String ip : ipList) {
            log.info(String.format("Block ip %s, dalay time %s seconds, "
                            + "type %d.",
                    ip, delaySecond, type));
        }
        return true;
    }

    private boolean doBlockPort(RemoteCmdClient client, String port, String protcol, int delayMinute, int triggerMinute) {
        if (delayMinute < 0 || triggerMinute < 0) {
            log.error("The delayMinute or triggerMinute value is invalid.");
            return false;
        }

        String cmd = "";
        cmd += String.format("sudo /sbin/iptables -A INPUT -p %s --dport %s -j DROP",
                protcol, port);
        cmd += String.format("sudo /sbin/iptables -A OUTPUT -p %s --sport %s -j DROP",
                protcol, port);
        String cmdString = String.format("*/1 * * * * crontab -u root -r; sleep %d; %s"
                        + "sleep %d; /sbin/iptables -F",
                triggerMinute * 60, cmd, delayMinute * 60);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/task", cmdString));
        remoteCmd.addCmd("sudo crontab -u root -r");
        remoteCmd.addCmd(String.format("sudo crontab task"));
        remoteCmd.addCmd("rm -rf ~/task");
        client.execCmdWithPTY(remoteCmd);
        log.info(String.format("Block port %s protcol %s, dalay time %d minutes, "
                        + "trigger after %d minutes.",
                port, protcol, delayMinute, triggerMinute));
        return true;
    }

    private boolean doBlockPort(RemoteCmdClient client, String port, String protcol, int delaySecond) {
        if (delaySecond < 0) {
            log.error("The delaySecond value is invalid.");
            return false;
        }


        String cmd = "";
        cmd += String.format("sudo /sbin/iptables -A INPUT -p %s --dport %s -j DROP",
                protcol, port);
        cmd += String.format("sudo /sbin/iptables -A OUTPUT -p %s --sport %s -j DROP",
                protcol, port);
        String cmdString = String.format("#\\!/bin/sh\n"
                        + "function action()\n"
                        + "{\n"
                        + cmd
                        + "sleep %d\n"
                        + "/sbin/iptables -F\n"
                        + "}\n"
                        + "action &",
                delaySecond);

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("echo \"%s\" > ~/block_port_task.sh", cmdString));
        remoteCmd.addCmd(String.format("chmod +x ~/block_port_task.sh"));
        remoteCmd.addCmd(String.format("sudo ~/block_port_task.sh"));
        remoteCmd.addCmd("rm -rf ~/block_port_task.sh");
        client.execCmdWithPTY(remoteCmd);
        log.info(String.format("Block port %s protcol %s, dalay time %d seconds.",
                port, protcol, delaySecond));
        return true;
    }

    public boolean blockIP(RemoteCmdClient client, String ip, int delayMinute) {

        return doBlockIP(client, Arrays.asList(ip), delayMinute, 0, ALL);
    }

    public boolean blockIPInSeconds(RemoteCmdClient client, String ip, int delaySecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, ALL);
    }

    public boolean blockIP(RemoteCmdClient client, List<String> ipList, int delayMinute) {

        return doBlockIP(client, ipList, delayMinute, 0, ALL);
    }

    public boolean blockIPInSeconds(RemoteCmdClient client, List<String> ipList, int delaySecond) {

        return doBlockIP(client, ipList, delaySecond, ALL);
    }

    public boolean blockIPInput(RemoteCmdClient client, String ip, int delayMinute) {

        return doBlockIP(client, Arrays.asList(ip), delayMinute, 0, INPUT);
    }

    public boolean blockIPInputInSeconds(RemoteCmdClient client, String ip, int delaySecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, INPUT);
    }

    public boolean blockIPInput(RemoteCmdClient client, List<String> ipList, int delayMinute) {

        return doBlockIP(client, ipList, delayMinute, 0, INPUT);
    }

    public boolean blockIPInputInSeconds(RemoteCmdClient client, List<String> ipList, int delaySecond) {

        return doBlockIP(client, ipList, delaySecond, INPUT);
    }

    public boolean blockIPOutput(RemoteCmdClient client, String ip, int delayMinute) {

        return doBlockIP(client, Arrays.asList(ip), delayMinute, 0, OUTPUT);
    }

    public boolean blockIPOutputInSeconds(RemoteCmdClient client, String ip, int delaySecond) {

        return doBlockIP(client, Arrays.asList(ip), delaySecond, OUTPUT);
    }

    public boolean blockIPOutput(RemoteCmdClient client, List<String> ipList, int delayMinute) {

        return doBlockIP(client, ipList, delayMinute, 0, OUTPUT);
    }

    public boolean blockIPOutputInSeconds(RemoteCmdClient client, List<String> ipList, int delaySecond) {

        return doBlockIP(client, ipList, delaySecond, OUTPUT);
    }

    public boolean blockIP(RemoteCmdClient client, String ip, int delayMinute, int triggerMinute) {

        return doBlockIP(client, Arrays.asList(ip), delayMinute, triggerMinute, ALL);
    }

    public boolean blockIP(RemoteCmdClient client, List<String> ipList, int delayMinute, int triggerMinute) {

        return doBlockIP(client, ipList, delayMinute, triggerMinute, ALL);
    }

    public boolean blockIPInput(RemoteCmdClient client, String ip, int delayMinute, int triggerMinute) {

        return doBlockIP(client, Arrays.asList(ip), delayMinute, triggerMinute, INPUT);
    }

    public boolean blockIPInput(RemoteCmdClient client, List<String> ipList, int delayMinute, int triggerMinute) {

        return doBlockIP(client, ipList, delayMinute, triggerMinute, INPUT);
    }

    public boolean blockIPOutput(RemoteCmdClient client, String ip, int delayMinute, int triggerMinute) {

        return doBlockIP(client, Arrays.asList(ip), delayMinute, triggerMinute, OUTPUT);
    }

    public boolean blockIPOutput(RemoteCmdClient client, List<String> ipList, int delayMinute, int triggerMinute) {

        return doBlockIP(client, ipList, delayMinute, triggerMinute, OUTPUT);
    }

    public boolean blockPortProtcol(RemoteCmdClient client, String port, String protcol, int delayMinute) {

        return doBlockPort(client, port, protcol, delayMinute, 0);
    }

    public boolean blockPortProtcolInSeconds(RemoteCmdClient client, String port, String protcol, int delaySecond) {

        return doBlockPort(client, port, protcol, delaySecond);
    }

    public boolean blockPortProtcol(RemoteCmdClient client, String port, String protcol, int delayMinute, int triggerMinute) {

        return doBlockPort(client, port, protcol, delayMinute, triggerMinute);
    }

    public boolean unblockIP(RemoteCmdClient client, String ip) {

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D OUTPUT -d %s -j DROP", ip));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D INPUT -s %s -j DROP", ip));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

    public boolean unblockIPInput(RemoteCmdClient client, String ip) {

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D INPUT -s %s -j DROP", ip));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

    public boolean unblockIPOutput(RemoteCmdClient client, String ip) {

        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D OUTPUT -d %s -j DROP", ip));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

    public boolean unblockPortProtcol(RemoteCmdClient client, String port, String protcol) {
        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D INPUT -p %s --dport %s -j DROP",
                port, protcol));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -D OUTPUT -p %s --sport %s -j DROP",
                port, protcol));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return true;
    }

    public void clearAll(RemoteCmdClient client) {
        RemoteCmd remoteCmd = new RemoteCmd();
        remoteCmd.addCmd(String.format("sudo /sbin/iptables -F"));
        remoteCmd.addCmd(String.format("sudo /sbin/iptables-save"));
        client.execCmdWithPTY(remoteCmd);
        return;
    }

}

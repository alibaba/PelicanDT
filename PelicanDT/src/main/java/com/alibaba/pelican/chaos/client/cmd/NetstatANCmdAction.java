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

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.cmd.event.CmdEvent;
import com.alibaba.pelican.chaos.client.dto.NetstatInternetDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author moyun@middleware
 */
@Slf4j
public class NetstatANCmdAction extends AbstractCmdAction {

    public static String NAME = CmdConstant.NETSTAT_AN_INTERNET;

    private static String INTERNET_TITLE = "Internet connections";

    private static String SOCKETS_TITLE = "UNIX domain sockets";

    private String prefix = "netstat -an";

    public NetstatANCmdAction() {
        super();
        super.cmd = prefix;
    }

    @Override
    public void doAction(CmdEvent event) {
        List<NetstatInternetDto> resultList = null;
        RemoteCmdClient client = event.getSourceClient();
        String resStr = client.execCmdGetString(RemoteCmdFactory.getCmd(super.getExecCmd(event)));
        Set<String> conds = event.getParams().getGrepConditions();
        if (conds == null || conds.size() == 0) {
            resultList = parseStringWithoutGrep(resStr);
        } else {
            resultList = parseStringWithGrep(resStr);
        }
        event.setResult(resultList);
    }

    private List<NetstatInternetDto> parseStringWithoutGrep(String resStr) {
        boolean start = false;
        List<NetstatInternetDto> resultList = new ArrayList<NetstatInternetDto>();
        List<String> ress = new ArrayList<String>();
        String items[] = resStr.split("\r\n");
        for (int i = 0; i < items.length; i++) {
            if (isSocketTitleLine(items[i])) {
                break;
            }
            if (start) {
                ress.add(items[i]);
            }
            if (isInternetTitleLine(items[i])) {
                start = true;
            }
        }
        convertLineToNetstatInternetUnit(ress, resultList);
        return resultList;
    }

    private List<NetstatInternetDto> parseStringWithGrep(String resStr) {
        List<NetstatInternetDto> resultList = new ArrayList<NetstatInternetDto>();
        List<String> ress = new ArrayList<String>();
        String items[] = resStr.split("\r\n");
        for (int i = 0; i < items.length; i++) {
            if (isInternetLine(items[i])) {
                ress.add(items[i]);
            }
        }
        convertLineToNetstatInternetUnit(ress, resultList);
        return resultList;
    }

    private void convertLineToNetstatInternetUnit(List<String> ress,
                                                  List<NetstatInternetDto> resultList) {
        for (int i = 0; i < ress.size(); i++) {
            if (!isInternetLine(ress.get(i))) {
                continue;
            }
            NetstatInternetDto unit = new NetstatInternetDto();
            String protocol = ress.get(i).substring(0, 6).trim();
            unit.setProtocol(protocol);
            String recv = ress.get(i).substring(6, 13).trim();
            unit.setRecvQueue(Integer.parseInt(recv));
            String send = ress.get(i).substring(13, 20).trim();
            unit.setSendQueue(Integer.parseInt(send));
            String localAddress = ress.get(i).substring(20, 48).trim();
            unit.setLocalAddress(localAddress);
            String foreignAddress = ress.get(i).substring(48, 76).trim();
            unit.setForeignAddress(foreignAddress);
            String state = ress.get(i).substring(76, 88).trim();
            unit.setState(state);
            String pidStr = ress.get(i).substring(88, ress.get(i).length()).trim();
            if (pidStr.equals("-") || pidStr.equals("")) {
                unit.setPid("-");
                unit.setProgramName("-");
            } else {
                int index = pidStr.indexOf("/");
                unit.setPid(pidStr.substring(0, index));
                unit.setProgramName(pidStr.substring(index + 1, pidStr.length()));
            }
            resultList.add(unit);
        }
    }

    private boolean isInternetLine(String line) {
        boolean res = true;
        String sub = line.substring(13, 19);
        try {
            Integer.parseInt(sub.trim());
        } catch (NumberFormatException e) {
            res = false;
        }
        return res;
    }

    private boolean isInternetTitleLine(String line) {
        return line.contains(INTERNET_TITLE);
    }

    private boolean isSocketTitleLine(String line) {
        return line.contains(SOCKETS_TITLE);
    }

    @Override
    public String getActionName() {
        return NAME;
    }
}

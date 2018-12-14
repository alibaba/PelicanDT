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
import com.alibaba.pelican.chaos.client.dto.NetstatSocketDto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author moyun@middleware
 */
public class NetstatSocketCmdAction extends AbstractCmdAction {

    public static String NAME = CmdConstant.NETSTAT_SOCKET;

    private String prefix = "netstat -lnp";

    public static String SOCKETS_TITLE = "UNIX domain sockets";

    public NetstatSocketCmdAction() {
        super();
        super.cmd = prefix;
    }

    @Override
    public void doAction(CmdEvent event) {
        boolean start = false;
        List<NetstatSocketDto> resultList = new ArrayList<NetstatSocketDto>();
        List<String> ress = new ArrayList<String>();
        RemoteCmdClient client = event.getSourceClient();
        String resStr = client.execCmdGetString(RemoteCmdFactory.getCmd(super.cmd));
        String items[] = resStr.split("\r\n");
        for (int i = 0; i < items.length; i++) {
            if (start) {
                ress.add(items[i]);
            }
            if (isSocketTitleLine(items[i])) {
                start = true;
            }
        }
        for (int i = 1; i < ress.size(); i++) {
            NetstatSocketDto unit = new NetstatSocketDto();
            String protocol = ress.get(i).substring(0, 6).trim();
            unit.setProtocol(protocol);
            String refCnt = ress.get(i).substring(6, 13).trim();
            unit.setRefCnt(Integer.parseInt(refCnt));
            String flags = ress.get(i).substring(13, 25).trim();
            unit.setFlags(flags);
            String type = ress.get(i).substring(25, 36).trim();
            unit.setType(type);
            String state = ress.get(i).substring(36, 50).trim();
            unit.setState(state);
            resultList.add(unit);
        }
        event.setResult(resultList);
    }

    private boolean isSocketTitleLine(String line) {
        return line.contains(SOCKETS_TITLE);
    }

    @Override
    public String getActionName() {
        return NAME;
    }
}

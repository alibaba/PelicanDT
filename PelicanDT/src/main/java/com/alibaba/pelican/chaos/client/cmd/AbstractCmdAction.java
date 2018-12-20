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
import com.alibaba.pelican.chaos.client.cmd.event.CmdParameter;

import java.util.Iterator;
import java.util.Set;

/**
 * @author moyun@middleware
 */
public abstract class AbstractCmdAction implements CmdAction {

    protected String cmd;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public abstract void doAction(CmdEvent event);

    @Override
    public abstract String getActionName();

    @Override
    public String getExecCmd(CmdEvent event) {
        CmdParameter cmdParameter = event.getParams();
        Set<String> conditions = cmdParameter.getGrepConditions();
        StringBuilder sb = new StringBuilder(cmd);
        for (Iterator<String> iterator = conditions.iterator(); iterator.hasNext(); ) {
            String condition = iterator.next();
            sb.append("|grep ").append(condition);
        }
        return sb.toString();
    }
}

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

import com.alibaba.pelican.chaos.client.RemoteCmd;

/**
 * @author moyun@middleware
 */
public class RemoteCmdFactory {

    public static RemoteCmd getCmd(String cmd) {
        RemoteCmd command = new RemoteCmd();
        command.addCmd(cmd);
        return command;
    }

    public static RemoteCmd getCmd(String... cmds) {
        RemoteCmd command = new RemoteCmd();
        for (int i = 0; i < cmds.length; i++) {
            command.addCmd(cmds[i]);
        }
        return command;
    }
}

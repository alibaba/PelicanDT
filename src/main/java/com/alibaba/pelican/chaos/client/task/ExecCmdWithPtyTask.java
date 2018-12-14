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

package com.alibaba.pelican.chaos.client.task;

import com.alibaba.pelican.chaos.client.RemoteCmd;
import com.alibaba.pelican.chaos.client.RemoteCmdResult;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;

import java.io.PipedInputStream;

/**
 * @author moyun@middleware
 */
public class ExecCmdWithPtyTask extends AbstractTask<RemoteCmdResult> {

    private PipedInputStream pis;

    private RemoteCmd command;

    public ExecCmdWithPtyTask(RemoteCmdClient client, RemoteCmd command) {
        super(client);
        this.command = command;
    }

    public ExecCmdWithPtyTask(RemoteCmdClient client, RemoteCmd command, PipedInputStream pis) {
        super(client);
        this.pis = pis;
        this.command = command;
    }

    @Override
    public RemoteCmdResult execute() {
        if (this.pis == null) {
            return client.execCmdWithPTY(command);
        }
        return client.execCmdWithPTY(command, this.pis);
    }
}

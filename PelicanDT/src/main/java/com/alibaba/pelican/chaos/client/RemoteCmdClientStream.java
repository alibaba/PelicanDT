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

package com.alibaba.pelican.chaos.client;

import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author moyun@middleware
 */
@Slf4j
public class RemoteCmdClientStream {

    private InputStream stdout;

    private InputStream stderr;

    private Session session;

    private String ip;

    public RemoteCmdClientStream(Session session, String ip) {
        if (session != null) {
            this.session = session;
            this.ip = ip;
            this.stdout = new StreamGobbler(session.getStdout());
            this.stderr = new StreamGobbler(session.getStderr());
        }
    }

    public InputStream getStderr() {
        return stderr;
    }

    public InputStream getStdout() {
        return stdout;
    }

    public void close() {
        if (stdout != null) {
            try {
                stdout.close();
                stderr.close();
            } catch (IOException e) {
                log.error(String.format("RemoteClientStream Close session stdout %s failed!", ip), e);
            }
        }
        if (session != null) {
            session.close();
        }
    }

}

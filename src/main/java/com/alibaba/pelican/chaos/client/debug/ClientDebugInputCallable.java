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

package com.alibaba.pelican.chaos.client.debug;

import com.trilead.ssh2.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author moyun@middleware
 */
@Slf4j
public class ClientDebugInputCallable implements Callable<String> {

    private Session session;

    private List<String> cmds;

    private volatile boolean stop = false;

    private String password;

    private static InputStream sysin = System.in;

    private OutputStream out;

    public ClientDebugInputCallable(Session session, List<String> cmds, String password) {
        this.session = session;
        this.cmds = cmds;
        this.password = password;
        this.out = this.session.getStdin();
    }

    public void stop() {
        this.stop = true;
    }

    private boolean sendCmd() {
        try {
            for (String cmd : cmds) {
                log.debug("input command " + cmd);
                if (cmd.contains("sudo")) {
                    cmd = cmd.replace("sudo ",
                            String.format("echo '%s'| sudo -S ", password));
                }
                out.write((cmd + "\n").getBytes());
                out.flush();
            }
            out.write("exit\n".getBytes());
            out.flush();
        } catch (Exception e) {
            log.error("An exception occurs when execute commands.", e);
            try {
                out.close();
            } catch (IOException e2) {
                log.error("An exception occurs when close the OutputStream.", e2);
            }
            return false;
        }
        return true;
    }

    private void getSystemInput() {
        try {
            while (true) {
                if (sysin.available() > 0) {
                    byte[] buffer = new byte[8192];
                    int length = sysin.read(buffer);
                    if (length > 0) {
                        String cmd = new String(buffer, 0, length);
                        out.write(cmd.replace("\r\n", "").getBytes());
                        out.write("\n".getBytes());
                        out.flush();
                    }
                } else {
                    Thread.sleep(50);
                }
                if (stop == true) {
                    log.debug("Input thread over!");
                    break;
                }
            }
        } catch (Exception e) {
            log.error("An exception occurs when read the input from console.", e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error("An exception occurs when close the OutputStream.", e);
            }
        }
    }

    @Override
    public String call() {
        if (sendCmd()) {
            getSystemInput();
        } else {
            log.error("Input thread exit when sendCmd throws an exception!");
        }
        return "";
    }

}

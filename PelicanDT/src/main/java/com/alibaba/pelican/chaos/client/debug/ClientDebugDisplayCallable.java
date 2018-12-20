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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author moyun@middleware
 */
@Slf4j
public class ClientDebugDisplayCallable implements Callable<String> {

    private Session session;

    private InputStream stdout;

    private PipedOutputStream pos = new PipedOutputStream();

    private PipedOutputStream customPos;

    private boolean startPrint = false;

    private static String resHeadString = "~]$";

    private String lastExportCmdString;

    private StringBuilder sbstd = new StringBuilder();

    private String password;

    private boolean isConnect = false;

    public ClientDebugDisplayCallable(Session session, List<String> cmds, String password) {
        this.session = session;
        this.lastExportCmdString = cmds.get(2);
        this.password = password;
    }

    public void connectPipedOutputStream(PipedInputStream pis) throws IOException {
        pos.connect(pis);
        isConnect = true;
    }

    public void setCustomPos(PipedOutputStream customPos) {
        this.customPos = customPos;
    }

    private boolean isContainsPsw(String data) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        int length = password.length() > data.length() ? data.length() : password.length();
        int matchCount = 0;
        int index = data.length() > password.length() ? data.length() - password.length() : 0;
        for (int i = 0; i < length; i++) {
            if (password.charAt(matchCount) == data.charAt(index + i)) {
                matchCount++;
                continue;
            } else {
                matchCount = 0;
            }
        }
        if (matchCount > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String call() {
        String startPrintToken = String.format("%s%s", resHeadString, lastExportCmdString);
        log.debug("DisplayThread start.");
        try {
            stdout = session.getStdout();
            byte[] buffer = new byte[10240];
            while (true) {
                int len = stdout.read(buffer);
                if (len > 0) {
                    if (isConnect) {
                        pos.write(buffer, 0, len);
                    }
                    sbstd.append(new String(buffer, 0, len));

                    if (!startPrint && sbstd.indexOf(startPrintToken) >= 0) {
                        String printData = sbstd.substring(sbstd.indexOf(startPrintToken) + startPrintToken.length());
                        if (!isContainsPsw(printData)) {
                            if (!StringUtils.isBlank(password)) {
                                if (customPos != null) {
                                    customPos.write(printData.replace(password, "***").getBytes());
                                } else {
                                    log.info(printData.replace(password, "***"));
                                }
                            } else {
                                if (customPos != null) {
                                    customPos.write(printData.getBytes());
                                } else {
                                    log.info(printData);
                                }
                            }
                            sbstd.delete(0, sbstd.length());
                            startPrint = true;
                        }
                        continue;
                    }
                    if (startPrint) {
                        if (!isContainsPsw(sbstd.toString())) {
                            if (!StringUtils.isBlank(password)) {
                                if (customPos != null) {
                                    customPos.write(sbstd.toString().replace(password, "***").getBytes());
                                } else {
                                    log.info(sbstd.toString().replace(password, "***"));
                                }
                            } else {
                                if (customPos != null) {
                                    customPos.write(sbstd.toString().getBytes());
                                } else {
                                    log.info(sbstd.toString());
                                }
                            }

                            sbstd.delete(0, sbstd.length());
                        }
                    }
                } else if (len == -1) {
                    if (sbstd.length() != 0) {
                        if (!StringUtils.isBlank(password)) {
                            if (customPos != null) {
                                customPos.write(sbstd.toString().replace(password,
                                        "***").getBytes());
                            } else {
                                log.info(sbstd.toString().replace(password, "***"));
                            }
                        } else {
                            if (customPos != null) {
                                customPos.write(sbstd.toString().getBytes());
                            } else {
                                log.info(sbstd.toString());
                            }
                        }
                    }
                    if (isConnect) {
                        pos.flush();
                        pos.close();
                    }
                    if (customPos != null) {
                        customPos.close();
                    }
                    Thread.sleep(500);
                    log.debug("DisplayThread over.");
                    return "";
                } else if (len == 0) {
                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            log.error("An exception occurs when display SSH output.", e);
        } finally {
            try {
                if (customPos != null) {
                    customPos.close();
                }
                pos.close();
            } catch (IOException e) {
                log.error("An exception occurs when close PipedOutputStream.", e);
            }
        }
        return "";
    }
}

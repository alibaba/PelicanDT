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

package com.alibaba.pelican.chaos.client.dto;

/**
 * @author moyun@middleware
 */
public class NetstatInternetDto {
    private String protocol       = "";
    private int    recvQueue      = 0;
    private int    sendQueue      = 0;
    private String localAddress   = "";
    private String foreignAddress = "";
    private String state          = "";
    private String pid            = "";
    private String programName    = "";

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getRecvQueue() {
        return recvQueue;
    }

    public void setRecvQueue(int recvQueue) {
        this.recvQueue = recvQueue;
    }

    public int getSendQueue() {
        return sendQueue;
    }

    public void setSendQueue(int sendQueue) {
        this.sendQueue = sendQueue;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getForeignAddress() {
        return foreignAddress;
    }

    public void setForeignAddress(String foreignAddress) {
        this.foreignAddress = foreignAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }
}

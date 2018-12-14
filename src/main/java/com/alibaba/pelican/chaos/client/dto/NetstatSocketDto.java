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
public class NetstatSocketDto {
    private String protocol = "";
    private int    refCnt   = 0;
    private String type     = "";
    private String flags    = "";
    private String state    = "";

    //    private String iNode       = "";
    //    private String pid         = "";
    //    private String programName = "";
    //    private String path        = "";

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getRefCnt() {
        return refCnt;
    }

    public void setRefCnt(int refCnt) {
        this.refCnt = refCnt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /*
     * public String getINode() { return iNode; } public void setINode(String
     * node) { iNode = node; } public String getPid() { return pid; } public
     * void setPid(String pid) { this.pid = pid; } public String
     * getProgramName() { return programName; } public void
     * setProgramName(String programName) { this.programName = programName; }
     * public String getPath() { return path; } public void setPath(String path)
     * { this.path = path; }
     */
}

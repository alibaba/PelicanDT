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

import lombok.Data;

/**
 * @author moyun@middleware
 */
@Data
public class RemoteCmdClientConfig {

    public static final int CLIENT_MODE_DEBUG = 1;

    public static final int CLIENT_MODE_NORMAL = 2;

    private static final String ROOT_NAME = "root";

    private String ip;

    private String userName;

    private String password;

    private Integer retryTime = 3;

    private Integer soTimeout = 300 * 1000;

    private Integer coTimeout = 120 * 1000;

    private Integer mode = CLIENT_MODE_NORMAL;

    public RemoteCmdClientConfig() {

    }

    public RemoteCmdClientConfig(String ip, String userName, String password) {
        this.ip = ip;
        this.userName = userName;
        this.password = password;
    }

    public String getDefaultDir() {
        if (!ROOT_NAME.equals(userName)) {
            return "/home/" + userName;
        } else {
            return "/" + userName;
        }
    }

    public String getConnectAddress() {
        return userName + "@" + ip;
    }
}

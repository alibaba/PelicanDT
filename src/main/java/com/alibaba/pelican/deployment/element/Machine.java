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
package com.alibaba.pelican.deployment.element;

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;

import java.util.List;

/**
 * @author moyun@middleware
 */
public interface Machine extends CustomConfiguration, Cloneable {

    void init();

    String getSSHID();

    String getUserHome();

    Application getApplicationByPath(String appName);

    String getIpAddress();

    void setIpAddress(String ipAddress);

    String getUserName();

    String getPassword();

    List<String> getGroup();

    RemoteCmdClient getRemoteCmdClient();

    void setRemoteCmdClient(RemoteCmdClient client);

    Machine cloneMachine();

    Integer getTimeout();

    List<Application> getAllApplications();

    Application getApplicationById(String id);

    List<Application> getApplicationsByIDs(String... ids);

    List<Application> getAllCustomizedApplications(Class<?> clazz);
}

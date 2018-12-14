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
import com.alibaba.pelican.deployment.configuration.manager.ConfigurationHolderManager;

import java.util.List;

/**
 * @author moyun@middleware
 */
public interface Application extends CustomConfiguration, Cloneable {

    void init();

    String getDeployPath();

    List<String> getGroup();

    int getPriority();

    RemoteCmdClient getRemoteCmdClient();

    void setRemoteCmdClient(RemoteCmdClient remoteCmdClient);

    void start();

    void start(Object arg);

    void stop();

    void restart();

    void restart(Object arg);

    boolean isReady();

    boolean isRemoteCmdClientOutputEnabled();

    void deploy();

    ConfigurationHolderManager getConfigurationHolderManager();

    String getAppDescribe();
    
    Application clone();
}

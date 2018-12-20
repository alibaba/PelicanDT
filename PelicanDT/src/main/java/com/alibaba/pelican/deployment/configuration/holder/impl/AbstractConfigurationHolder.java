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
package com.alibaba.pelican.deployment.configuration.holder.impl;

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.deployment.configuration.holder.ConfigurationHolder;
import com.alibaba.pelican.deployment.configuration.operator.ConfigurationOperator;
import com.alibaba.pelican.deployment.configuration.spring.impl.SpringConfigurationeOperatorImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author moyun@middleware
 */
@Slf4j
public abstract class AbstractConfigurationHolder implements ConfigurationHolder {

    protected ConfigurationOperator operator;

    protected RemoteCmdClient remoteCmdClient;

    protected Map<String, String> params = new HashMap<String, String>();

    public AbstractConfigurationHolder() {

    }

    public AbstractConfigurationHolder(RemoteCmdClient remoteCmdClient, Map<String, String> params) {
        this.remoteCmdClient = remoteCmdClient;
        this.params = params;
        operator = new SpringConfigurationeOperatorImpl();
    }

    public AbstractConfigurationHolder(RemoteCmdClient remoteCmdClient, ConfigurationOperator fileOperator,
                                       Map<String, String> params) {
        this.remoteCmdClient = remoteCmdClient;
        this.params = params;
        this.operator = fileOperator;
    }

    @Override
    public void isValid() {
        if (remoteCmdClient == null) {
            throw new IllegalArgumentException(String.format("%s must not be null!", remoteCmdClient));
        }
    }

    @Override
    public abstract void initConfiguration();

    public abstract boolean needCreateFile();

    @Override
    public abstract String getConfigurationName();

    public abstract String getUploadPath();

    @Override
    public void createAndUploadFile() {
        if (needCreateFile()) {
            isValid();
            initConfiguration();
            saveFile();
            uploadFile();
        }
    }

    @Override
    public boolean saveFile() {
        Object configurationObj = getConfigurationObject();
        if (configurationObj == null || operator == null) {
            return false;
        }

        operator.serialize(configurationObj, getConfigurationName());
        return true;
    }

    @Override
    public void uploadFile() {
        String result = remoteCmdClient.uploadFile(getConfigurationName(), getUploadPath());
        if (result.contains("Error in io")) {
            log.error(String.format("[%s] upload file failed,cause:%s", remoteCmdClient.getIp(), result));
        }
    }

    public void putParam(String key, String value) {
        params.put(key, value);
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public RemoteCmdClient getRemoteCmdClient() {
        return remoteCmdClient;
    }

    @Override
    public void setRemoteCmdClient(RemoteCmdClient remoteCmdClient) {
        this.remoteCmdClient = remoteCmdClient;
    }

    public ConfigurationOperator getConfigurationOperator() {
        return operator;
    }

    @Override
    public void setConfigurationOperator(ConfigurationOperator fileOperator) {
        this.operator = fileOperator;
    }
}

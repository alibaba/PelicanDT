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
package com.alibaba.pelican.deployment.configuration.holder;

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.deployment.configuration.operator.ConfigurationOperator;

import java.util.Map;

/**
 * @author moyun@middleware
 */
public interface ConfigurationHolder {

    void createAndUploadFile();

    void initConfiguration();

    void uploadFile();

    boolean saveFile();

    void isValid();

    String getConfigurationName();

    Object getConfigurationObject();

    void setConfigurationOperator(ConfigurationOperator fileOperator);

    void setRemoteCmdClient(RemoteCmdClient remoteCmdClient);

    void setParams(Map<String, String> params);
}

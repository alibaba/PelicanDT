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
package com.alibaba.pelican.deployment.configuration.manager.impl;

import com.alibaba.pelican.deployment.configuration.holder.ConfigurationHolder;
import com.alibaba.pelican.deployment.configuration.manager.ConfigurationHolderManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author moyun@middleware
 */
public class DefaultConfigurationHolderManager implements ConfigurationHolderManager {

    private Map<String, ConfigurationHolder> confMap = new HashMap<String, ConfigurationHolder>();

    public DefaultConfigurationHolderManager() {
    }

    @Override
    public ConfigurationHolder get(String name) {
        return confMap.get(name);
    }

    @Override
    public void createAndUploadAll() {
        for (ConfigurationHolder operator : confMap.values()) {
            operator.createAndUploadFile();
        }
    }

    @Override
    public void register(String fileName, ConfigurationHolder operator) {
        confMap.put(fileName, operator);
    }

    @Override
    public void clear() {
        confMap.clear();
    }

    @Override
    public void remove(String fileName) {
        confMap.remove(fileName);
    }
}

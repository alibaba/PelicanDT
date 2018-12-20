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
package com.alibaba.pelican.deployment.configuration.properties.impl;

import com.alibaba.pelican.deployment.configuration.properties.PropertiesConfigurationOperator;
import com.alibaba.pelican.deployment.utils.ConfigurationUtils;

import java.util.Map;
import java.util.Properties;

/**
 * @author moyun@middleware
 */
public class PropertiesConfigurationOperatorImpl implements PropertiesConfigurationOperator {

    @Override
    public Object deserialize(String file) {
        return ConfigurationUtils.initProperties(file);
    }

    @Override
    public Object deserialize(String file, Map<String, String> params) {
        return deserialize(file);
    }

    @Override
    public void serialize(Object object, String filePath) {
        ConfigurationUtils.storeProperties((Properties) object, filePath);
    }

    @Override
    public void serialize(Object object, String filePath, Map<String, String> params) {
        serialize(object, filePath);
    }

}

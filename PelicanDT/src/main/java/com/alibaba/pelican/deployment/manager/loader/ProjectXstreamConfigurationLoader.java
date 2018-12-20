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
package com.alibaba.pelican.deployment.manager.loader;

import com.alibaba.pelican.deployment.configuration.properties.PropertiesUtil;
import com.alibaba.pelican.deployment.configuration.xstream.XstreamConfigurationOperator;
import com.alibaba.pelican.deployment.configuration.xstream.impl.XstreamConfigurationOperatorImpl;
import com.alibaba.pelican.deployment.element.impl.DefaultProject;
import com.alibaba.pelican.deployment.exception.ConfigurationParsingException;
import com.alibaba.pelican.deployment.utils.ConfigurationUtils;
import com.thoughtworks.xstream.converters.Converter;

import java.io.File;

/**
 * @author moyun@middleware
 */
public class ProjectXstreamConfigurationLoader {

    public static final String VARIABLE_BRACKETS_PREFIX = "${";

    public static final String VARIABLE_BRACKETS_SUFFIX = "}";

    private DefaultProject testProject;

    private XstreamConfigurationOperator operator = new XstreamConfigurationOperatorImpl();

    public XstreamConfigurationOperator getOperator() {
        return operator;
    }

    public void registerConverter(Converter mapCustomConverter) {
        operator.registerConverter(mapCustomConverter);
    }

    public void addXstreamAnnotations(Class<?>... clazzLst) {
        if (clazzLst == null) {
            return;
        }
        for (Class<?> clazz : clazzLst) {
            operator.processAnnotations(clazz);
        }
    }

    public void serialize(DefaultProject testProject, String filePath) {
        operator.serialize(testProject, filePath);
    }

    public DefaultProject deserialize(File file) {
        String filePath = file.getAbsolutePath();
        if (!file.exists()) {
            throw new ConfigurationParsingException(String.format("The path [%s] is not exsited!", filePath));
        }
        if (!file.isFile()) {
            throw new ConfigurationParsingException(String.format("The path [%s] must be a file!", filePath));
        }
        testProject = (DefaultProject) operator.deserialize(filePath);
        String content = ConfigurationUtils.readFileToString(filePath);
        content = convertVariableForTestProject(content);
        testProject = (DefaultProject) operator.fromXML(content);
        return testProject;
    }

    private String convertVariableForTestProject(String content) {
        int posb = content.indexOf(VARIABLE_BRACKETS_PREFIX);
        int pose = content.indexOf(VARIABLE_BRACKETS_SUFFIX, posb);
        StringBuilder sb = new StringBuilder(content);
        while (posb != -1 && pose != -1) {
            String key = sb.substring(posb + VARIABLE_BRACKETS_PREFIX.length(), pose);
            String newValue = testProject.getCustomizedVariable(key);

            if (PropertiesUtil.get(key) != null) {
                newValue = PropertiesUtil.get(key);
            }

            if (newValue == null) {
                throw new IllegalArgumentException("Can't find property named " + key);
            }
            sb.replace(posb, pose + 1, newValue);
            posb = sb.indexOf(VARIABLE_BRACKETS_PREFIX, 1);
            if (posb != -1) {
                pose = sb.indexOf(VARIABLE_BRACKETS_SUFFIX, posb);
            } else {
                pose = -1;
            }
        }
        return sb.toString();
    }
}

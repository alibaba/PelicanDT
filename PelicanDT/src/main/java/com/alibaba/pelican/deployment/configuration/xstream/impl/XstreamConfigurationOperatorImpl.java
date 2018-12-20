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
package com.alibaba.pelican.deployment.configuration.xstream.impl;

import com.alibaba.pelican.deployment.configuration.xstream.XstreamConfigurationOperator;
import com.alibaba.pelican.deployment.exception.ConfigurationParsingException;
import com.alibaba.pelican.deployment.utils.ConfigurationUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.Map;


/**
 * @author moyun@middleware
 */
public class XstreamConfigurationOperatorImpl implements XstreamConfigurationOperator {

    private XStream xstream = null;

    public XstreamConfigurationOperatorImpl() {
        xstream = new XStream(new DomDriver());
    }

    @Override
    public Object deserialize(String fileName) {
        String content = ConfigurationUtils.readFileToString(fileName);
        Object obj;
        try {
            obj = xstream.fromXML(content);
        } catch (XStreamException e) {
            throw new ConfigurationParsingException(String.format("[%s] deserialize error", fileName), e);
        }
        return obj;
    }

    @Override
    public void serialize(Object object, String filePath) {
        String content = xstream.toXML(object);
        ConfigurationUtils.writeStringToFile(filePath, content);
    }

    @Override
    public Object deserialize(String file, Map<String, String> params) {
        return deserialize(file);
    }

    @Override
    public void serialize(Object object, String filePath, Map<String, String> params) {
        serialize(object, filePath);
    }

    @Override
    public void alias(String name, Class<?> clazz) {
        xstream.alias(name, clazz);
    }

    @Override
    public Object fromXML(String content) {
        return xstream.fromXML(content);
    }

    @Override
    public void processAnnotations(Class<?> clazz) {
        xstream.processAnnotations(clazz);
    }

    @Override
    public void registerConverter(Converter converter) {
        xstream.registerConverter(converter);
    }

}

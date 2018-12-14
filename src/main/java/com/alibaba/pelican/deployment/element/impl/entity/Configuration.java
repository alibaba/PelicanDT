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
package com.alibaba.pelican.deployment.element.impl.entity;

import com.alibaba.pelican.deployment.configuration.xstream.entity.XstreamMap;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

/**
 * @author moyun@middleware
 */
@XStreamAlias("ConfigFile")
public class Configuration implements Serializable {
    private String type = "";

    private String className = "";

    @XStreamAlias("params")
    protected XstreamMap<String, String> properties = new XstreamMap<String, String>();

    public XstreamMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(XstreamMap<String, String> properties) {
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}

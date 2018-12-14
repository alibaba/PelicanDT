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
package com.alibaba.pelican.deployment.element.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.pelican.deployment.configuration.xstream.entity.XstreamList;
import com.alibaba.pelican.deployment.configuration.xstream.entity.XstreamMap;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;
import java.util.Map;


/**
 * @author moyun@middleware
 */
public abstract class AbstractElement {

    protected String id = "";

    @XStreamAlias("params")
    protected XstreamMap<String, String> properties = new XstreamMap<String, String>();

    @XStreamAlias("properties")
    protected XstreamMap<String, String> variables = new XstreamMap<String, String>();

    protected boolean disabled = false;

    protected XstreamList<String> group = new XstreamList<String>();

    protected String description = "";

    public AbstractElement() {

    }

    public void init() {
        if (group == null) {
            group = new XstreamList<String>();
        }
        group.add("default");
    }

    public static String getFullKey(String preKey, String key, String suffKey) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(preKey)) {
            sb.append(preKey).append("_");
        }
        sb.append(key);
        if (StringUtils.isNotBlank(suffKey)) {
            sb.append("_").append(preKey);
        }
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getGroup() {
        return group;
    }

    public void setGroup(XstreamList<String> group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getCustomizedVariable(String key) {
        return variables.get(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void putProperty(String key, String value) {
        if (properties == null) {
            properties = new XstreamMap<String, String>();
        }
        properties.put(key, value);
    }

    public void removeProperty(String key) {

        properties.remove(key);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(XstreamMap<String, String> properties) {
        this.properties = properties;
    }

    public XstreamMap<String, String> getVariables() {
        return variables;
    }

    public void setVariables(XstreamMap<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("params",
                properties).toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}

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
package com.alibaba.pelican.deployment.configuration.spring.entity;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.pelican.deployment.configuration.spring.SpringConfigurationConstant;
import com.alibaba.pelican.deployment.exception.ConfigurationParsingException;
import com.alibaba.pelican.deployment.utils.ConfigurationUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author moyun@middleware
 */
public class BeanField implements SpringConfigurationConstant {

    public static AtomicInteger COUNTER = new AtomicInteger(1);

    private static BeanBlackList blackList = new BeanBlackList();

    private String name;

    private String classType;

    private String genericType;

    private String genericKeyType;

    private String genericValueType;

    private Object value;

    private String setMethodName;

    private String getMethodName;

    private boolean isEnum = false;

    private List<BeanField> fields = new ArrayList<BeanField>();

    public static void setBlackList(BeanBlackList blackList) {
        BeanField.blackList = blackList;
    }

    public String getGenericKeyType() {
        return genericKeyType;
    }

    public void setGenericKeyType(String genericKeyType) {
        this.genericKeyType = genericKeyType;
    }

    public String getGenericValueType() {
        return genericValueType;
    }

    public void setGenericValueType(String genericValueType) {
        this.genericValueType = genericValueType;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public void setEnum(boolean isEnum) {
        this.isEnum = isEnum;
    }

    public List<BeanField> getFields() {
        return fields;
    }

    public void setFields(List<BeanField> fields) {
        this.fields = fields;
    }

    public void addField(BeanField entity) {
        fields.add(entity);
    }

    public Iterator<BeanField> iterate() {
        return fields.iterator();
    }

    public String getGenericType() {
        return genericType;
    }

    public void setGenericType(String genericType) {
        this.genericType = genericType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getSetMethodName() {
        return setMethodName;
    }

    public void setSetMethodName(String setMethodName) {
        this.setMethodName = setMethodName;
    }

    public String getGetMethodName() {
        return getMethodName;
    }

    public void setGetMethodName(String getMethodName) {
        this.getMethodName = getMethodName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("filed name", name)
                .append("class type", classType).append("value", value).append("fields", fields)
                .toString();
    }

    public String getXmlDoc() {
        StringBuilder sb = new StringBuilder();
        if (blackList.inBlackList(name, classType)) {
            return sb.toString();
        }
        COUNTER.addAndGet(1);
        if (isEnum) {
            sb.append(getTabLine(String.format(PROPERTY_START, name)));
            COUNTER.addAndGet(1);
            sb.append(getTabLine(String.format(PROPERTY_VALUE, classType, value)));
            COUNTER.decrementAndGet();
            sb.append(getTabLine(PROPERTY_END));
        } else if (classType.startsWith(TYPE_MAP)) {
            if (ConfigurationUtils.isBasic(genericKeyType)) {
                sb.append(getTabLine(MAP_START));
                COUNTER.addAndGet(1);
                if (value instanceof Map<?, ?>) {
                    Map<Object, Object> map = (Map) value;
                    for (Map.Entry<Object, Object> entry : map.entrySet()) {
                        sb.append(getTabLine(String.format(ENTRY_START, entry.getKey())));
                        BeanField value = (BeanField) entry.getValue();
                        sb.append(value.getXmlDoc());
                        sb.append(getTabLine(ENTRY_END));
                    }
                }
                COUNTER.decrementAndGet();
                sb.append(getTabLine(MAP_END));
            } else {
                throw new ConfigurationParsingException("The key's type must be 'String' in for the map");
            }
        } else if (classType.startsWith(TYPE_SET)) {
            if (value instanceof Set<?>) {
                Set<Object> lst = (Set) value;
                sb.append(getTabLine(SET_START));
                if (ConfigurationUtils.isBasic(genericType)) {
                    COUNTER.addAndGet(1);
                    for (Iterator<Object> iterator = lst.iterator(); iterator.hasNext(); ) {
                        Object object = iterator.next();
                        sb.append(getTabLine(String.format(SET_VALUE, object)));
                    }
                    COUNTER.decrementAndGet();
                } else {
                    for (Iterator<Object> iterator = lst.iterator(); iterator.hasNext(); ) {
                        Object object = iterator.next();
                        BeanField tmp = (BeanField) object;
                        sb.append(tmp.getXmlDoc());
                    }
                }
                sb.append(getTabLine(SET_END));
            }
        } else if (classType.startsWith(TYPE_LIST)) {
            if (value instanceof List<?>) {
                List<Object> lst = (List) value;
                sb.append(getTabLine(LIST_START));
                if (ConfigurationUtils.isBasic(genericType)) {
                    COUNTER.addAndGet(1);
                    for (Iterator<Object> iterator = lst.iterator(); iterator.hasNext(); ) {
                        Object object = iterator.next();
                        sb.append(getTabLine(String.format(LIST_VALUE, object)));
                    }
                    COUNTER.decrementAndGet();
                } else {
                    for (Iterator<Object> iterator = lst.iterator(); iterator.hasNext(); ) {
                        Object object = iterator.next();
                        BeanField tmp = (BeanField) object;
                        sb.append(tmp.getXmlDoc());
                    }
                }
                sb.append(getTabLine(LIST_END));
            }
        } else if (ConfigurationUtils.isBasic(classType)) {
            if (StringUtils.isNotBlank(name)) {
                sb.append(getTabLine(String.format(PROPERTY_START, name)));
                COUNTER.addAndGet(1);
                sb.append(getTabLine(String.format(PROPERTY_VALUE_ONLY, value)));
                COUNTER.decrementAndGet();
                sb.append(getTabLine(PROPERTY_END));
            } else {
                sb.append(getTabLine(String.format(PROPERTY_VALUE_ONLY, value)));
            }
        } else {
            sb.append(getTabLine(getBeanLine(name, classType)));
            for (Iterator<BeanField> iterator = fields.iterator(); iterator.hasNext(); ) {
                BeanField entity = iterator.next();
                if (blackList.inBlackList(entity.getName(), entity.getClassType())) {
                    continue;
                }
                boolean needPropertyLine = true;
                if (ConfigurationUtils.isBasic(entity.getClassType()) || entity.isEnum()) {
                    needPropertyLine = false;
                }
                if (needPropertyLine) {
                    COUNTER.addAndGet(1);
                    sb.append(getTabLine(String.format(PROPERTY_NAME, entity.getName())));
                }
                sb.append(entity.getXmlDoc());
                if (needPropertyLine) {
                    sb.append(getTabLine(PROPERTY_END));
                    COUNTER.decrementAndGet();
                }
            }
            sb.append(getTabLine(BEAN_END));
        }
        COUNTER.decrementAndGet();
        return sb.toString();
    }

    private String getBeanLine(String name, String classType) {
        if (StringUtils.isBlank(name)) {
            return String.format(BEAN_CLASS_ONLY, classType);
        } else {
            return String.format(BEAN_LINE, name, classType);
        }
    }

    private String getTabLine(String line) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < COUNTER.intValue(); i++) {
            sb.append("    ");
        }
        return sb.append(line).toString();
    }
}

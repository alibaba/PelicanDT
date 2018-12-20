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

import com.alibaba.pelican.deployment.utils.ConfigurationUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author moyun@middleware
 */
public class BeanBlackList {

    private Map<String, String> nameMap = new HashMap<String, String>();

    public BeanBlackList() {

    }

    public BeanBlackList(Map<String, String> nameMap) {
        this.nameMap = nameMap;
    }

    public boolean inBlackList(String name, String classType) {
        if (nameMap.isEmpty()) {
            return false;
        }
        String desType = classType;
        if (ConfigurationUtils.isBasic(classType)) {
            desType = ConfigurationUtils.convertBasicTypeToObject(classType);
        } else {
            desType = getRealClassType(classType);
        }
        if (nameMap.containsKey(name)) {
            String value = nameMap.get(name);
            if (value.equals(desType)) {
                return true;
            }
        }
        return false;
    }

    private static String getRealClassType(String classType) {
        int index = classType.indexOf("<");
        if (index != -1) {
            return classType.substring(0, index);
        }
        return classType;
    }

    public Map<String, String> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<String, String> nameMap) {
        this.nameMap = nameMap;
    }
}

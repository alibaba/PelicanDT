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

package com.alibaba.pelican.chaos.client.cmd.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author moyun@middleware
 */
public class CmdParameter {

    private Map<String, String> customizeParams = new HashMap<String, String>();

    private Set<String> grepConditions = new HashSet<String>();

    public void addCustomizeParams(String key, String value) {
        customizeParams.put(key, value);
    }

    public void addConditionParams(String key) {
        grepConditions.add(key);
    }

    public Map<String, String> getCustomizeParams() {
        return customizeParams;
    }

    public void setCustomizeParams(Map<String, String> customizeParams) {
        this.customizeParams = customizeParams;
    }

    public Set<String> getGrepConditions() {
        return grepConditions;
    }

    public void setGrepConditions(Set<String> grepConditions) {
        this.grepConditions = grepConditions;
    }
}

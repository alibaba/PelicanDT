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
package com.alibaba.pelican.deployment.element;

import java.io.Serializable;
import java.util.Map;

/**
 * @author moyun@middleware
 */
public interface CustomConfiguration extends Serializable {

	String getId();

    String getProperty(String key);

    void putProperty(String key, String value);

    void removeProperty(String key);

    Map<String, String> getProperties();

    String getCustomizedVariable(String key);

    boolean isDisabled();
}

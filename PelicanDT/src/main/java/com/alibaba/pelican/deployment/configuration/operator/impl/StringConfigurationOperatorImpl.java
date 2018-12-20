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
package com.alibaba.pelican.deployment.configuration.operator.impl;

import com.alibaba.pelican.deployment.utils.FileUtils;
import com.alibaba.pelican.deployment.configuration.operator.ConfigurationOperator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author moyun@middleware
 */
public class StringConfigurationOperatorImpl implements ConfigurationOperator {

    @Override
    public Object deserialize(String file) {
        File f = new File(file);
        if (!f.exists()) {
            throw new IllegalArgumentException(String.format("No file named [%s] can be found!",
                    file));
        }
        String content = "";
        try {
            content = FileUtils.getContentFromFile(file);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Can't read file[%s]!", file));
        }
        return content;
    }

    @Override
    public void serialize(Object object, String filePath) {
        try {
            FileUtils.createFile((String) object, filePath);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Can't write string[%s] to file[%s]!",
                    object, filePath));
        }

    }

    @Override
    public Object deserialize(String file, Map<String, String> params) {
        return deserialize(file);
    }

    @Override
    public void serialize(Object object, String filePath, Map<String, String> params) {
        serialize(object, filePath);
    }
}

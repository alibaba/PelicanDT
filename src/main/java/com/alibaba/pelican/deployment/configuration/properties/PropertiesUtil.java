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
package com.alibaba.pelican.deployment.configuration.properties;

import com.alibaba.pelican.deployment.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * @author moyun@middleware
 */
@Slf4j
public class PropertiesUtil {

    private static final String FRAMEWORK_START_CONFIGURATION = "dtaf.properties";

    private static final String FRAMEWORK_START_CONFIGURATION_SPECIFIED_PATH = "dtaf.conf.path";

    public static Properties properties;

    static {
        properties = new Properties();
        String rootPath;
        String specifiedPath = System.getProperties().getProperty(FRAMEWORK_START_CONFIGURATION_SPECIFIED_PATH);
        if (StringUtils.isNotBlank(specifiedPath)) {
            rootPath = specifiedPath;
        } else {
            rootPath = PropertiesUtil.class.getResource("/").getPath() + "env";
        }
        log.info("framework start configuration rootPath, {}", rootPath);
        List<File> files = (List<File>) FileUtils.getFilesByKeyword(rootPath, FRAMEWORK_START_CONFIGURATION);
        if (files.size() > 0) {
            try {
                InputStream stream = new FileInputStream(files.get(0));
                properties.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Properties sysProperties = System.getProperties();
        for (Object key : sysProperties.keySet()) {
            properties.setProperty((String) key, (String) sysProperties.get(key));
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return value.trim();
        }
        return defaultValue;
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }
}

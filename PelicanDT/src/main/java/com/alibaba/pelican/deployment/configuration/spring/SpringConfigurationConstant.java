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
package com.alibaba.pelican.deployment.configuration.spring;

import com.alibaba.pelican.deployment.configuration.constant.ConfigurationConstant;

/**
 * @author moyun@middleware
 */
public interface SpringConfigurationConstant extends ConfigurationConstant {

    String XML_TITLE = "<?xml version=\"1.0\" encoding=\"GBK\"?><!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">\r\n";

    String BEAN_LINE = "<bean name=\"%s\" class=\"%s\">\r\n";

    String BEAN_CLASS_ONLY = "<bean class=\"%s\">\r\n";

    String BEAN_END = "</bean>\r\n";

    String PROPERTY_LINE = "<property name=\"%s\" value=\"%s\"/>\r\n";

    String SET_START = "<set>\r\n";

    String SET_VALUE = "<value>%s</value>\r\n";

    String SET_END = "</set>\r\n";

    String LIST_START = "<list>\r\n";

    String LIST_VALUE = "<value>%s</value>\r\n";

    String LIST_END = "</list>\r\n";

    String PROPERTY_START = "<property name=\"%s\">\r\n";

    String PROPERTY_VALUE = "<value type=\"%s\">%s</value>\r\n";

    String PROPERTY_VALUE_ONLY = "<value>%s</value>\r\n";

    String PROPERTY_NAME = "<property name=\"%s\">\r\n";

    String PROPERTY_END = "</property>\r\n";

    String MAP_START = "<map>\r\n";

    String MAP_END = "</map>\r\n";

    String ENTRY_START = "<entry key=\"%s\">\r\n";

    String ENTRY_END = "</entry>\r\n";
}

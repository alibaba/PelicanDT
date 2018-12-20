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
package com.alibaba.pelican.deployment.configuration.constant;

/**
 * @author moyun@middleware
 */
public interface ConfigurationConstant {

    String NEW_LINE = "\r\n";

    String TYPE_LONG = "java.lang.Long";

    String TYPE_LONG_S = "long";

    String TYPE_STRING = "java.lang.String";

    String TYPE_BOOLEAN = "java.lang.Boolean";

    String TYPE_BOOLEAN_S = "boolean";

    String TYPE_INTEGER = "java.lang.Integer";

    String TYPE_INTEGER_S = "int";

    String TYPE_SHORT = "java.lang.Short";

    String TYPE_SHORT_S = "short";

    String TYPE_DOUBLE = "java.lang.Double";

    String TYPE_DOUBLE_S = "double";

    String TYPE_DATE = "java.util.Date";

    String TYPE_LIST = "java.util.List";

    String TYPE_SET = "java.util.Set";

    String TYPE_MAP = "java.util.Map";

    int BASIC = 0;

    int BOOLEAN = 1;

    int OBJECT = 2;

    int MAP = 3;

    int LIST = 4;

    int SET = 5;
}

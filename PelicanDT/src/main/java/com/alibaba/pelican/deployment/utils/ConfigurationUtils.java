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
package com.alibaba.pelican.deployment.utils;

import com.alibaba.pelican.deployment.configuration.constant.ConfigurationConstant;
import com.alibaba.pelican.deployment.exception.ConfigurationParsingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author moyun@middleware
 */
@Slf4j
public class ConfigurationUtils implements ConfigurationConstant {

    public static boolean isBasic(String type) {
        return getType(type) == BASIC;
    }

    public static String convertBasicTypeToObject(String type) {
        if (type.equalsIgnoreCase(TYPE_LONG_S)) {
            return TYPE_LONG;
        } else if (type.equalsIgnoreCase(TYPE_INTEGER_S)) {
            return TYPE_INTEGER;
        } else if (type.equalsIgnoreCase(TYPE_BOOLEAN_S)) {
            return TYPE_BOOLEAN;
        } else if (type.equalsIgnoreCase(TYPE_SHORT_S)) {
            return TYPE_SHORT;
        } else if (type.equalsIgnoreCase(TYPE_DOUBLE_S)) {
            return TYPE_DOUBLE;
        }
        return type;
    }

    public static int getType(String type) {
        if (type.equals(TYPE_STRING) || type.equals(TYPE_LONG) || type.equals(TYPE_LONG_S)
                || type.equals(TYPE_INTEGER) || type.equals(TYPE_INTEGER_S)
                || type.equals(TYPE_SHORT) || type.equals(TYPE_SHORT_S) || type.equals(TYPE_DOUBLE)
                || type.equals(TYPE_BOOLEAN) || type.equals(TYPE_BOOLEAN_S)
                || type.equals(TYPE_DOUBLE_S) || type.equals(TYPE_DATE)) {
            return BASIC;
        }
        if (type.startsWith(TYPE_LIST)) {
            return LIST;
        }
        if (type.startsWith(TYPE_MAP)) {
            return MAP;
        }
        if (type.startsWith(TYPE_SET)) {
            return SET;
        }
        return OBJECT;
    }

    public static Class<?> getEnumClassType(Object obj) {
        if (obj.getClass().isEnum()) {
            return obj.getClass();
        } else if (obj.getClass().getSuperclass().isEnum()) {
            return obj.getClass().getSuperclass();
        } else {
            return obj.getClass();
        }
    }

    public static boolean isEnumType(Object obj) {
        if (obj.getClass().isEnum() || obj.getClass().getSuperclass().isEnum()) {
            return true;
        } else {
            return false;
        }
    }

    public static void writeStringToFile(String fileName, String data) {
        if (StringUtils.isBlank(fileName)) {
            return;
        }
        try {
            FileUtils.createFile(data, fileName);
        } catch (IOException e) {
            throw new ConfigurationParsingException(String.format("Failed to write file [%s]", fileName), e);
        }
    }

    public static String readFileToString(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        File file = new File(fileName);
        if (!file.exists()) {
            return "";
        }
        try {
            return FileUtils.getContentFromFile(fileName);
        } catch (IOException e) {
            throw new ConfigurationParsingException(String.format("Failed to read file [%s]", fileName), e);
        }
    }

    public static Properties initProperties(String path) {
        Properties prop = new Properties();

        File file = new File(path);
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            prop.load(inStream);
        } catch (FileNotFoundException e) {
            throw new ConfigurationParsingException(String.format("The path [%s] is not exsited!",
                    file.getAbsolutePath()), e);
        } catch (IOException e) {
            throw new ConfigurationParsingException(String.format("Read file [%s] failed!", path), e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    log.error("Failed to close InputStream!");
                }
            }
        }
        return prop;
    }

    public static void storeProperties(Properties pro, String path) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(path));
            Iterator<Map.Entry<Object, Object>> it = pro.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                String line = String.format("%s=%s", key, value);
                bw.append(line).append("\n");
            }
            bw.flush();
        } catch (IOException e) {
            throw new ConfigurationParsingException(String.format("Write to file [%s] failed!", path), e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    log.error("Failed to close buffered writer!");
                }
            }
        }
    }

    public static Method getMethod(Object obj, String name) {
        Method m = null;
        String methodName = "get" + StringUtils.capitalize(name);
        try {
            m = obj.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            try {
                methodName = "is" + StringUtils.capitalize(name);
                m = obj.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e1) {
                log.error(String.format("Parse failed!Can't find any set method for attibute [%s]!", name));
            }
        }
        return m;
    }

    public static Object getValue(Object obj, Method method) {
        Object value = null;
        try {
            value = method.invoke(obj);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;

    }
}

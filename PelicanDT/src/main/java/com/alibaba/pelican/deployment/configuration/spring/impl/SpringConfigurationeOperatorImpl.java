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
package com.alibaba.pelican.deployment.configuration.spring.impl;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.pelican.deployment.configuration.spring.SpringConfigurationConstant;
import com.alibaba.pelican.deployment.configuration.spring.SpringConfigurationOperator;
import com.alibaba.pelican.deployment.configuration.spring.entity.BeanField;
import com.alibaba.pelican.deployment.configuration.xml.XmlConfigurationOperator;
import com.alibaba.pelican.deployment.configuration.xml.impl.XmlConfigurationOperatorImpl;
import com.alibaba.pelican.deployment.utils.ConfigurationUtils;
import com.alibaba.pelican.deployment.utils.ReflectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author moyun@middleware
 */
public class SpringConfigurationeOperatorImpl implements SpringConfigurationConstant, SpringConfigurationOperator {

    public String createXML(BeanField root) {
        StringBuilder sb = new StringBuilder(XML_TITLE).append("<beans>").append(NEW_LINE);
        sb.append(root.getXmlDoc()).append("</beans>");
        return sb.toString();
    }

    public void parseObject(BeanField rootEntity, String id, Object obj) {

        if (obj instanceof List<?>) {
            String genericType = getGenericTypeFromValue(rootEntity.getClassType());
            if (StringUtils.isNotBlank(genericType)) {
                rootEntity.setGenericType(genericType);
            }
            List<Object> lst = new ArrayList<Object>();
            List<Object> srcLst = (List) obj;
            for (Iterator<Object> iterator = srcLst.iterator(); iterator.hasNext(); ) {
                Object object = iterator.next();
                if (ConfigurationUtils.isBasic(genericType)) {
                    lst.add(object);
                } else {
                    BeanField tmpNode = new BeanField();
                    parseObject(tmpNode, "", object);
                    lst.add(tmpNode);
                }
            }
            rootEntity.setValue(lst);
        } else if (obj instanceof Map<?, ?>) {
            String genericKeyType = getGenericKeyTypeFromValue(rootEntity.getClassType());
            String genericValueType = getGenericValueTypeFromValue(rootEntity.getClassType());
            if (StringUtils.isNotBlank(genericKeyType) && StringUtils.isNotBlank(genericValueType)) {
                rootEntity.setGenericKeyType(genericKeyType);
                rootEntity.setGenericValueType(genericValueType);
            }
            Map<Object, Object> srcMap = new HashMap<Object, Object>();
            Map<Object, Object> map = (Map) obj;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {

                BeanField tmpNode = new BeanField();
                tmpNode.setClassType(genericValueType);
                if (ConfigurationUtils.isBasic(genericValueType)) {
                    tmpNode.setValue(entry.getValue());
                } else {
                    parseObject(tmpNode, "", entry.getValue());
                }
                srcMap.put(entry.getKey(), tmpNode);
            }
            rootEntity.setValue(srcMap);
        } else if (obj instanceof Set<?>) {
            String genericType = getGenericTypeFromValue(rootEntity.getClassType());
            if (StringUtils.isNotBlank(genericType)) {
                rootEntity.setGenericType(genericType);
            }
            Set<Object> set = new HashSet<Object>();
            Set<Object> srcSet = (Set) obj;
            for (Iterator<Object> iterator = srcSet.iterator(); iterator.hasNext(); ) {
                Object object = iterator.next();
                if (ConfigurationUtils.isBasic(genericType)) {
                    set.add(object);
                } else {
                    BeanField tmpNode = new BeanField();
                    parseObject(tmpNode, "", object);
                    set.add(tmpNode);
                }
            }
            rootEntity.setValue(set);
        } else {
            rootEntity.setName(id);
            String classType = obj.getClass().getName();
            rootEntity.setClassType(classType);

            Field[] field = ReflectUtils.getAllDeclaredFieldsForClass(obj.getClass());

            for (int j = 0; j < field.length; j++) {
                if (Modifier.isStatic(field[j].getModifiers())) {
                    continue;
                }
                BeanField fe = new BeanField();
                String name = field[j].getName();
                fe.setName(name);
                String type = field[j].getGenericType().toString();
                type = getClassTypeFromValue(type);
                fe.setClassType(type);
                Method m = null;
                Object value = null;
                switch (ConfigurationUtils.getType(type)) {
                    case BASIC:
                    case BOOLEAN:
                        m = ConfigurationUtils.getMethod(obj, name);
                        fe.setGetMethodName(m.getName());
                        value = ConfigurationUtils.getValue(obj, m);
                        fe.setValue(value);
                        break;
                    case OBJECT:
                        m = ConfigurationUtils.getMethod(obj, name);
                        fe.setGetMethodName(m.getName());
                        value = ConfigurationUtils.getValue(obj, m);
                        if (value != null) {
                            if (ConfigurationUtils.isEnumType(value)) {
                                fe.setEnum(true);
                                fe.setClassType(ConfigurationUtils.getEnumClassType(value)
                                        .getName());
                                fe.setName(name);
                                fe.setValue(value.toString());
                            } else {
                                parseObject(fe, name, value);
                            }
                        }
                        break;
                    case LIST:
                    case SET:
                    case MAP:
                        m = ConfigurationUtils.getMethod(obj, name);
                        fe.setGetMethodName(m.getName());
                        value = ConfigurationUtils.getValue(obj, m);
                        if (value != null) {
                            parseObject(fe, name, value);
                        }
                        break;
                    default:
                        break;
                }
                rootEntity.addField(fe);
            }
        }
    }

    private String getGenericTypeFromValue(String typeStr) {
        int posb = typeStr.indexOf("<");
        int pose = typeStr.indexOf(">");
        if (posb != -1 && pose != -1) {
            return typeStr.substring(posb + 1, pose);
        } else {
            return typeStr;
        }
    }

    private String getGenericKeyTypeFromValue(String typeStr) {
        int posb = typeStr.indexOf("<");
        int posd = typeStr.indexOf(",");
        if (posb != -1 && posd != -1) {
            return typeStr.substring(posb + 1, posd);
        } else {
            return "";
        }
    }

    private String getGenericValueTypeFromValue(String typeStr) {
        int pose = typeStr.lastIndexOf(">");
        int posd = typeStr.indexOf(",");
        if (pose != -1 && posd != -1) {
            return typeStr.substring(posd + 1, pose).trim();
        } else {
            return "";
        }
    }

    private String getClassTypeFromValue(String typeStr) {
        int pos = typeStr.indexOf("class ");
        if (pos != -1) {
            return typeStr.substring(pos + 6);
        } else {
            return typeStr;
        }
    }

    @Override
    public Object deserialize(String filePath, Map<String, String> params) {
        String id = params.get(PARAM_ID);
        if (StringUtils.isBlank(id)) {
            return null;
        }
        ApplicationContext context = new ClassPathXmlApplicationContext(filePath);
        Object object = context.getBean(id);
        return object;
    }

    @Override
    public void serialize(Object object, String filePath, Map<String, String> params) {
        String id = params.get(PARAM_ID);
        if (StringUtils.isBlank(id)) {
            id = StringUtils.uncapitalize(object.getClass().getSimpleName());
        }
        serialize(object, id, filePath);
    }

    public void serialize(Object object, String id, String filePath) {
        BeanField rootEntity = new BeanField();
        parseObject(rootEntity, id, object);
        String content = createXML(rootEntity);
        ConfigurationUtils.writeStringToFile(filePath, content);
    }

    @Override
    public Object deserialize(String file) {
        ApplicationContext context = new ClassPathXmlApplicationContext(file);
        XmlConfigurationOperator xcfo = new XmlConfigurationOperatorImpl(file);
        String id = xcfo.getAttributeValues("beans/bean", "name").get(0);
        return context.getBean(id);
    }

    @Override
    public void serialize(Object object, String filePath) {
        String id = StringUtils.uncapitalize(object.getClass().getSimpleName());
        serialize(object, id, filePath);
    }
}

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
package com.alibaba.pelican.deployment.element.impl.entity;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.pelican.deployment.element.impl.annotation.*;
import com.alibaba.pelican.deployment.utils.CommonUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author moyun@middleware
 */
@Slf4j
public class FlowAnnotationScanner {

    private final Set<String> worlds = new HashSet<String>();

    private Map<Class<?>, Collection<Class<?>>> annotationMap = new HashMap<Class<?>, Collection<Class<?>>>();

    public FlowAnnotationScanner() {
        worlds.add(AfterActive.class.toString());
        worlds.add(AfterDeploy.class.toString());
        worlds.add(BeforeActive.class.toString());
        worlds.add(BeforeDeploy.class.toString());
        worlds.add(ConfigurationValidator.class.toString());
        worlds.add(EnvironmentValidator.class.toString());
        worlds.add(XStreamAlias.class.toString());
    }

    public Collection<Class<?>> getAnnotationClassImpl(
            Class<? extends Annotation> clazz) {
        Collection<Class<?>> customizedClass = annotationMap.get(clazz);
        if (customizedClass == null) {
            customizedClass = new ArrayList<Class<?>>();
        } else {
            customizedClass = sort(customizedClass, clazz);
        }
        return customizedClass;
    }

    public void match(String className) {
        try {
            Class<?> clazz = FlowAnnotationScanner.class.getClassLoader()
                    .loadClass(className);
            for (Annotation annotation : clazz.getAnnotations()) {
                if (worlds.contains(annotation.annotationType().toString())) {
                    if (!annotationMap.containsKey(annotation.annotationType())) {
                        annotationMap.put(annotation.annotationType(),
                                new ArrayList<Class<?>>());
                    }
                    annotationMap.get(annotation.annotationType()).add(clazz);
                }
            }
        } catch (Throwable e) {
            log.warn(String.format("[%s] can't be loaded by FlowAnnotationScanner,skip!", className));
        }
    }

    private Integer fetchCustomAnnotationValue(Class<? extends Annotation> annotationClazz, Class<?> customClazz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Annotation an = customClazz.getAnnotation(annotationClazz);
        for (Method method : an.getClass().getDeclaredMethods()) {
            if (method.getName().equals("value")) {
                String strValue = (String) method.invoke(an);
                if (StringUtils.isBlank(strValue) || !NumberUtils.isDigits(strValue)) {
                    strValue = "0";
                }
                Integer value = Integer.parseInt(strValue);
                return value;
            }
        }
        return -1;
    }

    private Collection<Class<?>> sort(Collection<Class<?>> customClazz, Class<? extends Annotation> annotation) {
        if (customClazz.isEmpty()) {
            return customClazz;
        }
        Map<Class<?>, Integer> clazzMap = new LinkedHashMap<Class<?>, Integer>();
        for (Class<?> clazz : customClazz) {
            Integer value = -1;
            try {
                value = fetchCustomAnnotationValue(annotation, clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            clazzMap.put(clazz, value);
        }
        clazzMap = CommonUtils.sortByValue(clazzMap);
        customClazz = new LinkedList<Class<?>>(clazzMap.keySet());
        return customClazz;
    }
}
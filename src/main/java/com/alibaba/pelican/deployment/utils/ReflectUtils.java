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

import com.alibaba.pelican.deployment.element.impl.entity.FlowAnnotationScanner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author moyun@middleware
 */
public class ReflectUtils {

    private static final Map<String, String> BASIC_TYPE_MAPING = new HashMap<String, String>();
    private static final String[] BASIC_TYPE = new String[]{"int", "double", "float", "long", "short", "boolean",
            "byte", "char",};
    private static final String[] WRAP_TYPE = new String[]{Integer.class.getName(), Double.class.getName(),
            Float.class.getName(), Long.class.getName(), Short.class.getName(), Boolean.class.getName(),
            Byte.class.getName(), Character.class.getName(),};

    static {
        for (int i = 0; i < BASIC_TYPE.length; i++) {
            BASIC_TYPE_MAPING.put(BASIC_TYPE[i], WRAP_TYPE[i]);
        }
    }

    public static Field getFieldContainsParent(Object obj, String name) {
        Class<?> desClass = obj.getClass();
        Field[] fields = getAllDeclaredFieldsForClass(desClass);
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public static Field getField(Object obj, String name) {
        Field[] targets = obj.getClass().getDeclaredFields();
        for (Field field : targets) {
            field.setAccessible(true);
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public static Object getFieldValue(Object obj, String name) {
        Field field = getField(obj, name);
        if (field == null) {
            return null;
        }
        Object res = null;
        try {
            res = field.get(obj);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void setFieldValue(Object obj, String name, Object value) {
        Field field = getFieldContainsParent(obj, name);
        if (field == null) {
            throw new IllegalArgumentException(String.format("Can't set filed[%s] to value[%s] in class[%s]", name,
                    value, obj.getClass().getName()));
        } else {
            field.setAccessible(true);
        }
        try {
            field.set(obj, value);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Field[] getAllDeclaredFieldsForClass(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> tmpClass = clazz;
        while (tmpClass != Object.class) {
            Field[] tmpField = tmpClass.getDeclaredFields();
            if (tmpField != null && tmpField.length > 0) {
                for (Field field : tmpField) {
                    fields.add(field);
                }
            }
            tmpClass = tmpClass.getSuperclass();
        }
        Field[] res = new Field[fields.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = fields.get(i);
        }
        return res;
    }

    public static Object executeNoParamsMethod(Object object, String methodName) {
        try {
            Class<?> clz = object.getClass();
            Method method = clz.getMethod(methodName, new Class[0]);
            return method.invoke(object, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object executeNoParamsMethod(Method method, Object object) {
        return executeParamsMethod(object, method, new Object[0]);
    }

    public static Object executeParamsMethod(Object object, Method method, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object executeParamsMethod(Object object, String methodName, Object... args) {
        Class<?> clz = object.getClass();
        Method method = getMethodFromClass(clz, methodName, args);
        if (method != null) {
            return executeParamsMethod(object, method, args);
        }
        return null;
    }

    public static Object executeParamsStaticMethod(String className, String methodName, Object... args) {
        Class<?> clz = null;
        try {
            clz = Class.forName(className);
            Method method = getMethodFromClass(clz, methodName, args);
            if (method != null) {
                return executeParamsMethod(null, method, args);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object executeNoParamsStaticMethod(String className, String methodName) {
        Class<?> clz = null;
        try {
            clz = Class.forName(className);
            Method method = getMethodFromClass(clz, methodName, new Object[]{});
            if (method != null) {
                return executeParamsMethod(null, method, new Object[]{});
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object executeNoParamsStaticMethod(Class<?> clazz, String methodName) {
        Method method = getMethodFromClass(clazz, methodName, new Object[]{});
        if (method != null) {
            return executeParamsMethod(null, method, new Object[]{});
        }
        return null;
    }

    public static Object executeParamsMethodByType(Object object, String methodName, Object[] args,
                                                   Class<?>[] argTypes) {
        Class<?> clz = object.getClass();
        Method method = null;
        try {
            method = clz.getMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (method != null) {
            return executeParamsMethod(object, method, args);
        }
        return null;
    }

    public static Method getMethodFromClass(Class<?> clz, String methodName, Object... args) {
        Class<?>[] parameterTypes = null;
        Method method = null;
        try {
            parameterTypes = ReflectUtils.getParamsClassArray(args);
            method = clz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Method[] methods = clz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    Class<?>[] types = methods[i].getParameterTypes();
                    if (isParamArraysEqual(types, parameterTypes)) {
                        try {
                            method = clz.getMethod(methodName, types);
                            break;
                        } catch (NoSuchMethodException e1) {
                            e1.printStackTrace();
                        } catch (SecurityException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        return method;
    }

    private static boolean typeEqualsWithBasicAndWrap(String src, String des) {
        if (BASIC_TYPE_MAPING.containsKey(src)) {
            return BASIC_TYPE_MAPING.get(src).equals(des);
        } else if (BASIC_TYPE_MAPING.containsKey(des)) {
            return BASIC_TYPE_MAPING.get(des).equals(src);
        }
        return false;
    }

    private static boolean isParamArraysEqual(Class<?>[] src, Class<?>[] des) {
        if (src.length != des.length) {
            return false;
        }
        for (int j = 0; j < src.length; j++) {
            if (!typeEqualsWithBasicAndWrap(src[j].getName(), des[j].getName())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWrapClass(Class<?> clz) {
        try {
            return ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    public static Class<?>[] getParamsClassArray(Object... objects) {
        if (objects == null) {
            return null;
        }
        Class<?>[] classes = new Class<?>[objects.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = objects[i].getClass();
        }
        return classes;
    }

    public static List<Class<?>> getAnnotationFromClassPath(Class<? extends Annotation> clazz) {
        AnnotationTypeFilter f = new AnnotationTypeFilter(clazz);
        return realGetFromClassPath(f);
    }

    public static Map<Class<? extends Annotation>, List<Class<?>>> getAnnotationMapsFromClassPath(List<Class<? extends Annotation>> clazz) {
        Map<Class<? extends Annotation>, List<Class<?>>> res = new HashMap<Class<? extends Annotation>, List<Class<?>>>();
        return res;
    }

    public static List<Class<?>> getClassesFromClassPath(Class<?> clazz) {
        AssignableTypeFilter f = new AssignableTypeFilter(clazz);
        return realGetFromClassPath(f);
    }

    public static void parseAnnotationClass(FlowAnnotationScanner filter) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        resolver.setPathMatcher(new AntPathMatcher());
        Resource[] resources;
        try {
            resources = resolver
                    .getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "com/taobao/test/**/*.class");
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
            for (Resource r : resources) {
                MetadataReader reader = readerFactory.getMetadataReader(r);
                String className = reader.getClassMetadata().getClassName();
                filter.match(className);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Class<?>> realGetFromClassPath(TypeFilter filter) {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        resolver.setPathMatcher(new AntPathMatcher());
        Resource[] resources;
        try {
            resources = resolver
                    .getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "com/taobao/**/*.class");
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
            for (Resource r : resources) {
                MetadataReader reader = readerFactory.getMetadataReader(r);
                if (filter.match(reader, readerFactory)) {
                    String className = reader.getClassMetadata().getClassName();
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                    classes.add(clazz);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classes;
    }
}

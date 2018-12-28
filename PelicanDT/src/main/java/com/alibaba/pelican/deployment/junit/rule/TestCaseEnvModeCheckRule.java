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
package com.alibaba.pelican.deployment.junit.rule;


import junitparams.Parameters;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.pelican.deployment.configuration.properties.PropertiesUtil;
import com.alibaba.pelican.deployment.junit.annotation.EnvironmentMode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author moyun@middleware
 */
@Slf4j
public class TestCaseEnvModeCheckRule implements TestRule {

    private EnvironmentMode getEnvModeAnnotation(Description description) {
        Class<?> testClass = description.getTestClass();
        EnvironmentMode envMode = testClass.getAnnotation(EnvironmentMode.class);
        String methodName = description.getMethodName();
        methodName = methodName.replaceAll("\\[[\\s\\S]+\\]$", "");
        Method[] methods = testClass.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(Test.class) != null) {
                if (method.getAnnotation(Parameters.class) != null) {
                    if (methodName.indexOf("(") != -1) {
                        methodName = methodName.substring(0, methodName.indexOf("("));
                    }
                }
                if (StringUtils.equals(method.getName(), methodName)) {
                    if (method.getAnnotation(EnvironmentMode.class) != null) {
                        envMode = method.getAnnotation(EnvironmentMode.class);
                    }
                }
            }
        }
        if (envMode == null) {
            Class<?> superClass = testClass.getSuperclass();
            while (!superClass.equals(Object.class)) {
                envMode = superClass.getAnnotation(EnvironmentMode.class);
                if (envMode != null) {
                    return envMode;
                }
                superClass = superClass.getSuperclass();
            }
        }
        return envMode;
    }

    private boolean validEnvironmentMode(EnvironmentMode environmentMode) {
        String specifiedEnvironmentMode = PropertiesUtil.get("env.mode");

        if (environmentMode == null || StringUtils.isBlank(environmentMode.value())) {
            return true;
        }
        if (environmentMode.value().toLowerCase().equals("all")) {
            return true;
        }

        String value = environmentMode.value();
        if (value.contains(",")) {
            String[] items = value.split(",");
            return new HashSet<String>(Arrays.asList(items)).contains(specifiedEnvironmentMode);
        } else {
            return specifiedEnvironmentMode.equals(environmentMode.value());
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {

        if (Boolean.valueOf(PropertiesUtil.get("dtaf.skip", "false"))) {
            return base;
        }

        EnvironmentMode environmentMode = getEnvModeAnnotation(description);
        boolean needRun = this.validEnvironmentMode(environmentMode);

        if (needRun) {
            return base;
        } else {
            log.warn(String.format("The environment mode in testcase[class:%s->method:%s] is [%s] not match specified environment mode value[%s], the test case will be skipped!",
                    description.getTestClass().getSimpleName(),
                    description.getMethodName(),
                    environmentMode == null ? "none" : environmentMode.value(),
                    PropertiesUtil.get("env.mode")));
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {

                }
            };
        }
    }
}

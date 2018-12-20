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

import org.apache.commons.lang3.StringUtils;
import com.alibaba.pelican.deployment.configuration.properties.PropertiesUtil;
import com.alibaba.pelican.deployment.element.Project;
import com.alibaba.pelican.deployment.manager.environment.EnvironmentManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collection;

/**
 * @author moyun@middleware
 */
@Slf4j
public class EnvironmentModeRule implements TestRule {

    protected static EnvironmentManager testEnvironmentManager;

    public static String DEPLOY_SKIP_KEY = "deploy.skip";

    public static String DEPLOY_STATE = "deploy.state";

    public static String ENV_MODE_KEY = "env.mode";

    protected static Boolean DEPLOY_SKIP = true;

    private static String CUR_TEST_MODE = "";

    static {
        if (!Boolean.valueOf(PropertiesUtil.get("dtaf.skip", "false"))) {
            String value = PropertiesUtil.get(DEPLOY_SKIP_KEY);
            testEnvironmentManager = EnvironmentManager.getInstance();
            if (StringUtils.isNotBlank(value)) {
                DEPLOY_SKIP = Boolean.valueOf(value);
                log.debug("Rest deploy skip value to:" + value);
            }
            String mode = PropertiesUtil.get(ENV_MODE_KEY);
            if (StringUtils.isBlank(mode)) {
                Collection<Project> projects = testEnvironmentManager.getAllTestProjects();
                if (projects != null && projects.size() == 1) {
                    for (Project testProject : projects) {
                        mode = testProject.getEnvironmentMode();
                        CUR_TEST_MODE = mode;
                        log.debug(String.format("No envMode found,use [%s] as default!", mode));
                        break;
                    }
                }
            } else {
                CUR_TEST_MODE = mode;
                log.debug(String.format("Use [%s] as default envMode!", mode));
            }
            testEnvironmentManager.setCurrentEnvMode(CUR_TEST_MODE);
            log.debug("Set env mode as:" + CUR_TEST_MODE);
            if (!StringUtils.isBlank(CUR_TEST_MODE)) {
                PropertiesUtil.set(ENV_MODE_KEY, CUR_TEST_MODE);
                testEnvironmentManager.getTestProject(CUR_TEST_MODE);
            }
        } else {
            log.debug("DTAF had been desabled by argument[dtaf.skip=true]");
        }
    }

    public static Boolean getDEPLOY_SKIP() {
        return DEPLOY_SKIP;
    }

    public static EnvironmentManager getTestEnvironmentManager() {
        return testEnvironmentManager;
    }

    public Project getDtafTestProject(String envMode) {
        return testEnvironmentManager.getTestProject(envMode);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (!Boolean.valueOf(PropertiesUtil.get("dtaf.skip", "false"))) {
            testEnvironmentManager.getTestProject(testEnvironmentManager.getCurrentEnvMode());
        }
        return base;
    }

}

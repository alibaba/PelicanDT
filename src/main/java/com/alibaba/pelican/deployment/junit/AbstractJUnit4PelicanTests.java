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
package com.alibaba.pelican.deployment.junit;

import com.alibaba.pelican.deployment.element.Project;
import com.alibaba.pelican.deployment.junit.rule.EnvironmentModeRule;
import com.alibaba.pelican.deployment.junit.rule.LogRule;
import com.alibaba.pelican.deployment.junit.rule.TestCaseEnvModeCheckRule;
import com.alibaba.pelican.deployment.manager.environment.EnvironmentManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.rules.RuleChain;

/**
 * @author moyun@middleware
 */
@Slf4j
public abstract class AbstractJUnit4PelicanTests {

    private static EnvironmentModeRule environmentModeRule;
    private static TestCaseEnvModeCheckRule testCaseEnvModeCheckRule;
    private static LogRule printDocLogRule;

    @Rule
    public RuleChain ruleChain = RuleChain.outerRule(testCaseEnvModeCheckRule).around(printDocLogRule).around(environmentModeRule);

    protected static EnvironmentManager manager;

    static {
        printDocLogRule = new LogRule();
        environmentModeRule = new EnvironmentModeRule();
        testCaseEnvModeCheckRule = new TestCaseEnvModeCheckRule();
        manager = EnvironmentModeRule.getTestEnvironmentManager();
    }

    public Project getTestProject() {
        if (Boolean.valueOf(System.getProperty("dtaf.skip", "false"))) {
            throw new IllegalAccessError("Found dtaf.skip=true,you can't use DtafTestProject!");
        }
        if (StringUtils.isBlank(manager.getCurrentEnvMode())) {
            return manager.getDefaultTestProject();
        }
        return manager.getTestProject(manager.getCurrentEnvMode());
    }
}

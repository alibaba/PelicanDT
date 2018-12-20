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
package com.alibaba.pelican.deployment.manager.environment.impl;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.manager.entity.ExcutorResult;
import com.alibaba.pelican.deployment.manager.entity.ExcutorResultCollector;
import com.alibaba.pelican.deployment.manager.excutor.ApplicationDeployExcutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author moyun@middleware
 */
@Slf4j
public class ParallelEnvironmentManager {

    protected ExecutorService executorService;

    private List<Application> getNonCloneApplication(List<Application> applications) {
        List<Application> apps = new ArrayList<Application>();
        for (Application application : applications) {
            apps.add(application);
        }
        return apps;
    }

    private void paralleDeploy(List<Application> apps) {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        if (cpuNum < 0 || cpuNum > 40) {
            cpuNum = 4;
        }

        executorService = Executors.newFixedThreadPool(cpuNum * 2);
        int timeout = 300;
        String debug = System.getProperty("debug");
        if (!StringUtils.isBlank(debug) && debug.equalsIgnoreCase("true")) {
            timeout = 600;
        }
        ExcutorResultCollector resCollector = new ExcutorResultCollector(timeout);
        for (Application application : apps) {
            ApplicationDeployExcutor excutor = new ApplicationDeployExcutor(application);
            Future<ExcutorResult> future = executorService.submit(excutor);
            resCollector.addFuture(application, future);
        }
        resCollector.waitAndCheckAllDeployReault();
        executorService.shutdown();
    }

    protected void realDeploy(List<Application> apps) {
        long startTime = System.currentTimeMillis();
        paralleDeploy(getNonCloneApplication(apps));
        long endTime = System.currentTimeMillis();
        log.info(String.format("It took %s second to deploy all test application!",
                (endTime - startTime) / 1000));
    }
}

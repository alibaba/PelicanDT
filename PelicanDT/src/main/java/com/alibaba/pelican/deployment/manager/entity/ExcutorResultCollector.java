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
package com.alibaba.pelican.deployment.manager.entity;

import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.exception.DeployApplicationFailedException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author moyun@middleware
 */
@Slf4j
public class ExcutorResultCollector {

    private int timeout = 180;

    private Map<Application, Future<ExcutorResult>> futureResults = new HashMap<Application, Future<ExcutorResult>>();

    private Map<Application, ExcutorResult> resultsMap = new HashMap<Application, ExcutorResult>();

    public ExcutorResultCollector() {
    }

    public ExcutorResultCollector(int timeout) {
        this.timeout = timeout;
    }

    public void waitAndCheckAllDeployReault() {
        waitForAllDeployComplete();
        checkAllDeployResult();
    }

    public void waitForAllDeployComplete() {
        for (Entry<Application, Future<ExcutorResult>> entry : futureResults.entrySet()) {
            Application app = entry.getKey();
            ExcutorResult res = null;
            Future<ExcutorResult> future = entry.getValue();
            try {
                res = future.get(timeout, TimeUnit.SECONDS);
            } catch (Throwable e) {
                log.error(String.format("App[%s:%s] deploy failed!", app.getId(), app.getRemoteCmdClient().getIp()), e);
                res = ExcutorResult.getFailedResult(e);
            }
            resultsMap.put(app, res);
        }
    }

    public void checkAllDeployResult() {
        List<String> failedApp = new ArrayList<String>();
        for (Entry<Application, ExcutorResult> entry : resultsMap.entrySet()) {
            Application app = entry.getKey();
            ExcutorResult res = entry.getValue();
            if (!res.isSuccess()) {
                log.error(
                        String.format("Deploy application[%s] failed because %s", app.getAppDescribe(), res.getInfo()), res.getError());
                failedApp.add(String.format("App[%s]", app.getAppDescribe()));
            }
        }
        if (!failedApp.isEmpty()) {
            throw new DeployApplicationFailedException(String.format("There were applications which deploy failed:%s", failedApp));
        }
    }

    public void addFuture(Application app, Future<ExcutorResult> future) {
        this.futureResults.put(app, future);
    }
}

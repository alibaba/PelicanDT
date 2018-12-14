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
package com.alibaba.pelican.deployment.manager.excutor;

import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.manager.entity.ExcutorResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * @author moyun@middleware
 */
@Slf4j
public class ApplicationDeployExcutor implements Callable<ExcutorResult> {

    private Application application;

    public ApplicationDeployExcutor(Application application) {
        this.application = application;
    }

    @Override
    public ExcutorResult call() throws Exception {
        ExcutorResult res = null;
        try {
            application.deploy();
            res = ExcutorResult.getSuccessResult();
        } catch (Exception e) {
            log.error(String.format("application[%s] deploy failed because of [%s]", application.getAppDescribe(), e.getMessage()), e);
            res = ExcutorResult.getFailedResult(e);
        }
        return res;
    }
}

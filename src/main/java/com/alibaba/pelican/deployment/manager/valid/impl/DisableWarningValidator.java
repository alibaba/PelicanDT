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
package com.alibaba.pelican.deployment.manager.valid.impl;

import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.element.Machine;
import com.alibaba.pelican.deployment.element.Project;
import com.alibaba.pelican.deployment.element.impl.DefaultMachine;
import com.alibaba.pelican.deployment.element.impl.DefaultProject;
import com.alibaba.pelican.deployment.element.impl.annotation.ConfigurationValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author moyun@middleware
 */
@Slf4j
@ConfigurationValidator
public class DisableWarningValidator implements com.alibaba.pelican.deployment.manager.valid.ConfigurationValidator {

    @Override
    public void doCheck(Project testProject) {
        DefaultProject project = (DefaultProject) testProject;
        List<Machine> machines = project.getMachines();
        if (machines != null && !machines.isEmpty()) {
            for (Machine machine : machines) {
                if (machine.isDisabled()) {
                    log.warn(String.format("Disable Machine[%s]", machine.getIpAddress()));
                } else {
                    List<Application> disableApplications = ((DefaultMachine) machine)
                            .getDisabledApplications();
                    if (disableApplications != null && !disableApplications.isEmpty()) {
                        for (Application app : disableApplications) {
                            log.warn(String.format("Disable Application[%s:%s]",
                                    machine.getIpAddress(), app.getId()));
                        }
                    }
                }
            }
        }
    }
}

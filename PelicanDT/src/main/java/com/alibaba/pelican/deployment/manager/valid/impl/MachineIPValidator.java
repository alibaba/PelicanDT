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

import com.alibaba.pelican.deployment.element.Machine;
import com.alibaba.pelican.deployment.element.Project;
import com.alibaba.pelican.deployment.element.impl.annotation.ConfigurationValidator;
import com.alibaba.pelican.deployment.exception.ConfigurationCheckException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author moyun@middleware
 */
@ConfigurationValidator
public class MachineIPValidator implements com.alibaba.pelican.deployment.manager.valid.ConfigurationValidator {

    private List<String> ipList = new ArrayList<String>();

    private static Pattern pattern;

    public MachineIPValidator() {
        if (pattern == null) {
            String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            pattern = Pattern.compile(ip);
        }
    }

    @Override
    public void doCheck(Project testProject) {
        List<Machine> machines = testProject.getAllMachines();
        if (machines != null && !machines.isEmpty()) {
            for (Machine machine : machines) {
                addIpList(machine.getIpAddress());
            }
        }
    }

    private void addIpList(String ip) {
        if (ip == null || ip.isEmpty()) {
            throw new ConfigurationCheckException("Empty ip exists!");
        }
        if (!isIpv4(ip)) {
            throw new ConfigurationCheckException(String.format("Illegal ip[%s]！", ip));
        }
        if (ipList.contains(ip)) {
            throw new ConfigurationCheckException(String.format("Duplicate ip[%s]！", ip));
        }
        ipList.add(ip);
    }

    public static boolean isIpv4(String ipAddress) {
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
}

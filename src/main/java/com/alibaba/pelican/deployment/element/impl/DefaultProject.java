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
package com.alibaba.pelican.deployment.element.impl;

import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.element.Machine;
import com.alibaba.pelican.deployment.element.Project;
import com.alibaba.pelican.deployment.manager.entity.ExcutorResult;
import com.alibaba.pelican.deployment.manager.entity.ExcutorResultCollector;
import com.alibaba.pelican.deployment.manager.excutor.ApplicationDeployExcutor;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author moyun@middleware
 */
@XStreamAlias("project")
public class DefaultProject extends AbstractElement implements Project {

    protected String projectName;

    protected int version = 0;

    protected boolean autoLogin = false;

    protected boolean checkLinuxCommand = false;

    @XStreamOmitField
    protected boolean actived = false;

    @XStreamAlias("machines")
    @XStreamImplicit
    private List<Machine> machines = new ArrayList<Machine>();

    @XStreamOmitField
    private Map<String, Machine> cacheMachineNameMap = new HashMap<String, Machine>();

    private String envMode;

    public static final String DEF_MODE = "defaultMode";

    @XStreamAlias("timeout")
    private int timeout = 300;

    @Override
    public void init() {
        if (actived) {
            return;
        }
        super.init();
        for (Machine machine : getAllMachines()) {
            machine.init();
        }
    }

    @Override
    public boolean isActived() {
        return actived;
    }

    @Override
    public void setActived(boolean actived) {
        this.actived = actived;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public String getEnvironmentMode() {
        return envMode;
    }

    @Override
    public void setEnvironmentMode(String envMode) {
        this.envMode = envMode;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public List<Machine> getAllMachines() {
        List<Machine> machines = new ArrayList<Machine>();
        if (this.machines != null) {
            for (Machine machine : this.machines) {
                if (!machine.isDisabled()) {
                    machines.add(machine);
                }
            }
        }
        return machines;
    }

    @Override
    public Machine getMachineById(String id) {
        if (cacheMachineNameMap == null || cacheMachineNameMap.size() == 0 || cacheMachineNameMap.get(id) == null) {
            initCacheMap();
        }
        return cacheMachineNameMap.get(id);
    }

    private synchronized void initCacheMap() {
        if (cacheMachineNameMap == null) {
            cacheMachineNameMap = new HashMap<String, Machine>();
        }
        cacheMachineNameMap.clear();
        for (Machine machine : getAllMachines()) {
            cacheMachineNameMap.put(machine.getId(), machine);
        }
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public void addMachine(Machine machine) {
        machines.add(machine);
    }

    public List<Machine> getMachines() {
        return machines;
    }

    public void setMachines(List<Machine> machines) {
        this.machines = machines;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, Machine> getCacheMachineNameMap() {
        return cacheMachineNameMap;
    }

    public void setCacheMachineNameMap(Map<String, Machine> cacheMachineNameMap) {
        this.cacheMachineNameMap = cacheMachineNameMap;
    }

    @Override
    public boolean autoLoginEnable() {
        return autoLogin;
    }

    @Override
    public boolean checkLinuxCommandEnable() {
        return checkLinuxCommand;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("projectName", projectName)
                .append("machines", machines).toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public List<Machine> getAllCustomizedMachines(Class<?> clazz) {
        List<Machine> res = new ArrayList<Machine>();
        for (Machine physicalMachine : getAllMachines()) {
            if (physicalMachine.getClass().equals(clazz)) {
                res.add(physicalMachine);
            }
        }
        return res;
    }

    @Override
    public List<Machine> getAllMachinesByGroup(String group) {
        List<Machine> res = new ArrayList<Machine>();
        for (Machine physicalMachine : getAllMachines()) {
            if (physicalMachine.getGroup().contains(group)) {
                res.add(physicalMachine);
            }
        }
        return res;
    }

    @Override
    public List<Machine> getAllMachinesByIds(String... ids) {
        Set<String> nameSet = new HashSet<String>(Arrays.asList(ids));
        List<Machine> res = new ArrayList<Machine>();
        for (Machine physicalMachine : getAllMachines()) {
            if (nameSet.contains(physicalMachine.getId())) {
                res.add(physicalMachine);
            }
        }
        return res;
    }

    @Override
    public List<Application> getApplicationsByGroup(String group) {
        List<Application> res = new ArrayList<Application>();
        for (Machine physicalMachine : getAllMachines()) {
            for (Application application : physicalMachine.getAllApplications()) {
                if (application.getGroup().contains(group)) {
                    res.add(application);
                }
            }
        }
        return res;
    }

    @Override
    public void removeMachine(String id) {
        for (Iterator<Machine> machines = this.machines.iterator(); machines.hasNext(); ) {
            Machine p = machines.next();
            if (p.getId().equals(id)) {
                machines.remove();
            }
        }
    }

    @Override
    public void deploy() {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        if (cpuNum < 0 || cpuNum > 40) {
            cpuNum = 4;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(cpuNum * 2);
        String debug = System.getProperty("debug");
        if (!StringUtils.isBlank(debug) && debug.equalsIgnoreCase("true")) {
            timeout = timeout * 10;
        }
        ExcutorResultCollector resCollector = new ExcutorResultCollector(timeout);
        Collection<Application> apps = new ArrayList<Application>();
        for (Machine physicalMachine : getAllMachines()) {
            for (Application application : physicalMachine.getAllApplications()) {
                apps.add(application);
            }
        }
        for (Application application : apps) {
            ApplicationDeployExcutor excutor = new ApplicationDeployExcutor(application);
            Future<ExcutorResult> future = executorService.submit(excutor);
            resCollector.addFuture(application, future);
        }
        resCollector.waitAndCheckAllDeployReault();
        executorService.shutdown();
    }

    @Override
    public Application getApplicationById(String id) {
        for (Machine physicalMachine : getAllMachines()) {
            for (Application application : physicalMachine.getAllApplications()) {
                if (application.getId().equals(id)) {
                    return application;
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getAllMachineIps() {
        List<String> machines = new ArrayList<String>();
        if (this.machines != null) {
            for (Machine machine : this.machines) {
                if (!machine.isDisabled()) {
                    machines.add(machine.getIpAddress());
                }
            }
        }
        return machines;
    }
}

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
package com.alibaba.pelican.deployment.manager.loader;

import com.alibaba.pelican.deployment.configuration.properties.PropertiesUtil;
import com.alibaba.pelican.deployment.configuration.xstream.converter.ListCustomConverter;
import com.alibaba.pelican.deployment.configuration.xstream.converter.MapCustomConverter;
import com.alibaba.pelican.deployment.configuration.xstream.converter.StringTrimConverter;
import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.element.Machine;
import com.alibaba.pelican.deployment.element.Project;
import com.alibaba.pelican.deployment.element.impl.AbstractElement;
import com.alibaba.pelican.deployment.element.impl.DefaultApplication;
import com.alibaba.pelican.deployment.element.impl.DefaultMachine;
import com.alibaba.pelican.deployment.element.impl.entity.FlowAnnotationScanner;
import com.alibaba.pelican.deployment.exception.MachineCloneFailedException;
import com.alibaba.pelican.deployment.junit.rule.EnvironmentModeRule;
import com.alibaba.pelican.deployment.utils.FileUtils;
import com.alibaba.pelican.deployment.utils.SystemUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.mapper.DefaultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.*;

/**
 * @author moyun@middleware
 */
@Slf4j
public class ProjectConfigurationLoader {

    private FlowAnnotationScanner flowAnnotationFilter;

    private ProjectXstreamConfigurationLoader configurationLoader = new ProjectXstreamConfigurationLoader();

    public void setFlowAnnotationFilter(FlowAnnotationScanner flowAnnotationFilter) {
        this.flowAnnotationFilter = flowAnnotationFilter;
    }

    public Map<String, Project> loadAllConfigure() {
        Map<String, Project> testProjectMaps = new HashMap<String, Project>();
        Collection<Class<?>> customizedClass = new ArrayList<Class<?>>();
        Converter convert = new MapCustomConverter(
                new DefaultMapper(AbstractElement.class.getClassLoader()));
        configurationLoader.registerConverter(convert);
        convert = new ListCustomConverter(
                new DefaultMapper(AbstractElement.class.getClassLoader()));
        configurationLoader.registerConverter(convert);
        convert = new StringTrimConverter(
                new DefaultMapper(AbstractElement.class.getClassLoader()));
        configurationLoader.registerConverter(convert);

        customizedClass = flowAnnotationFilter.getAnnotationClassImpl(XStreamAlias.class);
        for (Class<?> clazz : customizedClass) {
            configurationLoader.addXstreamAnnotations(clazz);
            log.debug("Add Annotation Class:" + clazz.getName());
        }

        for (File configurationPath : getAllConfigurationPath()) {
            Project testProject = configurationLoader.deserialize(configurationPath);
            if (StringUtils.isBlank(PropertiesUtil.get(EnvironmentModeRule.ENV_MODE_KEY))
                    || testProject.getEnvironmentMode().equals(PropertiesUtil.get(EnvironmentModeRule.ENV_MODE_KEY))) {
                this.analyzeTestProject(testProject);
                log.debug(String.format("Parse envMode[%s] successfully!", testProject.getEnvironmentMode()));
            }
            testProjectMaps.put(testProject.getEnvironmentMode(), testProject);
        }
        return testProjectMaps;
    }

    private void analyzeTestProject(Project testProject) {
        Collection<String> toRemovedIds = new ArrayList<String>();
        List<Machine> machines = testProject.getAllMachines();
        for (Machine physicalMachine : machines) {
            List<String> ips = parseIpAddress(physicalMachine.getIpAddress());
            if (ips.size() == 1) {
                physicalMachine.setIpAddress(physicalMachine.getIpAddress().replaceAll(",", "").trim());
            } else {
                String defid = physicalMachine.getId();
                toRemovedIds.add(defid);
                for (int i = 0; i < ips.size(); i++) {
                    DefaultMachine newMachine = (DefaultMachine) physicalMachine
                            .cloneMachine();
                    if (newMachine == null) {
                        throw new MachineCloneFailedException(
                                String.format("Clone machine[%s:%s] failed!",
                                        physicalMachine.getIpAddress(), physicalMachine.getId()));
                    }
                    newMachine.setIpAddress(ips.get(i));
                    newMachine.setId(defid + i);
                    for (Application application : newMachine.getAllApplications()) {
                        DefaultApplication app = (DefaultApplication) application;
                        app.setId(app.getId() + i);
                    }
                    testProject.addMachine(newMachine);
                }
            }
        }

        for (String id : toRemovedIds) {
            testProject.removeMachine(id);
        }
    }

    private List<String> parseIpAddress(String ipAdress) {
        List<String> res = new ArrayList<String>();
        if (ipAdress.toLowerCase().startsWith("size")) {
            String[] items = ipAdress.split("=");
            String sizeStr = items[1];
            if (!NumberUtils.isDigits(sizeStr)) {
                throw new IllegalArgumentException("Wrong value of repetition ip, modify to [size=5] instead!");
            } else {
                int size = NumberUtils.toInt(sizeStr);
                for (int i = 0; i < size; i++) {
                    res.add(String.valueOf(i));
                }
            }
        } else {
            if (!ipAdress.contains(",")) {
                res.add(ipAdress);
                return res;
            }
            String[] items = ipAdress.split(",");
            for (String item : items) {
                res.add(item);
            }
        }
        return res;
    }

    private Collection<File> getAllConfigurationPath() {
        Collection<File> res = new LinkedList<File>();
        String rootPath = "";
        String specifiedPath = System.getProperties().getProperty("dtaf.conf.path");
        if (StringUtils.isNotBlank(specifiedPath)) {
            rootPath = specifiedPath;
            File funcDir = new File(rootPath);
            if (funcDir.exists() && funcDir.isDirectory()) {
                Collection<File> files = FileUtils.getCurrentPathFilesByKeyword(rootPath, ".xml");
                res.addAll(files);
            }
        } else {
            rootPath = PropertiesUtil.class.getResource("/").getPath() + "env";
            String funcPath = rootPath + "/func";
            Collection<File> files = null;
            File funcDir = new File(funcPath);
            if (funcDir.exists() && funcDir.isDirectory()) {
                files = FileUtils.getCurrentPathFilesByKeyword(funcPath, ".xml");
                res.addAll(files);
            }
        }

        if (res.isEmpty()) {
            SystemUtils.exit(String.format("No available xml file in [%s]!Please check if your dir is Chinese!", rootPath));
        }
        return res;
    }

}

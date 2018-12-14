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
package com.alibaba.pelican.deployment.manager.environment;

import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.deployment.configuration.properties.PropertiesUtil;
import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.element.Machine;
import com.alibaba.pelican.deployment.element.Project;
import com.alibaba.pelican.deployment.element.impl.annotation.*;
import com.alibaba.pelican.deployment.element.impl.entity.FlowAnnotationScanner;
import com.alibaba.pelican.deployment.exception.CmdClientConnectException;
import com.alibaba.pelican.deployment.exception.EnvironmentModeActiveFailedException;
import com.alibaba.pelican.deployment.exception.EnvironmentModeUndefinedException;
import com.alibaba.pelican.deployment.exception.IllegalConstructionMethodCustomizedOperationException;
import com.alibaba.pelican.deployment.junit.rule.EnvironmentModeRule;
import com.alibaba.pelican.deployment.manager.loader.ProjectConfigurationLoader;
import com.alibaba.pelican.deployment.utils.ReflectUtils;
import com.alibaba.pelican.deployment.utils.SystemUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author moyun@middleware
 */
@Slf4j
public class EnvironmentManager {

    private static EnvironmentManager instance;

    private String currentEnvMode;

    protected Map<String, Project> testProjectMaps = null;

    protected Map<String, RemoteCmdClient> remoteCmdClientMap = new HashMap<String, RemoteCmdClient>();

    protected Map<String, String> globalParameterMap = new HashMap<String, String>();

    private List<com.alibaba.pelican.deployment.manager.valid.ConfigurationValidator> configureFileRuleCheckers = new ArrayList<com.alibaba.pelican.deployment.manager.valid.ConfigurationValidator>();

    private ProjectConfigurationLoader configurationLoader;

    private FlowAnnotationScanner flowAnnotationFilter = new FlowAnnotationScanner();

    private EnvironmentManager() {
        configurationLoader = new ProjectConfigurationLoader();
        configurationLoader.setFlowAnnotationFilter(flowAnnotationFilter);
        init();
    }

    public static synchronized EnvironmentManager getInstance() {
        if (instance == null) {
            instance = new EnvironmentManager();
        }
        return instance;
    }

    public boolean containsRemoteCmdClient(String key) {
        return remoteCmdClientMap.containsKey(key);
    }

    public void addRemoteCmdClient(String key, RemoteCmdClient client) {
        remoteCmdClientMap.put(key, client);
    }

    public void init() {
        printPid();
        initCusAnnotationClass();
        initConfFileChecker();
        initTestProjectFile();
        closeRemoteCmdClient();
    }

    private void closeRemoteCmdClient() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (RemoteCmdClient client : remoteCmdClientMap.values()) {
                    client.close();
                    log.info("Close ssh remoteCmdClient[" + client.getIp() + "]");
                }
            }
        });
    }

    private void initCusAnnotationClass() {
        ReflectUtils.parseAnnotationClass(flowAnnotationFilter);
    }

    private void printPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        log.info("INFO.processId=" + pid);
    }

    protected void initTestProjectFile() {
        testProjectMaps = configurationLoader.loadAllConfigure();
    }

    public Project getDefaultTestProject() {
        Project testProject = null;
        if (testProjectMaps.size() != 1) {
            String message = "The evnModes which configured in path[./env/func,./env/perf] are not unique, dtaf can't define which one to be load in test runtime, please define your envMode first!";
            log.error(message);
            throw new EnvironmentModeUndefinedException(message);
        }
        int count = 0;
        for (String name : testProjectMaps.keySet()) {
            testProject = testProjectMaps.get(name);
            count++;
            if (count >= 2) {
                String message = "The evnModes which configured in path[./env/func,./env/perf] are not unique, dtaf can't define which one to be load in test runtime, please define your envMode first!";
                log.error(message);
                throw new EnvironmentModeUndefinedException(message);
            }
        }
        if (!testProject.isActived()) {
            doCustomizedBeforeActive();
            activedTestProject(testProject);
            doCustomizedAfterActive();
        }
        return testProject;
    }

    public Project getTestProjectNoActived(String envMode) {
        return testProjectMaps.get(envMode);
    }

    /**
     * 获取的测试工程
     *
     * @param envMode
     * @return
     */
    public Project getTestProject(String envMode) {
        Project testProject = testProjectMaps.get(envMode);
        if (testProject == null) {
            String message = String.format(
                    "No evnMode named[%s] in TestEnvironmentManager, please check your test project config file!",
                    envMode);
            SystemUtils.exit(message);
        }
        if (!testProject.isActived()) {
            doCustomizedBeforeActive();
            activedTestProject(testProject);
            doCustomizedAfterActive();

            log.info(String.format("Set current EnvMode as [%s]", envMode));
            deployProjectEnv(testProject);
        }
        return testProject;
    }

    public void addGlobalParameter(String key, String value) {
        globalParameterMap.put(key, value);
    }

    public String getGlobalParameter(String key) {
        return globalParameterMap.get(key);
    }

    public Collection<Project> getAllTestProjects() {
        return testProjectMaps.values();
    }

    public String getCurrentEnvMode() {
        return currentEnvMode;
    }

    public void setCurrentEnvMode(String currentEnvMode) {
        this.currentEnvMode = currentEnvMode;
    }

    private void initConfFileChecker() {
        Collection<Class<?>> customizedClass = this.flowAnnotationFilter
                .getAnnotationClassImpl(ConfigurationValidator.class);
        for (Class<?> clazz : customizedClass) {
            com.alibaba.pelican.deployment.manager.valid.ConfigurationValidator checker = null;
            try {
                checker = (com.alibaba.pelican.deployment.manager.valid.ConfigurationValidator) clazz.newInstance();
                configureFileRuleCheckers.add(checker);
            } catch (Exception e) {
                log.error(
                        String.format("Load configure file checker[%s] failed!", clazz.getName()),
                        e);
            }
        }
    }

    private void activedTestProject(Project testProject) {
        List<Machine> machines = testProject.getAllMachines();
        for (Machine physicalMachine : machines) {
            RemoteCmdClient client;
            try {
                if (remoteCmdClientMap.containsKey(physicalMachine.getIpAddress())) {
                    client = remoteCmdClientMap.get(physicalMachine.getIpAddress());
                } else {
                    client = connectServer(physicalMachine.getIpAddress(),
                            physicalMachine.getUserName(), physicalMachine.getPassword(),
                            physicalMachine.getTimeout());
                }
                physicalMachine.setRemoteCmdClient(client);
                List<Application> applications = physicalMachine.getAllApplications();
                for (Application application : applications) {
                    application.setRemoteCmdClient(client);
                    injectionDtafValue(application, testProject);
                }
            } catch (Exception e) {
                log.error(String.format(
                        "Can't connect to server[%s] in envMode[%s],please check config file!",
                        physicalMachine.getSSHID(), testProject.getEnvironmentMode()), e);
                String message = String.format(
                        "Active envMode[%s] failed because of can't connect to server[%s]",
                        testProject.getEnvironmentMode(), physicalMachine.getSSHID());
                throw new EnvironmentModeActiveFailedException(message);
            }
        }
        testProject.init();
        testProject.setActived(true);
    }

    private void injectionDtafValue(Application application, Project testProject) {
        Field[] fields = application.getClass().getDeclaredFields();
        for (Field field : fields) {
            DtafAutowired clazz = field.getAnnotation(DtafAutowired.class);
            if (clazz == null) {
                continue;
            }
            String key = field.getName();
            if (StringUtils.isNotBlank(clazz.value())) {
                key = clazz.value();
            }
            try {
                String valueStr = testProject.getCustomizedVariable(key);
                String dtafValue = PropertiesUtil.get(key);
                if (dtafValue != null) {
                    valueStr = dtafValue;
                }
                if (valueStr == null) {
                    log.warn(String.format("DtafAutowired [%s] failed in application[%s]:No value found in properties file!", key, application.getId()));
                    continue;
                }
                Class<?> typeClass = field.getType();
                field.setAccessible(true);
                if (typeClass.equals(int.class) || typeClass.equals(Integer.class)) {
                    int value = Integer.parseInt(valueStr);
                    field.set(application, value);
                } else if (typeClass.equals(long.class) || typeClass.equals(Long.class)) {
                    long value = Long.parseLong(valueStr);
                    field.set(application, value);
                } else if (typeClass.equals(float.class) || typeClass.equals(Float.class)) {
                    float value = Float.parseFloat(valueStr);
                    field.set(application, value);
                } else if (typeClass.equals(boolean.class) || typeClass.equals(Boolean.class)) {
                    boolean value = Boolean.parseBoolean(valueStr);
                    field.set(application, value);
                } else if (typeClass.equals(double.class) || typeClass.equals(Double.class)) {
                    double value = Double.parseDouble(valueStr);
                    field.set(application, value);
                } else if (typeClass.equals(String.class)) {
                    String value = valueStr;
                    field.set(application, value);
                }
            } catch (Exception e) {
                log.error(String.format("DtafAutowired [%s] failed in application[%s]:%s!", key,
                        application.getId(), e.getMessage()), e);

            }
        }
    }

    private RemoteCmdClient connectServer(String ip, String name, String psswd, String timeout)
            throws Exception {
        String key = ip + "@" + name;
        if (remoteCmdClientMap.containsKey(key)) {
            return remoteCmdClientMap.get(key);
        }
        RemoteCmdClientConfig remoteCmdClientConfig = new RemoteCmdClientConfig();
        remoteCmdClientConfig.setIp(ip);
        remoteCmdClientConfig.setUserName(name);
        remoteCmdClientConfig.setPassword(psswd);
        if (NumberUtils.isDigits(timeout)) {
            remoteCmdClientConfig.setCoTimeout(NumberUtils.toInt(timeout));
        }
        RemoteCmdClient remoteCmdClient = new RemoteCmdClient(remoteCmdClientConfig);

        if (remoteCmdClient.isReady()) {
            log.debug(String.format("connect to server[%s] successfully!", remoteCmdClientConfig.getIp()));
        } else {
            log.error(String.format("connect to server[%s] failed!", remoteCmdClientConfig.getIp()));
            throw new CmdClientConnectException(String.format(
                    "Can't connection to server[%s],Test failed and exit!", remoteCmdClientConfig.getIp()));
        }
        remoteCmdClientMap.put(key, remoteCmdClient);
        return remoteCmdClient;
    }

    private void deployProjectEnv(Project testProject) {
        if (!EnvironmentModeRule.getDEPLOY_SKIP()) {
            doCustomizedBeforeDeploy();
            log.info("Deploy application...");
            try {
                log.info("\r\nINFO.deployState=Start");
                testProject.deploy();
                System.setProperty(EnvironmentModeRule.DEPLOY_STATE, "Success");
            } catch (Throwable e) {
                SystemUtils.exit(e,
                        "Deploy End... deploy failed,check deploy log first please!");
                System.setProperty(EnvironmentModeRule.DEPLOY_STATE, "Failed");
            }
            log.info("INFO.deployState=End");
            doCustomizedAfterDeploy();
        } else {
            log.info("INFO.deployState=Skip");
        }
        log.info("Deploy End...");
        doCustomizedEnvCheck();// 用户自定义初始化回调，不受deploySkip影响
    }

    private void doCustomizedAfterActive() {
        Collection<Class<?>> classes = this.flowAnnotationFilter
                .getAnnotationClassImpl(AfterActive.class);
        doCustomizedOperation(classes, "AfterActive");
    }

    private void doCustomizedBeforeActive() {
        Collection<Class<?>> classes = this.flowAnnotationFilter
                .getAnnotationClassImpl(BeforeActive.class);
        doCustomizedOperation(classes, "BeforeActive");
    }

    private void doCustomizedBeforeDeploy() {
        Collection<Class<?>> classes = this.flowAnnotationFilter
                .getAnnotationClassImpl(BeforeDeploy.class);
        doCustomizedOperation(classes, "BeforeDeploy");
    }

    private void doCustomizedAfterDeploy() {
        Collection<Class<?>> classes = this.flowAnnotationFilter
                .getAnnotationClassImpl(AfterDeploy.class);
        doCustomizedOperation(classes, "AfterDeploy");
    }

    private void doCustomizedEnvCheck() {
        Collection<Class<?>> classes = this.flowAnnotationFilter
                .getAnnotationClassImpl(EnvironmentValidator.class);
        doCustomizedOperation(classes, "EnvCheck");
    }

    private void doCustomizedOperation(Collection<Class<?>> customizedClass, String annotation) {
        for (Class<?> clazz : customizedClass) {
            Constructor<?> con;
            try {
                con = clazz.getConstructor(EnvironmentManager.class);
                con.newInstance(this);
            } catch (NoSuchMethodException e) {
                String errorInfo = String.format(
                        "Test failed!No match construction method with parameter[TestEnvironmentManager] found in class[%s],Please check your customized class first!",
                        clazz.getName());
                log.error(errorInfo, e);
                throw new IllegalConstructionMethodCustomizedOperationException(
                        "Test failed when init customized " + annotation + " operation！");
            } catch (SecurityException e) {
                String errorInfo = String.format(
                        "Test failed!No match construction method with parameter[TestEnvironmentManager] access in class[%s] because of security,Please check your customized class first!",
                        clazz.getName());
                log.error(errorInfo, e);
                throw new IllegalConstructionMethodCustomizedOperationException(
                        "Test failed when init customized " + annotation + " operation！");
            } catch (InstantiationException e) {
                log.error("Execute cunstomized " + annotation + " operation failed!", e);
                throw new IllegalConstructionMethodCustomizedOperationException(
                        "Test failed when execute customized " + annotation + " operation！");
            } catch (IllegalAccessException e) {
                log.error("Execute cunstomized " + annotation + " operation failed!", e);
                throw new IllegalConstructionMethodCustomizedOperationException(
                        "Test failed when execute customized " + annotation + " operation！");
            } catch (IllegalArgumentException e) {
                log.error("Execute cunstomized " + annotation + " operation failed!", e);
                throw new IllegalConstructionMethodCustomizedOperationException(
                        "Test failed when execute customized " + annotation + " operation！");
            } catch (InvocationTargetException e) {
                log.error("Execute cunstomized " + annotation + " operation failed!", e);
                throw new IllegalConstructionMethodCustomizedOperationException(
                        "Test failed when execute customized " + annotation + " operation！");
            }
        }
    }
}

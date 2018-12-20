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

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.utils.MemUtils;
import com.alibaba.pelican.deployment.configuration.holder.ConfigurationHolder;
import com.alibaba.pelican.deployment.configuration.manager.ConfigurationHolderManager;
import com.alibaba.pelican.deployment.configuration.manager.impl.DefaultConfigurationHolderManager;
import com.alibaba.pelican.deployment.configuration.operator.ConfigurationOperator;
import com.alibaba.pelican.deployment.configuration.operator.impl.StringConfigurationOperatorImpl;
import com.alibaba.pelican.deployment.configuration.properties.impl.PropertiesConfigurationOperatorImpl;
import com.alibaba.pelican.deployment.configuration.spring.impl.SpringConfigurationeOperatorImpl;
import com.alibaba.pelican.deployment.configuration.xml.impl.XmlConfigurationOperatorImpl;
import com.alibaba.pelican.deployment.configuration.xstream.impl.XstreamConfigurationOperatorImpl;
import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.element.impl.entity.Configuration;
import com.alibaba.pelican.deployment.utils.CommonUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.*;
import java.util.Comparator;
import java.util.List;

/**
 * @author moyun@middleware
 */
@XStreamAlias("application")
@Slf4j
public class DefaultApplication extends AbstractElement implements Application {

    protected String deployScriptPath;

    protected String startScriptPath;

    protected String killProcessMark;

    protected boolean deploySkip = false;

    protected boolean confSkip = false;

    protected boolean confBeforeSkip = false;

    protected boolean confAfterSkip = false;

    protected int priority = 5;

    protected boolean remoteCmdClientOutput = false;

    @XStreamAlias("ConfigFiles")
    @XStreamImplicit
    protected List<Configuration> configurations;

    @XStreamOmitField
    protected RemoteCmdClient remoteCmdClient;

    @XStreamOmitField
    protected ConfigurationHolderManager configurationManager;

    @Override
    public ConfigurationHolderManager getConfigurationHolderManager() {
        return configurationManager;
    }

    @Override
    public RemoteCmdClient getRemoteCmdClient() {
        return remoteCmdClient;
    }

    @Override
    public void setRemoteCmdClient(RemoteCmdClient remoteCmdClient) {
        this.remoteCmdClient = remoteCmdClient;
    }

    @Override
    public boolean isRemoteCmdClientOutputEnabled() {
        return this.remoteCmdClientOutput;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("ID", id).toString();
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
    public boolean isReady() {
        if (remoteCmdClient == null) {
            return true;
        }
        String res = remoteCmdClient.getPID(this.killProcessMark);
        if (StringUtils.isBlank(this.killProcessMark) || StringUtils.isNotBlank(res)) {
            return true;
        }
        return false;
    }

    @Override
    public void restart() {
        stop();
        CommonUtils.waitForSeconds(3);
        start();
    }

    @Override
    public void restart(Object arg) {
        stop();
        CommonUtils.waitForSeconds(3);
        start(arg);
    }

    @Override
    public void start() {
        if (remoteCmdClient != null && remoteCmdClient.isReady()) {
            remoteCmdClient.scpAndExecScript(startScriptPath);
        } else {
            log.warn(String.format("No ssh remoteCmdClient for application [%s],can not execute start method!", id));
        }
    }

    @Override
    public void start(Object arg) {
        start();
    }

    @Override
    public void stop() {
        if (remoteCmdClient != null && remoteCmdClient.isReady()) {
            remoteCmdClient.killProcess(this.killProcessMark);

        } else {
            log.warn(String.format("No ssh remoteCmdClient for application [%s],can not excute stop method!", id));
        }
    }

    @Override
    public void init() {
        super.init();
        initConfigurationHolder();
    }

    public void initConfigurationHolder() {
        if (configurations == null || configurations.isEmpty()) {
            return;
        }
        configurationManager = new DefaultConfigurationHolderManager();
        for (Configuration config : configurations) {
            String className = config.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.newInstance();
                if (!(obj instanceof ConfigurationHolder)) {
                    throw new IllegalArgumentException(
                            String.format("The holder class must implements the interface[ConfigurationFileHolder] in application[%s]", id));
                }
                ConfigurationHolder holder = (ConfigurationHolder) obj;
                ConfigurationOperator operator = null;
                if (config.getType().equalsIgnoreCase("PROP")) {
                    operator = new PropertiesConfigurationOperatorImpl();
                } else if (config.getType().equalsIgnoreCase("SPRING")) {
                    operator = new SpringConfigurationeOperatorImpl();
                } else if (config.getType().equalsIgnoreCase("XML")) {
                    operator = new XmlConfigurationOperatorImpl();
                } else if (config.getType().equalsIgnoreCase("XSTREAM")) {
                    operator = new XstreamConfigurationOperatorImpl();
                } else if (config.getType().equalsIgnoreCase("STRING")) {
                    operator = new StringConfigurationOperatorImpl();
                } else {
                    operator = new StringConfigurationOperatorImpl();
                }
                holder.setConfigurationOperator(operator);
                holder.setRemoteCmdClient(remoteCmdClient);
                holder.setParams(properties);
                configurationManager.register(holder.getConfigurationName(), holder);
            } catch (ClassNotFoundException e) {
                String error = String.format("No class found with name[%s] in config file of application[%s]", className, id);
                log.error(error);
                throw new IllegalArgumentException(error);
            } catch (InstantiationException e) {
                String error = String.format("Init file holder[%s] failed in config file of application[%s]", className, id);
                log.error(error);
                throw new IllegalArgumentException(error);
            } catch (IllegalAccessException e) {
                String error = String.format("Init file holder[%s] failed in config file of application[%s]", className, id);
                log.error(error);
                throw new IllegalArgumentException(error);
            } catch (SecurityException e) {
                String error = String.format("Init file holder[%s] failed in config file of application[%s]", className, id);
                log.error(error);
                throw new IllegalArgumentException(error);
            } catch (IllegalArgumentException e) {
                String error = String.format("Init file holder[%s] failed in config file of application[%s]", className, id);
                log.error(error);
                throw new IllegalArgumentException(error);
            }
        }
    }

    public static class ApplicationPriority implements Comparator<Application> {
        @Override
        public int compare(Application a1, Application a2) {
            return a2.getPriority() - a1.getPriority();
        }
    }

    public void setDeployScriptPath(String deployScriptPath) {
        this.deployScriptPath = deployScriptPath;
    }

    @Override
    public String getDeployScriptPath() {
        return deployScriptPath;
    }

    @Override
    public void deploy() {
        if (remoteCmdClient != null && remoteCmdClient.isReady()) {
            remoteCmdClient.scpAndExecScript(deployScriptPath);
        } else {
            log.warn(String.format("No ssh remoteCmdClient for application [%s],can not excute deploy method!", id));
        }
    }

    @Override
    public String getAppDescribe() {
        return remoteCmdClient == null ? "" : String.format("%s:%s", id, remoteCmdClient.getIp());
    }

    @Override
    public Application clone() {
        DefaultApplication res = null;
        ObjectOutputStream oo;
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            oo = new ObjectOutputStream(bo);
            oo.writeObject(this);
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
            ObjectInputStream oi = new ObjectInputStream(bi);
            res = (DefaultApplication) oi.readObject();
            res.setProperties(properties);
            res.setVariables(variables);
            res.setGroup(group);
        } catch (Exception e) {
            log.error(String.format("Clone application [%s:%s] failed!", getRemoteCmdClient().getIp(), id), e);
        }
        return res;
    }
}

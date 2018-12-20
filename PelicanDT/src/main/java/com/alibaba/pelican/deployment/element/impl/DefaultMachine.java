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

import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.deployment.element.Application;
import com.alibaba.pelican.deployment.element.Machine;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

/**
 * @author moyun@middleware
 */
@XStreamAlias("machine")
@Slf4j
public class DefaultMachine extends AbstractElement implements Machine {

    @XStreamAlias("applications")
    @XStreamImplicit
    protected List<Application> applications = new ArrayList<Application>();

    @XStreamOmitField
    protected List<Application> enabledApplications;

    protected String ipAddress = "";
    protected String userName = "";
    protected String password = "";
    protected Integer timeout = 600 * 1000;

    @XStreamOmitField
    transient protected RemoteCmdClient remoteCmdClient;

    @XStreamOmitField
    transient protected Map<String, Application> cacheApplicationNameMap = new HashMap<String, Application>();

    @Override
    public void init() {
        super.init();
        RemoteCmdClientConfig connectUnit = new RemoteCmdClientConfig();
        connectUnit.setIp(ipAddress);
        connectUnit.setUserName(userName);
        connectUnit.setPassword(password);
        this.remoteCmdClient = new RemoteCmdClient(connectUnit);
        for (Application application : getAllApplications()) {
            application.setRemoteCmdClient(remoteCmdClient);
            application.init();
        }
    }

    @Override
    public RemoteCmdClient getRemoteCmdClient() {
        return remoteCmdClient;
    }

    @Override
    public Application getApplicationByPath(String applicationName) {
        if (cacheApplicationNameMap == null || cacheApplicationNameMap.size() == 0
                || cacheApplicationNameMap.get(applicationName) == null) {
            initCacheMap();
        }
        return cacheApplicationNameMap.get(applicationName);
    }

    private synchronized void initCacheMap() {
        if (cacheApplicationNameMap == null) {
            cacheApplicationNameMap = new HashMap<String, Application>();
        }
        cacheApplicationNameMap.clear();
        for (Application application : applications) {
            cacheApplicationNameMap.put(application.getId(), application);
        }
    }

    @Override
    public String getSSHID() {
        return userName + "@" + ipAddress;
    }

    @Override
    public String getUserHome() {
        return "/home/" + userName;
    }

    public List<Application> getEnabledApplications() {
        return enabledApplications;
    }

    public List<Application> getDisabledApplications() {
        if (applications == null) {
            applications = new ArrayList<Application>();
        }
        List<Application> disabledApplications = new ArrayList<Application>();
        for (Application application : applications) {
            if (application.isDisabled()) {
                disabledApplications.add(application);
            }
        }
        return disabledApplications;
    }

    public void setEnabledApplications(List<Application> enabledApplications) {
        this.enabledApplications = enabledApplications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, Application> getCacheApplicationNameMap() {
        return cacheApplicationNameMap;
    }

    public void setCacheApplicationNameMap(Map<String, Application> cacheApplicationNameMap) {
        this.cacheApplicationNameMap = cacheApplicationNameMap;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("ID", id).append("ip", ipAddress)
                .append("user", userName).append("applications", applications).toString();
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
    public void setRemoteCmdClient(RemoteCmdClient client) {
        this.remoteCmdClient = client;
    }

    @Override
    public DefaultMachine cloneMachine() {
        DefaultMachine res = null;
        try {
            res = (DefaultMachine) super.clone();
            List<Application> newapplications = new ArrayList<Application>();
            for (Application app : applications) {
                DefaultApplication newapp = (DefaultApplication) app.clone();
                newapp.setId(app.getId());
                newapplications.add(newapp);
            }
            res.setApplications(newapplications);
        } catch (CloneNotSupportedException e) {
            log.error(String.format("Clone machine[%s] failed!", getSSHID()), e);
        }
        return res;
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public List<Application> getAllApplications() {
        if (applications == null) {
            applications = new ArrayList<Application>();
        }
        if (enabledApplications == null || enabledApplications.isEmpty()) {
            enabledApplications = new ArrayList<Application>();
            for (Application application : applications) {
                if (!application.isDisabled()) {
                    enabledApplications.add(application);
                }
            }
        }
        return enabledApplications;
    }

    @Override
    public List<Application> getAllCustomizedApplications(Class<?> clazz) {
        List<Application> res = new ArrayList<Application>();
        for (Application app : getAllApplications()) {
            if (app.getClass().equals(clazz)) {
                res.add(app);
            }
        }
        return res;
    }

    @Override
    public Application getApplicationById(String id) {
        for (Application app : getAllApplications()) {
            if (app.getId().equals(id)) {
                return app;
            }
        }
        return null;
    }

    @Override
    public List<Application> getApplicationsByIDs(String... ids) {
        Set<String> nameSet = new HashSet<String>(Arrays.asList(ids));
        List<Application> res = new ArrayList<Application>();
        for (Application app : getAllApplications()) {
            if (nameSet.contains(app.getId())) {
                res.add(app);
            }
        }
        return res;
    }
}

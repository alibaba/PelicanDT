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

package com.alibaba.pelican.chaos.client.group;

import com.alibaba.pelican.chaos.client.RemoteCmd;
import com.alibaba.pelican.chaos.client.RemoteCmdClientConfig;
import com.alibaba.pelican.chaos.client.RemoteCmdResult;
import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.task.*;
import com.alibaba.pelican.chaos.client.ICmdGroupExecutor;
import com.alibaba.pelican.chaos.client.task.*;
import com.alibaba.pelican.chaos.client.task.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.PipedInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author moyun@middleware
 */
@Slf4j
public class RemoteCmdClientGroup implements ICmdGroupExecutor {

    private static final String RETURN_TYPE_VOID = "void";

    private String groupName;

    private Set<String> ipSet = new TreeSet<String>();

    private Map<String, RemoteCmdClient> clientMap = new ConcurrentHashMap<String, RemoteCmdClient>();

    private String userName;

    private String password;

    private boolean isReady = false;

    private ExecutorService threadPool = new ThreadPoolExecutor(5, 200,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(1024), new BasicThreadFactory.Builder().namingPattern("simple-remote-group-remoteCmdClient-%d")
            .build(), new ThreadPoolExecutor.AbortPolicy());

    public RemoteCmdClientGroup(String groupName, Set<String> ipSet, String userName, String password) {
        this.groupName = groupName;
        this.userName = userName;
        this.password = password;
        this.ipSet.addAll(ipSet);
        RemoteCmdClientConfig connectUnit = new RemoteCmdClientConfig();
        connectUnit.setUserName(userName);
        connectUnit.setPassword(password);
        createClientMapAndConnect(connectUnit);
    }

    public RemoteCmdClientGroup(Set<String> ipSet, String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.ipSet.addAll(ipSet);
        RemoteCmdClientConfig connectUnit = new RemoteCmdClientConfig();
        connectUnit.setUserName(userName);
        connectUnit.setPassword(password);
        createClientMapAndConnect(connectUnit);
    }

    public RemoteCmdClientGroup(Set<? extends RemoteCmdClientConfig> connectUnits) {
        createClientMapAndConnect(connectUnits);
    }

    public RemoteCmdClientGroup(String groupName, Set<? extends RemoteCmdClientConfig> connectUnits) {
        this.groupName = groupName;
        createClientMapAndConnect(connectUnits);
    }

    private void createClientMapAndConnect(RemoteCmdClientConfig connectUnit) {
        for (String ip : ipSet) {
            connectUnit.setIp(ip);
            RemoteCmdClient client = new RemoteCmdClient(connectUnit);
            if (!client.isReady()) {
                removeAll();
                log.info("Connect to {} failed, " + "create remoteCmdClient group failed.", ip);
                return;
            }
            clientMap.put(ip, client);
        }
        setReady(true);
        log.info("Connect to {} ok.", ipSet);
        return;
    }

    private void createClientMapAndConnect(Set<? extends RemoteCmdClientConfig> connectUnits) {
        RemoteCmdClient client;
        for (RemoteCmdClientConfig connectUnit : connectUnits) {
            client = new RemoteCmdClient(connectUnit);
            if (!client.isReady()) {
                removeAll();
                log.info("Connect to {} failed, create remoteCmdClient group failed.", connectUnit.getIp());
                return;
            }
            ipSet.add(client.getIp());
            clientMap.put(connectUnit.getIp(), client);
        }
        setReady(true);
        log.info("Connect to {} ok.", ipSet);
        return;
    }

    public boolean connectAll() {
        for (String ip : ipSet) {
            RemoteCmdClient client = getClientByIP(ip);
            if (!client.isReady()) {
                client.initClient();
                if (!client.isReady()) {
                    log.info("Connect to failed.", ip);
                    setReady(false);
                    return false;
                }
            }
        }
        setReady(true);
        return true;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getPassword() {
        return this.password;
    }

    public Set<String> getIpSet() {
        return this.ipSet;
    }

    public Map<String, RemoteCmdClient> getClientMap() {
        return this.clientMap;
    }

    public RemoteCmdClient getClientByIP(String ip) {
        return this.clientMap.get(ip);
    }

    public boolean addClient(String ip) {
        if (StringUtils.isBlank(password) || StringUtils.isBlank(userName)) {
            log.error("password or userName is blank, please use connectUnit for add a remoteCmdClient.");
            return false;
        }
        RemoteCmdClientConfig connectUnit = new RemoteCmdClientConfig();
        connectUnit.setIp(ip);
        connectUnit.setPassword(password);
        connectUnit.setUserName(userName);
        RemoteCmdClient client = new RemoteCmdClient(connectUnit);

        if (!client.isReady()) {
            log.error("Connect to {} failed, add failed.", ip);
            return false;
        }
        this.ipSet.add(client.getIp());
        this.clientMap.put(client.getIp(), client);
        return true;
    }

    public boolean addClient(RemoteCmdClientConfig connectUnit) {
        RemoteCmdClient client = new RemoteCmdClient(connectUnit);
        if (!client.isReady()) {
            log.error("Connect to {} failed, add failed.", connectUnit.getIp());
            return false;
        }
        this.ipSet.add(client.getIp());
        this.clientMap.put(client.getIp(), client);
        return true;
    }

    public boolean addClient(RemoteCmdClient client) {
        if (client == null) {
            return false;
        }
        if (!client.isReady()) {
            log.error("Connect to {} failed, add failed.", client.getIp());
            return false;
        }
        this.ipSet.add(client.getIp());
        this.clientMap.put(client.getIp(), client);
        return true;
    }

    public void removeClient(String ip) {
        this.ipSet.remove(ip);
        if (this.clientMap.get(ip) != null) {
            this.clientMap.get(ip).close();
        }
        this.clientMap.remove(ip);
        if (ipSet.isEmpty()) {
            setReady(false);
        }
    }

    public void removeAll() {
        closeAll();
        clientMap.clear();
        ipSet.clear();
    }

    public void closeAll() {
        for (String ip : ipSet) {
            if (clientMap.containsKey(ip)) {
                clientMap.get(ip).close();
            }
        }
        setReady(false);
    }

    public boolean isReady() {
        return isReady;
    }

    private void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    private class TaskVoid implements Runnable {

        public Object[] args;

        public Method method;

        public RemoteCmdClient client;

        public TaskVoid(Method method, Object[] args, String ip) {
            this.args = args;
            this.method = method;
            this.client = getClientByIP(ip);
        }

        @Override
        public void run() {
            try {
                method.invoke(client, args);
            } catch (IllegalArgumentException e) {
                log.error("IllegalArgumentException, Arguments are Illegal.");
            } catch (IllegalAccessException e) {
                log.error("IllegalAccessException, can not access this method.");
            } catch (InvocationTargetException e) {
                log.error("InvocationTargetException, " + "this is an illegal invocable target.");
            }
        }
    }

    @Override
    public Map<String, Object> execCmdByName(String methodName, Object... args) {
        Class<?>[] argsClass = new Class[args.length];

        for (int i = 0, j = args.length; i < j; i++) {
            if (args[i].getClass() == Integer.class) {
                argsClass[i] = int.class;
            } else if (args[i].getClass() == Boolean.class) {
                argsClass[i] = boolean.class;
            } else if (args[i].getClass() == Long.class) {
                argsClass[i] = long.class;
            } else {
                argsClass[i] = args[i].getClass();
            }
        }

        Class<?> ownerClass;
        try {
            ownerClass = Class.forName("com.alibaba.pelican.chaos.client.impl.RemoteCmdClient");
            Method m = ownerClass.getMethod(methodName, argsClass);
            Class<?> returnType = m.getReturnType();
            if (!RETURN_TYPE_VOID.equals(returnType.getName())) {
                List<LetCmdTask> taskList = new ArrayList<LetCmdTask>();
                for (String ip : ipSet) {
                    taskList.add(new LetCmdTask(m, args, getClientByIP(ip)));
                }
                return ParallelExecutor.execCmdByTask(taskList);
            } else {

                for (String ip : ipSet) {
                    threadPool.execute(new TaskVoid(m, args, ip));
                }
                threadPool.shutdown();
                threadPool.awaitTermination(1, TimeUnit.HOURS);
                return Collections.emptyMap();
            }

        } catch (ClassNotFoundException e) {
            log.error("execCmdByName ClassNotFoundException");
        } catch (SecurityException e) {
            log.error("execCmdByName SecurityException");
        } catch (NoSuchMethodException e) {
            StringBuilder argInfo = new StringBuilder();
            for (int i = 0; i < argsClass.length; i++) {
                argInfo.append(argsClass[i].getName());
                argInfo.append(" ");
            }
            log.error("No Such a Method " + methodName + ", args: " + argInfo);
        } catch (InterruptedException e) {
            log.error("execCmdByName awaitTermination, InterruptedException has occured.");
        }

        return Collections.emptyMap();

    }

    @Override
    public Map<String, RemoteCmdResult> execCmdWithPTY(RemoteCmd cmd) {
        List<ExecCmdWithPtyTask> tasks = new ArrayList<ExecCmdWithPtyTask>();
        for (String ip : ipSet) {
            tasks.add(new ExecCmdWithPtyTask(getClientByIP(ip), cmd));
        }
        return ParallelExecutor.execCmdByTask(tasks);
    }

    @Override
    public Map<String, RemoteCmdResult> execCmdWithPTY(RemoteCmd cmd, Map<String, PipedInputStream> pisMap) {
        List<ExecCmdWithPtyTask> tasks = new ArrayList<ExecCmdWithPtyTask>();
        for (String ip : ipSet) {
            PipedInputStream pis = pisMap.get(ip);
            if (pis != null) {
                tasks.add(new ExecCmdWithPtyTask(getClientByIP(ip), cmd, pisMap.get(ip)));
            }
        }
        return ParallelExecutor.execCmdByTask(tasks);
    }

    @Override
    public Map<String, Boolean> execCmdBlockWithPTY(RemoteCmd cmd, Map<String, PipedInputStream> pisMap) {
        List<ExecCmdBlockWithPtyTask> tasks = new ArrayList<ExecCmdBlockWithPtyTask>();
        for (String ip : ipSet) {
            PipedInputStream pis = pisMap.get(ip);
            if (pis != null) {
                tasks.add(new ExecCmdBlockWithPtyTask(getClientByIP(ip), cmd, pisMap.get(ip)));
            }
        }
        return ParallelExecutor.execCmdByTask(tasks);
    }

    @Override
    public Map<String, String> execCmdForResult(RemoteCmd cmd) {
        List<ExecCmdTask> tasks = new ArrayList<ExecCmdTask>();
        for (String ip : ipSet) {
            tasks.add(new ExecCmdTask(getClientByIP(ip), cmd));
        }
        return ParallelExecutor.execCmdByTask(tasks);
    }

    @Override
    public Map<String, Boolean> uploadFile(String srcFile, String destDir) {
        List<UploadFileTask> tasks = new ArrayList<UploadFileTask>();
        for (String ip : ipSet) {
            tasks.add(new UploadFileTask(getClientByIP(ip), srcFile, destDir));
        }
        return ParallelExecutor.execCmdByTask(tasks);
    }
}

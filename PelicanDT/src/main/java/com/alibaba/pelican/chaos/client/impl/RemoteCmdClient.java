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

package com.alibaba.pelican.chaos.client.impl;

import com.alibaba.pelican.chaos.client.*;
import com.alibaba.pelican.chaos.client.cmd.*;
import com.alibaba.pelican.chaos.client.cmd.event.CmdEvent;
import com.alibaba.pelican.chaos.client.debug.ClientDebugDisplayCallable;
import com.alibaba.pelican.chaos.client.debug.ClientDebugInputCallable;
import com.alibaba.pelican.chaos.client.exception.ConnectException;
import com.trilead.ssh2.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author moyun@middleware
 */
@Slf4j
public class RemoteCmdClient implements ICmdExecutor {

    private static final String DIR_NAME = "dir_name";
    private static final String FILE_NAME = "file_name";

    private static ExecutorService threadPool = new ThreadPoolExecutor(5, 200,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(1024), new BasicThreadFactory.Builder().namingPattern("parallel-executor-%d")
            .build(), new ThreadPoolExecutor.AbortPolicy());

    private Connection connection;

    private RemoteCmdClientConfig remoteCmdClientConfig;

    private boolean ready = false;

    public RemoteCmdClient(RemoteCmdClientConfig connectUnit) {
        this.remoteCmdClientConfig = connectUnit;
        initClient();
    }

    public RemoteCmdClient(String ip, String userName, String passWord) {
        remoteCmdClientConfig = new RemoteCmdClientConfig(ip, userName, passWord);
        initClient();
    }

    public boolean isReady() {
        return ready;
    }

    public String getIp() {
        return remoteCmdClientConfig.getIp();
    }

    public String getDefaultDir() {
        return remoteCmdClientConfig.getDefaultDir();
    }

    public String getUserName() {
        return remoteCmdClientConfig.getUserName();
    }

    private void openConnect(String ip) {
        connection = new Connection(ip);
        for (int i = 1; i <= remoteCmdClientConfig.getRetryTime(); i++) {
            try {
                connection.connect(null, remoteCmdClientConfig.getCoTimeout(), remoteCmdClientConfig.getCoTimeout());
            } catch (IOException e) {
                if (i == remoteCmdClientConfig.getRetryTime()) {
                    String errorInfo = String.format("Open connection failed!Can't connect to %s", ip);
                    throw new ConnectException(errorInfo, e);
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e1) {
                        log.warn("InterruptedException occued in connect.", e1);
                    }
                    continue;
                }
            }
            break;
        }
    }

    private void authConnect() {
        String userName = remoteCmdClientConfig.getUserName();
        String passWord = remoteCmdClientConfig.getPassword();
        String errorMsg = String.format("Open connection failed because of remoteCmdClient's auth [ip=%s,userName=%s]",
                remoteCmdClientConfig.getIp(), remoteCmdClientConfig.getUserName());
        try {
            if (StringUtils.isBlank(passWord)) {
                ready = connection.authenticateWithPublicKey(userName,
                        new File(System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa"),
                        null);
            } else {
                ready = connection.authenticateWithPassword(userName, passWord);
            }
        } catch (IOException e) {
            log.error(errorMsg, e);
        }
        if (!ready || !checkConnectionStatus(connection)) {
            connection.close();
            throw new ConnectException();
        }
    }

    public void initClient() {
        if (StringUtils.isBlank(remoteCmdClientConfig.getIp()) || StringUtils.isBlank(remoteCmdClientConfig.getUserName())) {
            String errorMsg = String.format("Open connection failed because of IP or user name is blank![ip=%s,userName=%s]",
                    remoteCmdClientConfig.getIp(), remoteCmdClientConfig.getUserName());
            throw new IllegalArgumentException(errorMsg);
        }
        String ip = remoteCmdClientConfig.getIp();
        openConnect(ip);
        authConnect();
    }

    private boolean checkConnectionStatus(Connection connection) {

        RemoteCmd cmd = new RemoteCmd();
        cmd.addCmd("ps");
        try {
            String checkString = execCmdGetString(cmd);
            if (StringUtils.isBlank(checkString)) {
                ready = false;
            } else {
                ready = true;
            }
        } catch (Exception e) {
            log.error(String.format("check connection status failed, ip: %s", remoteCmdClientConfig.getIp()));
            ready = false;
        }
        return ready;
    }

    public boolean isAvailableNow() {
        return checkConnectionStatus(connection);
    }

    @Override
    public RemoteCmdClientStream execCmdGetStream(RemoteCmd cmd) {
        Session session = null;
        String ip = remoteCmdClientConfig.getIp();
        if (!isReady()) {
            log.error(String.format("remoteCmdClient %s is not ready!", ip));
            return new RemoteCmdClientStream(null, ip);
        }
        try {
            session = connection.openSession();
        } catch (IOException e) {
            log.error(String.format("Open session %s failed!", ip));
            return new RemoteCmdClientStream(null, ip);
        }
        try {
            session.execCommand(cmd.getCmd());
        } catch (IOException e) {
            log.error(String.format("execCommand %s failed!", ip));
            return new RemoteCmdClientStream(null, ip);
        }
        return new RemoteCmdClientStream(session, ip);
    }

    @Override
    public String execCmdGetString(RemoteCmd cmd) {
        InputStream stdout = null;
        InputStream stderr = null;
        BufferedReader br = null;
        BufferedReader errbr = null;
        StringBuilder sb = new StringBuilder();
        StringBuilder errsb = new StringBuilder();
        String ip = remoteCmdClientConfig.getIp();
        Session session = null;
        try {
            session = connection.openSession();
            session.execCommand(cmd.getCmd());
            stdout = new StreamGobbler(session.getStdout());
            stderr = new StreamGobbler(session.getStderr());
            br = new BufferedReader(new InputStreamReader(stdout));
            errbr = new BufferedReader(new InputStreamReader(stderr));

            String line = null;
            while ((line = errbr.readLine()) != null) {
                errsb.append(line);
                errsb.append("\r\n");
            }

            if (errsb.length() != 0) {
                return errsb.toString();
            }

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
            }

            return sb.toString();
        } catch (IOException e) {
            log.error(String.format("Open session %s failed!", ip));
            return "";
        } finally {
            if (stdout != null) {
                try {
                    stdout.close();
                } catch (IOException e) {
                    log.error(String.format("Close session stdout %s failed!", ip));
                }
            }
            if (stderr != null) {
                try {
                    stderr.close();
                } catch (IOException e) {
                    log.error(String.format("Close session stderr %s failed!", ip));
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error(String.format("Close session br buffered reader %s failed!", ip));
                }
            }
            if (errbr != null) {
                try {
                    errbr.close();
                } catch (IOException e) {
                    log.error(String.format("Close session errbr buffered reader %s failed!", ip));
                }
            }
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public RemoteCmdResult execCmdWithPTY(RemoteCmd cmd, PipedInputStream customPis) {
        RemoteCmdResult res = new RemoteCmdResult();
        InputStream stdout = null;
        InputStream stderr = null;
        BufferedReader bfStdoutReader = null;
        BufferedReader bfStderrReader = null;
        String ip = remoteCmdClientConfig.getIp();
        String passWord = remoteCmdClientConfig.getPassword();
        Session session = null;
        try {
            session = connection.openSession();
            log.debug("start cmd remoteCmdClient.......");
            session.requestPTY("dumb", 500, 300, 0, 0, null);
            session.startShell();

            List<String> cmds = cmd.getCmds();

            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream customPos = new PipedOutputStream();

            ClientDebugDisplayCallable clientDebugDisplayCallable = new ClientDebugDisplayCallable(session, cmds, passWord);
            clientDebugDisplayCallable.setCustomPos(customPos);
            customPis.connect(customPos);
            clientDebugDisplayCallable.connectPipedOutputStream(pis);

            stdout = new StreamGobbler(pis);
            stderr = new StreamGobbler(session.getStderr());

            ClientDebugInputCallable ClientDebugInputCallable = new ClientDebugInputCallable(session, cmds, passWord);

            try {
                Future<String> clientDebugDisplayFuture = threadPool.submit(clientDebugDisplayCallable);
                threadPool.submit(ClientDebugInputCallable);
                clientDebugDisplayFuture.get();

                ClientDebugInputCallable.stop();
            } catch (InterruptedException e) {
                log.error("An InterruptedException occurs when debug threads exit.");
            } catch (ExecutionException e) {
                log.error("An ExecutionException occurs when debug threads exit.");
            }

            int conditions = session.waitForCondition(ChannelCondition.CLOSED
                    | ChannelCondition.EOF
                    | ChannelCondition.EXIT_STATUS
                    | ChannelCondition.TIMEOUT, remoteCmdClientConfig.getSoTimeout());

            log.debug("Here is the output from stdout.......");
            if ((conditions & ChannelCondition.TIMEOUT) != 0) {
                String errorInfo = String.format("Execute cmd list[%s] timeout on machine %s!", cmds, ip);
                res.setStdInfo(errorInfo);
                log.error(errorInfo);
                System.err.println(errorInfo);
            } else {
                StringBuilder sbstd = new StringBuilder();
                byte[] buffer = new byte[8192];

                int len = 0;
                while ((len = stdout.read(buffer)) != -1) {
                    if (len > 0) {
                        String line = new String(buffer, 0, len);
                        sbstd.append(line);
                    }
                }
                log.debug(sbstd.toString().replace(passWord, "***"));

                if (sbstd.length() != 0) {
                    String resHeadString = "]$";
                    int resHeadFlag = sbstd.indexOf(resHeadString + cmds.get(2)) + cmds.get(2).length()
                            + resHeadString.length();
                    String standardInfo = sbstd.substring(resHeadFlag).toString().replace(passWord, "***");
                    res.setStdInfo(standardInfo);
                } else {
                    res.setStdInfo("");
                }

                StringBuilder sberr = new StringBuilder();
                while ((len = stderr.read(buffer)) != -1) {
                    if (len > 0) {
                        String line = new String(buffer, 0, len);
                        sberr.append(line);
                        log.debug(line);
                    }
                }

                res.setErrInfo(sberr.toString());
            }

            return res;

        } catch (IOException e) {
            log.error(String.format("Close session %s failed!", ip));
            return res;
        } finally {
            if (stdout != null) {
                try {
                    stdout.close();
                } catch (IOException e) {
                    log.error(String.format("Close session stdout outter %s failed!", ip));
                }
            }
            if (stderr != null) {
                try {
                    stderr.close();
                } catch (IOException e) {
                    log.error(String.format("Close session stdout error outter %s failed!", ip));
                }
            }
            if (bfStdoutReader != null) {
                try {
                    bfStdoutReader.close();
                } catch (IOException e) {
                    log.error(String.format("Close session buffered stdout reader %s failed!", ip));
                }
            }
            if (bfStderrReader != null) {
                try {
                    bfStderrReader.close();
                } catch (IOException e) {
                    log.error(String.format("Close session buffered stdout error reader %s failed!", ip));
                }
            }
            if (session != null) {
                session.close();
            }

        }
    }

    @Override
    public void exeCmdBlockWithPTY(RemoteCmd cmd, PipedInputStream customerIn) {
        InputStream stderr = null;
        PipedOutputStream customeredOut = new PipedOutputStream();
        String ip = remoteCmdClientConfig.getIp();
        String passWord = remoteCmdClientConfig.getPassword();
        Session session = null;
        try {
            session = connection.openSession();
            log.debug("start cmd remoteCmdClient.......");
            session.requestPTY("dumb", 500, 300, 0, 0, null);
            session.startShell();

            List<String> cmds = cmd.getCmds();

            ClientDebugDisplayCallable clientDebugDisplayCallable = new ClientDebugDisplayCallable(session, cmds, passWord);
            clientDebugDisplayCallable.setCustomPos(customeredOut);
            customerIn.connect(customeredOut);
            stderr = new StreamGobbler(session.getStderr());
            ClientDebugInputCallable ClientDebugInputCallable = new ClientDebugInputCallable(session, cmds, passWord);

            try {
                Future<String> clientDebugDisplayFuture = threadPool.submit(clientDebugDisplayCallable);
                threadPool.submit(ClientDebugInputCallable);
                clientDebugDisplayFuture.get();
                while (true) {
                    if (customerIn.available() > 0) {
                        Thread.sleep(100);
                        continue;
                    }
                    break;
                }
                ClientDebugInputCallable.stop();
            } catch (InterruptedException e) {
                log.error("An InterruptedException has occured when debug threads exit.");
            } catch (ExecutionException e) {
                log.error("An ExecutionException has occured when debug threads exit.");
            }

            int conditions = session.waitForCondition(ChannelCondition.CLOSED | ChannelCondition.EOF
                    | ChannelCondition.EXIT_STATUS | ChannelCondition.TIMEOUT, remoteCmdClientConfig.getSoTimeout());

            log.debug("Here is the output from stdout.......");
            if ((conditions & ChannelCondition.TIMEOUT) != 0) {
                String errorInfo = String.format("Execute cmd list[%s] timeout on machine %s!", cmds, ip);
                log.error(errorInfo);
            }

            log.debug("ExitCode is: " + session.getExitStatus());

        } catch (IOException e) {
            log.error(String.format("Close session %s failed!", ip));
        } finally {
            if (stderr != null) {
                try {
                    stderr.close();
                } catch (IOException e) {
                    log.error(String.format("Close session stdout error outter %s failed!", ip));
                }
            }
            if (session != null) {
                session.close();
            }

        }
    }

    @Override
    public RemoteCmdResult execCmdWithPTY(RemoteCmd cmd) {
        RemoteCmdResult res = new RemoteCmdResult();
        InputStream stdout = null;
        InputStream stderr = null;
        BufferedReader bfStdoutReader = null;
        BufferedReader bfStderrReader = null;
        String ip = remoteCmdClientConfig.getIp();
        String passWord = remoteCmdClientConfig.getPassword();
        Session session = null;
        try {
            session = connection.openSession();
            log.debug("start cmd remoteCmdClient.......");
            session.requestPTY("dumb", 500, 300, 0, 0, null);
            session.startShell();

            List<String> cmds = cmd.getCmds();

            if (remoteCmdClientConfig.getMode() == RemoteCmdClientConfig.CLIENT_MODE_DEBUG) {

                PipedInputStream pis = new PipedInputStream();

                ClientDebugDisplayCallable clientDebugDisplayCallable = new ClientDebugDisplayCallable(session, cmds, passWord);
                clientDebugDisplayCallable.connectPipedOutputStream(pis);

                stdout = new StreamGobbler(pis);
                stderr = new StreamGobbler(session.getStderr());

                ClientDebugInputCallable ClientDebugInputCallable = new ClientDebugInputCallable(session, cmds, passWord);

                try {
                    Future<String> clientDebugDisplayFuture = threadPool.submit(clientDebugDisplayCallable);
                    threadPool.submit(ClientDebugInputCallable);
                    clientDebugDisplayFuture.get();

                    ClientDebugInputCallable.stop();
                } catch (InterruptedException e) {
                    log.error("An InterruptedException occurs when debug threads exit.");
                } catch (ExecutionException e) {
                    log.error("An ExecutionException occurs when debug threads exit.");
                }
            } else {
                stdout = new StreamGobbler(session.getStdout());
                stderr = new StreamGobbler(session.getStderr());

                OutputStream out = session.getStdin();

                for (String commandLine : cmds) {
                    log.debug("input command " + commandLine);
                    if (commandLine.contains("sudo ")) {
                        commandLine = commandLine.replace("sudo ",
                                String.format("echo '%s'| sudo -S -p '' ", passWord));
                    }
                    out.write((commandLine + "\n").getBytes());
                    out.flush();
                }
                // 清除历史记录
                out.write("export HISTFILE=/dev/null\n".getBytes());
                out.write("exit\n".getBytes());
                out.flush();
                out.close();
                log.debug("end cmd remoteCmdClient.......");
            }

            int conditions = session.waitForCondition(ChannelCondition.CLOSED
                    | ChannelCondition.EOF
                    | ChannelCondition.EXIT_STATUS
                    | ChannelCondition.TIMEOUT, remoteCmdClientConfig.getSoTimeout());

            log.debug("Here is the output from stdout.......");
            if ((conditions & ChannelCondition.TIMEOUT) != 0) {
                String errorInfo = String.format("Execute command list[%s] timeout on machine %s!", cmds, ip);
                res.setStdInfo(errorInfo);
                log.error(errorInfo);
                System.err.println(errorInfo);
            } else {
                StringBuilder sbstd = new StringBuilder();
                byte[] buffer = new byte[8192];

                int len = 0;
                while ((len = stdout.read(buffer)) != -1) {
                    if (len > 0) {
                        String line = new String(buffer, 0, len);
                        sbstd.append(line);
                    }
                }
                log.debug(sbstd.toString().replace(passWord, "***"));

                if (sbstd.length() != 0) {
                    String resHeadString = "]$";
                    int resHeadFlag = sbstd.indexOf(resHeadString + cmds.get(2)) + cmds.get(2).length()
                            + resHeadString.length();
                    String standardInfo = sbstd.substring(resHeadFlag).toString().replace(passWord, "***");
                    res.setStdInfo(standardInfo);
                } else {
                    res.setStdInfo("");
                }

                StringBuilder sberr = new StringBuilder();
                while ((len = stderr.read(buffer)) != -1) {
                    if (len > 0) {
                        String line = new String(buffer, 0, len);
                        sberr.append(line);
                        log.debug(line);
                    }
                }

                res.setErrInfo(sberr.toString());
            }

            log.debug("ExitCode is: " + session.getExitStatus());

            return res;

        } catch (IOException e) {
            log.error(String.format("Close session %s failed!", ip));
            return res;
        } finally {
            if (stdout != null) {
                try {
                    stdout.close();
                } catch (IOException e) {
                    log.error(String.format("Close session stdout outter %s failed!", ip));
                }
            }
            if (stderr != null) {
                try {
                    stderr.close();
                } catch (IOException e) {
                    log.error(String.format("Close session stdout error outter %s failed!", ip));
                }
            }
            if (bfStdoutReader != null) {
                try {
                    bfStdoutReader.close();
                } catch (IOException e) {
                    log.error(String.format("Close session buffered stdout reader %s failed!", ip));
                }
            }
            if (bfStderrReader != null) {
                try {
                    bfStderrReader.close();
                } catch (IOException e) {
                    log.error(String.format("Close session buffered stdout error reader %s failed!", ip));
                }
            }
            if (session != null) {
                session.close();
            }

        }
    }

    @Override
    public void execCmd(RemoteCmd cmd) {
        List<String> cmds = cmd.getCmds();
        String ip = remoteCmdClientConfig.getIp();
        Session session = null;
        for (String commandLine : cmds) {
            try {
                session = connection.openSession();
                session.execCommand(commandLine);
            } catch (IOException e) {
                log.error(String.format("Close session %s failed!", ip));
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }

    @Override
    public RemoteCmdResult execScript(String path) {
        RemoteCmdResult info = new RemoteCmdResult();
        Map<String, String> pathMap = splitDirAndFile(path);
        if (pathMap == null || pathMap.size() < 2) {
            info.setErrInfo("Error full path!");
            return info;
        }
        RemoteCmd command = new RemoteCmd();
        command.addCmd("cd " + pathMap.get(DIR_NAME));
        command.addCmd("sh " + pathMap.get(FILE_NAME));
        return execCmdWithPTY(command);
    }

    @Override
    public RemoteCmdResult scpAndExecScript(String filePath) {
        return scpAndExecScript(filePath, true);
    }

    @Override
    public RemoteCmdResult scpAndExecScript(String filePath, boolean override) {
        if (StringUtils.isBlank(filePath)) {
            throw new RuntimeException("filePath is empty.");
        }
        String scriptDir = this.getDefaultDir() + "/scripts/";
        this.createFile(filePath, scriptDir, override);
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        RemoteCmdResult remoteCmdResult = this.execScript(scriptDir + fileName);
        log.debug(remoteCmdResult.getStdInfo());
        return remoteCmdResult;
    }

    @Override
    public String killProcess(String keyWord) {
        Set<String> conditions = new HashSet<String>();
        conditions.add(keyWord);
        CmdEvent event = new CmdEvent();
        event.setActionCmd(CmdConstant.KILL_PROCESS);
        event.setSourceClient(this);
        event.setWhen(System.currentTimeMillis());
        event.getParams().setGrepConditions(conditions);
        KillPIDCmdAction cmd = new KillPIDCmdAction();
        cmd.doAction(event);
        return (String) event.getResult();
    }

    @Override
    public String uploadFile(String srcFile, String destDir) {
        String res = "";
        File f = new File(srcFile);
        if (!f.exists()) {
            res = "Error in io:" + srcFile + " dose not exist!";
            return res;
        }
        try {
            SCPClient client = connection.createSCPClient();
            client.put(srcFile, destDir);
        } catch (IOException e) {
            res = "Error in io:" + e.getMessage();
        }
        return res;
    }


    @Override
    public boolean hasDirectory(String path) {
        boolean res = true;
        String commandStr = String.format("[ -d %s ] && echo exist", path);
        RemoteCmd command = new RemoteCmd();
        command.addCmd(commandStr);
        String result = execCmdGetString(command);
        if (StringUtils.isEmpty(result)) {
            res = false;
        }
        return res;
    }

    @Override
    public boolean hasFile(String filePath) {
        boolean res = true;
        String commandStr = String.format("[ -f %s ] && echo exist", filePath);
        RemoteCmd command = new RemoteCmd();
        command.addCmd(commandStr);
        String result = execCmdGetString(command);
        if (StringUtils.isEmpty(result)) {
            res = false;
        }
        return res;
    }

    @Override
    public String mkdir(String directory) {
        StringBuilder sb = new StringBuilder();
        RemoteCmd command = new RemoteCmd();
        command.addCmd("mkdir -p " + directory);
        sb.append(execCmdGetString(command));
        return sb.toString();
    }

    public boolean createFile(String content, String fileName, String remoteFolder) {
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        if (!this.hasDirectory(remoteFolder)) {
            this.mkdir(remoteFolder);
        }
        File localFile = new File(fileName);
        try {
            FileWriter writer = new FileWriter(localFile);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            return false;
        }
        this.uploadFile(localFile.getAbsolutePath(), remoteFolder);
        localFile.delete();
        return true;
    }

    public long getLocalFileSize(File fileName) {
        FileInputStream fis = null;
        long fileSize = 0;
        try {
            fis = new FileInputStream(fileName);
            fileSize = fis.available();
        } catch (FileNotFoundException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        } finally {
            try {
                fis.close();
            } catch (Exception e2) {
                //ignore
            }
        }
        return fileSize;
    }

    public synchronized boolean createFile(InputStream iStream, String fileName, String remoteFolder) {
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        if (iStream == null) {
            return false;
        }
        if (!this.hasDirectory(remoteFolder)) {
            this.mkdir(remoteFolder);
        }

        File localFile = new File(fileName);
        if (getLocalFileSize(localFile) == 0) {
            BufferedInputStream fis = null;
            FileOutputStream fos = null;
            byte[] buffer = new byte[1024];
            try {
                localFile.createNewFile();
                fis = new BufferedInputStream(iStream);
                fos = new FileOutputStream(localFile);
                int bytesRead = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                return false;
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        this.uploadFile(localFile.getAbsolutePath(), remoteFolder);
        localFile.delete();
        return true;
    }

    public synchronized boolean createFile(String filePath, String destDirectory) {
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        if (!this.hasDirectory(destDirectory)) {
            this.mkdir(destDirectory);
        }

        File localFile = new File(fileName);
        if (getLocalFileSize(localFile) == 0) {
            BufferedInputStream fis = null;
            FileOutputStream fos = null;
            byte[] buffer = new byte[1024];
            try {
                localFile.createNewFile();
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
                fis = new BufferedInputStream(is);
                fos = new FileOutputStream(localFile);
                int bytesRead = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                log.error(ExceptionUtils.getMessage(e));
                return false;
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        this.uploadFile(localFile.getAbsolutePath(), destDirectory);
        localFile.delete();
        return true;
    }

    public synchronized boolean createFile(String filePath, String destDirectory, boolean override) {
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        if (StringUtils.isBlank(fileName)) {
            return false;
        }

        if (!override && this.hasFile(destDirectory + "/" + fileName)) {
            throw new RuntimeException("this file is exist, please select mode override");
        }

        if (!this.hasDirectory(destDirectory)) {
            this.mkdir(destDirectory);
        }

        File localFile = new File(fileName);
        if (getLocalFileSize(localFile) == 0) {
            BufferedInputStream fis = null;
            FileOutputStream fos = null;
            byte[] buffer = new byte[1024];
            try {
                localFile.createNewFile();
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
                fis = new BufferedInputStream(is);
                fos = new FileOutputStream(localFile);
                int bytesRead = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                log.error(ExceptionUtils.getMessage(e));
                return false;
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        this.uploadFile(localFile.getAbsolutePath(), destDirectory);
        localFile.delete();
        return true;
    }

    @Override
    public String getPID(String condition) {

        CmdEvent event = new CmdEvent();
        event.setActionCmd(CmdConstant.PROCESS_ID);
        event.setSourceClient(this);
        event.setWhen(System.currentTimeMillis());
        Set<String> conditions = new HashSet<String>();
        conditions.add(condition);
        event.getParams().setGrepConditions(conditions);
        PIDCmdAction cmd = new PIDCmdAction();
        cmd.doAction(event);
        return (String) event.getResult();
    }

    @Override
    public String getPID(Set<String> conds) {
        CmdEvent event = new CmdEvent();
        event.setActionCmd(CmdConstant.PROCESS_ID);
        event.setSourceClient(this);
        event.setWhen(System.currentTimeMillis());
        event.getParams().setGrepConditions(conds);
        PIDCmdAction cmd = new PIDCmdAction();
        cmd.doAction(event);
        return (String) event.getResult();
    }


    @Override
    public String netstatAn(String condition) {
        CmdEvent event = new CmdEvent();
        event.setActionCmd(CmdConstant.NETSTAT_AN_INTERNET);
        event.setSourceClient(this);
        event.setWhen(System.currentTimeMillis());
        Set<String> conditions = new HashSet<String>();
        conditions.add(condition);
        event.getParams().setGrepConditions(conditions);
        NetstatANCmdAction cmd = new NetstatANCmdAction();
        cmd.doAction(event);
        return (String) event.getResult();
    }

    @Override
    public String netstatLnp(String condition) {
        CmdEvent event = new CmdEvent();
        event.setActionCmd(CmdConstant.NETSTAT_LNP_INTERNET);
        event.setSourceClient(this);
        event.setWhen(System.currentTimeMillis());
        Set<String> conditions = new HashSet<String>();
        conditions.add(condition);
        event.getParams().setGrepConditions(conditions);
        NetstatLNPCmdAction cmd = new NetstatLNPCmdAction();
        cmd.doAction(event);
        return (String) event.getResult();
    }

    @Override
    public String jps(String condition) {
        CmdEvent event = new CmdEvent();
        event.setActionCmd(CmdConstant.JPS);
        event.setSourceClient(this);
        event.setWhen(System.currentTimeMillis());
        Set<String> conditions = new HashSet<String>();
        conditions.add(condition);
        event.getParams().setGrepConditions(conditions);
        JpsCmdAction cmd = new JpsCmdAction();
        cmd.doAction(event);
        return (String) event.getResult();
    }


    public void close() {
        connection.close();
        ready = false;
    }

    private static Map<String, String> splitDirAndFile(String fullPath) {
        Map<String, String> resMap = new HashMap<String, String>();
        String path = fullPath;
        int paramPos = fullPath.indexOf(" ");
        if (paramPos != -1) {
            path = fullPath.substring(0, paramPos);
        }
        String[] items = path.split("/");
        if (items == null || items.length <= 2) {
            return resMap;
        }
        String script = items[items.length - 1];
        int index = fullPath.lastIndexOf(script);
        resMap.put(DIR_NAME, fullPath.substring(0, index));
        resMap.put(FILE_NAME, fullPath.substring(index, fullPath.length()));
        return resMap;
    }

}

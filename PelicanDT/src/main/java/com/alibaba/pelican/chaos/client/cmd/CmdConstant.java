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

package com.alibaba.pelican.chaos.client.cmd;


/**
 * @author moyun@middleware
 */
public class CmdConstant {

    // jps命令
    public static String JPS = "JPS";
    // 利用某个关键字杀掉某个进程的命令
    public static String KILL_PROCESS = "KILL_PROCESS";
    // 取得某个进程的ID
    public static String PROCESS_ID = "PROCESS_ID";
    // 查看Internet连接的命令,netstat -nlp
    public static String NETSTAT_NLP_INTERNET = "NETSTAT_NLP_INTERNET";
    // 查看Internet连接的命令,netstat -an
    public static String NETSTAT_AN_INTERNET = "NETSTAT_AN_INTERNET";
    // 查看Socket连接的命令
    public static String NETSTAT_SOCKET = "NETSTAT_SOCKET";
    // 查看文件或文件夹状态的命令
    public static String FILE_INFO = "FILE_INFO";

}

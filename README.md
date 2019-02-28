## 产品介绍
PelicanDT（Pelican Distributed Test），是阿里云提供的一款 Linux 系统远程控制利器，是主要针对分布式应用提供的集成测试解决方案，用于帮助开发者简单、高效地测试分布式应用。

PelicanDT 具有以下特点：

- 使用 Java 语言与 Linux 系统交互。
- 本地控制 Linux 系统执行命令。
- 通过简单的操作对应用注入异常来模拟测试环境。例如：应用停服、CPU 过高、内存过高、网络中断、网络流量延时等环境。
    
基于 PelicanDT 实现的Demo：
- Dubbo测试Demo工程：[Dubbo-example](https://github.com/alibaba/PelicanDT/tree/master/Dubbo-example)
- Nacos测试Demo工程：[Nacos-example](https://github.com/alibaba/PelicanDT/tree/master/Nacos-example)

## 使用指南
https://help.aliyun.com/document_detail/102518.html

## 快速入门

### 安装PelicanDT SDK
1. 将SDK添加到项目中
2. 使用PelicanDT SDK有两种方式：
- 下载SDK源码包，在下载中心下载最新版的JAVA SDK到本地，并import 到您的工作目录中。
- 引入PelicanDT SDK依赖，通过maven二方库依赖的方式将PelicanDT的sdk加入到自己的项目中
    ``````
    <dependency>
        <groupId>com.alibaba.pelican</groupId>
        <artifactId>PelicanDT</artifactId>
        <version>1.0.6</version>
    </dependency>

### 使用PelicanDT SDK

以下代码示例展示了使用PelicanDT SDK使用方式：

本地代码控制远程服务器执行命令：
1. 创建并初始化RemoteCmdClient实例，RemoteCmdClient为远程服务器客户端。
2. 填写远程服务器ip、userName、password信息
3. 本地执行示例程序，向远程服务器执行pwd命令
    ``````
        import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
        import lombok.extern.slf4j.Slf4j;
        import org.junit.Test;
          
        /**
         * @author moyun@middleware
         */
        @Slf4j
        public class TestRemoteCmdClient {
          
            @Test
            public void testRemoteCmdClient() {
                //ECS可公网访问的IP
                String ip = "";
                //ECS用户名，一般为root
                String userName = "";
                //ECS登录密码
                String password = "";
          
                //创建并初始化RemoteCmdClient实例
                RemoteCmdClientConfig remoteCmdClientConfig = new RemoteCmdClientConfig();
                remoteCmdClientConfig.setIp(ip);
                remoteCmdClientConfig.setUserName(userName);
                remoteCmdClientConfig.setPassword(password);
                RemoteCmdClient client = new RemoteCmdClient(remoteCmdClientConfig);
                
                //执行pwd命令
                RemoteCmdResult resultInfo = client.execCmdWithPTY(new RemoteCmd("pwd"));
                log.info(resultInfo.getStdInfo());
            }
        }


### 预期结果
日志输出内容如下
    
    [root@iz2ze0kv2rqck9wpheu5vxz ~]$pwd
    root
    [root@iz2ze0kv2rqck9wpheu5vxz ~]$export HISTFILE=/dev/null
    [root@iz2ze0kv2rqck9wpheu5vxz ~]$exit
    logout
    
通过第2行内容可以看出，命令执行默认目录/root/

### 如有疑问：请加钉钉

![avatar](http://moyuns.oss-cn-hangzhou.aliyuncs.com/lADPDgQ9qg8emUzNBKnNAu4_750_1193.jpg)

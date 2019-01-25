# PelicanDT In Alibaba

## 一行代码实现服务器执行本地shell脚本
### 应用场景
用户经常遇到将本地shell脚本上传至远程服务器并执行的场景。
### 传统解决办法
需要执行如下步骤：
- 执行scp命令，将本地shell脚本上传至远程服务器
- 登陆远程服务器
- 根据上传目录找到该shell脚本并执行
### PelicanDT实现
通过PelicanDT只需要通过一行Java代码即可轻松完成，具体请参考示例：https://github.com/alibaba/PelicanDT/blob/master/Demo-example/src/main/java/com/alibaba/pelican/demo/DemoExecScript.java
## 一行代码实现服务器应用中止
### 应用场景
用户在远程服务器启动了一个应用，用户需要终止该应用。
### 传统解决办法
需要执行如下步骤：
- 登录远程服务器
- 查询应用进程
- 执行KILL命令，完成应用终止
### PelicanDT实现
通过PelicanDT只需要通过一行Java代码即可轻松停止该应用，，具体请参考示例：https://github.com/alibaba/PelicanDT/blob/master/Demo-example/src/main/java/com/alibaba/pelican/demo/KillPID.java


## 一行代码实现服务器内存占用率拉高
### 应用场景
用户在远程服务器启动了一个应用，为了验证当内存占用率高的场景下，应用表现是否正常。

### 传统解决办法
该用户为了验证该场景需要考虑如下2种情况：
- 模拟内存占用率高
- 模拟内存占用率稳定在某一水位情况下，持续观察应用表现是否正常
对于第1种情况，通过一个shell脚本，实现一个循环命令即可，但是这样做容易将服务器内存撑爆，导致服务器宕机
对于第2种情况，实现起来显得较为复杂
### PelicanDT实现
通过PelicanDT只需要一行代码即可轻松地完成远程服务器内存占用率控制，观察该服务器应用表现是否正常，具体请参考示例：https://github.com/alibaba/PelicanDT/blob/master/Demo-example/src/main/java/com/alibaba/pelican/demo/DemoMem.java


## 一行代码实现服务器CPU占用率拉高
### 应用场景
用户在远程服务器启动了一个应用，为了验证当CPU占用率高的场景下，应用表现是否正常。

### 传统解决办法
该用户为了验证该场景需要考虑如下2种情况：
- 模拟CPU占用率高
- 模拟CPU占用率稳定在某一水位情况下，持续观察应用表现是否正常

对于第1种情况，通过一个shell脚本，实现一个循环命令即可，但是这样做容易将服务器CPU过高，导致服务器宕机
对于第2种情况，实现起来显得更为复杂
### PelicanDT实现
通过PelicanDT只需要一行代码即可轻松地完成远程服务器CPU占用率控制，观察该服务器应用表现是否正常，具体请参考示例：https://github.com/alibaba/PelicanDT/blob/master/Demo-example/src/main/java/com/alibaba/pelican/demo/DemoCPU.java



## 一行代码实现服务器网络中断
### 应用场景
用户在远程服务器启动了一个应用，为了验证当网络中断的场景下，应用表现是否正常。

### 传统解决办法
该用户为了验证该场景需要考虑如下2种情况：
- 模拟网络中断
- 模拟网络中断的持续时间
对于第1种情况，可以通过iptables命令完成，需要人工到远程服务器执行
对于第2种情况，需要人工添加、删除iptables规则实现网络中断持续时间
### PelicanDT实现
通过PelicanDT只需要一行代码即可轻松地完成远程服务器网络中断的控制，观察该服务器应用表现是否正常，具体请参考示例：https://github.com/alibaba/PelicanDT/blob/master/Demo-example/src/main/java/com/alibaba/pelican/demo/DemoNetAccess.java


## 一行代码实现服务器网络延时
### 应用场景
用户在远程服务器启动了一个应用，为了验证当网络延时的场景下，应用表现是否正常。
### 遇到问题
该用户为了验证该场景需要考虑如下2种情况：
    
- 模拟网络延时
- 模拟网络延时的持续时间

### 传统解决办法
对于第1种情况，可以通过tc命令完成，不过需要人工到远程服务器执行，且需要学习tc命令
对于第2种情况，需要人工添加、删除tc规则实现网络中断持续时间

### PelicanDT实现 
通过PelicanDT只需要一行代码即可轻松地完成远程服务器网络延时的控制，观察该服务器应用表现是否正常，具体请参考示例：https://github.com/alibaba/PelicanDT/blob/master/Demo-example/src/main/java/com/alibaba/pelican/demo/DemoNetTraffic.java


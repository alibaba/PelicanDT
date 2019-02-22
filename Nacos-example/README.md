## 具体介绍
Nacos-example，是基于PelicanDT实现nacos环境准备，禁止端口网络访问，执行接口调用验证端口是否禁用示例
    
## 前期准备
1. 本示例程序是基于阿里云ECS或远程Linux服务器完成，只需[购买](https://ecs-buy.aliyun.com/wizard?spm=5176.8789780.1092585.1.520157a8WqaKjA#/prepay/cn-zhangjiakou)阿里云机器，或者选定已准备好的远程服务器即可
2. 下载[Nacos-example](https://github.com/alibaba/PelicanDT/tree/master/Nacos-example)代码

## 快速入门

### 修改配置
1. 打开nacos.properties配置文件，具体路径：Nacos-example/src/test/resources/env/func/nacos.properties
2. 填写ip，userName，password

### 运行示例

本地代码控制远程服务器执行Nacos验证：
1. 打开TestNacosNetwork.java，具体路径：Nacos-example/src/test/java/com/alibaba/pelican/nacos/TestNacosNetwork.java
2. 运行单元测试

### 预期结果
日志输出内容如下
    
    2019-02-22 18:43:30 [INFO] [main] c.a.p.c.client.utils.NetAccessUtils - Block port 8848 protcol TCP, dalay time 20 seconds.
    2019-02-22 18:43:40 [INFO] [main] c.a.pelican.nacos.TestNacosNetwork - Operation timed out (Connection timed out)
    {"metadata":{},"dom":"nacos.naming.serviceName","cacheMillis":10000,"useSpecifiedURL":false,"hosts":[{"valid":true,"marked":false,"metadata":{},"instanceId":"20.18.7.10#8080#DEFAULT#nacos.naming.serviceName","port":8080,"ip":"20.18.7.10","clusterName":"DEFAULT","weight":1.0,"serviceName":"nacos.naming.serviceName","enabled":true}],"checksum":"974cf987832bcf52812828aab46248501550832220958","lastRefTime":1550832220958,"env":"","clusters":""}
    
- 通过第1行日志可以看出，8848端口断网
- 通过第2行日志可以看出，在8848端口断网的情况下，接口访问超时
- 通过第3行日志可以看出，端口网络恢复后，接口访问成功
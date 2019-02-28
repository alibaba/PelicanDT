## 具体介绍
RocketMQ-example，是基于PelicanDT实现RocketMQ环境准备，收发消息验证示例
    
## 前期准备
1. 本示例程序是基于阿里云ECS或远程Linux服务器完成，只需[购买](https://ecs-buy.aliyun.com/wizard?spm=5176.8789780.1092585.1.520157a8WqaKjA#/prepay/cn-zhangjiakou)阿里云机器，或者选定已准备好的远程服务器即可
2. 下载[RocketMQ-example](https://github.com/alibaba/PelicanDT/tree/master/RocketMQ-example)代码

注意事项：如果购买的是阿里云ECS，配置：8C16G，且安全组配置访问端口：9876

## 快速入门

### 修改配置
1. 打开rocketmq.properties配置文件，具体路径：RocketMQ-example/src/test/resources/env/func/rocketmq.properties
2. 填写ip，userName，password

### 运行示例

本地代码控制远程服务器执行Dubbo验证：
1. 打开TestRocketMQ.java，具体路径：RocketMQ-example/src/test/java/com/alibaba/pelican/rocketmq/TestRocketMQ.java
2. 运行单元测试

### 预期结果
日志输出内容如下
    
    2019-02-28 19:46:46 [INFO] [main] c.a.p.deployment.junit.rule.LogRule - --------- TO NEXT CASE ---------
    2019-02-28 19:46:46 [INFO] [main] c.a.p.deployment.junit.rule.LogRule - Run TC[test(com.alibaba.pelican.rocketmq.TestRocketMQ)]
    SendResult [sendStatus=SEND_OK, msgId=1E057C08A74518B4AAC28F4A3D090000, offsetMsgId=781B1FC600002A9F0000000000008BC4, messageQueue=MessageQueue [topic=TopicTest, brokerName=iZm5e0pe3xy3tjh9sw1kgpZ, queueId=3], queueOffset=50]
    SendResult [sendStatus=SEND_OK, msgId=1E057C08A74518B4AAC28F4A3D920001, offsetMsgId=781B1FC600002A9F0000000000008C76, messageQueue=MessageQueue [topic=TopicTest, brokerName=iZm5e0pe3xy3tjh9sw1kgpZ, queueId=0], queueOffset=50]
    SendResult [sendStatus=SEND_OK, msgId=1E057C08A74518B4AAC28F4A3DB10002, offsetMsgId=781B1FC600002A9F0000000000008D28, messageQueue=MessageQueue [topic=TopicTest, brokerName=iZm5e0pe3xy3tjh9sw1kgpZ, queueId=1], queueOffset=50]
    SendResult [sendStatus=SEND_OK, msgId=1E057C08A74518B4AAC28F4A3DD60003, offsetMsgId=781B1FC600002A9F0000000000008DDA, messageQueue=MessageQueue [topic=TopicTest, brokerName=iZm5e0pe3xy3tjh9sw1kgpZ, queueId=2], queueOffset=50]
    SendResult [sendStatus=SEND_OK, msgId=1E057C08A74518B4AAC28F4A3DFE0004, offsetMsgId=781B1FC600002A9F0000000000008E8C, messageQueue=MessageQueue [topic=TopicTest, brokerName=iZm5e0pe3xy3tjh9sw1kgpZ, queueId=3], queueOffset=51]
    2019-02-28 19:46:47 [INFO] [NettyClientSelector_1] RocketmqRemoting - closeChannel: close the connection to remote address[120.27.31.198:10911] result: true
    2019-02-28 19:46:47 [INFO] [NettyClientSelector_1] RocketmqRemoting - closeChannel: close the connection to remote address[120.27.31.198:9876] result: true
    2019-02-28 19:46:47 [INFO] [NettyClientSelector_1] RocketmqRemoting - closeChannel: close the connection to remote address[120.27.31.198:10909] result: true
    ConsumeMessageThread_2 Receive New Messages: [MessageExt [queueId=3, storeSize=178, queueOffset=51, sysFlag=0, bornTimestamp=1551354407422, bornHost=/42.120.74.97:44264, storeTimestamp=1551354407473, storeHost=/120.27.31.198:10911, msgId=781B1FC600002A9F0000000000008E8C, commitLogOffset=36492, bodyCRC=601994070, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=52, CONSUME_START_TIME=1551354408017, UNIQ_KEY=1E057C08A74518B4AAC28F4A3DFE0004, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 52], transactionId='null'}]] 
    ConsumeMessageThread_1 Receive New Messages: [MessageExt [queueId=3, storeSize=178, queueOffset=50, sysFlag=0, bornTimestamp=1551354407177, bornHost=/42.120.74.97:44264, storeTimestamp=1551354407322, storeHost=/120.27.31.198:10911, msgId=781B1FC600002A9F0000000000008BC4, commitLogOffset=35780, bodyCRC=613185359, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=52, CONSUME_START_TIME=1551354408017, UNIQ_KEY=1E057C08A74518B4AAC28F4A3D090000, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 48], transactionId='null'}]] 
    ConsumeMessageThread_3 Receive New Messages: [MessageExt [queueId=2, storeSize=178, queueOffset=50, sysFlag=0, bornTimestamp=1551354407382, bornHost=/42.120.74.97:44264, storeTimestamp=1551354407440, storeHost=/120.27.31.198:10911, msgId=781B1FC600002A9F0000000000008DDA, commitLogOffset=36314, bodyCRC=1032136437, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=51, CONSUME_START_TIME=1551354411038, UNIQ_KEY=1E057C08A74518B4AAC28F4A3DD60003, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 51], transactionId='null'}]] 
    ConsumeMessageThread_4 Receive New Messages: [MessageExt [queueId=1, storeSize=178, queueOffset=50, sysFlag=0, bornTimestamp=1551354407345, bornHost=/42.120.74.97:44264, storeTimestamp=1551354407400, storeHost=/120.27.31.198:10911, msgId=781B1FC600002A9F0000000000008D28, commitLogOffset=36136, bodyCRC=1250039395, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=51, CONSUME_START_TIME=1551354411040, UNIQ_KEY=1E057C08A74518B4AAC28F4A3DB10002, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 50], transactionId='null'}]] 
    ConsumeMessageThread_5 Receive New Messages: [MessageExt [queueId=0, storeSize=178, queueOffset=50, sysFlag=0, bornTimestamp=1551354407314, bornHost=/42.120.74.97:44264, storeTimestamp=1551354407361, storeHost=/120.27.31.198:10911, msgId=781B1FC600002A9F0000000000008C76, commitLogOffset=35958, bodyCRC=1401636825, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=51, CONSUME_START_TIME=1551354411042, UNIQ_KEY=1E057C08A74518B4AAC28F4A3D920001, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 49], transactionId='null'}]] 
    
- SendResult 开头的日志代表发送消息
- Receive New 开头的日志代表消费消息
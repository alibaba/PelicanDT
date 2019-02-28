#!/bin/bash
cd /root
yum -y install wget
yum -y install unzip

if [ ! -f "/root/jdk-8u141-linux-x64.tar.gz" ];then
    wget http://moyuns.oss-cn-hangzhou.aliyuncs.com/jdk-8u141-linux-x64.tar.gz
fi
if [ ! -d "/root/jdk1.8.0_141/" ];then
    tar -zxvf jdk-8u141-linux-x64.tar.gz
    sed -i '/export JAVA_HOME=/root/jdk1.8.0_141/d' /etc/profile
    sed -i '$ a\export JAVA_HOME=/root/jdk1.8.0_141' /etc/profile
    sed -i '/export PATH=$JAVA_HOME/bin:$PATH/d' /etc/profile
    sed -i '$ a\export PATH=$JAVA_HOME/bin:$PATH' /etc/profile
    source /etc/profile
fi

if [ ! -f "/root/apache-maven-3.0.5-bin.zip" ];then
    wget https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.zip
fi

if [ ! -d "/root/apache-maven-3.0.5/" ];then
    unzip apache-maven-3.0.5-bin.zip
    sed -i '/export MAVEN_HOME=/root/apache-maven-3.0.5/d' /etc/profile
    sed -i '$ a\export MAVEN_HOME=/root/apache-maven-3.0.5' /etc/profile
    sed -i '/export PATH=$MAVEN_HOME/bin:$PATH/d' /etc/profile
    sed -i '$ a\export PATH=$MAVEN_HOME/bin:$PATH' /etc/profile
    source /etc/profile
fi

if [ ! -d "/root/rocketmq-all-4.3.2/" ];then
    wget http://mirror.bit.edu.cn/apache/rocketmq/4.3.2/rocketmq-all-4.3.2-source-release.zip
    unzip rocketmq-all-4.3.2-source-release.zip
fi

cd /root/rocketmq-all-4.3.2/
# sed -i "s\-server -Xms4g -Xmx4g -Xmn4g\-server -Xms4g -Xmx4g -Xmn4g\g" /root/rocketmq-all-4.3.2/distribution/target/apache-rocketmq/bin/runbroker.sh
mvn -Prelease-all -DskipTests clean install -U


count=`ps -ef | grep mq | grep -v "grep" | wc -l`
if [ $count gt 0 ]
then
    ps -ef | grep mq | grep -v grep | awk '{print $2}' | xargs kill -9
    sleep 5s
fi

ps -fe | grep mqnamesrv | grep -v grep
if [ $? -ne 0 ]
then
    nohup sh /root/rocketmq-all-4.3.2/distribution/target/apache-rocketmq/bin/mqnamesrv >/dev/null 2>/dev/null &
    sleep 10s
fi
ps -fe | grep mqbroker | grep -v grep
if [ $? -ne 0 ]
then
    nohup sh /root/rocketmq-all-4.3.2/distribution/target/apache-rocketmq/bin/mqbroker -n localhost:9876  >/dev/null 2>/dev/null &
    sleep 10s
fi



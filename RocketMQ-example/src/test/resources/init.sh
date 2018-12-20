#!/bin/bash
cd /home/admin/

yum install wget
yum install unzip

if [ ! -d "/home/admin/jdk1.8.0_141/" ];then
    wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u141-b15/336fa29ff2bb4ef291e347e091f7f4a7/jdk-8u141-linux-x64.tar.gz"
    tar -zxvf jdk-8u141-linux-x64.tar.gz
    sed -i '/export JAVA_HOME=/home/admin/jdk1.8.0_141/d' /etc/profile
    sed -i '$ a\export JAVA_HOME=/home/admin/jdk1.8.0_141' /etc/profile
    sed -i '/export PATH=$JAVA_HOME/bin:$PATH/d' /etc/profile
    sed -i '$ a\export PATH=$JAVA_HOME/bin:$PATH' /etc/profile
    source /etc/profile
fi
if [ ! -d "/home/admin/apache-maven-3.0.5/" ];then
    wget https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.zip
    unzip apache-maven-3.0.5-bin.zip
    sed -i '/export MAVEN_HOME=/home/admin/apache-maven-3.0.5/d' /etc/profile
    sed -i '$ a\export MAVEN_HOME=/home/admin/apache-maven-3.0.5' /etc/profile
    sed -i '/export PATH=$MAVEN_HOME/bin:$PATH/d' /etc/profile
    sed -i '$ a\export PATH=$MAVEN_HOME/bin:$PATH' /etc/profile
    source /etc/profile
fi

if [ ! -d "/home/admin/rocketmq-all-4.3.2/" ];then
    wget http://mirror.bit.edu.cn/apache/rocketmq/4.3.2/rocketmq-all-4.3.2-source-release.zip
    unzip rocketmq-all-4.3.2-source-release.zip
fi

if [ ! -d "/home/admin/rocketmq-all-4.3.2/distribution/target/apache-rocketmq/" ];then
    cd /home/admin/rocketmq-all-4.3.2/
    mvn -Prelease-all -DskipTests clean install -U
fi
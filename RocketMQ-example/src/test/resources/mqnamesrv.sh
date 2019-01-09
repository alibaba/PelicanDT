#!/bin/bash

cd /root/rocketmq-all-4.3.2/distribution/target/apache-rocketmq
nohup sh bin/mqnamesrv >/dev/null 2>/dev/null &

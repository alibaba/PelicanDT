#!/bin/bash
cd /home/admin/rocketmq-all-4.3.2/distribution/target/apache-rocketmq
nohup sh bin/mqbroker -n localhost:9876  >/dev/null 2>/dev/null &

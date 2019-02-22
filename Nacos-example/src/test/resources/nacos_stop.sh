#!/bin/bash
cd /root

ps -ef | grep nacos-server | grep -v grep | awk '{print $2}' | xargs kill -9
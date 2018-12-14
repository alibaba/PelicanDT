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

package com.alibaba.pelican.chaos.client.group;

import com.alibaba.pelican.chaos.client.task.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author moyun@middleware
 */
@Slf4j
public class ParallelExecutor {

    private static ExecutorService threadPool = new ThreadPoolExecutor(5, 200,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(1024), new BasicThreadFactory.Builder().namingPattern("parallel-executor-%d")
            .build(), new ThreadPoolExecutor.AbortPolicy());

    /**
     * 并发执行器
     */
    public static <T> Map<String, T> execCmdByTask(List<? extends Callable<T>> taskList) {

        Map<String, Future<T>> results = new ConcurrentHashMap<String, Future<T>>(16);
        for (int i = 0; i < taskList.size(); i++) {
            AbstractTask<T> t = (AbstractTask<T>) taskList.get(i);
            results.put(t.getClient().getIp(), threadPool.submit(taskList.get(i)));
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            log.warn("exec command task awaitTermination, InterruptedException has occurred.");
            return Collections.emptyMap();
        }
        Map<String, T> resultInfos = new ConcurrentHashMap<String, T>(16);
        try {
            for (int i = 0; i < taskList.size(); i++) {
                AbstractTask<T> t = (AbstractTask<T>) taskList.get(i);
                String ip = t.getClient().getIp();
                resultInfos.put(ip, results.get(ip).get());
            }
        } catch (InterruptedException e) {
            log.error("exec command task, InterruptedException has occurred.");
            return Collections.emptyMap();
        } catch (ExecutionException e) {
            log.error("exec command task, ExecutionException has occurred.");
            return Collections.emptyMap();
        }
        return resultInfos;
    }
}

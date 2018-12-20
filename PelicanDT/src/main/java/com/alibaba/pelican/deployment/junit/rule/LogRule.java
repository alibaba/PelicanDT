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
package com.alibaba.pelican.deployment.junit.rule;

import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;

/**
 * @author moyun@middleware
 */
@Slf4j
public class LogRule extends TestWatcher {

    public static String getTestPath() {
        File f = new File(LogRule.class.getClassLoader().getResource("./").getFile()).getParentFile().getParentFile();
        File base = new File(f.getPath() + File.separator + "/src/test/java/");
        return base.getPath();
    }

    @Override
    public void succeeded(Description description) {
        log.info(String.format("Success for TC[%s]!", description.getDisplayName()));
    }

    @Override
    public void failed(Throwable e, Description description) {
        log.info(String.format("Failed for TC[%s]!", description.getDisplayName()));
    }

    @Override
    public void starting(Description description) {
        log.info("--------- TO NEXT CASE ---------");
        log.info(String.format("Run TC[%s]", description.getDisplayName()));
    }
}

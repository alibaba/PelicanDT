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
package com.alibaba.pelican.deployment.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author moyun@middleware
 */
@Slf4j
public class SystemUtils {

    public static void exit(Throwable ex, String message) {
        log.error(message, ex);
        log.error(message);
        log.error("system error", ex);
        System.exit(1);
    }

    public static void exit(String message) {
        log.error(message);
        log.error(message);
        System.exit(1);
    }
}

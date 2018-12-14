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

package com.alibaba.pelican.chaos.client.task;

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author moyun@middleware
 */
@Slf4j
public class LetCmdTask extends AbstractTask<Object> {

    private Object[] args;

    private Method method;

    public LetCmdTask(Method method, Object[] args, RemoteCmdClient client) {
        super(client);
        this.args = args;
        this.method = method;
    }

    @Override
    public Object execute() {
        try {
            return (Object) method.invoke(client, args);
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException, Arguments are Illegal.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException, can not access this method.");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException, " + "this is an illegal invocable target.");
            e.printStackTrace();
        }
        return null;
    }
}

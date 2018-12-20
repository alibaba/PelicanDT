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

package com.alibaba.pelican.chaos.client.cmd.event;

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import lombok.Data;

/**
 * @author moyun@middleware
 */
@Data
public class CmdEvent {

    protected RemoteCmdClient sourceClient;

    protected CmdParameter params = new CmdParameter();

    private long when;

    private String actionCmd;

    private Object result;

    private boolean successful = false;

    public static final String GREP_KEY_WORDS = "GREP_KEY_WORDS";

}

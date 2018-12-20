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

/**
 * @author moyun@middleware
 */
public class UploadFileTask extends AbstractTask<Boolean> {

    private String srcFile;

    private String destDir;

    public UploadFileTask(RemoteCmdClient client, String srcFile, String destDir) {
        super(client);
        this.srcFile = srcFile;
        this.destDir = destDir;
    }

    @Override
    public Boolean execute() {
        return client.uploadFile(srcFile, destDir).isEmpty();
    }
}

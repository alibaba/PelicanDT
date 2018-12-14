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
package com.alibaba.pelican.deployment.manager.entity;

/**
 * @author moyun@middleware
 */
public class ExcutorResult {

    private String info;

    private Throwable error;

    private ExcutorStatus result = ExcutorStatus.SUCCESS;

    public static ExcutorResult getSuccessResult() {
        ExcutorResult res = new ExcutorResult();
        res.setResult(ExcutorStatus.SUCCESS);
        return res;
    }

    public static ExcutorResult getSuccessResult(String succInfo) {
        ExcutorResult res = new ExcutorResult();
        res.setInfo(succInfo);
        res.setResult(ExcutorStatus.SUCCESS);
        return res;
    }

    public static ExcutorResult getFailedResult(Throwable exception) {
        ExcutorResult res = new ExcutorResult();
        res.setInfo(exception.getMessage());
        res.setError(exception);
        res.setResult(ExcutorStatus.FAILED);
        return res;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable exception) {
        this.error = exception;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ExcutorStatus getResult() {
        return result;
    }

    public void setResult(ExcutorStatus result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return this.result.equals(ExcutorStatus.SUCCESS);
    }
}

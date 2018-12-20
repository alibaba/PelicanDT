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

package com.alibaba.pelican.chaos.client.dto;


import org.apache.commons.lang3.StringUtils;

/**
 * @author moyun@middleware
 */
public class TrafficDto {

    private String nicName;

    private String rxBytePerSecond;

    private String txBytePerSecond;

    public TrafficDto(String trafficString) {
        String[] trafficArray = StringUtils.split(trafficString, " ");
        this.nicName = trafficArray[1].trim();
        this.rxBytePerSecond = trafficArray[4].trim();
        this.txBytePerSecond = trafficArray[5].trim();
    }

    public TrafficDto() {
    }

    public String getNicName() {
        return nicName;
    }

    public String getRxBytePerSecond() {
        return rxBytePerSecond;
    }

    public String getTxBytePerSecond() {
        return txBytePerSecond;
    }

    @Override
    public String toString() {
        return String.format("NIC\t: %s\nrx\t: %s\tB/s\ntx\t: %s\tB/s\n",
                nicName, rxBytePerSecond, txBytePerSecond);
    }
}

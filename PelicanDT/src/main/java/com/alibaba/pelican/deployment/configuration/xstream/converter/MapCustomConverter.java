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
package com.alibaba.pelican.deployment.configuration.xstream.converter;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.pelican.deployment.configuration.properties.PropertiesUtil;
import com.alibaba.pelican.deployment.configuration.xstream.entity.XstreamMap;
import com.alibaba.pelican.deployment.utils.ConfigurationUtils;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * @author moyun@middleware
 */
public class MapCustomConverter extends AbstractCollectionConverter {

    public MapCustomConverter(Mapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(XstreamMap.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        Map<?, ?> map = (Map) source;
        for (Iterator<?> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Entry entry = (Entry) iterator.next();
            ExtendedHierarchicalStreamWriterHelper.startNode(writer, "param", Entry.class);

            writer.addAttribute("key", entry.getKey().toString().trim());
            writer.addAttribute("value", entry.getValue().toString().trim());
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map<String, String> map = (Map) createCollection(context.getRequiredType());
        populateMap(reader, context, map);
        return map;
    }

    protected void populateMap(HierarchicalStreamReader reader, UnmarshallingContext context,
                               Map<String, String> map) {
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String key = "";
            if (StringUtils.isNotBlank(reader.getAttribute("file"))) {
                String path = reader.getAttribute("value").trim();
                if (!path.contains("target")) {
                    String targetPath = this.getClass().getResource("/").getPath() + "../";
                    path = targetPath + path;
                }
                String cusProp = PropertiesUtil.get("dtaf.conf.path");//云环境指定path
                if (StringUtils.isNotBlank(cusProp)) {
                    path = cusProp +System.getProperty("file.separator")+ path.substring(path.lastIndexOf("/")+1);
                    System.out.println("Cloud properties path = " + path);
                }
                // 读取属性文件
                Properties p = ConfigurationUtils.initProperties(path);
                // 合并
                for (Entry<Object, Object> entry : p.entrySet()) {
                    map.put((String) entry.getKey(), (String) entry.getValue());
                }
            } else {
                key = reader.getAttribute("key").trim();
                String value = reader.getAttribute("value").trim();
                map.put(key, value);
            }
            reader.moveUp();
        }
    }
}

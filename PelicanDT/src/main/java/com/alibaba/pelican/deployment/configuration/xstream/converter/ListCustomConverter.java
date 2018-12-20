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
import com.alibaba.pelican.deployment.configuration.xstream.entity.XstreamList;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.Iterator;
import java.util.List;


/**
 * @author moyun@middleware
 */
public class ListCustomConverter extends AbstractCollectionConverter {

    public ListCustomConverter(Mapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(XstreamList.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // 把list输出为字符串
        List<String> list = (List) source;
        if (!list.isEmpty()) {
            ExtendedHierarchicalStreamWriterHelper.startNode(writer, "group", String.class);
            StringBuilder sb = new StringBuilder();
            for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
                String string = iterator.next();
                sb.append(string.trim());
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            writer.setValue(sb.toString());
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        List<String> list = (List) createCollection(context.getRequiredType());
        populateList(reader, context, list);
        return list;
    }

    protected void populateList(HierarchicalStreamReader reader, UnmarshallingContext context,
                                List<String> list) {
        String value = reader.getValue();
        if (StringUtils.isBlank(value)) {
            list.add("default");
        } else {
            for (String item : value.split(",")) {
                list.add(item.trim());
            }
        }
    }
}

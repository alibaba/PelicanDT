package com.alibaba.pelican.deployment.configuration.xml.impl;


import com.alibaba.pelican.deployment.configuration.xml.XmlConfigurationOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author moyun@middleware
 */
@Slf4j
public class XmlConfigurationOperatorImpl implements XmlConfigurationOperator {

    private Document document = null;

    public XmlConfigurationOperatorImpl() {

    }

    public XmlConfigurationOperatorImpl(Document document) {
        this.document = document;
    }

    public XmlConfigurationOperatorImpl(String path) {
        this.document = (Document) deserialize(path);
    }

    @Override
    public Document getDocument() {
        return document;
    }

    private boolean isIllegal(String path) {
        boolean illegal = false;
        if (StringUtils.isBlank(path)) {
            illegal = true;
        } else if (path.endsWith("/") && !path.equals("/")) {
            illegal = true;
        }
        return illegal;
    }

    @Override
    public void createDocument(String filePath) {
        if (document == null) {
            log.warn("No document for saving operation, operation skip!");
        } else {
            serialize(document, filePath);
        }
    }

    @Override
    public List<Node> getNodes(String path) {
        List<Node> nodes = new ArrayList<Node>();
        if (isIllegal(path)) {
            log.warn("The path you provided is illegal, operation skip!");
        } else {
            List<Object> allnodes = document.selectNodes(path);
            for (Object node : allnodes) {
                if (node instanceof Node) {
                    nodes.add((Node) node);
                }
            }
        }
        return nodes;
    }

    @Override
    public Node getUniqueNode(String path) {
        List<Object> allnodes = null;
        if (isIllegal(path)) {
            log.warn("The path you provided is illegal, operation skip!");
        } else {
            allnodes = document.selectNodes(path);
            if (allnodes == null || allnodes.size() == 0) {
                return null;
            } else if (allnodes.size() > 1) {
                throw new IllegalArgumentException(String.format(
                        "No unique node found by path[%s]", path));
            }
        }
        return (Node) allnodes.get(0);
    }

    @Override
    public List<Node> getNodes(Node node, String path) {
        List<Node> nodes = new ArrayList<Node>();
        if (isIllegal(path)) {
            log.warn("The path you provided is illegal, operation skip!");
        } else {
            List<Object> allnodes = node.selectNodes(path);
            for (Object n : allnodes) {
                if (n instanceof Node) {
                    nodes.add((Node) n);
                }
            }
        }
        return nodes;
    }

    @Override
    public List<Node> getNodesWithText(String path, String textContent) {
        List<Node> nodes = new ArrayList<Node>();
        if (isIllegal(path)) {
            log.warn("The path you provided is illegal, operation skip!");
        } else {
            if (null == textContent) {
                textContent = "";
            }

            List<Node> allnodes = getNodes(path);
            for (Node node : allnodes) {
                if (node.getText().equals(textContent)) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }

    @Override
    public List<Node> getNodesWithChildTextNode(String path, String textName, String textValue) {
        List<Node> nodes = new ArrayList<Node>();
        if (isIllegal(path)) {
            log.warn("The path you provided is illegal, operation skip!");
        } else if (null == textName) {
            log.warn("The textNode name is null, operation skip!");
        } else {
            if (null == textValue) {
                textValue = "";
            }
            nodes = getNodes(path + "[" + textName + "=\"" + textValue + "\"]");
        }
        return nodes;
    }

    @Override
    public List<Node> getNodeWithSilbingTextNode(String path, String sibTextName,
                                                 String sibTextValue) {
        List<Node> nodes = new ArrayList<Node>();
        if (isIllegal(path)) {
            log.warn("The path you provided is empty, operation skip!");
        } else if (StringUtils.isBlank(sibTextName)) {
            log.warn("The sibling name you privide is blank, operation skip!");
        } else {
            String parentPath = path.substring(0, path.lastIndexOf('/'));
            if (StringUtils.isBlank(parentPath)) {
                log.warn("The path you provided is root, do not have sibling, operation skip!");
            } else {
                if (null == sibTextValue) {
                    sibTextValue = "";
                }

                String childPath = path.substring(path.lastIndexOf('/') + 1, path.length());
                List<Node> parents = getNodes(parentPath + "/.[" + sibTextName + "=\""
                        + sibTextValue + "\"]");
                for (Node parent : parents) {
                    nodes.addAll(getNodes(parent, childPath));
                }
            }
        }
        return nodes;
    }

    @Override
    public List<Node> getNodeWithDescendantTextNode(String path, String desPath, String desTextValue) {
        List<Node> nodes = new ArrayList<Node>();
        if (isIllegal(path) || isIllegal(desPath) || desPath.startsWith("/")) {
            log.warn("The path you provided is empty, operation skip!");
        } else {
            if (null == desTextValue) {
                desTextValue = "";
            }

            List<Node> allnodes = getNodes(path);
            for (Node node : allnodes) {
                List<Node> descentants = getNodes(node, desPath);
                for (Node descentant : descentants) {
                    if (descentant.getText().equals(desTextValue)) {
                        nodes.add(node);
                        break;
                    }
                }
            }
        }
        return nodes;
    }

    @Override
    public List<Node> getNodesWithAttribute(String path, String attriName, String attriValue) {
        List<Node> nodes = null;
        if (isIllegal(path)) {
            log.warn("The path you provided or the attribute name is null, operation skip!");
        } else if (null == attriName) {
            log.warn("The attribute name is null, operation skip!");
        } else {
            if (null == attriValue) {
                attriValue = "";
            }
            nodes = getNodes(path + "[@" + attriName + "=\"" + attriValue + "\"]");
        }
        return nodes;
    }

    @Override
    public List<String> getTextValues(String path) {
        List<String> text = new ArrayList<String>();
        List<Node> nodes = getNodes(path);
        for (Node node : nodes) {
            text.add(((Node) node).getText());
        }
        return text;
    }

    @Override
    public List<String> getAttributeValues(String path, String attriName) {
        List<String> attribute = new ArrayList<String>();
        if (null == attriName) {
            log.warn("attriName is null, operation skip");
        } else {
            List<Node> nodes = getNodes(path);
            for (Object node : nodes) {
                if (node instanceof Element) {
                    Attribute attri = ((Element) node).attribute(attriName);
                    if (null != attri) {
                        attribute.add(attri.getValue());
                    }
                }
            }
        }
        return attribute;
    }

    @Override
    public void replaceTextValue(String path, String oldValue, String newValue) {
        List<Node> nodes = getNodes(path);
        replaceTextValue(nodes, oldValue, newValue);
    }

    @Override
    public void replaceTextValue(List<Node> nodes, String oldValue, String newValue) {
        if (null != nodes) {
            for (Node node : nodes) {
                replaceTextValue(node, oldValue, newValue);
            }
        }
    }

    @Override
    public void replaceTextValue(Node node, String oldValue, String newValue) {
        if (null == node) {
            log.warn("node is null, operation skip");
        } else {
            if (null == oldValue) {
                oldValue = "";
            }
            if (null == newValue) {
                newValue = "";
            }

            if (node.getText().equals(oldValue)) {
                node.setText(newValue);
            }
        }

    }

    @Override
    public void modifyTextValue(String path, String newValue) {
        List<Node> nodes = getNodes(path);
        modifyTextValue(nodes, newValue);
    }

    @Override
    public void modifyTextValue(List<Node> nodes, String newValue) {
        if (null != nodes) {
            for (Node node : nodes) {
                modifyTextValue(node, newValue);
            }
        }
    }

    @Override
    public void modifyTextValue(Node node, String newValue) {
        if (null == node) {
            log.warn("node is null, operation skip");
        } else {
            if (null == newValue) {
                newValue = "";
            }
            node.setText(newValue);
        }
    }

    @Override
    public void modifyAttribute(Node node, String attriName, String newAttriValue) {
        if (null == node || StringUtils.isBlank(attriName)) {
            log.warn("node is null or attriName is blank, operation skip!");
        } else if (node instanceof Element) {
            if (null == newAttriValue) {
                newAttriValue = "";
            }
            Attribute attri = ((Element) node).attribute(attriName);
            if (null != attri) {
                attri.setValue(newAttriValue);
            }
        }
    }

    @Override
    public void modifyAttribute(List<Node> nodes, String attriName, String newAttriValue) {
        if (null != nodes) {
            for (Node node : nodes) {
                modifyAttribute(node, attriName, newAttriValue);
            }
        }
    }

    @Override
    public void modifyAttribute(String path, String attriName, String newAttriValue) {
        List<Node> nodes = getNodes(path);
        modifyAttribute(nodes, attriName, newAttriValue);
    }

    @Override
    public void modifyAttribute(String path, String attriNameforSearch, String attriValueforSearch,
                                String attriName, String newAttriValue) {
        List<Node> nodes = getNodesWithAttribute(path, attriNameforSearch, attriValueforSearch);
        modifyAttribute(nodes, attriName, newAttriValue);
    }

    @Override
    public void replaceAttribute(Node node, String attriName, String oldAttriValue,
                                 String newAttriValue) {
        if (null == node || StringUtils.isBlank(attriName)) {
            log.warn("node is null or attrName is blank, operation skip");
        } else if (node instanceof Element) {
            //如果属性值为null，则设置为空字符串
            if (oldAttriValue == null) {
                oldAttriValue = "";
            }
            if (newAttriValue == null) {
                newAttriValue = "";
            }

            Attribute attri = ((Element) node).attribute(attriName);
            if ((null != attri) && attri.getValue().equals(oldAttriValue)) {
                attri.setValue(newAttriValue);
            }
        }
    }

    @Override
    public void replaceAttribute(List<Node> nodes, String attriName, String oldAttriValue,
                                 String newAttriValue) {
        if (null != nodes) {
            for (Node node : nodes) {
                replaceAttribute(node, attriName, oldAttriValue, newAttriValue);
            }
        }
    }

    @Override
    public void replaceAttribute(String path, String attriName, String oldAttriValue,
                                 String newAttriValue) {
        List<Node> nodes = getNodes(path);
        replaceAttribute(nodes, attriName, oldAttriValue, newAttriValue);
    }

    @Override
    public void deleteNodes(String path) {
        List<Node> nodes = getNodes(path);
        deleteNodes(nodes);
    }

    @Override
    public void deleteNodes(List<Node> nodes) {
        if (null != nodes) {
            for (Node node : nodes) {
                deleteNode(node);
            }
        }
    }

    @Override
    public void deleteNode(Node node) {
        if (null == node) {
            log.warn("The node to be deleted is null, operation skip!");
        } else {
            node.detach();
        }
    }

    @Override
    public void deleteNodesWithChildTextNode(String path, String textName, String textValue) {
        List<Node> nodes = getNodesWithChildTextNode(path, textName, textValue);
        deleteNodes(nodes);
    }

    @Override
    public void deleteNodesWithText(String path, String textContent) {
        List<Node> nodes = getNodesWithText(path, textContent);
        deleteNodes(nodes);
    }

    @Override
    public void deleteNodesWithAttribute(String path, String attriName, String attriValue) {
        List<Node> nodes = getNodesWithAttribute(path, attriName, attriValue);
        deleteNodes(nodes);
    }

    @Override
    public void deleteAttribute(Node node, String attriName, String attriValue) {
        if (null == node || StringUtils.isBlank(attriName)) {
            log.warn("node is null or attrName is blank, operation skip");
        } else if (node instanceof Element) {
            //如果属性值为null，则设置为空字符串
            if (attriValue == null) {
                attriValue = "";
            }
            Attribute attri = ((Element) node).attribute(attriName);
            if ((null != attri) && attri.getValue().equals(attriValue)) {
                attri.detach();
            }
        }
    }

    @Override
    public void deleteAttribute(List<Node> nodes, String attriName, String attriValue) {
        if (null != nodes) {
            for (Node node : nodes) {
                deleteAttribute(node, attriName, attriValue);
            }
        }
    }

    @Override
    public void deleteAttribute(String path, String attriName, String attriValue) {
        List<Node> nodes = getNodes(path);
        deleteAttribute(nodes, attriName, attriValue);
    }

    @Override
    public void deleteAttribute(Node node, String attriName) {
        if (null == node || StringUtils.isBlank(attriName)) {
            log.warn("node is null or attrName is blank, operation skip");
        } else if (node instanceof Element) {
            Attribute attri = ((Element) node).attribute(attriName);
            if (null != attri) {
                attri.detach();
            }
        }
    }

    @Override
    public void deleteAttribute(List<Node> nodes, String attriName) {
        if (null != nodes) {
            for (Node node : nodes) {
                deleteAttribute(node, attriName);
            }
        }
    }

    @Override
    public void deleteAttribute(String path, String attriName) {
        List<Node> nodes = getNodes(path);
        deleteAttribute(nodes, attriName);
    }

    @Override
    public void deleteAttribute(String path, String attriNameforSearch, String attriValueforSearch,
                                String attriName) {
        List<Node> nodes = getNodesWithAttribute(path, attriNameforSearch, attriValueforSearch);
        deleteAttribute(nodes, attriName);
    }

    public void addChildNode(Node node, Node newNode) {
        if (null == node || null == newNode) {
            log.warn("node to be added or add is null, operation skip!");
        } else if (node instanceof Branch) {
            ((Branch) node).add(newNode);
        } else {
            log.warn("The node is a leaf, could not append child , operation skip!");
        }
    }

    public void addChildNode(List<Node> nodes, Node newNode) {
        if (null != nodes) {
            for (Node node : nodes) {
                addChildNode(node, (Node) newNode.clone());
            }
        }
    }

    public void addChildNode(String path, Node newNode) {
        List<Node> nodes = getNodes(path);
        addChildNode(nodes, newNode);
    }

    @Override
    public void addAttribute(Node node, String attriName, String attriValue) {
        if (node == null || StringUtils.isBlank(attriName)) {
            log.warn("node is null or attriName is blank, operation skip!");
        } else if (node instanceof Element) {
            if (null == attriValue) {
                attriValue = "";
            }
            Attribute attr = DocumentHelper.createAttribute((Element) node, attriName, attriValue);
            ((Element) node).add(attr);
        } else {
            log.warn("node is not a element, operation skip!");
        }
    }

    public void addAttribute(List<Node> nodes, String attriName, String attriValue) {
        if (null != nodes) {
            for (Node node : nodes) {
                addAttribute(node, attriName, attriValue);
            }
        }
    }

    @Override
    public void addAttribute(String path, String attriName, String attriValue) {
        List<Node> nodes = getNodes(path);
        addAttribute(nodes, attriName, attriValue);
    }

    @Override
    public Node addChildTextNode(Node node, String textNodeName, String textNodeValue) {
        Node newTextNode = null;
        if (node == null || StringUtils.isBlank(textNodeName)) {
            log.warn("node is null or textNodeName is blank, operation skip!");
        } else {
            if (textNodeValue == null) {
                textNodeValue = "";
            }
            newTextNode = DocumentHelper.createElement(textNodeName);
            newTextNode.setText(textNodeValue);
            addChildNode(node, newTextNode);
        }
        return newTextNode;
    }

    public void addChildTextNode(List<Node> nodes, String textNodeName, String textNodeValue) {
        for (Node node : nodes) {
            addChildTextNode(node, textNodeName, textNodeValue);
        }
    }

    @Override
    public void addChildTextNode(String path, String textNodeName, String textNodeValue) {
        List<Node> nodes = getNodes(path);
        addChildTextNode(nodes, textNodeName, textNodeValue);
    }

    /**
     * 将一个xml文档转化成一个Document对象
     */
    @Override
    public Object deserialize(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException(String.format(
                    "The file[%s] must be existed and must be a real file!", path));
        }
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = (Document) reader.read(file);
        } catch (DocumentException e) {
            log.error(String.format("The file[%s] can't convert to be a document", path), e);
        }
        return document;
    }

    @Override
    public void serialize(Object object, String filePath) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("utf-8");
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(new FileOutputStream(filePath), format);
            writer.write(object);
        } catch (IOException e) {
            log.error(String.format("Writing xml object into a file[%s] failed!", filePath), e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                log.error("Close xml writer error!", e);
            }
        }
    }

    @Override
    public Object deserialize(String file, Map<String, String> params) {
        return deserialize(file);
    }

    @Override
    public void serialize(Object object, String filePath, Map<String, String> params) {
        serialize(object, filePath);
    }

    @Override
    public Node addChildNode(String path, String nodeName) {
        List<Node> nodes = getNodes(path);
        if (nodes.size() > 0) {
            Node pNode = nodes.get(0);
            Node cNode = null;
            if (pNode == null || StringUtils.isBlank(nodeName)) {
                log.warn("node is null or NodeName is blank, operation skip!");
            } else {
                cNode = DocumentHelper.createElement(nodeName);
                addChildNode(pNode, cNode);
                return cNode;
            }
        }
        return null;
    }
}

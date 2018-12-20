package com.alibaba.pelican.deployment.configuration.xml;

import com.alibaba.pelican.deployment.configuration.operator.ConfigurationOperator;
import org.dom4j.Document;
import org.dom4j.Node;

import java.util.List;


/**
 * @author moyun@middleware
 */
public interface XmlConfigurationOperator extends ConfigurationOperator {

    void createDocument(String filePath);

    List<Node> getNodes(String path);

    List<Node> getNodes(Node node, String path);

    Node getUniqueNode(String path);

    List<Node> getNodesWithText(String path, String textContent);

    List<Node> getNodesWithChildTextNode(String path, String textName, String textValue);

    List<Node> getNodeWithSilbingTextNode(String path, String sibTextName, String sibTextValue);

    List<Node> getNodeWithDescendantTextNode(String path, String desPath, String desTextValue);

    List<Node> getNodesWithAttribute(String path, String attriName, String attriValue);

    List<String> getTextValues(String path);

    List<String> getAttributeValues(String path, String attriName);

    void modifyTextValue(Node node, String newValue);

    void modifyTextValue(List<Node> nodes, String newValue);

    void modifyTextValue(String path, String newValue);

    void replaceTextValue(Node node, String oldValue, String newValue);

    void replaceTextValue(List<Node> nodes, String oldValue, String newValue);

    void replaceTextValue(String path, String oldValue, String newValue);

    void modifyAttribute(String path, String attriName, String newAttriValue);

    void modifyAttribute(Node node, String attriName, String newAttriValue);

    void modifyAttribute(List<Node> nodes, String attriName, String newAttriValue);

    void modifyAttribute(String path, String attriNameforSearch, String attriValueforSearch,
                         String attriName, String newAttriValue);

    void replaceAttribute(Node node, String attriName, String oldAttriValue, String newAttriValue);

    void replaceAttribute(List<Node> nodes, String attriName, String oldAttriValue,
                          String newAttriValue);

    void replaceAttribute(String path, String attriName, String oldAttriValue, String newAttriValue);

    void deleteNode(Node node);

    void deleteNodes(List<Node> nodes);

    void deleteNodes(String path);

    void deleteNodesWithChildTextNode(String path, String textName, String textValue);

    void deleteNodesWithText(String path, String textContent);

    void deleteNodesWithAttribute(String path, String attriName, String attriValue);

    void deleteAttribute(Node node, String attriName, String attriValue);

    void deleteAttribute(List<Node> nodes, String attriName, String attriValue);

    void deleteAttribute(String path, String attriName, String attriValue);

    void deleteAttribute(Node node, String attriName);

    void deleteAttribute(List<Node> nodes, String attriName);

    void deleteAttribute(String path, String attriName);

    void deleteAttribute(String path, String attriNameforSearch, String attriValueforSearch,
                         String attriName);

    Node addChildTextNode(Node node, String textNodeName, String textNodeValue);

    void addChildTextNode(String path, String textNodeName, String textNodeValue);

    Node addChildNode(String path, String nodeName);

    void addAttribute(Node node, String attriName, String attriValue);

    void addAttribute(String path, String attriName, String attriValue);

    public Document getDocument();
}

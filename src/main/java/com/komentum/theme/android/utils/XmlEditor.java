package com.komentum.theme.android.utils;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
@Component
public class XmlEditor {

  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

  /**
   * load color.xml document
   *
   * @param path path of color.xml
   * @return color.xml document
   */
  public Document loadDocument(String path) {
    try {
      File resource = new File(path);
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      return documentBuilder.parse(resource);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * save updated color.xml on the output file path
   *
   * @param outputFilePath output file's path
   * @param document       updated color.xml document
   */
  public void transform(String outputFilePath, Document document) {
    try {
      File outputFile = new File(outputFilePath);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.transform(new DOMSource(document), new StreamResult(outputFile));
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public void editAttributeByTagName(Document document, String tagName, String attrName,
      String updatedValue) {
    NodeList tagNodes = document.getElementsByTagName(tagName);
    for (int i = 0; i < tagNodes.getLength(); i++) {
      Element tagElement = (Element) tagNodes.item(i);
      String attrValue = tagElement.getAttribute(attrName);
      if (!attrValue.isEmpty()) {
        tagElement.setAttribute(attrName, updatedValue);
      }
    }
  }

  public void editTextByTagAttr(Document document, String tagName, String attrName,
      String attrValue, String textValue) {
    NodeList tagNodes = document.getElementsByTagName(tagName);
    for (int i = 0; i < tagNodes.getLength(); i++) {
      Element tagElement = (Element) tagNodes.item(i);
      if (tagElement.getAttribute(attrName).equals(attrValue)) {
        tagElement.setTextContent(textValue);
      }
    }
  }
}

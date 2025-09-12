package com.komentum.theme.android.editor;

import com.komentum.theme.android.dto.AndroidColorDto;
import com.komentum.theme.utils.ThemePathManager;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
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
public class AndroidColorStyleEditor {

  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

  private final String COLOR_TAG_NAME = "color";

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
  private void transform(String outputFilePath, Document document) {
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

  /**
   * get the first element with a specific attribute name from the node list
   *
   * @param nodeList node list to find elements
   * @param attrName attribute name to find
   * @return Element with a specific attribute name
   */
  private Element getElementByAttribute(NodeList nodeList, String attrName) {
    String ELEMENT_PROPS_NAME = "name";
    for (int i = 0; i < nodeList.getLength(); i++) {
      Element element = (Element) nodeList.item(i);
      if (element.getAttribute(ELEMENT_PROPS_NAME).equals(attrName)) {
        return element;
      }
    }
    return null;
  }

  /**
   * edit all color with theme's color info list
   *
   * @param themeId      theme's id
   * @param colorDtoList information list about theme's color
   */
  public void editColors(String themeId, List<AndroidColorDto> colorDtoList) {
    // 색상은 소문자 8자리 헥사코드이어야함 ( 안그러면 빌드가 안됨 )
    Path colorSheetPath = ThemePathManager.getColorSheetPath(themeId);
    Document document = loadDocument(colorSheetPath.toString());
    NodeList colorList = document.getElementsByTagName(COLOR_TAG_NAME);
    for (AndroidColorDto colorDto : colorDtoList) {
      Element colorElement = getElementByAttribute(colorList, colorDto.getAttrName());
      if (colorElement != null) {
        colorElement.setTextContent(colorDto.getColor());
      }
    }
    transform(colorSheetPath.toString(), document);
  }
}

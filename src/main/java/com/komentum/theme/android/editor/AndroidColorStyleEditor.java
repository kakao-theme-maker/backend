package com.komentum.theme.android.editor;

import com.komentum.global.utils.RegexValidator;
import com.komentum.theme.android.dto.AndroidColorDto;
import com.komentum.theme.android.utils.ThemePathManager;
import com.komentum.theme.android.utils.XmlEditor;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
@Component
@RequiredArgsConstructor
public class AndroidColorStyleEditor {

  private final XmlEditor xmlEditor;

  private final static String COLOR_TAG_NAME = "color";
  private final static String ELEMENT_PROPS_NAME = "name";

  /**
   * get the first element with a specific attribute name from the node list
   *
   * @param nodeList node list to find elements
   * @param attrName attribute name to find
   * @return Element with a specific attribute name
   */
  private Element getElementByAttribute(NodeList nodeList, String attrName) {
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
    Path colorSheetPath = ThemePathManager.getAndroidColorSheetPath(themeId);
    Document document = xmlEditor.loadDocument(colorSheetPath.toString());
    NodeList colorList = document.getElementsByTagName(COLOR_TAG_NAME);
    for (AndroidColorDto colorDto : colorDtoList) {
      Element colorElement = getElementByAttribute(colorList, colorDto.getAttrName());
      if (colorElement == null) {
        throw new IllegalArgumentException("cannot find color element on colors.xml");
      }
      if (!RegexValidator.isValidHexColor(colorDto.getColor())) {
        throw new IllegalArgumentException("invalid hex color");
      }
      colorElement.setTextContent(colorDto.getColor());
    }
    xmlEditor.transform(colorSheetPath.toString(), document);
  }
}

package com.komentum.theme.component.domain;

import com.komentum.theme.component.enums.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "color_style")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorStyle {

  @Id //PK
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "color_type_id")
  private Integer colorTypeId;

  @Column(name = "`explain`")
  private String explain;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Platform platform;

  @Column(name = "style_sheet_path", nullable = false)
  private String styleSheetPath;

  @Column(name = "style_element_name", nullable = false)
  private String styleElementName;

  @Column(name = "style_props_name", nullable = false)
  private String stylePropsName;

  public void update(ColorStyle colorStyle) {
    this.explain = colorStyle.explain;
    this.platform = colorStyle.platform;
    this.styleSheetPath = colorStyle.styleSheetPath;
    this.styleElementName = colorStyle.styleElementName;
    this.stylePropsName = colorStyle.stylePropsName;
  }

  @Deprecated
  public String getIosStyleName() {
    if (platform == Platform.IOS) {
      return styleElementName + "|" + stylePropsName;
    }
    return null;
  }

  @Deprecated
  public String getAndroidStyleName() {
    if (platform == Platform.ANDROID) {
      return stylePropsName;
    }
    return null;
  }

  public static class ColorStyleBuilder {
    @Deprecated
    public ColorStyleBuilder iosStyleName(String iosStyleName) {
      this.platform = Platform.IOS;
      if (iosStyleName != null && iosStyleName.contains("|")) {
        String[] parts = iosStyleName.split("\\|");
        this.styleElementName = parts[0];
        this.stylePropsName = parts[1];
      } else {
        this.styleElementName = iosStyleName;
        this.stylePropsName = "color";
      }
      this.styleSheetPath = "styles/ios.css";
      return this;
    }

    @Deprecated
    public ColorStyleBuilder androidStyleName(String androidStyleName) {
      this.platform = Platform.ANDROID;
      this.styleElementName = "View";
      this.stylePropsName = androidStyleName;
      this.styleSheetPath = "android/colors.xml";
      return this;
    }
  }
}

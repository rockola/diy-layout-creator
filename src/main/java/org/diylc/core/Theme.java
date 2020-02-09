/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.core;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.beryx.awt.color.ColorFactory;

import org.diylc.App;
import org.diylc.parsing.XmlNode;
import org.diylc.parsing.XmlReader;
import org.diylc.appframework.miscutils.Utils;

public class Theme implements Serializable {

  private static final long serialVersionUID = 1L;
  private static Logger LOG = LogManager.getLogger(Theme.class);

  public static final String THEME_KEY = "theme";
  public static final Theme DEFAULT_THEME = new Theme(
      "Default", Color.white, new Color(240, 240, 240), Color.black);

  private static Map<String, Theme> themes = new HashMap<>();

  private String name;
  private Color bgColor;
  private Color gridColor;
  private Color outlineColor;

  static {
    themes.put("Default", DEFAULT_THEME);

    URL themesUrl = Theme.class.getResource("/themes/themes.xml");
    XmlNode themesXml = XmlReader.read(themesUrl);
    themesXml.children.asMap().entrySet().stream().forEach((e) -> {
        String tag = e.getKey();
        if (tag.toLowerCase().equals("theme")) {
          for (XmlNode node : e.getValue()) {
            Theme theme = new Theme(node);
            LOG.trace("Found theme {}", theme.getName());
            themes.put(theme.getName(), theme);
          }
        } else {
          LOG.error("unknown element {} in themes.xml", tag);
        }
      });

    File themeDir = new File(Utils.getUserDataDirectory("themes"));
    if (themeDir.exists()) {
      for (File file : themeDir.listFiles()) {
        if (file.getName().toLowerCase().endsWith(".xml")) {
          try {
            Theme theme = new Theme(XmlReader.read(file));
            LOG.debug("Found theme {}", theme.getName());
            Theme.getThemes().put(theme.getName(), theme);
          } catch (Exception e) {
            LOG.info("Could not load theme file " + file.getName(), e);
          }
        }
      }
    }
  }

  public Theme(String name, Color bgColor, Color gridColor, Color outlineColor) {
    super();
    this.name = name;
    this.bgColor = bgColor;
    this.gridColor = gridColor;
    this.outlineColor = outlineColor;
  }

  public Theme(Theme theme) {
    super();
    this.name = theme.name;
    this.bgColor = theme.bgColor;
    this.bgColor = theme.bgColor;
    this.outlineColor = theme.outlineColor;
  }

  public Theme() {
    this(DEFAULT_THEME);
  }

  public Theme(XmlNode xml) {
    this();
    this.name = xml.attributes.get("name");
    xml.children.entries().stream().forEach((e) -> {
        XmlNode node = e.getValue();
        switch (node.tagName) {
          case "background-color":
            this.bgColor = getColor(node);
            break;
          case "grid-color":
            this.gridColor = getColor(node);
            break;
          case "outline-color":
            this.outlineColor = getColor(node);
            break;
          default:
        }
      });
  }

  private Color getColor(XmlNode xml) {
    // TODO: opacity <1
    int red = 0;
    int green = 0;
    int blue = 0;
    for (String key : xml.attributes.keySet()) {
      String value = xml.attributes.get(key);
      switch (key) {
        case "ref":
          return ColorFactory.valueOf(value);
        case "grayscale":
          int v = Integer.parseInt(value);
          return new Color(v, v, v);
        case "r":
          red = Integer.parseInt(value);
          break;
        case "g":
          green = Integer.parseInt(value);
          break;
        case "b":
          blue = Integer.parseInt(value);
          break;
        default:
      }
    }
    return new Color(red, green, blue);
  }

  public static Map<String, Theme> getThemes() {
    return themes;
  }

  public static Theme getTheme(String themeName) {
    return themes.get(themeName);
  }

  public String getName() {
    return name;
  }

  public Color getBgColor() {
    return bgColor;
  }

  public Color getGridColor() {
    return gridColor;
  }

  public Color getOutlineColor() {
    return outlineColor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bgColor == null) ? 0 : bgColor.hashCode());
    result = prime * result + ((gridColor == null) ? 0 : gridColor.hashCode());
    result = prime * result + ((outlineColor == null) ? 0 : outlineColor.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Theme other = (Theme) obj;
    if (bgColor == null) {
      if (other.bgColor != null) {
        return false;
      }
    } else if (!bgColor.equals(other.bgColor)) {
      return false;
    }
    if (gridColor == null) {
      if (other.gridColor != null) {
        return false;
      }
    } else if (!gridColor.equals(other.gridColor)) {
      return false;
    }
    if (outlineColor == null) {
      if (other.outlineColor != null) {
        return false;
      }
    } else if (!outlineColor.equals(other.outlineColor)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return name;
  }
}

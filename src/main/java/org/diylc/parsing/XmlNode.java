/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2020 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.parsing;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XmlNode {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(XmlNode.class);

  public static final String NO_TAG = "<>";

  public String tagName;
  public Map<String, String> attributes = new HashMap<>();
  public ListMultimap<String, XmlNode> children =
      MultimapBuilder.hashKeys().arrayListValues().build();
  public String value;

  public XmlNode() {
    //
  }

  public XmlNode(Element element) {
    tagName = element.getTagName();
    LOG.debug("XmlNode([element]) tagName {}", tagName);
    NamedNodeMap attrs = element.getAttributes();
    if (attrs != null) {
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attribute = attrs.item(i);
        addAttribute(attribute.getNodeName(), attribute.getNodeValue());
      }
    }
  }

  public void addAttribute(String key, String value) {
    LOG.debug("addAttribute({}, {})", key, value);
    attributes.put(key, value);
  }

  public String toString() {
    return toString(0);
  }

  private String toString(int level) {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < level; i++) {
      s.append(" ");
    }
    String pad = s.toString();

    return pad
        + "<"
        + tagName
        + (attributes.isEmpty() ? "" : " ")
        + attributes.entrySet().stream()
            .map(e -> String.format("%s=\"%s\"", e.getKey(), e.getValue()))
            .collect(Collectors.joining(" "))
        + ">"
        + (children.isEmpty() ? "" : "\n")
        + children.values().stream()
            .map(v -> v.toString(level + 1))
            .collect(Collectors.joining("\n"))
        + (value == null ? "" : value)
        + (children.isEmpty() ? "" : pad)
        + "</"
        + tagName
        + ">";
  }

  /**
   * Get value from first child that has it.
   *
   * @param key Key.
   * @return value if found, or null
   */
  public String getValue(String key) {
    String value = null;
    for (XmlNode n : children.get(key)) {
      value = n.getValue();
      if (value != null) {
        break;
      }
    }
    return value;
  }

  public String getValue() {
    return value;
  }

  /** */
  public boolean nodeIsA(String nodeName) {
    return tagName.toLowerCase().equals(nodeName.toLowerCase());
  }
}

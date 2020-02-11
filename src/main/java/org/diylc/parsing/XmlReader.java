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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Simplified XML reader for unmarshaling Project files. */
public class XmlReader {

  private static Logger LOG = LogManager.getLogger(XmlReader.class);

  private XmlReader() {
    //
  }

  private static XmlNode readInternal(Object o) throws IOException {
    XmlNode xml = null;
    try {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = null;
      if (o instanceof File) {
        document = db.parse((File) o);
      } else if (o instanceof URL) {
        String uriString = ((URL) o).toString();
        document = db.parse(uriString);
      } else {
        throw new RuntimeException("unknown argument of type " + o.getClass());
      }
      xml = unmarshal(document.getDocumentElement());
      LOG.debug("readInternal()\n{}", xml);
    } catch (SAXException | ParserConfigurationException e) {
      LOG.error("Could not parse " + o.toString(), e);
      throw new RuntimeException(e);
    } catch (IOException e) {
      LOG.error("Could not unmarshal " + o.toString(), e);
      throw e;
    }
    return xml;
  }

  public static XmlNode readFile(String fileName) {
    try {
      return readInternal(new File(fileName));
    } catch (IOException e) {
      LOG.error("readFile() could not read " + fileName, e);
    }
    return null;
  }

  public static XmlNode read(URL url) {
    try {
      return readInternal(url);
    } catch (IOException e) {
      LOG.error("read() could not read " + url.toString(), e);
    }
    return null;
  }

  public static XmlNode read(File file) throws IOException {
    try {
      return readInternal(file);
    } catch (IOException e) {
      LOG.error("read() could not read file", e);
      throw e;
    }
  }

  private static String nodeTypeToString(short nodeType) {
    switch (nodeType) {
      case Node.ELEMENT_NODE:
        return "ELEMENT";
      case Node.TEXT_NODE:
        return "TEXT";
      case Node.ATTRIBUTE_NODE:
        return "ATTR";
      default:
        return String.format("[unknown type %d]", nodeType);
    }
  }

  private static XmlNode unmarshal(Element element) {
    XmlNode node = new XmlNode(element);
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      LOG.debug(
          "Child #{} {} name {} value {}",
          i,
          nodeTypeToString(child.getNodeType()),
          child.getNodeName(),
          child.getNodeValue());
      switch (child.getNodeType()) {
        case Node.ELEMENT_NODE:
          // node.tagName = child.getNodeName();
          Element elt = (Element) child;
          LOG.trace("Add {} as child of {}", node.tagName, elt.getTagName());
          node.children.put(elt.getTagName(), unmarshal(elt));
          break;
        case Node.TEXT_NODE:
          node.value = child.getNodeValue();
          break;
        case Node.ATTRIBUTE_NODE:
          // handled earlier, we should not end up here
          LOG.error(
              "Found attribute node where none expected? {} {}",
              child.getNodeName(),
              child.getNodeValue());
          break;
        default:
          // ignore
      }
    }
    return node;
  }
}

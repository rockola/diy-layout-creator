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
  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.appframework;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.diylc.appframework.miscutils.IconImageConverter;

public class Serializer {
  private static XStream xs = null;
  private static XStream xsd = null;
  private static XStream xsj = null;

  static {
    xs = new XStream();
    xsd = new XStream(new DomDriver());
    xsj = new XStream(new JettisonMappedXmlDriver());

    xsd.registerConverter(new IconImageConverter());

    XStream.setupDefaultSecurity(xs); // to be removed after 1.5
    XStream.setupDefaultSecurity(xsd); // to be removed after 1.5
    XStream.setupDefaultSecurity(xsj); // to be removed after 1.5
    String[] allowTypes =
        new String[] {
          "org.diylc.**", "com.diyfever.**", "java.lang.**", "java.awt.**",
        };
    xs.allowTypesByWildcard(allowTypes);
    xsd.allowTypesByWildcard(allowTypes);
    xsj.allowTypesByWildcard(allowTypes);
  }

  /**
     Fetch object from URL and deserialize.

     @param url String specifying the URL
     @return deserialized object
  */
  public static Object fromURL(String url) throws IOException {
    Object o = null;
    try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream())) {
      o = xsd.fromXML(in);
    }
    return o;
  }

  /**
     Fetch object from resource and deserialize.

     @param resource String specifying the resource
     @return deserialized object
  */
  public static Object fromResource(String resource) throws IOException {
    Object o = null;
    try (BufferedInputStream in =
         new BufferedInputStream(Serializer.class.getResourceAsStream(resource))) {
      o = xsd.fromXML(in);
    }
    return o;
  }

  /**
     Fetch object from file and deserialize.

     Files should be used sparingly; use resources or remote URLs instead.

     @param file Filename
     @return deserialized object
  */
  public static Object fromFile(String file) throws IOException {
    Object o = null;
    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
      o = xsd.fromXML(in);
    }
    return o;
  }

  /**
     Fetch object from file and deserialize.

     Files should be used sparingly; use resources or remote URLs instead.

     @param file File to deserialize
     @return deserialized object
  */
  public static Object fromFile(File file) throws IOException {
    Object o = null;
    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
      o = xsd.fromXML(in);
    }
    return o;
  }

  /**
     Serialize object to file.

     @param file Filename
     @param o Object to serialize
  */
  public static void toFile(String file, Object o) throws IOException {
    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
      xsd.toXML(o, out);
    }
  }

  /**
     Serialize object to file.

     @param file File
     @param o Object to serialize
  */
  public static void toFile(File file, Object o) throws IOException {
    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
      xsd.toXML(o, out);
    }
  }

  /**
     Deserialize object from input stream.

     @param stream Input stream
     @return deserialized object
  */
  public static Object fromInputStream(InputStream stream) throws IOException {
    Object o = null;
    if (stream != null) {
      // Deserialize the stream
      xsj.setMode(XStream.NO_REFERENCES);
      o = xsj.fromXML(stream);
    }
    return o;
  }
}

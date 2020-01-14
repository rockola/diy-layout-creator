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

  public static Object fromURL(String url) throws IOException {
    BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
    Object o = xsd.fromXML(in);
    in.close();
    return o;
  }

  public static Object fromResource(String r) throws IOException {
    BufferedInputStream in = new BufferedInputStream(Serializer.class.getResourceAsStream(r));
    Object o = xsd.fromXML(in);
    in.close();
    return o;
  }

  public static Object fromFile(String file) throws IOException {
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
    Object o = xsd.fromXML(in);
    in.close();
    return o;
  }

  public static Object fromFile(File file) throws IOException {
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
    Object o = xsd.fromXML(in);
    in.close();
    return o;
  }

  public static void toFile(String file, Object o) throws IOException {
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
    xsd.toXML(o, out);
    out.close();
  }

  public static void toFile(File file, Object o) throws IOException {
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
    xsd.toXML(o, out);
    out.close();
  }

  public static Object fromInputStream(InputStream stream) throws IOException {
    if (stream == null) {
      return null;
    }
    // Deserialize the stream
    xsj.setMode(XStream.NO_REFERENCES);
    return xsj.fromXML(stream);
  }
}

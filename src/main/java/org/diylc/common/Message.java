/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2010-2019 held jointly by the individual authors.

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

package org.diylc.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public final class Message {
  private static Logger LOG = LogManager.getLogger(Message.class);
  private static Parser parser = Parser.builder().build();
  private static HtmlRenderer defaultRenderer = HtmlRenderer.builder().build();

  private Message() {}

  public static String getHTML(String name, boolean wrapInHtmlTags, String softbreak) {
    ClassLoader loader = Message.class.getClassLoader();
    try {
      // look for 'name' in configuration first
      String markdownString = Config.getString(name);
      if (markdownString == null) {
        // 'name' was not found in configuration,
        // let's try it as a file in resources
        String markdownResource = String.format("org/diylc/messages/%s.md", name);
        LOG.trace("getHtml({}) looking for {}", name, markdownResource);
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(
              new InputStreamReader(loader.getResourceAsStream(markdownResource)));
          markdownString = reader.lines().collect(Collectors.joining("\n"));
        } finally {
          reader.close();
        }
      }
      Node document = parser.parse(markdownString);
      HtmlRenderer renderer = defaultRenderer;
      if (softbreak != null) {
        renderer = HtmlRenderer.builder().softbreak(softbreak).build();
      }
      String ret = renderer.render(document);
      LOG.info("getHtml({}) returns [{}]", name, ret);
      if (wrapInHtmlTags && ret != null) {
        ret = "<html>" + ret + "</html>";
      }
      return ret;
    } catch (Exception e) {
      LOG.error("getHtml(" + name + ") failed", e);
    }
    return "";
  }

  public static String getHTML(String name) {
    // wrap result in <html>...</html> by default
    return getHTML(name, true, null);
  }
}

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.*;

public final class Message {
    private static Parser parser = Parser.builder().build();
    private static HtmlRenderer renderer = HtmlRenderer.builder().build();

    private Message() { }

    public static String getHTML(String name) {
	ClassLoader loader = Message.class.getClassLoader();
	try {
	    String markdownResource = String.format("org/diylc/messages/%s.md", name);
	    LogManager.getLogger(Message.class).trace("getHtml({}) looking for {}",
						      name, markdownResource);
	    BufferedReader reader =
		new BufferedReader(new InputStreamReader(loader.getResourceAsStream(markdownResource)));
	    Node document = parser.parse(reader.lines().collect(Collectors.joining("\n")));
	    String ret = renderer.render(document);
	    LogManager.getLogger(Message.class).info("getHtml({}) returns [{}]", name, ret);
	    return ret;
	} catch (Exception e) {
	    LogManager.getLogger(Message.class).error("getHtml(" + name + ") failed", e);
	}
	return "";
    }
}

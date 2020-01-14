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

package org.diylc.appframework.miscutils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassLoaderUtil {

  private static Logger LOG = LogManager.getLogger(ClassLoaderUtil.class);
  private static final Class<?>[] parameters = new Class<?>[] {URL.class};

  /**
   * Add file to CLASSPATH.
   *
   * @param s File name
   * @throws IOException IOException
   */
  public static void addFile(String s) throws IOException {
    File f = new File(s);
    addFile(f);
  }

  /**
   * Add file to CLASSPATH.
   *
   * @param f File object
   * @throws IOException IOException
   */
  public static void addFile(File f) throws IOException {
    LOG.info("Adding file to the classpath: " + f.getAbsolutePath());
    addURL(f.toURI().toURL());
  }

  /**
   * Add URL to CLASSPATH.
   *
   * @param u URL
   * @throws IOException IOException
   */
  public static void addURL(URL u) throws IOException {

    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    URL[] urls = sysLoader.getURLs();
    for (int i = 0; i < urls.length; i++) {
      if (urls[i].toString().equalsIgnoreCase(u.toString())) {
        LOG.debug("URL " + u + " is already in the classpath.");
        return;
      }
    }
    Class<URLClassLoader> sysClass = URLClassLoader.class;
    try {
      Method method = sysClass.getDeclaredMethod("addURL", parameters);
      method.setAccessible(true);
      method.invoke(sysLoader, new Object[] {u});
    } catch (Throwable throwable) {
      LOG.error("Error, could not add URL to system classloader", throwable);
      throw new IOException("Error, could not add URL to system classloader");
    }
  }
}

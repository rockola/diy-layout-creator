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

import com.google.common.reflect.ClassPath;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;

public class Utils {

  private static final Logger LOG = LogManager.getLogger(Utils.class);

  static final String[] browsers = {
    "google-chrome",
    "firefox",
    "opera",
    "epiphany",
    "konqueror",
    "conkeror",
    "midori",
    "kazehakase",
    "mozilla"
  };
  static final String errMsg = "Error attempting to launch web browser";
  static final String userDataDirectoryBase = String.format(
      "%s%s.%s%sv%d%s",
      System.getProperty("user.home"),
      File.separator,
      App.getString("app.name"),
      File.separator,
      App.getVersionNumber().getMajor(),
      File.separator);

  public static void openURL(URL url) throws Exception {
    openURL(url.toString());
  }

  public static void openURL(String url) throws Exception {
    try { // attempt to use Desktop library from JDK 1.6+
      Class<?> d = Class.forName("java.awt.Desktop");
      d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
          d.getDeclaredMethod("getDesktop").invoke(null),
          new Object[] {java.net.URI.create(url)});
      // above code mimicks: java.awt.Desktop.getDesktop().browse()
    } catch (Exception ignore) { // library not available or failed
      String osName = System.getProperty("os.name");
      if (osName.startsWith("Mac OS")) {
        Class.forName("com.apple.eio.FileManager")
            .getDeclaredMethod("openURL", new Class[] {String.class})
            .invoke(null, new Object[] {url});
      } else if (osName.startsWith("Windows")) {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
      } else { // assume Unix or Linux
        String browser = null;
        for (String b : browsers) {
          if (browser == null
              && Runtime.getRuntime().exec(new String[] {"which", b}).getInputStream().read()
                  != -1) {
            Runtime.getRuntime().exec(new String[] {browser = b, url});
          }
        }
        if (browser == null) {
          throw new Exception(Arrays.toString(browsers));
        }
      }
    }
  }

  public static Object clone(Object o) {
    Object clone = null;

    try {
      clone = o.getClass().getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }

    // Walk up the superclass hierarchy
    for (Class<?> obj = o.getClass(); !obj.equals(Object.class); obj = obj.getSuperclass()) {
      Field[] fields = obj.getDeclaredFields();
      for (int i = 0; i < fields.length; i++) {
        fields[i].setAccessible(true);
        try {
          // for each class/superclass, copy all fields
          // from this object to the clone
          fields[i].set(clone, clone(fields[i].get(o)));
        } catch (IllegalArgumentException | IllegalAccessException e) {
          LOG.debug("clone() exception", e);
        }
      }
    }
    return clone;
  }

  private static boolean checkOsName(String target) {
    String os = System.getProperty("os.name").toLowerCase();
    // windows
    return os.indexOf(target) >= 0;
  }

  public static boolean isWindows() {
    return checkOsName("win");
  }

  public static boolean isMac() {
    return checkOsName("mac");
  }

  public static boolean isUnix() {
    // linux or unix
    return checkOsName("nix") || checkOsName("nux");
  }

  public static Set<Class<?>> getClasses(String packageName) throws Exception {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    return getClasses(loader, packageName);
  }

  private static Set<Class<?>> getClasses(ClassLoader loader, String packageName)
      throws IOException, ClassNotFoundException {

    Set<Class<?>> classes = new HashSet<Class<?>>();

    ClassPath cp = ClassPath.from(loader);
    for (ClassPath.ClassInfo ci : cp.getTopLevelClassesRecursive(packageName)) {

      LOG.debug(
          "getClasses(loader, '{}') Found class {} in package {} ({})",
          packageName,
          ci.getSimpleName(),
          ci.getPackageName(),
          ci.getName());
      classes.add(ci.load());
    }

    return classes;
  }

  /**
   * Creates a rectangle with opposite corners in the specified points.
   *
   * @param p1 First corner.
   * @param p2 Second corner.
   * @return new Rectangle
   */
  public static Rectangle createRectangle(Point p1, Point p2) {
    int minX = p1.x < p2.x ? p1.x : p2.x;
    int minY = p1.y < p2.y ? p1.y : p2.y;
    int width = Math.abs(p1.x - p2.x);
    int height = Math.abs(p1.y - p2.y);
    return new Rectangle(minX, minY, width, height);
  }

  public static String toCommaString(List<?> list) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        if (i == list.size() - 1) {
          builder.append(" and ");
        } else {
          builder.append(", ");
        }
      }
      builder.append(list.get(i));
    }
    return builder.toString();
  }

  public static String getUserDataDirectory(String subdirectory) {
    String userDir =
        subdirectory == null
        ? userDataDirectoryBase
        : String.format("%s%s%s", userDataDirectoryBase, subdirectory, File.separator);
    try {
      File dir = new File(userDir);
      if (!dir.isDirectory()) {
        // directory does not already exist
        boolean directoryCreated = dir.mkdirs();
        if (!directoryCreated) {
          throw new RuntimeException("Could not create user data directory " + userDir);
        }
      }
    } catch (SecurityException e) {
      LOG.error("Could not create user data directory {}", userDir);
      throw e;
    }
    return userDir;
  }

  public static String getUserDataDirectory() {
    return getUserDataDirectory(null);
  }
}

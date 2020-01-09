/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2019-2020 held jointly by the individual authors.

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
package org.diylc.common;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.KeyStroke;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ClasspathLocationStrategy;
import org.apache.commons.configuration2.io.CombinedLocationStrategy;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystemLocationStrategy;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.swing.gui.Keymap;

public final class Config {

  private static final Logger LOG = LogManager.getLogger(Config.class);

  private Config() {}

  private static final CompositeConfiguration config;
  // private static final Configurations configs = new Configurations();

  // private static XMLConfiguration config = null;
  // private static FileBasedConfigurationBuilder<XMLConfiguration> builder = null;
  private static Keymap keymap = null;

  private static final List<String> configurationFiles =
      Arrays.asList(
          "org/diylc/defaults.xml",
          "org/diylc/fonts.xml",
          "org/diylc/icons.xml",
          "org/diylc/strings-EN.xml");

  private static FileBasedConfigurationBuilder<XMLConfiguration> getBuilder(
      String configurationFile) {
    final FileLocationStrategy strategy =
        new CombinedLocationStrategy(
            Arrays.asList(new ClasspathLocationStrategy(), new FileSystemLocationStrategy()));
    return new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
        .configure(
            new Parameters().xml().setLocationStrategy(strategy).setFileName(configurationFile));
  }

  /* initialize configurations */
  static {
    config = new CompositeConfiguration();
    String f = "";
    try {
      for (String configurationFile : configurationFiles) {
        f = configurationFile;
        LOG.info("Adding {} to configurations.", configurationFile);
        config.addConfiguration(getBuilder(configurationFile).getConfiguration());
      }
      LOG.info("{} configurations added.", config.getNumberOfConfigurations());
    } catch (ConfigurationException e) {
      LOG.error("Could not read " + f, e);
    }
    try {
      keymap = Keymap.readDefaultKeymap();
    } catch (Exception e) {
      LOG.error("Could not load default keymap", e);
    }

    LOG.debug("{} configurations exist.", config.getNumberOfConfigurations());
    if (LOG.getLevel().isMoreSpecificThan(Level.TRACE)) {
      for (int i = 0; i < config.getNumberOfConfigurations(); i++) {
        Configuration c = config.getConfiguration(i);
        LOG.trace("Configuration #{} size {}", i, c.size());
        Iterator<String> keys = c.getKeys();
        while (keys.hasNext()) {
          String key = keys.next();
          LOG.trace("Key {}", key);
        }
      }
    }
  }

  public static URI getURI(String key) {
    URI uri = null;
    String u = getString("url." + key);
    try {
      uri = new URI(u);
    } catch (URISyntaxException e) {
      LogManager.getLogger(Config.class).error(key + "=" + u + " does not have URI syntax", e);
    }
    return uri;
  }

  public static URL getURL(String key) {
    URL url = null;
    try {
      url = getURI(key).toURL();
    } catch (MalformedURLException e) {
      LogManager.getLogger(Config.class).error(key + " does not have URL syntax", e);
    }
    return url;
  }

  public static Boolean getBoolean(String key) {
    return config.getBoolean(key, null);
  }

  /**
   * Fetch _key_ contents from defaults as String.
   *
   * <p>If contents is the empty string, return key in title case. This means that <key>Key</key>
   * can be stored simply as <key/>. Hyphens in _key_ are changed to spaces. Only the last part
   * after any dot ('.') is used.
   *
   * <p>TODO: use a proper title case function that can turn "bill of materials" into "Bill of
   * Materials" (instead of "Bill Of Materials")
   */
  public static String getString(String key) {
    String s = config.getString(key);
    if (s != null && s.isEmpty()) {
      int beginIndex = key.lastIndexOf('.') > 0 ? key.lastIndexOf('.') + 1 : 0;
      return WordUtils.capitalizeFully(key.substring(beginIndex).replace('-', ' '));
    }
    return s;
  }

  public static KeyStroke getKeyStroke(String action) {
    return keymap.stroke(action);
  }
}

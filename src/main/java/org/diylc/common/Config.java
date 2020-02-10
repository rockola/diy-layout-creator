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
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ClasspathLocationStrategy;
import org.apache.commons.configuration2.io.CombinedLocationStrategy;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystemLocationStrategy;
import org.apache.commons.text.CaseUtils;
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

  private static final List<String> configurationFiles = Arrays.asList(
      "org/diylc/defaults.xml",
      "org/diylc/fonts.xml",
      "icons-material.xml", // "org/diylc/icons.xml",
      "org/diylc/strings-EN.xml");

  private static FileBasedConfigurationBuilder<XMLConfiguration> getBuilder(
      String configurationFile) {
    final FileLocationStrategy strategy = new CombinedLocationStrategy(Arrays.asList(
        new ClasspathLocationStrategy(),
        new FileSystemLocationStrategy()));
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
    config.addConfiguration(new SystemConfiguration());

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
          LOG.trace("Key [{}]=[{}]", key, c.getString(key));
        }
      }
    }
  }

  /**
     Find URI corresponding to given key from String resources.

     @param key Resource key
     @return URI if found, or null
   */
  public static URI getUri(String key) {
    URI uri = null;
    String u = getString("url." + key);
    try {
      uri = new URI(u);
    } catch (URISyntaxException e) {
      LOG.error(key + "=" + u + " does not have URI syntax", e);
    }
    return uri;
  }

  /**
     Find URL corresponding to given key from String resources.

     @see getUri
     @param key Resource key
     @return URL if found, or null
   */
  public static URL getUrl(String key) {
    URL url = null;
    try {
      url = getUri(key).toURL();
    } catch (MalformedURLException e) {
      LOG.error(key + " does not have URL syntax", e);
    }
    return url;
  }

  public static Boolean getBoolean(String key) {
    return config.getBoolean(key, null);
  }

  /**
   * Fetch _key_ contents from defaults as String.
   *
   * <p>If contents is the empty string, return key in title
   * case. <code>&lt;Key&gt;&lt;/key&gt;</code> can be stored simply
   * as <code>&lt;key/&gt;</code>. Hyphens in _key_ are changed to
   * spaces. Only the last part after any dot ('.') is used.
   *
   * @param key Resource key
   * @return String found in defaults, or null
   */
  public static String getString(String key) {
    String s = config.getString(key);
    if (s == null) {
      LOG.debug("getString({}) key not found", key);
    } else if (s.isEmpty()) {
      // use transformed key for empty values
      int beginIndex = key.lastIndexOf('.') > 0 ? key.lastIndexOf('.') + 1 : 0;
      return WordUtils.capitalizeFully(key.substring(beginIndex).replace('-', ' '));
    }
    return s;
  }

  public static KeyStroke getKeyStroke(String action) {
    return keymap.stroke(action);
  }

  public enum Flag {
    ABNORMAL_EXIT,
    ANTI_ALIASING,
    AUTO_EDIT,
    AUTO_PADS,
    BLOCKS,
    CONTINUOUS_CREATION,
    DEBUG_COMPONENT_AREA,
    DEBUG_CONTINUITY_AREA,
    DEFAULT_TEMPLATES,
    EXPORT_GRID,
    EXTRA_SPACE,
    FAVORITES,
    HARDWARE_ACCELERATION,
    HEARTBEAT,
    HIGHLIGHT_CONTINUITY_AREA,
    HI_QUALITY_RENDER,
    METRIC,
    OUTLINE,
    RECENT_COMPONENTS,
    RECENT_FILES,
    SHOW_GRID,
    SHOW_RULERS,
    SNAP_TO_GRID,
    STICKY_POINTS,
    TEMPLATES,
    WHEEL_ZOOM;

    private static final char[] camelCaseSeparators = new char[]{'_'};

    @Override
    public String toString() {
      return CaseUtils.toCamelCase(super.toString(), false, camelCaseSeparators);
    }
  }
}

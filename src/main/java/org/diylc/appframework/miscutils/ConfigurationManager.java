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

package org.diylc.appframework.miscutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.appframework.Serializer;
import org.diylc.common.Config;

/**
 * Utility that reads and writes configuration to an XML file. Each
 * configuration item should have a unique name. To write to the
 * configuration use {@link #writeValue(String, Object)}. Use <code>
 * readXYZ</code> methods where XYZ stands for specific data types.
 *
 * @author Branislav Stojkovic
 */
public class ConfigurationManager {

  private static final Logger LOG = LogManager.getLogger(ConfigurationManager.class);

  private static ConfigurationManager instance = new ConfigurationManager();
  private static final String fileName = "config.xml";

  private Map<String, Object> configuration;
  private Map<String, List<IConfigListener>> listeners;

  private boolean fileWithErrors = false;

  public static ConfigurationManager getInstance() {
    return instance;
  }

  public ConfigurationManager() {
    this.listeners = new HashMap<String, List<IConfigListener>>();
    initializeConfiguration();
  }

  public void addConfigListener(String key, IConfigListener listener) {
    List<IConfigListener> listenerList = listeners.get(key);
    if (listenerList == null) {
      listenerList = new ArrayList<IConfigListener>();
      listeners.put(key, listenerList);
    }
    listenerList.add(listener);
  }

  public static void addListener(String key, IConfigListener listener) {
    getInstance().addConfigListener(key, listener);
  }

  public static void addListener(Config.Flag flag, IConfigListener listener) {
    getInstance().addConfigListener(flag.toString(), listener);
  }

  public static boolean isFileWithErrors() {
    return getInstance().fileWithErrors;
  }

  private void initializeConfiguration() {
    LOG.info("Initializing configuration");
    String configFileName = Utils.getUserDataDirectory() + fileName;
    File configFile = new File(configFileName);
    if (!configFile.exists()) {
      // if there's no file in the preferred folder, look for it in the app folder
      // configFile = new File(fileName); // created but not used? //ola 20200109
      LOG.info("Empty configuration");
      configuration = new HashMap<String, Object>();
    } else {
      try {
        // Reader reader = new InputStreamReader(in, "UTF-8");
        configuration = (Map<String, Object>) Serializer.fromFile(configFileName);
      } catch (Exception e) {
        LOG.error("Could not initialize configuration", e);
        // make a backup of the old config file
        fileWithErrors = true;
        configuration = new HashMap<String, Object>();
        try {
          File backupFile = new File(configFileName + "~");
          while (backupFile.exists()) {
            backupFile = new File(backupFile.getAbsolutePath() + "~");
          }
          copyFileUsingStream(configFile, backupFile);
        } catch (Exception e1) {
          LOG.error("Could not create configuration backup", e1);
        }
      }
    }
  }

  private static void copyFileUsingStream(File source, File dest) throws IOException {
    try (InputStream is = new FileInputStream(source)) {
      try (OutputStream os = new FileOutputStream(dest)) {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
          os.write(buffer, 0, length);
        }
      }
    }
  }

  private void saveConfiguration() {
    LOG.info("Saving configuration");

    String configFile = Utils.getUserDataDirectory() + fileName;
    try {
      File f = new File(Utils.getUserDataDirectory());
      if (!f.isDirectory()) {
        boolean success = f.mkdirs();
        if (!success) {
          throw new RuntimeException(String.format("Directories not created for %s", configFile));
        }
      }
      // Writer writer = new OutputStreamWriter(out, "UTF-8");
      Serializer.toFile(configFile, configuration);
    } catch (IOException e) {
      LOG.error("Could not save configuration to " + configFile, e);
    } catch (SecurityException e) {
      LOG.error("Access denied for " + configFile, e);
    }
  }

  public boolean readBoolean(String key, boolean defaultValue) {
    if (configuration.containsKey(key)) {
      return (Boolean) configuration.get(key);
    } else {
      return defaultValue;
    }
  }

  public static boolean getBoolean(String key, boolean defaultValue) {
    return getInstance().readBoolean(key, defaultValue);
  }

  public static boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  public static boolean getBoolean(Config.Flag flag, boolean defaultValue) {
    return getBoolean(flag.toString(), defaultValue);
  }

  public static boolean getBoolean(Config.Flag flag) {
    return getBoolean(flag, false);
  }

  public String readString(String key, String defaultValue) {
    String ret = (String) configuration.get(key);
    return ret == null ? defaultValue : ret;
  }

  public static String getString(String key, String defaultValue) {
    return getInstance().readString(key, defaultValue);
  }

  public static String getString(String key) {
    // default String value = null
    return getInstance().readString(key, null);
  }

  public int readInt(String key, int defaultValue) {
    Integer ret = (Integer) configuration.get(key);
    return ret == null ? defaultValue : ret.intValue();
  }

  public static int getInt(String key, int defaultValue) {
    return getInstance().readInt(key, defaultValue);
  }

  public float readFloat(String key, float defaultValue) {
    Float ret = (Float) configuration.get(key);
    return ret == null ? defaultValue : ret.floatValue();
  }

  public static float getFloat(String key, float defaultValue) {
    return getInstance().readFloat(key, defaultValue);
  }

  public double readDouble(String key, double defaultValue) {
    Double ret = (Double) configuration.get(key);
    return ret == null ? defaultValue : ret.doubleValue();
  }

  public static double getDouble(String key, double defaultValue) {
    return getInstance().readDouble(key, defaultValue);
  }

  public Object readObject(String key, Object defaultValue) {
    Object ret = configuration.get(key);
    return ret == null ? defaultValue : ret;
  }

  public static Object getObject(String key, Object defaultValue) {
    return getInstance().readObject(key, defaultValue);
  }

  public static Object getObject(String key) {
    return getObject(key, null);
  }

  public static Object getObject(Config.Flag flag, Object defaultValue) {
    return getObject(flag.toString(), defaultValue);
  }

  public static Object getObject(Config.Flag flag) {
    return getObject(flag, null);
  }

  public void writeValue(String key, Object value) {
    LOG.debug("writeValue({}, {})", key, value);
    configuration.put(key, value);
    saveConfiguration();
    if (listeners.containsKey(key)) {
      for (IConfigListener listener : listeners.get(key)) {
        listener.valueChanged(key, value);
      }
    }
  }

  public static void putValue(String key, Object value) {
    getInstance().writeValue(key, value);
  }

  public static void putValue(Config.Flag flag, Object value) {
    putValue(flag.toString(), value);
  }
}

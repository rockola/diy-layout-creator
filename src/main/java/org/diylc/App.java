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

package org.diylc;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.action.UndoManager;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.update.VersionNumber;
import org.diylc.common.Config;
import org.diylc.common.Message;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Theme;
import org.diylc.swing.gui.MainFrame;
import org.diylc.swing.gui.TemplateDialog;

/** Main class that runs the application. */
public class App {

  private static final Logger LOG = LogManager.getLogger(App.class);
  private static final String SCRIPT_RUN = "org.diylc.scriptRun";
  private static TemplateDialog templateDialog;
  private static UndoManager theUndoManager;

  static {
    // Show splash screen
    new Splash().start();

    theUndoManager = new UndoManager();
  }

  // ----------------------------------------------------------------
  public static UndoManager undoManager() {
    return theUndoManager;
  }

  // ----------------------------------------------------------------
  /**
   * Get boolean value for configuration item, or use given default if not set.
   *
   * @param key Name of configuration item.
   * @param defaultValue Value to use if not set.
   */
  public static boolean getBoolean(String key, boolean defaultValue) {
    // First look in Config, then in ConfigurationManager
    Boolean b = Config.getBoolean(key);
    return b != null ? b.booleanValue() : ConfigurationManager.getBoolean(key, defaultValue);
  }

  /**
   * Get boolean value for configuration item, defaulting to false if not set.
   *
   * @param key Name of configuration item.
   */
  public static boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  /**
   * Get boolean value for configuration item specified by Config.Flag, or use given default if not
   * set.
   *
   * @param flag Name of configuration item.
   * @param defaultValue Value to use if not set.
   */
  public static boolean getBoolean(Config.Flag flag, boolean defaultValue) {
    return ConfigurationManager.getBoolean(flag, defaultValue);
  }

  /**
   * Get boolean value for configuration item specified by Config.Flag, defaulting to false if not
   * set.
   *
   * @param flag Name of configuration item.
   */
  public static boolean getBoolean(Config.Flag flag) {
    return ConfigurationManager.getBoolean(flag, false);
  }

  public static int getInt(String key, int defaultValue) {
    return ConfigurationManager.getInt(key, defaultValue);
  }

  public static float getFloat(String key, float defaultValue) {
    return ConfigurationManager.getFloat(key, defaultValue);
  }

  public static double getDouble(String key, double defaultValue) {
    return ConfigurationManager.getDouble(key, defaultValue);
  }

  /**
   * Get string from configuration, or given default if not found.
   *
   * @param key Name of configuration parameter.
   * @param defaultValue Default value if key not found in configuration.
   */
  public static String getString(String key, String defaultValue) {
    String s = Config.getString(key);
    if (s == null) {
      s = ConfigurationManager.getString(key, defaultValue);
    }
    return s;
  }

  public static String getString(String key) {
    return getString(key, null);
  }

  public static String title() {
    return getString("app.title");
  }

  public static Object getObject(String key, Object defaultValue) {
    return ConfigurationManager.getObject(key, defaultValue);
  }

  public static Object getObject(String key) {
    return getObject(key, null);
  }

  public static Object getObject(Config.Flag flag, Object defaultValue) {
    return ConfigurationManager.getObject(flag, defaultValue);
  }

  public static Object getObject(Config.Flag flag) {
    return getObject(flag, null);
  }

  public static void putValue(String key, Object value) {
    LOG.trace("putValue(String {}, {})", key, value);
    ConfigurationManager.putValue(key, value);
  }

  public static void putValue(Config.Flag flag, Object value) {
    LOG.trace("putValue(Flag {}, {})", flag, value);
    ConfigurationManager.putValue(flag, value);
  }

  public static String getHtml(String key) {
    return Message.getHtml(key);
  }

  public static URI getUri(String key) {
    return Config.getUri(key);
  }

  public static URL getUrl(String key) {
    return Config.getUrl(key);
  }

  public static KeyStroke getKeyStroke(String key) {
    return Config.getKeyStroke(key);
  }

  // ****************************************************************

  public static Theme getTheme() {
    return (Theme) App.getObject(Theme.THEME_KEY, Theme.DEFAULT_THEME);
  }

  public static void setTheme(Theme theme) {
    App.putValue(Theme.THEME_KEY, theme);
  }

  /**
   * Get full version string.
   *
   * <p>The format for the version string is "x.y.z{-specifier}", where x = major version, y = minor
   * version, z = build number, and specifier is an optional string specifying, for example, an
   * alpha build.
   *
   * @return Application version string.
   */
  public static String getFullVersionString() {
    return getString("app.version");
  }

  /**
   * Get version string.
   *
   * <p>If full version string contains a hyphen (example: "4.0.0-testing"), the hyphen and the
   * substring following it are stripped.
   *
   * @return String representing the current version.
   */
  public static String getVersionString() {
    String v = getFullVersionString();
    int hyphen = v.indexOf("-");
    return hyphen > -1 ? v.substring(0, hyphen) : v;
  }

  public static VersionNumber getVersionNumber() {
    return new VersionNumber(getVersionString());
  }

  // ****************************************************************
  public static boolean antiAliasing() {
    return getBoolean(Config.Flag.ANTI_ALIASING, true);
  }

  public static boolean autoEdit() {
    return getBoolean(Config.Flag.AUTO_EDIT);
  }

  public static boolean autoPads() {
    return getBoolean(Config.Flag.AUTO_PADS);
  }

  public static boolean continuousCreation() {
    return getBoolean(Config.Flag.CONTINUOUS_CREATION);
  }

  public static boolean exportGrid() {
    return getBoolean(Config.Flag.EXPORT_GRID);
  }

  public static boolean extraSpace() {
    return getBoolean(Config.Flag.EXTRA_SPACE, true);
  }

  public static boolean hardwareAcceleration() {
    return getBoolean(Config.Flag.HARDWARE_ACCELERATION);
  }

  public static boolean highQualityRendering() {
    return getBoolean(Config.Flag.HI_QUALITY_RENDER);
  }

  public static boolean highlightContinuityArea() {
    return getBoolean(Config.Flag.HIGHLIGHT_CONTINUITY_AREA);
  }

  public static boolean isDebug(Config.Flag flag) {
    return flag.toString().startsWith("debug") && getBoolean(flag);
  }

  public static boolean metric() {
    return getBoolean(Config.Flag.METRIC, true);
  }

  /** @return previous value of 'metric'. */
  public static boolean setMetric(boolean isMetric) {
    boolean previous = metric();
    ConfigurationManager.putValue(Config.Flag.METRIC, isMetric);
    return previous;
  }

  /** @return true if outline mode is selected, otherwise false. */
  public static boolean outlineMode() {
    boolean outline = getBoolean(Config.Flag.OUTLINE, false);
    LOG.trace("outlineMode() is {}", outline);
    return outline;
  }

  /** @return previous value of 'outline mode'. */
  public static boolean setOutlineMode(boolean isOutline) {
    boolean previous = outlineMode();
    LOG.trace("setOutlineMode({}) was {}", isOutline, previous);
    ConfigurationManager.putValue(Config.Flag.OUTLINE, isOutline);
    return previous;
  }

  public static boolean showGrid() {
    return getBoolean(Config.Flag.SHOW_GRID, true);
  }

  public static boolean snapToGrid() {
    return getBoolean(Config.Flag.SNAP_TO_GRID, true);
  }

  public static boolean showRulers() {
    return getBoolean(Config.Flag.SHOW_RULERS, true);
  }

  public static boolean stickyPoints() {
    return getBoolean(Config.Flag.STICKY_POINTS, true);
  }

  public static boolean wheelZoom() {
    return getBoolean(Config.Flag.WHEEL_ZOOM);
  }

  public static Map<String, List<IDIYComponent<?>>> getBlocks() {
    if (getObject(Config.Flag.BLOCKS) == null) {
      setBlocks(new HashMap<String, List<IDIYComponent<?>>>());
    }
    return (Map<String, List<IDIYComponent<?>>>) getObject(Config.Flag.BLOCKS);
  }

  public static void setBlocks(Map<String, List<IDIYComponent<?>>> blocks) {
    App.putValue(Config.Flag.BLOCKS, blocks);
  }

  public static void addBlock(String name, List<IDIYComponent<?>> components) {
    getBlocks().put(name, components);
  }

  public static void removeBlock(String name) {
    getBlocks().remove(name);
  }

  // ****************************************************************

  private static MainFrame mainFrame;

  public static MainFrame ui() {
    return mainFrame;
  }

  public static void showTemplateDialog() {
    if (templateDialog == null) {
      templateDialog = new TemplateDialog(mainFrame);
    }
    templateDialog.setVisible(true);
  }

  // ****************************************************************

  public static void openUrl(URL url) {
    try {
      Utils.openUrl(url);
    } catch (Exception e) {
      LOG.error("openUrl(" + url + ") failed", e);
    }
  }

  // ****************************************************************

  /**
   * Application main method. Starts the application, optionally loading a Project if specified in
   * args.
   *
   * @param args Command line arguments
   */
  public static void main(String[] args) {

    LOG.info("{} is running", App.title());

    // Handle command line arguments
    final CommandLineArguments commandLine = new CommandLineArguments(args);
    // TODO: do stuff according to options
    //  e.g. choose language other than that specified by locale
    //  for now only interested in filenames, see below

    // Set look and feel
    // TODO: choose look and feel other than system?
    String lookAndFeel = "(class name not found)";
    try {
      // Set system look and feel
      lookAndFeel = UIManager.getSystemLookAndFeelClassName();
      LOG.debug("Setting Look and Feel to {}", lookAndFeel);
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Exception e) {
      LOG.error("Could not set Look and Feel to " + lookAndFeel, e);
      // OK to continue, app will use a default look and feel
    }

    LOG.trace(
        "Logger configuration log4j2.xml coming from {}",
        App.class.getClassLoader().getResource("log4j2.xml"));
    LOG.info(
        "OS: {} {}, Java version: {} by {}",
        System.getProperty("os.name"),
        System.getProperty("os.version"),
        System.getProperty("java.runtime.version"),
        System.getProperty("java.vm.vendor"));
    LOG.info("Starting {} with working directory {}", App.title(), System.getProperty("user.dir"));

    // TODO: make sure proper Java options are always taken into
    // account so that SCRIPT_RUN property is not needed -
    // is different handling per platform required?
    String scriptRun = System.getProperty(SCRIPT_RUN);
    if (!scriptRun.equals("true")) {
      int response =
          JOptionPane.showConfirmDialog(
              null,
              Message.getHtml("startup-warning"),
              Config.getString("app.title"),
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
      if (response != JOptionPane.YES_OPTION) {
        System.exit(0);
      }
    }

    mainFrame = new MainFrame();
    mainFrame.activate();
    if (ConfigurationManager.isFileWithErrors()) {
      mainFrame.warn(Message.getHtml("configuration-file-errors"));
    }

    if (!commandLine.filenames().isEmpty()) {
      for (String filename : commandLine.filenames()) {
        LOG.debug("Loading project {}", filename);
        mainFrame.getPresenter().loadProject(filename);
      }
    } else {
      // show template dialog at startup iff project not loaded
      // from command line and SHOW_TEMPLATES_KEY is true in
      // config, user can always bring it up from UI
      if (App.getBoolean(TemplateDialog.SHOW_TEMPLATES_KEY)) {
        templateDialog = new TemplateDialog(mainFrame);
        if (!templateDialog.getFiles().isEmpty()) {
          LOG.debug("Showing templates");
          templateDialog.setVisible(true);
        } else {
          LOG.debug("Would have shown templates but there aren't any");
        }
      } else {
        LOG.debug("Not showing templates");
      }
    }
  }
}

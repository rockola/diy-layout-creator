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

import java.awt.SplashScreen;
import java.net.URI;
import java.net.URL;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.update.VersionNumber;
import org.diylc.common.Config;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Message;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.MainFrame;
import org.diylc.swing.gui.TemplateDialog;

/**
 * Main class that runs DIYLC.
 *
 * @author Branislav Stojkovic
 * @see Presenter
 * @see MainFrame
 */
public class App {

  private static TemplateDialog templateDialog;

  private static final Logger LOG = LogManager.getLogger(App.class);
  private static final String SCRIPT_RUN = "org.diylc.scriptRun";

  public static boolean getBoolean(String key, boolean defaultValue) {
    // First look in Config, then in ConfigurationManager
    Boolean b = Config.getBoolean(key);
    return b != null ? b.booleanValue() : ConfigurationManager.getBoolean(key, defaultValue);
  }

  public static boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  public static boolean getBoolean(IPlugInPort.Key key, boolean defaultValue) {
    return ConfigurationManager.getBoolean(key, defaultValue);
  }

  public static boolean getBoolean(IPlugInPort.Key key) {
    return ConfigurationManager.getBoolean(key, false);
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
  // public static double getDouble(String key) { return getDouble(key, (double) 0.0); }

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

  public static Object getObject(IPlugInPort.Key key, Object defaultValue) {
    return ConfigurationManager.getObject(key, defaultValue);
  }

  public static Object getObject(IPlugInPort.Key key) {
    return getObject(key, null);
  }

  public static void putValue(String key, Object value) {
    ConfigurationManager.putValue(key, value);
  }

  public static void putValue(IPlugInPort.Key key, Object value) {
    ConfigurationManager.putValue(key, value);
  }

  public static String getHTML(String key) {
    return Message.getHTML(key);
  }

  public static URI getURI(String key) {
    return Config.getURI(key);
  }

  public static URL getURL(String key) {
    return Config.getURL(key);
  }

  public static KeyStroke getKeyStroke(String key) {
    return Config.getKeyStroke(key);
  }

  // ****************************************************************
  public static String getFullVersionString() {
    return getString("app.version");
  }

  public static String getVersionString() {
    String v = getFullVersionString();
    int hyphen = v.indexOf("-");
    return (hyphen > -1 ? v.substring(0, hyphen) : v);
  }

  public static VersionNumber getVersionNumber() {
    return new VersionNumber(getVersionString());
  }

  // ****************************************************************
  public static boolean snapToGrid() {
    return getBoolean(IPlugInPort.Key.SNAP_TO_GRID, true);
  }

  public static boolean autoEdit() {
    return getBoolean(IPlugInPort.Key.AUTO_EDIT);
  }

  public static boolean continuousCreation() {
    return getBoolean(IPlugInPort.Key.CONTINUOUS_CREATION);
  }

  public static boolean stickyPoints() {
    return getBoolean(IPlugInPort.Key.STICKY_POINTS, true);
  }

  public static boolean highQualityRendering() {
    return getBoolean(IPlugInPort.Key.HI_QUALITY_RENDER);
  }

  public static boolean highlightContinuityArea() {
    return getBoolean(IPlugInPort.Key.HIGHLIGHT_CONTINUITY_AREA);
  }

  public static boolean hardwareAcceleration() {
    return getBoolean(IPlugInPort.Key.HARDWARE_ACCELERATION);
  }

  public static boolean antiAliasing() {
    return getBoolean(IPlugInPort.Key.ANTI_ALIASING, true);
  }

  public static boolean outlineMode() {
    return getBoolean(IPlugInPort.Key.OUTLINE, false);
  }

  public static boolean showGrid() {
    return getBoolean(IPlugInPort.Key.SHOW_GRID, true);
  }

  public static boolean exportGrid() {
    return getBoolean(IPlugInPort.Key.EXPORT_GRID);
  }

  public static boolean extraSpace() {
    return getBoolean(IPlugInPort.Key.EXTRA_SPACE, true);
  }

  public static boolean showRulers() {
    return getBoolean(IPlugInPort.Key.SHOW_RULERS, true);
  }

  public static boolean metric() {
    return getBoolean(Presenter.Key.METRIC, true);
  }

  public static boolean wheelZoom() {
    return getBoolean(IPlugInPort.Key.WHEEL_ZOOM);
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

  public static void openURL(URL url) {
    try {
      Utils.openURL(url);
    } catch (Exception e) {
      LOG.error("openURL(" + url + ") failed", e);
    }
  }

  // ****************************************************************

  /**
     Application main method.
     Starts the application, optionally loading a Project if specified in args.

     @param args Command line arguments
  */
  public static void main(String[] args) {

    LOG.info("{} is running", App.title());

    String lookAndFeel = "(class name not found)";
    try {
      lookAndFeel = UIManager.getSystemLookAndFeelClassName();
      LOG.debug("Setting Look and Feel to {}", lookAndFeel);
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Exception e) {
      LOG.error("Could not set Look and Feel to " + lookAndFeel, e);
    }

    ClassLoader loader = App.class.getClassLoader();
    LOG.trace("App.class coming from {}", loader.getResource("org/diylc/App.class"));
    LOG.trace("log4j2.xml coming from {}", loader.getResource("log4j2.xml"));
    LOG.trace("java.class.path is {}", System.getProperty("java.class.path"));

    // Initialize splash screen
    final SplashScreen splash = SplashScreen.getSplashScreen();
    new Splash(splash).start();

    Package p = App.class.getPackage();
    LOG.trace("App.class package: {}", p.getName());
    LOG.debug(
        "Implementation: version [{}] title [{}] vendor [{}]",
        p.getImplementationVersion(),
        p.getImplementationTitle(),
        p.getImplementationVendor());
    LOG.debug(
        "Specification: version [{}] title [{}] vendor [{}]",
        p.getSpecificationVersion(),
        p.getSpecificationTitle(),
        p.getSpecificationVendor());
    //
    LOG.info(
        "OS: {} {}, Java version: {} by {}",
        System.getProperty("os.name"),
        System.getProperty("os.version"),
        System.getProperty("java.runtime.version"),
        System.getProperty("java.vm.vendor"));
    LOG.info("Starting {} with working directory {}", App.title(), System.getProperty("user.dir"));

    String val = System.getProperty(SCRIPT_RUN);
    if (!"true".equals(val)) {
      int response =
          JOptionPane.showConfirmDialog(
              null,
              Message.getHTML("startup-warning"),
              Config.getString("app.title"),
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
      if (response != JOptionPane.YES_OPTION) {
        System.exit(0);
      }
    }

    mainFrame = new MainFrame();
    mainFrame.installPlugins();
    mainFrame.setLocationRelativeTo(null);
    mainFrame.setVisible(true);
    if (args.length > 0) {
      mainFrame.getPresenter().loadProjectFromFile(args[0]);
    } else {
      // show template dialog at startup iff project not loaded
      // from command line and SHOW_TEMPLATES_KEY is true in
      // config, user can always bring it up from UI
      boolean showTemplates = App.getBoolean(TemplateDialog.SHOW_TEMPLATES_KEY);
      if (showTemplates) {
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

    if (ConfigurationManager.isFileWithErrors()) {
      mainFrame.warn(Message.getHTML("configuration-file-errors"));
    }
  }
}

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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.PropertyInjector;
import org.diylc.common.Config;
import org.diylc.common.Message;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.MainFrame;
import org.diylc.swing.gui.TemplateDialog;
import org.diylc.swingframework.FontChooserComboBox;

/**
 * Main class that runs DIYLC.
 * 
 * @author Branislav Stojkovic
 * 
 * @see Presenter
 * @see MainFrame
 */
public class DIYLC {

    private static final Logger LOG = LogManager.getLogger(DIYLC.class);

    private static final String SCRIPT_RUN = "org.diylc.scriptRun";

    public static boolean getBoolean(String key, boolean defaultValue) {
	return ConfigurationManager.getBoolean(key, defaultValue);
    }
    public static boolean getBoolean(String key) { return getBoolean(key, false); }

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
	// TODO: handle defaultValue for strings in Config?
	String s = Config.getString(key);
	if (s == null) {
	    s = ConfigurationManager.getString(key, defaultValue);
	}
	return s;
    }
    public static String getString(String key) { return getString(key, null); }

    public static Object getObject(String key, Object defaultValue) {
	return ConfigurationManager.getObject(key, defaultValue);
    }
    public static Object getObject(String key) { return getObject(key, null); }

    public static void putValue(String key, Object value) {
	ConfigurationManager.putValue(key, value);
    }

    public static URI getURI(String key) { return Config.getURI(key); }
    public static URL getURL(String key) { return Config.getURL(key); }
    public static KeyStroke getKeyStroke(String key) { return Config.getKeyStroke(key); }

    /**
     * @param args
     */
    public static void main(String[] args) {

	LOG.info("DIYLC is running");

	try {
	    LOG.debug("Setting Look and Feel to {}",
		      UIManager.getSystemLookAndFeelClassName());
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) {
	    LOG.error("Could not set Look and Feel", e);
	}

	ClassLoader loader = DIYLC.class.getClassLoader();
	LOG.debug("DIYLC.class coming from {}",
		  loader.getResource("org/diylc/DIYLC.class"));
	LOG.debug("log4j2.xml coming from {}",
		  loader.getResource("log4j2.xml"));
	LOG.debug("java.class.path is {}", System.getProperty("java.class.path"));

	// Initialize splash screen
	final SplashScreen splash = SplashScreen.getSplashScreen();
	new Splash(splash).start();

	ConfigurationManager.initialize("diylc");

	Package p = DIYLC.class.getPackage();
	LOG.debug("DIYLC package: {}", p.getName());
	LOG.debug("Implementation: version [{}] title [{}] vendor [{}]",
		  p.getImplementationVersion(),
		  p.getImplementationTitle(),
		  p.getImplementationVendor());
	LOG.debug("Specification: version [{}] title [{}] vendor [{}]",	
		  p.getSpecificationVersion(),
		  p.getSpecificationTitle(),
		  p.getSpecificationVendor());
	//
	LOG.info("OS: {} {}, Java version: {} by {}",
		  System.getProperty("os.name"),
		  System.getProperty("os.version"),
		  System.getProperty("java.runtime.version"),
		  System.getProperty("java.vm.vendor"));
	LOG.info("Starting DIYLC with working directory {}",
		 System.getProperty("user.dir"));

	String val = System.getProperty(SCRIPT_RUN);
	if (!"true".equals(val)) {
	    int response =
		JOptionPane.showConfirmDialog(null,
					      Message.getHTML("startup-warning"),
					      "DIYLC",
					      JOptionPane.YES_NO_OPTION,
					      JOptionPane.WARNING_MESSAGE);
	    if (response != JOptionPane.YES_OPTION) {
		System.exit(0);
	    }
	}

	MainFrame mainFrame = new MainFrame();
	mainFrame.setLocationRelativeTo(null);
	mainFrame.setVisible(true);
	if (args.length > 0) {
	    mainFrame.getPresenter().loadProjectFromFile(args[0]);
	} else {
	    boolean showTemplates = DIYLC.getBoolean(TemplateDialog.SHOW_TEMPLATES_KEY);
	    if (showTemplates) {
		TemplateDialog templateDialog = new TemplateDialog(mainFrame,
								   mainFrame.getPresenter());
		if (!templateDialog.getFiles().isEmpty()) {
		    templateDialog.setVisible(true);
		}
	    }
	}

	if (ConfigurationManager.isFileWithErrors())
	    mainFrame.warn(Message.getHTML("configuration-file-errors"));
    }
}

/*
  DIY Layout Creator (DIYLC). 
  Copyright (c) 2009-2019 held jointly by the individual authors.
  
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.PropertyInjector;
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
public class DIYLCStarter {

    private static final Logger LOG = LogManager.getLogger(DIYLCStarter.class);

    private static final String SCRIPT_RUN = "org.diylc.scriptRun";

    /**
     * @param args
     */
    public static void main(String[] args) {

	LOG.info("DIYLC is running");

	try {
	    LOG.debug("Setting Look and Feel to {}", UIManager.getSystemLookAndFeelClassName());
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) {
	    LOG.error("Could not set Look and Feel", e);
	}

	ClassLoader loader = DIYLCStarter.class.getClassLoader();
	LOG.debug("DIYLCStarter.class coming from {}",
		  loader.getResource("org/diylc/DIYLCStarter.class"));
	LOG.debug("log4j2.xml coming from {}",
		  loader.getResource("log4j2.xml"));
	LOG.debug("java.class.path is {}", System.getProperty("java.class.path"));

	// Initialize splash screen
	final SplashScreen splash = SplashScreen.getSplashScreen();
	new Splash(splash).start();

	ConfigurationManager.initialize("diylc");

	Package p = DIYLCStarter.class.getPackage();
	LOG.debug("DIYLCStarter package: {}", p.getName());
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

	/*
	  // TODO:
	  //Should be done using getResource()
	  //and also should be done with free fonts

	Thread fontThread = new Thread(new Runnable() {

		@Override
		public void run() {        
		    LOG.debug("Starting font pre-loading");
        
		    File dir = new File("fonts");
		    File[] fonts = dir.listFiles();

		    for (int i = 0; i < fonts.length; i++) {
			try {
			    LOG.info("Dynamically loading font: "
				     + fonts[i].getName());
			    Font customFont =
				Font.createFont(Font.TRUETYPE_FONT,
						new File(fonts[i].getAbsolutePath())).deriveFont(12f);
			    GraphicsEnvironment ge =
				GraphicsEnvironment.getLocalGraphicsEnvironment();
			    // register the font
			    ge.registerFont(customFont);
			} catch (IOException e) {
			    LOG.error("Could not load font", e);
			} catch (FontFormatException e) {
			    LOG.error("Font format error", e);
			}
		    }
        
		    FontChooserComboBox box = new FontChooserComboBox();
		    box.getPreferredSize();
		    JPanel p = new JPanel();
		    box.paint(p.getGraphics());
		    LOG.debug("Finished font pre-loading");
		}
	    });
	fontThread.start();
	*/

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
	    boolean showTemplates = ConfigurationManager.getBoolean(TemplateDialog.SHOW_TEMPLATES_KEY);
	    if (showTemplates) {
		TemplateDialog templateDialog = new TemplateDialog(mainFrame, mainFrame.getPresenter());
		if (!templateDialog.getFiles().isEmpty()) {
		    templateDialog.setVisible(true);
		}
	    }
	}

	/*
	  // probably not needed //ola 20191230
	properties = new Properties();
	try {
	    LOG.info("Injecting default properties.");
	    File f = new File("config.properties");
	    if (f.exists()) {
		properties.load(new FileInputStream(f));
		PropertyInjector.injectProperties(properties);
	    }
	} catch (Exception e) {
	    LOG.error("Could not read config.properties file", e);
	}
	*/
    
	if (ConfigurationManager.isFileWithErrors())
	    mainFrame.warn(Message.getHTML("configuration-file-errors"));
    }
}

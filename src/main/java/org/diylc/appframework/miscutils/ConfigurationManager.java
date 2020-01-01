package org.diylc.appframework.miscutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.appframework.Serializer;

/**
   Utility that reads and writes configuration to an XML file. Each
   configuration item should have a unique name. To write to the
   configuration use {@link #writeValue(String, Object)}. Use
   <code>readXYZ</code> methods where XYZ stands for specific data
   types.
  
   @author Branislav Stojkovic
*/
public class ConfigurationManager {

    private static final Logger LOG = LogManager.getLogger(ConfigurationManager.class);

    private static ConfigurationManager instance;
    private static String path = Utils.getUserDataDirectory("generic");
    private static final String fileName = "config.xml";

    private Map<String, Object> configuration;
    private Map<String, List<IConfigListener>> listeners;
  
    private boolean fileWithErrors = false;

    public static void initialize(String appName) {
	path = Utils.getUserDataDirectory(appName);
    }

    public static ConfigurationManager getInstance() {
	if (instance == null) {
	    instance = new ConfigurationManager();
	}
	return instance;
    }

    public ConfigurationManager() {
	this.listeners = new HashMap<String, List<IConfigListener>>();
	initializeConfiguration();
    }

    public void addConfigListener(String key, IConfigListener listener) {
	List<IConfigListener> listenerList;
	if (listeners.containsKey(key)) {
	    listenerList = listeners.get(key);
	} else {
	    listenerList = new ArrayList<IConfigListener>();
	    listeners.put(key, listenerList);
	}
	listenerList.add(listener);
    }
  
    public boolean isFileWithErrors() {
	return fileWithErrors;
    }

    @SuppressWarnings("unchecked")
    private void initializeConfiguration() {
	LOG.info("Initializing configuration");

	String configFileName = path + fileName;
	File configFile = new File(configFileName);
	// if there's no file in the preferred folder, look for it in the app folder
	if (!configFile.exists()) {
	    configFile = new File(fileName);
	    configuration = new HashMap<String, Object>();
	} else {
	    try {
		// Reader reader = new InputStreamReader(in, "UTF-8");
		configuration =
		    (Map<String, Object>) Serializer.fromFile(configFileName);
	    } catch (Exception e) {
		LOG.error("Could not initialize configuration", e);
		// make a backup of the old config file
		fileWithErrors = true;
		configuration = new HashMap<String, Object>();
		try {
		    File backupFile = new File(path + fileName + "~");
		    while (backupFile.exists())
			backupFile = new File(backupFile.getAbsolutePath() + "~");
		    copyFileUsingStream(configFile, backupFile);
		} catch (Exception e1) {
		    LOG.error("Could not create configuration backup", e1);
		}        
	    }
	}
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
	InputStream is = null;
	OutputStream os = null;
	try {
	    is = new FileInputStream(source);
	    os = new FileOutputStream(dest);
	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = is.read(buffer)) > 0) {
		os.write(buffer, 0, length);
	    }
	} finally {
	    is.close();
	    os.close();
	}
    }

    private void saveConfiguration() {
	LOG.info("Saving configuration");

	String configFile = path + fileName;
	try {
	    new File(path).mkdirs();
	    // Writer writer = new OutputStreamWriter(out, "UTF-8");
	    Serializer.toFile(configFile, configuration);
	} catch (Exception e) {
	    LOG.error("Could not save configuration: " + e.getMessage());
	}
    }

    public boolean readBoolean(String key, boolean defaultValue) {
	if (configuration.containsKey(key)) {
	    return (Boolean) configuration.get(key);
	} else {
	    return defaultValue;
	}
    }

    public String readString(String key, String defaultValue) {
	if (configuration.containsKey(key)) {
	    return (String) configuration.get(key);
	} else {
	    return defaultValue;
	}
    }

    public int readInt(String key, int defaultValue) {
	if (configuration.containsKey(key)) {
	    return (Integer) configuration.get(key);
	} else {
	    return defaultValue;
	}
    }

    public float readFloat(String key, float defaultValue) {
	if (configuration.containsKey(key)) {
	    return (Float) configuration.get(key);
	} else {
	    return defaultValue;
	}
    }

    public double readDouble(String key, double defaultValue) {
	if (configuration.containsKey(key)) {
	    return (Double) configuration.get(key);
	} else {
	    return defaultValue;
	}
    }

    public Object readObject(String key, Object defaultValue) {
	if (configuration.containsKey(key)) {
	    return configuration.get(key);
	} else {
	    return defaultValue;
	}
    }

    public void writeValue(String key, Object value) {
	configuration.put(key, value);
	saveConfiguration();
	if (listeners.containsKey(key)) {
	    for (IConfigListener listener : listeners.get(key)) {
		listener.valueChanged(key, value);
	    }
	}
    }
}

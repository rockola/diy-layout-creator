/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2019 held jointly by the individual authors.

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

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ClasspathLocationStrategy;
import org.apache.commons.configuration2.io.CombinedLocationStrategy;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystemLocationStrategy;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class Config {

    private Config() { }
    
    private static final Configurations configs = new Configurations();
    
    private static XMLConfiguration config = null;
    private static FileBasedConfigurationBuilder<XMLConfiguration> builder = null;

    private static final String defaults = "org/diylc/defaults.xml";

    private static FileBasedConfigurationBuilder<XMLConfiguration> getBuilder() {
	if (builder == null) {
	    final FileLocationStrategy strategy =
		new CombinedLocationStrategy(Arrays.asList(new ClasspathLocationStrategy(),
							   new FileSystemLocationStrategy()));
	    builder = new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
		.configure(new Parameters()
			   .xml()
			   .setLocationStrategy(strategy)
			   .setFileName(defaults));
	}

	return builder;
    }

    private static void initConfig() {
	if (config == null) {
	    try {
		config = getBuilder().getConfiguration();
	    } catch (ConfigurationException e) {
		LogManager.getLogger(Config.class).error("Could not read " + defaults, e);
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

    public static String getString(String key) {
	initConfig();
	return config.getString(key);
    }
}
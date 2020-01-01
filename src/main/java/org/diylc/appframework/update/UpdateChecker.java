package org.diylc.appframework.update;

import java.io.BufferedInputStream;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.common.Config;
import org.diylc.appframework.Serializer;

public class UpdateChecker {

    private static final Logger LOG = LogManager.getLogger(UpdateChecker.class);

    private static final String VERSION_HTML = "<p><b>v%d.%d.%d (released on %s)</b><br>\n%s</p>\n";
    private static final String CHANGE_HTML = "&nbsp;&nbsp;&nbsp;<b>&rsaquo;</b>&nbsp;[%s] %s<br>\n";
    private static final Format dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private VersionNumber currentVersion;
    private String updateFileURL;

    public UpdateChecker(VersionNumber currentVersion, String updateFileURL) {
	super();
	this.currentVersion = currentVersion;
	this.updateFileURL = updateFileURL;
    }

    @SuppressWarnings("unchecked")
    public List<Version> findNewVersions() throws Exception {
	LOG.info("Trying to download file {}", updateFileURL);
	List<Version> allVersions = (List<Version>) Serializer.fromURL(updateFileURL);
	List<Version> filteredVersions = new ArrayList<Version>();
	for (Version version : allVersions) {
	    if (currentVersion.compareTo(version.getVersionNumber()) < 0) {
		filteredVersions.add(version);
	    }
	}
	Collections.sort(filteredVersions);
	LOG.info("{} updates found", filteredVersions.size());
	return filteredVersions;
    }

    public String findNewVersionShort() throws Exception {
	List<Version> versions = findNewVersions();
	if (versions != null && !versions.isEmpty()) {
	    Version v = versions.get(0);
	    return v.getName()
		+ "v" + v.getVersionNumber()
		+ " released on " + dateFormat.format(v.getReleaseDate());
	}
	return null;
    }

    public static String createUpdateHTML(List<Version> versions) {
	if (versions == null) {
	    return "Could not obtain update information.";
	}

	String bodyHtml = "";
	for (Version version : versions) {
	    String changeStr = "";
	    for (Change change : version.getChanges()) {
		changeStr +=
		    String.format(CHANGE_HTML,
				  convertChangeTypeToHTML(change.getChangeType()),
				  change.getDescription());
	    }
	    bodyHtml +=
		String.format(VERSION_HTML,
			      version.getVersionNumber().getMajor(),
			      version.getVersionNumber().getMinor(),
			      version.getVersionNumber().getBuild(),
			      dateFormat.format(version.getReleaseDate()),
			      changeStr);
	}
	return String.format("<html><font face=\"%s\" size=\"2\">\n%s\n</font></html>",
			     Config.getString("font.sans-serif"),
			     bodyHtml);
    }

    private static String convertChangeTypeToHTML(ChangeType changeType) {
	String color;
	switch (changeType) {
	case BUG_FIX:
	    color = "red";
	    break;
	case NEW_FEATURE:
	    color = "blue";
	    break;
	case IMPROVEMENT:
	    color = "green";
	    break;
	default:
	    color = "black";
	}
	return "<font color=\"" + color + "\">" + changeType + "</font>";
    }
}

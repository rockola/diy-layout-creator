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

package org.diylc.appframework.update;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.appframework.Serializer;
import org.diylc.common.Config;

public class UpdateChecker {

  private static final Logger LOG = LogManager.getLogger(UpdateChecker.class);

  private static final Format dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private VersionNumber currentVersion;
  private String updateFileURL;

  public UpdateChecker(VersionNumber currentVersion, String updateFileURL) {
    super();
    this.currentVersion = currentVersion;
    this.updateFileURL = updateFileURL;
  }

  private static String getMsg(String key) {
    return App.getString("message.update." + key);
  }

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
      return String.format(getMsg("version-short-format"),
                           v.getName(),
                           v.getVersionNumber(),
                           dateFormat.format(v.getReleaseDate()));
    }
    return null;
  }

  public static String createUpdateHTML(List<Version> versions) {
    if (versions == null) {
      return getMsg("versions-not-found");
    }

    StringBuffer bodyHtml = new StringBuffer();
    for (Version version : versions) {
      LOG.debug("Version = {}", version);
      StringBuffer changeStr = new StringBuffer();
      for (Change change : version.getChanges()) {
        changeStr.append(
            String.format(
                "&nbsp;&nbsp;&nbsp;<b>&rsaquo;</b>&nbsp;[<span class=\"%\">%s</span>] %s<br>%n",
                change.getChangeType(),
                change.getChangeType(),
                change.getDescription()));
      }

      bodyHtml.append(
          String.format(
              "<p><b>v%d.%d.%d (%s %s)</b><br>%n%s</p>%n",
              version.getVersionNumber().getMajor(),
              version.getVersionNumber().getMinor(),
              version.getVersionNumber().getBuild(),
              getMsg("released-on"),
              dateFormat.format(version.getReleaseDate()),
              changeStr));
    }

    return String.format(
        "<html><font face=\"%s\" size=\"2\">%n%s%n</font></html>",
        Config.getString("font.sans-serif"),
        bodyHtml.toString());
  }
}

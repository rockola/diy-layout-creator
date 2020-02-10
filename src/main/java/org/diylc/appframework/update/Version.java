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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.diylc.parsing.XmlNode;
import org.w3c.dom.Element;

public class Version implements Serializable, Comparable<Version> {

  private static final long serialVersionUID = 1L;
  private static Logger LOG = LogManager.getLogger(Version.class);
  private static Parser parser = Parser.builder().build();
  private static String UPDATE_MD = "org/diylc/update.md";

  private VersionNumber versionNumber;
  private Date releaseDate;
  private String name;
  private List<Change> changes;
  private String url;

  public Version(VersionNumber versionNumber, Date releaseDate, String name, String url) {
    super();
    this.versionNumber = versionNumber;
    this.releaseDate = new Date(releaseDate.getTime());
    this.name = name;
    this.url = url;
    this.changes = new ArrayList<Change>();
  }

  public Version(VersionNumber versionNumber) {
    this(versionNumber, null, null, null);
  }

  public VersionNumber getVersionNumber() {
    return versionNumber;
  }

  public void setVersionNumber(VersionNumber versionNumber) {
    this.versionNumber = versionNumber;
  }

  public Date getReleaseDate() {
    return new Date(releaseDate.getTime());
  }

  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = new Date(releaseDate.getTime());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Change> getChanges() {
    return changes;
  }

  public void setChanges(List<Change> changes) {
    this.changes = changes;
  }

  public Change addChange(Change change) {
    changes.add(change);
    return change;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((changes == null) ? 0 : changes.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((releaseDate == null) ? 0 : releaseDate.hashCode());
    result = prime * result + ((versionNumber == null) ? 0 : versionNumber.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Version other = (Version) obj;
    if (changes == null) {
      if (other.changes != null) {
        return false;
      }
    } else if (!changes.equals(other.changes)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (releaseDate == null) {
      if (other.releaseDate != null) {
        return false;
      }
    } else if (!releaseDate.equals(other.releaseDate)) {
      return false;
    }
    if (versionNumber == null) {
      if (other.versionNumber != null) {
        return false;
      }
    } else if (!versionNumber.equals(other.versionNumber)) {
      return false;
    }
    return true;
  }

  private String releaseNameToString(boolean withSpace) {
    if (name == null || name.isEmpty()) {
      return "";
    }
    return (name + (withSpace ? " " : ""));
  }

  private String releaseDateToString(boolean withText) {
    if (releaseDate == null) {
      return "";
    }
    return ((withText ? "released on " : "")
            + new SimpleDateFormat("yyyy-MM-dd").format(releaseDate));
  }

  private String versionNumberToString(boolean withSpace) {
    return versionNumber.toString() + (withSpace ? " " : "");
  }

  @Override
  public String toString() {
    return String.format(
        "<Version %s%s%s />",
        releaseNameToString(true), versionNumberToString(true), releaseDateToString(true));
  }

  @Override
  public int compareTo(Version o) {
    return o.getVersionNumber().compareTo(this.getVersionNumber());
  }

  public static List<Version> getRecentUpdates() {
    ClassLoader loader = Version.class.getClassLoader();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(
          loader.getResourceAsStream(UPDATE_MD), StandardCharsets.UTF_8));
      Node document = parser.parse(reader.lines().collect(Collectors.joining("\n")));
      UpdateVisitor v = new UpdateVisitor();
      document.accept(v);
      return v.getVersions();
    } catch (Exception e) {
      LOG.error("getRecentUpdates() failed", e);
      throw e;
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        LOG.error("getRecentUpdates() couldn't close reader", e);
      }
    }
  }
}

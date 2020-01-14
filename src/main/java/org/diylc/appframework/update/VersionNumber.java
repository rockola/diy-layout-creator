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

import java.io.Serializable;

public class VersionNumber implements Serializable, Comparable<VersionNumber> {

  private static final long serialVersionUID = 1L;

  private int major;
  private int minor;
  private int build;

  private final void initComponents(int major, int minor, int build) {
    this.major = major;
    this.minor = minor;
    this.build = build;
  }

  private final void initComponents(String versionString) {
    String[] versionComponents = versionString.split("\\.", 0);
    initComponents(
        Integer.parseInt(versionComponents[0]),
        Integer.parseInt(versionComponents[1]),
        Integer.parseInt(versionComponents[2]));
  }

  public VersionNumber(int major, int minor, int build) {
    super();
    initComponents(major, minor, build);
  }

  public VersionNumber(String versionString) {
    super();
    initComponents(versionString);
  }

  public int getMajor() {
    return major;
  }

  public void setMajor(int major) {
    this.major = major;
  }

  public int getMinor() {
    return minor;
  }

  public void setMinor(int minor) {
    this.minor = minor;
  }

  public int getBuild() {
    return build;
  }

  public void setBuild(int build) {
    this.build = build;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + build;
    result = prime * result + major;
    result = prime * result + minor;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    VersionNumber other = (VersionNumber) obj;
    if (build != other.build) return false;
    if (major != other.major) return false;
    if (minor != other.minor) return false;
    return true;
  }

  @Override
  public int compareTo(VersionNumber o) {
    int result = Integer.compare(major, o.major);
    if (result == 0) {
      result = Integer.compare(minor, o.minor);
      if (result == 0) {
        result = Integer.compare(build, o.build);
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return String.format("%d.%d.%d", major, minor, build);
  }
}

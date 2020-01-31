/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.WordUtils;

import org.junit.jupiter.api.Test;

import org.diylc.appframework.update.VersionNumber;

public class ProjectTest {
  @Test
  public void unmarshal() {
    Project project = Project.unmarshal("src/test/data/ruby.diy");
    assertNotNull(project);
    assertNotNull(project.getFileVersion());
    assertEquals(project.getFileVersion().getMajor(), 4);
    assertEquals(project.getFileVersion().getMinor(), 0);
    assertEquals(project.getFileVersion().getBuild(), 0);
  }
}

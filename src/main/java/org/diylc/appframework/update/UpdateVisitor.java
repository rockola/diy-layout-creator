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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.Text;

public class UpdateVisitor extends AbstractVisitor {
  private Logger LOG = LogManager.getLogger(UpdateVisitor.class);

  private List<Version> versions = new ArrayList<>();
  private List<DateTimeFormatter> formatters =
      new ArrayList<>(Arrays.asList(DateTimeFormatter.ISO_DATE_TIME, DateTimeFormatter.ISO_DATE));

  // temp variables
  private Version version;
  private Change change;
  private int lastLevel = -1;

  public List<Version> getVersions() {
    return versions;
  }

  private final int VERSION_LEVEL = 2;
  private final int DATE_LEVEL = 3;
  private final int CHANGE_LEVEL = 4;

  @Override
  public void visit(Heading node) {
    switch (node.getLevel()) {
      case VERSION_LEVEL:
      case DATE_LEVEL:
      case CHANGE_LEVEL: // feature/bug fix/improvement
        // do nothing till we have the contents
        break;
      default:
        // <H1> will get us here, as will <H5> etc.
        LOG.error("Unknown level {}?", node.getLevel());
        return;
    }

    lastLevel = node.getLevel();

    visitChildren(node);
  }

  @Override
  public void visit(Text node) {
    VersionNumber versionNumber;
    Date releaseDate;
    String nodeContent = node.getLiteral();

    switch (lastLevel) {
      case VERSION_LEVEL:
        LOG.debug("Reading {} as version number", nodeContent);
        versionNumber = new VersionNumber(node.getLiteral());
        LOG.debug("Version number is {}", versionNumber);
        version = new Version(versionNumber);
        versions.add(version);
        break;
      case DATE_LEVEL:
        boolean formatSuccess = false;
        for (DateTimeFormatter f : formatters) {
          try {
            LOG.debug("Reading {} as release date", nodeContent);

            // LocalDateTime to java.util.Date via Instant:
            // https://stackoverflow.com/a/19726814/355028
            LocalDateTime releaseDateAsLDT = LocalDateTime.parse(nodeContent, f);
            releaseDate = Date.from(releaseDateAsLDT.toInstant(ZoneOffset.UTC));
            LOG.debug("Release date is {}", releaseDate);
            version.setReleaseDate(releaseDate);
            formatSuccess = true;
            break; // from for-loop
          } catch (DateTimeParseException e) {
            // f could not parse node contents, let's hope another formatter can
            LOG.debug("{} could not parse {}", f, nodeContent);
          }
        }
        if (!formatSuccess) {
          // none of the formatters worked
          LOG.warn("None of the formatters could parse {}", nodeContent);
        }
        break;
      case CHANGE_LEVEL:
        LOG.debug("Reading {} as change type", nodeContent);
        ChangeType changeType = ChangeType.parse(nodeContent);
        if (changeType != null) {
          change = version.addChange(new Change(changeType));
        } else {
          change.setDescription(nodeContent);
        }
        break;
      default:
        LOG.error(
            "Text {} in something other than a Change? lastLevel == {}",
            node.getLiteral(),
            lastLevel);
    }
  }
}

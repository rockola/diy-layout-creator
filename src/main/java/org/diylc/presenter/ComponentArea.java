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

package org.diylc.presenter;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;
import org.diylc.components.Area;

public class ComponentArea {

  private Area outlineArea;
  private List<Area> continuityPositiveAreas;
  private List<Area> continuityNegativeAreas;

  public ComponentArea(
      Area outlineArea, List<Area> continuityPositiveAreas, List<Area> continuityNegativeAreas) {
    super();
    this.outlineArea = outlineArea;
    this.continuityPositiveAreas = continuityPositiveAreas;
    this.continuityNegativeAreas = continuityNegativeAreas;
  }

  // TODO: use a standard XML tag library function
  private String tag(String tagName, String contents) {
    return contents == null
        ? String.format("<%s/>", tagName)
        : String.format("<%s>%s</%s>", tagName, contents, tagName);
  }

  public String toString() {
    if (outlineArea == null) {
      return tag("ComponentArea", null);
    }
    if (outlineArea.isEmpty()) {
      return tag("ComponentArea", tag("outlineArea", null));
    }
    Rectangle bounds = outlineArea.getBounds();
    return tag(
        "componentArea",
        tag(
            "outlineArea",
            String.format(
                "<bounds h=\"%d\" w=\"%d\" x=\"%d\" y=\"%d\"/>",
                bounds.height, bounds.width, bounds.x, bounds.y)));
  }

  public Area getOutlineArea() {
    return outlineArea;
  }

  public boolean inOutlineArea(Point2D point) {
    return outlineArea != null && point != null && outlineArea.contains(point);
  }

  public boolean intersectsOutlineArea(Rectangle rect) {
    return outlineArea != null && rect != null && outlineArea.intersects(rect);
  }

  public List<Area> getContinuityPositiveAreas() {
    return continuityPositiveAreas;
  }

  public boolean hasContinuityPositiveAreas() {
    return continuityPositiveAreas != null && !continuityPositiveAreas.isEmpty();
  }

  public List<Area> getContinuityNegativeAreas() {
    return continuityNegativeAreas;
  }

  public boolean hasContinuityNegativeAreas() {
    return continuityNegativeAreas != null && !continuityNegativeAreas.isEmpty();
  }
}

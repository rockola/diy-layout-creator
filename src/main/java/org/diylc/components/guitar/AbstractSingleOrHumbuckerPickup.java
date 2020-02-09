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

package org.diylc.components.guitar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.OrientationHV;
import org.diylc.common.VerticalAlignment;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;

public abstract class AbstractSingleOrHumbuckerPickup extends AbstractGuitarPickup {

  private static final long serialVersionUID = 1L;

  @Override
  protected void drawTerminalLabels(Graphics2D g2d, Color color, Project project) {
    Point[] points = getControlPoints();
    g2d.setColor(color);
    g2d.setFont(project.getFont().deriveFont(TERMINAL_FONT_SIZE * 1f));

    int dx = 0;
    int dy = 0;
    final boolean horizontal = getControlPointDirection().isHorizontal();
    switch (orientation) {
      case _90:
        dx = horizontal ? TERMINAL_FONT_SIZE : 0;
        dy = horizontal ? 0 : (int) (TERMINAL_FONT_SIZE * 0.8);
        break;
      case _180:
        dx = horizontal ? 0 : -(int) (TERMINAL_FONT_SIZE * 0.8);
        dy = horizontal ? TERMINAL_FONT_SIZE : 0;
        break;
      case _270:
        dx = horizontal ? -TERMINAL_FONT_SIZE : 0;
        dy = horizontal ? 0 : -(int) (TERMINAL_FONT_SIZE * 0.8);
        break;
      case DEFAULT:
      default:
        dx = horizontal ? 0 : (int) (TERMINAL_FONT_SIZE * 0.8);
        dy = horizontal ? -TERMINAL_FONT_SIZE : 0;
        break;
    }

    if (getPolarity() == Polarity.North || getPolarity() == Polarity.South) {
      StringUtils.drawCenteredText(
          g2d,
          getPolarity().name().substring(0, 1),
          (points[1].x + points[2].x) / 2 + dx,
          (points[1].y + points[2].y) / 2 + dy,
          HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
    } else {
      StringUtils.drawCenteredText(
          g2d,
          "N",
          (points[0].x + points[1].x) / 2 + dx,
          (points[0].y + points[1].y) / 2 + dy,
          HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(
          g2d,
          "S",
          (points[2].x + points[3].x) / 2 + dx,
          (points[2].y + points[3].y) / 2 + dy,
          HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
    }
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    if (getPolarity() != Polarity.Humbucking && (index == 0 || index == 3)) {
      return VisibilityPolicy.NEVER;
    }
    return VisibilityPolicy.ALWAYS;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    if (getPolarity() != Polarity.Humbucking && (index == 0 || index == 3)) {
      return false;
    }
    return true;
  }

  @Override
  public String getControlPointNodeName(int index) {
    switch (index) {
      case 0:
        return isHumbucking() ? "North Start" : null;
      case 1:
        return isSouthPolarity()
            ? "South Start"
            : (isNorthPolarity() ? "North Start" : "North Finish");
      case 2:
        return isSouthPolarity()
            ? "South Finish"
            : (isNorthPolarity() ? "North Finish" : "South Start");
      case 3:
        return isHumbucking() ? "South Finish" : null;
      default:
        throw new RuntimeException("unknown control point name for node " + index);
    }
  }

  @Override
  public String getInternalLinkName(int index1, int index2) {
    switch (getPolarity()) {
      case Humbucking:
        if (index1 == 0 && index2 == 1) {
          return Polarity.North.toString() + "->";
        } else if (index1 == 1 && index2 == 0) {
          return Polarity.North.toString() + "<-";
        } else if (index1 == 2 && index2 == 3) {
          return Polarity.South.toString() + "->";
        } else if (index1 == 3 && index2 == 2) {
          return Polarity.South.toString() + "<-";
        }
        break;
      case North:
      case South:
        if (index1 == 1 && index2 == 2) {
          return getPolarity().toString() + "->";
        } else if (index1 == 2 && index2 == 1) {
          return getPolarity().toString() + "<-";
        }
        break;
      default:
    }
    return null;
  }

  public boolean isHumbucker() {
    return isHumbucking();
  }
}

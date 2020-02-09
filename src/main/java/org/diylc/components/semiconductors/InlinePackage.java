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

package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Point;

import org.diylc.common.Display;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.components.PinCount;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.measures.Size;

public abstract class InlinePackage extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static final Color BODY_COLOR = Color.gray;
  public static final Color BORDER_COLOR = Color.gray.darker();
  public static final Color PIN_COLOR = Color.decode("#00B2EE");
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color INDENT_COLOR = Color.gray.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final int EDGE_RADIUS = 6;
  public static final Size INDENT_SIZE = Size.in(0.07);

  protected PinCount pinCount;
  protected Point[] controlPoints = new Point[] {new Point(0, 0)};
  protected transient Area[] body;

  protected Color bodyColor = BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Color labelColor = LABEL_COLOR;
  protected Color indentColor = INDENT_COLOR;
  protected Size pinSpacing = Size.in(0.1);
  protected String value = "";
  protected Orientation orientation = Orientation.DEFAULT;

  protected InlinePackage(PinCount pinCount, Display display) {
    super();
    this.pinCount = pinCount;
    this.display = display;
    updateControlPoints();
  }

  protected abstract void updateControlPoints();

  public abstract Orientation getOrientation();

  public abstract void setOrientation(Orientation orientation);

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }
}

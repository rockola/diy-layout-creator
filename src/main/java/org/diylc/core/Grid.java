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
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.core;

import java.awt.Point;
import org.diylc.core.measures.Size;

public class Grid {

  public static final Size DEFAULT_GRID_SPACING = Size.in(0.1);

  private final Size gridSpacing;

  public Grid(Size gridSpacing) {
    super();
    this.gridSpacing = gridSpacing;
  }

  public Grid() {
    this(DEFAULT_GRID_SPACING);
  }

  public Size getSpacing() {
    return new Size(gridSpacing);
  }

  /**
   * Rounds the coordinate to the closest grid line.
   *
   * @param coordinate Coordinate before rounding.
   * @return Coordinate rounded to the closest grid line as pixels.
   */
  public int roundToGrid(double coordinate) {
    double grid = gridSpacing.convertToPixels();
    return (int) (Math.round(1f * x / grid) * grid);
  }

  /**
     Align point with grid.

     @return Grid point closest to given point.
   */
  public Point snapToGrid(Point point) {
    int x = roundToGrid(point.x);
    int y = roundToGrid(point.y);
    return new Point(x, y);
  }
}

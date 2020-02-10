/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Toolkit;

public class Constants {

  public static final double MM_PER_INCH = 25.4d;
  public static final int PIXELS_PER_INCH = 200;
  public static final Double DEGREES_PER_RADIAN = Double.valueOf(0.017453292519943295D);
  public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
  public static final Color MULTI_VALUE_COLOR = Color.yellow;
  public static final double PIXEL_SIZE =
      1d * PIXELS_PER_INCH / Toolkit.getDefaultToolkit().getScreenResolution();
  public static final Stroke DASHED_STROKE =
      new BasicStroke(
          1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {2.0f, 4.0f}, 0.0f);
}

/*
  DIY Layout Creator (DIYLC). Copyright (c) 2009-2019 held jointly by
  the individual authors.

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

package org.diylc.swingframework.ruler;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import org.diylc.images.Icon;

public enum CursorLoader {
  ScrollCenter(Icon.Scroll_Center),
  ScrollN(Icon.Scroll_N),
  ScrollNE(Icon.Scroll_NE),
  ScrollE(Icon.Scroll_E),
  ScrollSE(Icon.Scroll_SE),
  ScrollS(Icon.Scroll_S),
  ScrollSW(Icon.Scroll_SW),
  ScrollW(Icon.Scroll_W),
  ScrollNW(Icon.Scroll_NW);

  private Icon icon;
  private Cursor cursor;

  CursorLoader(Icon icon) {
    this.icon = icon;
    this.cursor = icon.image() == null
                  ? Cursor.getDefaultCursor()
                  : Toolkit.getDefaultToolkit().createCustomCursor(
                        icon.image(),
                        new Point(
                            icon.image().getWidth(null) / 2,
                            icon.image().getHeight(null) / 2),
                        "custom:" + name());
  }

  public Cursor getCursor() {
    return this.cursor;
  }
}

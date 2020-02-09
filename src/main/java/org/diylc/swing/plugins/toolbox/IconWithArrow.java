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

package org.diylc.swing.plugins.toolbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.UIManager;
import org.diylc.images.Icon;

class IconWithArrow implements javax.swing.Icon {

  private static javax.swing.Icon arrow = Icon.Arrow.icon();
  private static int arrowHeight = arrow.getIconHeight();
  private static Color bright;
  private static Color dark;

  static {
    bright = UIManager.getColor("controlHighlight");
    dark = UIManager.getColor("controlShadow");
  }

  private javax.swing.Icon orig;
  private boolean paintRollOver;

  private int height;
  private int origHeight;
  private int origWidth;

  public IconWithArrow(javax.swing.Icon orig, boolean paintRollOver) {
    assert (orig != null);
    this.orig = orig;
    this.paintRollOver = paintRollOver;
    this.height = getIconHeight();
    this.origHeight = orig.getIconHeight();
    this.origWidth = orig.getIconWidth();
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    orig.paintIcon(c, g, x, y + (height - origHeight) / 2);
    arrow.paintIcon(c, g, x + 6 + origWidth, y + (height - arrowHeight) / 2);
    if (paintRollOver) {
      Color brighter = bright;
      Color darker = dark;
      if ((brighter == null) || (darker == null)) {
        brighter = c.getBackground().brighter();
        darker = c.getBackground().darker();
      }
      if (brighter != null) {
        g.setColor(brighter);
        g.drawLine(x + origWidth + 1, y, x + origWidth + 1, y + height);
      }
      if (darker != null) {
        g.setColor(darker);
        g.drawLine(x + origWidth + 2, y, x + origWidth + 2, y + height);
      }
    }
  }

  public int getIconWidth() {
    return orig.getIconWidth() + 6 + arrow.getIconWidth();
  }

  public int getIconHeight() {
    return Math.max(orig.getIconHeight(), arrow.getIconHeight());
  }

  public static int getArrowAreaWidth() {
    return 8;
  }
}

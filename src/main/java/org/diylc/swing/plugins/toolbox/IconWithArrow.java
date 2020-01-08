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

//import javax.swing.Icon;
import javax.swing.UIManager;

import org.diylc.images.Icon;

class IconWithArrow implements javax.swing.Icon {
    private javax.swing.Icon orig;
    private javax.swing.Icon arrow = Icon.Arrow.icon();
    private boolean paintRollOver;

    public IconWithArrow(javax.swing.Icon orig, boolean paintRollOver) {
	assert(orig != null);
	this.orig = orig;
	this.paintRollOver = paintRollOver;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
	int height = getIconHeight();
	this.orig.paintIcon(c, g,
			    x,
			    y + (height - this.orig.getIconHeight()) / 2);
	this.arrow.paintIcon(c, g,
			     x + 6 + this.orig.getIconWidth(),
			     y + (height - this.arrow.getIconHeight()) / 2);
	if (this.paintRollOver) {
	    Color brighter = UIManager.getColor("controlHighlight");
	    Color darker = UIManager.getColor("controlShadow");
	    if ((null == brighter) || (null == darker)) {
		brighter = c.getBackground().brighter();
		darker = c.getBackground().darker();
	    }
	    if ((null != brighter) && (null != darker)) {
		g.setColor(brighter);
		g.drawLine(x + this.orig.getIconWidth() + 1, y,
			   x + this.orig.getIconWidth() + 1, y + getIconHeight());

		g.setColor(darker);
		g.drawLine(x + this.orig.getIconWidth() + 2, y,
			   x + this.orig.getIconWidth() + 2, y + getIconHeight());
	    }
	}
    }

    public int getIconWidth() {
	return this.orig.getIconWidth() + 6 + this.arrow.getIconWidth();
    }

    public int getIconHeight() {
	return Math.max(this.orig.getIconHeight(), this.arrow.getIconHeight());
    }

    public static int getArrowAreaWidth() {
	return 8;
    }
}

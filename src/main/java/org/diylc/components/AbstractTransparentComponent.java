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

package org.diylc.components;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractTransparentComponent<T> extends AbstractComponent<T> {

  private static final long serialVersionUID = 1L;

  public static final int MAX_ALPHA = 127;

  protected int alpha = MAX_ALPHA;

  @EditableProperty
  public int getAlpha() {
    return alpha;
  }

  public void setAlpha(int alpha) {
    this.alpha = alpha;
  }

  private Composite setAlphaTransparency(Graphics2D g2d, int alpha) {
    final Composite oldComposite = g2d.getComposite();
    alpha = Math.max(0, Math.min(MAX_ALPHA, alpha));
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER,
          1f * alpha / MAX_ALPHA));
    }
    return oldComposite;
  }

  /**
     Set transparency using alpha.

     @param g2d Graphics context.
     @return old Composite for later restoration
  */
  protected Composite setTransparency(Graphics2D g2d) {
    return setAlphaTransparency(g2d, alpha);
  }

  /**
     Set transparency using alpha, but use a different alpha when dragging.

     @param g2d Graphics context.
     @param alphaWhenDragging Alpha to use when dragging (0 = transparent, 100 = opaque).
     @return old Composite for later restoration
  */
  protected Composite setTransparency(Graphics2D g2d, int alphaWhenDragging) {
    return setAlphaTransparency(g2d, isDragging() ? alphaWhenDragging : alpha);
  }
}

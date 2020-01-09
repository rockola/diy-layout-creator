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
package org.diylc.swingframework.ruler;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import org.diylc.swingframework.IDrawingProvider;

/**
 * {@link IDrawingProvider} implementation that draws the specified {@link Component} onto the
 * canvas.
 *
 * @author Branislav Stojkovic
 */
public class ComponentThumbnailProvider implements IDrawingProvider {

  private Component view;

  public ComponentThumbnailProvider(Component view) {
    super();
    this.view = view;
  }

  @Override
  public Dimension getSize() {
    return view.getSize();
  }

  @Override
  public void draw(int page, Graphics g, double zoomFactor) {
    ((Graphics2D) g).scale(zoomFactor, zoomFactor);
    view.paint(g);
  }

  @Override
  public int getPageCount() {
    return 1;
  }
}

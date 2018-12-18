/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.components.chassis;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.shapes.AbstractShape;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;

@ComponentDescriptor(name = "Elliptical Cutout", author = "Branislav Stojkovic", category = "Electromechanical",
    instanceNamePrefix = "ELC", description = "Elliptical chassis cutout", zOrder = IDIYComponent.CHASSIS + 0.1,
    bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false, transformer = SimpleComponentTransformer.class)
public class EllipticalCutout extends AbstractShape {

  private static final long serialVersionUID = 1L;

  public EllipticalCutout() {
    this.borderColor = LIGHT_METAL_COLOR.darker();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((int) borderThickness.convertToPixels()));

    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(color);
      g2d.fillOval(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y - firstPoint.y);
      g2d.setComposite(oldComposite);
    }
    // Do not track any changes that follow because the whole oval has been
    // tracked so far.
    drawingObserver.stopTracking();
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : borderColor);
    g2d.drawOval(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y - firstPoint.y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(COLOR);
    g2d.fillOval(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
    g2d.setColor(LIGHT_METAL_COLOR.darker());
    g2d.drawOval(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
  }
}

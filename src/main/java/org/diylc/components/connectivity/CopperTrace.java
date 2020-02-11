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

package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.PcbLayer;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.Area;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Copper Trace",
    author = "Branislav Stojkovic",
    category = "Connectivity",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "Trace",
    description = "Straight copper trace",
    zOrder = IDIYComponent.TRACE,
    bomPolicy = BomPolicy.NEVER_SHOW,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "PCB",
    transformer = SimpleComponentTransformer.class)
public class CopperTrace extends AbstractLeadedComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static final Size THICKNESS = Size.mm(1);
  public static final Color COLOR = Color.black;

  private Size thickness = THICKNESS;
  private PcbLayer layer = PcbLayer._1;

  public CopperTrace() {
    super();
    this.leadColor = COLOR;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
    g2d.setColor(COLOR);
    g2d.drawLine(1, height - 2, width - 2, 1);
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  @EditableProperty(name = "Color")
  public Color getLeadColor() {
    return leadColor;
  }

  @EditableProperty(name = "Width")
  public Size getThickness() {
    return thickness;
  }

  public void setThickness(Size thickness) {
    this.thickness = thickness;
  }

  @EditableProperty
  public PcbLayer getLayer() {
    if (layer == null) {
      layer = PcbLayer._1;
    }
    return layer;
  }

  public void setLayer(PcbLayer layer) {
    this.layer = layer;
  }

  @Override
  protected float getLeadThickness() {
    return (float) getThickness().convertToPixels();
  }

  @Override
  protected boolean shouldShadeLeads() {
    return false;
  }

  public Color getBodyColor() {
    return super.getBodyColor();
  }

  @Override
  public Color getBorderColor() {
    return super.getBorderColor();
  }

  @Override
  public int getAlpha() {
    return super.getAlpha();
  }

  @Override
  public Size getLength() {
    return super.getLength();
  }

  @Override
  public Size getWidth() {
    return super.getWidth();
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  protected Area getBodyShape() {
    return null;
  }

  @Override
  protected Size getDefaultWidth() {
    return null;
  }

  @Override
  protected Size getDefaultLength() {
    return null;
  }

  @Override
  protected boolean isCopperArea() {
    return true;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  public boolean getMoveLabel() {
    // override to disable edit
    return false;
  }
}

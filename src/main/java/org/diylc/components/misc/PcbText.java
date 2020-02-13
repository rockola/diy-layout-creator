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

package org.diylc.components.misc;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import org.diylc.common.Config;
import org.diylc.common.PcbLayer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.transform.TextTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(
    name = "PCB Text",
    author = "Branislav Stojkovic",
    category = "Misc",
    description = "Mirrored text for PCB artwork",
    instanceNamePrefix = "L",
    zOrder = AbstractComponent.TRACE,
    flexibleZOrder = false,
    bomPolicy = BomPolicy.NEVER_SHOW,
    transformer = TextTransformer.class)
public class PcbText extends Misc {

  private static final long serialVersionUID = 1L;

  public static final Font DEFAULT_FONT =
      new Font(Config.getString("font.monospace"), Font.BOLD, 15);

  private PcbLayer layer = PcbLayer._1;

  {
    font = DEFAULT_FONT;
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    super.draw(g2d, componentState, outlineMode, project, drawingObserver);

    final AffineTransform oldTransform = g2d.getTransform();
    // Flip horizontally
    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    tx.translate(-2 * positionX - textWidth, 0);
    g2d.transform(tx);
    g2d.drawString(text, positionX, positionY);
    g2d.setTransform(oldTransform);
  }

  @Override
  protected void flipText(Graphics2D g2d, int width) {
    g2d.setFont(DEFAULT_FONT.deriveFont(15f * width / 32).deriveFont(Font.BOLD));
    g2d.scale(-1, 1);
    g2d.translate(-width, 0);
  }

  // TODO should this be done with @StringValue("Text") instead?
  @EditableProperty(defaultable = false)
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
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
}

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
import org.diylc.components.transform.TextTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(
    name = "Label",
    author = "Branislav Stojkovic",
    category = "Misc",
    description = "User defined label",
    instanceNamePrefix = "L",
    zOrder = IDIYComponent.TEXT,
    flexibleZOrder = true,
    bomPolicy = BomPolicy.NEVER_SHOW,
    transformer = TextTransformer.class)
public class Label extends Misc<String> {

  private static final long serialVersionUID = 1L;

  // @Deprecated private boolean center;

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    super.draw(g2d, componentState, outlineMode, project, drawingObserver);
    g2d.drawString(text, positionX, positionY);
  }

  @Override
  protected void flipText(Graphics2D g2d, int width) {
    g2d.setFont(LABEL_FONT.deriveFont(13f * width / 32).deriveFont(Font.PLAIN));
  }

  @EditableProperty(name = "Text", defaultable = false)
  @Override
  public String getValue() {
    return text;
  }

  @Override
  public void setValue(String value) {
    this.text = value;
  }
}

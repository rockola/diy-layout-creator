/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.plugins.canvas;

import java.awt.Point;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import org.diylc.common.IPlugInPort;


/**
 * {@link DragSourceListener} for {@link CanvasPanel}.
 * 
 * @author Branislav Stojkovic
 */
class CanvasSourceListener implements DragSourceListener {

  private IPlugInPort presenter;

  public CanvasSourceListener(IPlugInPort presenter) {
    super();
    this.presenter = presenter;
  }

  @Override
  public void dragDropEnd(DragSourceDropEvent dsde) {
    presenter.dragEnded(null);
  }

  @Override
  public void dragEnter(DragSourceDragEvent dsde) {}

  @Override
  public void dragExit(DragSourceEvent dse) {}

  @Override
  public void dragOver(DragSourceDragEvent dsde) {
    Point p = dsde.getDragSourceContext().getComponent().getMousePosition();
    if (p != null) {
      dsde.getDragSourceContext().getComponent().firePropertyChange("dragPoint", p.x, p.y);
    }
  }

  @Override
  public void dropActionChanged(DragSourceDragEvent dsde) {}
}

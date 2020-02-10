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

package org.diylc.swing.plugins.canvas;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import org.diylc.common.IPlugInPort;

/**
 * {@link DropTargetListener} for {@link CanvasPanel}.
 *
 * @author Branislav Stojkovic
 */
class CanvasTargetListener implements DropTargetListener {

  private IPlugInPort presenter;

  // Cached values
  private Point currentPoint = null;
  private boolean lastAccept;

  public CanvasTargetListener(IPlugInPort presenter) {
    super();
    this.presenter = presenter;
  }

  @Override
  public void dragEnter(DropTargetDragEvent event) {
    //
  }

  @Override
  public void dragExit(DropTargetEvent event) {
    //
  }

  @Override
  public void dragOver(DropTargetDragEvent event) {
    // If dragOver was previously called for this location use cached value.
    if (event.getLocation().equals(currentPoint)) {
      if (lastAccept) {
        event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      } else {
        event.rejectDrag();
      }
      return;
    }
    try {
      currentPoint = event.getLocation();
      if (presenter.dragOver(currentPoint)) {
        event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        lastAccept = true;
      } else {
        event.rejectDrag();
        lastAccept = false;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      event.rejectDrag();
      lastAccept = false;
    }
  }

  @Override
  public void drop(DropTargetDropEvent event) {
    presenter.dragEnded(event.getLocation());
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent event) {
    presenter.dragActionChanged(event.getDropAction());
  }
}

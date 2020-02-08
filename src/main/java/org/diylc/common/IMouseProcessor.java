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

package org.diylc.common;

import java.awt.Point;

public interface IMouseProcessor {

  /**
   * Notifies the presenter that mouse is clicked.
   *
   * <p>Note: point coordinates are display based, i.e. scaled for zoom factor.
   *
   * @param point Cursor coordinates
   * @param button Button(s) pressed
   * @param ctrlDown true if Control was pressed
   * @param shiftDown true if Shift was pressed
   * @param altDown true if Alt was pressed
   * @param clickCount 1 for single click, 2 for double etc.
   */
  void mouseClicked(
      Point point,
      int button,
      boolean ctrlDown,
      boolean shiftDown,
      boolean altDown,
      int clickCount);

  /**
   * Notifies the presenter that mouse is moved.
   *
   * <p>Note: point coordinates are display based, i.e. scaled for zoom factor.
   *
   * @param point Cursor coordinates
   * @param ctrlDown true if Control was pressed
   * @param shiftDown true if Shift was pressed
   * @param altDown true if Alt was pressed
   */
  void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown);

  /**
   * Notification that drag has been started from the specified point.
   *
   * <p>Note: point coordinates are scaled for zoom factor.
   *
   * @param point Cursor coordinates
   * @param dragAction
   * @param forceSelectionRect
   */
  void dragStarted(Point point, int dragAction, boolean forceSelectionRect);

  /**
   * Checks if it's possible to drop over the specified point.
   *
   * <p>Note: point coordinates are scaled for zoom factor.
   *
   * @param point Cursor coordinates
   * @return
   */
  boolean dragOver(Point point);

  /**
   * Changes the current drag action during the dragging.
   *
   * @param dragAction
   */
  void dragActionChanged(int dragAction);

  /**
   * Notification that drag has been ended in the specified point.
   *
   * <p>Note: point coordinates are scaled for zoom factor.
   *
   * @param point
   */
  void dragEnded(Point point);
}

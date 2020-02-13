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

package org.diylc.action;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.diylc.components.AbstractComponent;

public class ComponentEdit extends AbstractUndoableEdit {

  private static final long serialVersionUID = 1L;

  public ComponentEdit(AbstractComponent c) {
    super();
    // TODO sth w/c
  }

  @Override
  public boolean addEdit(UndoableEdit anEdit) {
    throw new RuntimeException("addEdit TODO");
  }

  @Override
  public boolean canRedo() {
    throw new RuntimeException("canRedo TODO");
  }

  @Override
  public boolean canUndo() {
    throw new RuntimeException("canUndo TODO");
  }

  @Override
  public void die() {
    throw new RuntimeException("die TODO");
  }

  @Override
  public String getPresentationName() {
    throw new RuntimeException("getPresentationName TODO");
  }

  @Override
  public String getRedoPresentationName() {
    throw new RuntimeException("getRedoPresentationName TODO");
  }

  @Override
  public boolean isSignificant() {
    throw new RuntimeException("isSignificant TODO");
  }

  @Override
  public void redo() throws CannotRedoException {
    super.redo();
    throw new RuntimeException("redo TODO");
  }

  @Override
  public boolean replaceEdit(UndoableEdit anEdit) {
    throw new RuntimeException("replaceEdit TODO");
  }

  @Override
  public void undo() throws CannotUndoException {
    super.undo();
    throw new RuntimeException("undo TODO");
  }

  public void execute() {}
}

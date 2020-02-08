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

import java.util.Collection;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

public class ProjectEdit extends CompoundEdit {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(ProjectEdit.class);

  private Project project;
  private Collection<IDIYComponent<?>> oldComponents;
  private Collection<IDIYComponent<?>> newComponents;


  public ProjectEdit(
      Project project,
      Collection<IDIYComponent<?>> oldComponents,
      Collection<IDIYComponent<?>> newComponents) {
    super();
    this.project = project;
    this.oldComponents = oldComponents;
    this.newComponents = newComponents;
  }

  /*
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
  */

  @Override
  public void redo() throws CannotRedoException {
    super.redo();
    throw new RuntimeException("redo TODO");
  }

  /*
  @Override
  public boolean replaceEdit(UndoableEdit anEdit) {
    throw new RuntimeException("replaceEdit TODO");
  }
  */

  @Override
  public void undo() throws CannotUndoException {
    super.undo();
    //throw new RuntimeException("undo TODO");
    // remove new
    if (!(newComponents == null || newComponents.isEmpty())) {
      project.removeComponents(newComponents);
    }
    // add old
    if (!(oldComponents == null || oldComponents.isEmpty())) {
      project.addComponents(oldComponents);
    }
  }

  public void execute() {
    LOG.debug("execute()");
    // remove old
    if (!(oldComponents == null || oldComponents.isEmpty())) {
      LOG.debug("execute() removing {} components", oldComponents.size());
      project.removeComponents(oldComponents);
    }
    // add new
    if (!(newComponents == null || newComponents.isEmpty())) {
      LOG.debug("execute() adding {} components", newComponents.size());
      project.addComponents(newComponents);
    }
  }
}

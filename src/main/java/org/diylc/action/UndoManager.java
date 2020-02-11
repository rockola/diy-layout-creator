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

import java.util.ArrayList;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

public class UndoManager extends javax.swing.undo.UndoManager {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(UndoManager.class);

  public void replaceComponents(
      Project project,
      Collection<IDIYComponent<?>> oldComponents,
      Collection<IDIYComponent<?>> newComponents) {
    LOG.trace("replaceComponents()");
    ProjectEdit edit =
        new ProjectEdit(
            project,
            oldComponents == null ? null : new ArrayList<IDIYComponent<?>>(oldComponents),
            newComponents == null ? null : new ArrayList<IDIYComponent<?>>(newComponents));
    edit.execute();
    boolean editAdded = addEdit(edit);
  }

  public void addComponents(Project project, Collection<IDIYComponent<?>> components) {
    replaceComponents(project, null, components);
  }

  public void removeComponents(Project project, Collection<IDIYComponent<?>> components) {
    replaceComponents(project, components, null);
  }
}

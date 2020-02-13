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

package org.diylc.components.autocreate;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.components.AbstractComponent;
import org.diylc.components.connectivity.SolderPad;
import org.diylc.core.annotations.IAutoCreator;

public class SolderPadAutoCreator implements IAutoCreator {

  private static final Logger LOG = LogManager.getLogger(SolderPadAutoCreator.class);

  @Override
  public List<AbstractComponent> createIfNeeded(AbstractComponent lastAdded) {
    List<AbstractComponent> res = null;
    if (App.autoPads() && !(lastAdded instanceof SolderPad)) {
      res = new ArrayList<AbstractComponent>();
      for (int i = 0; i < lastAdded.getControlPointCount(); i++) {
        if (lastAdded.isControlPointSticky(i)) {
          try {
            SolderPad pad = new SolderPad();
            pad.setControlPoint(lastAdded.getControlPoint(i), 0);
            res.add(pad);
          } catch (Exception e) {
            LOG.error("Could not auto-create solder pad", e);
          }
        }
      }
    }
    return res;
  }
}

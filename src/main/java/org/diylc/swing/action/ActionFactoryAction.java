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

package org.diylc.swing.action;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.diylc.App;
import org.diylc.common.IPlugInPort;

public abstract class ActionFactoryAction extends AbstractAction {

  protected IPlugInPort plugInPort;

  protected ActionFactoryAction(
      IPlugInPort plugInPort, String name, KeyStroke actionKeyStroke, Object icon) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, name);
    if (actionKeyStroke != null) {
      putValue(AbstractAction.ACCELERATOR_KEY, actionKeyStroke);
    }
    if (icon != null) {
      putValue(AbstractAction.SMALL_ICON, icon);
    }
  }

  protected ActionFactoryAction(
      IPlugInPort plugInPort, String name, String actionName, Object icon) {
    this(plugInPort, name, App.getKeyStroke(actionName), icon);
  }

  protected ActionFactoryAction(
      IPlugInPort plugInPort, String name, String actionName) {
    this(plugInPort, name, App.getKeyStroke(actionName), null);
  }

  protected ActionFactoryAction(IPlugInPort plugInPort, String name, Object icon) {
    this(plugInPort, name, (KeyStroke) null, icon);
  }

  protected ActionFactoryAction(IPlugInPort plugInPort, String name) {
    this(plugInPort, name, (KeyStroke) null, null);
  }
}

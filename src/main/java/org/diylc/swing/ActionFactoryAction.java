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
package org.diylc.swing;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.DIYLC;
import org.diylc.common.IPlugInPort;

public abstract class ActionFactoryAction extends AbstractAction {

    protected IPlugInPort plugInPort;
    protected ISwingUI swingUI;

    private void initMe(IPlugInPort plugInPort,
			ISwingUI swingUI,
			String name,
			Object icon) {
	this.plugInPort = plugInPort;
	this.swingUI = swingUI;
	putValue(AbstractAction.NAME, name);
	if (icon != null)
	    putValue(AbstractAction.SMALL_ICON, icon);
    }

    protected ActionFactoryAction(IPlugInPort plugInPort,
				  ISwingUI swingUI,
				  String name,
				  String actionName,
				  Object icon) {
	super();
	initMe(plugInPort, swingUI, name, icon);
	if (actionName != null)
	    putValue(AbstractAction.ACCELERATOR_KEY, DIYLC.getKeyStroke(actionName));
    }

    protected ActionFactoryAction(IPlugInPort plugInPort,
				  ISwingUI swingUI,
				  String name,
				  KeyStroke actionKeyStroke,
				  Object icon) {
	super();
	initMe(plugInPort, swingUI, name, icon);
	putValue(AbstractAction.ACCELERATOR_KEY, actionKeyStroke);
    }

    protected ActionFactoryAction(IPlugInPort plugInPort,
				  ISwingUI swingUI,
				  String name,
				  Object icon) {
	super();
	initMe(plugInPort, swingUI, name, icon);
    }

    protected ActionFactoryAction(IPlugInPort plugInPort,
				  ISwingUI swingUI,
				  String name) {
	super();
	initMe(plugInPort, swingUI, name, null);
    }

    protected ActionFactoryAction(IPlugInPort plugInPort,
				  String name,
				  Object icon) {
	super();
	initMe(plugInPort, null, name, icon);
    }

    protected ActionFactoryAction(IPlugInPort plugInPort,
				  String name) {
	super();
	initMe(plugInPort, null, name, null);
    }
}


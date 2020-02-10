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
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.swing.plugins.toolbox;

import java.util.EnumSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.plugins.config.ConfigPlugin;

public class Toolbox implements IPlugIn {

  private static final Logger LOG = LogManager.getLogger(Toolbox.class);

  public Toolbox() {
    //
  }

  @Override
  public void connect(IPlugInPort ignore) {
    LOG.trace("connect() adding component browser listener");
    ConfigurationManager.addListener(
        ConfigPlugin.COMPONENT_BROWSER,
        (key, value) -> App.ui().getComponentTabbedPane().setVisible(
            ConfigPlugin.COMPONENT_BROWSER.equals(key)
            && ConfigPlugin.TABBED_TOOLBAR.equals(value)));
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    // TODO Auto-generated method stub

  }
}

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

package org.diylc.swing.plugins.config;

import java.util.ArrayList;
import java.util.List;

import org.diylc.App;
import org.diylc.common.Config;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.action.ActionFactory;

public class ConfigActions {
  private List<ConfigAction> actions;

  public ConfigActions() {
    actions = new ArrayList<ConfigAction>();
  }

  public void add(String name, String action, boolean defaultValue) {
    actions.add(new ConfigAction(Config.getString("menu.config." + name), action, defaultValue));
  }

  public void add(String name, IPlugInPort.Key action, boolean defaultValue) {
    add(name, action.toString(), defaultValue);
  }

  public void injectActions(IPlugInPort plugInPort, String menuName) {
    for (ConfigAction a : actions) {
      App.ui().injectMenuAction(ActionFactory.createConfigAction(
          plugInPort, a.getName(), a.getAction(), a.getDefault()),
                                menuName);
    }
  }

  private static class ConfigAction {
    private String name;
    private String action;
    private boolean defaultValue;

    public ConfigAction(String name, String action, boolean defaultValue) {
      this.name = name;
      this.action = action;
      this.defaultValue = defaultValue;
    }

    public String getName() {
      return name;
    }

    public String getAction() {
      return action;
    }

    public boolean getDefault() {
      return defaultValue;
    }
  }
}

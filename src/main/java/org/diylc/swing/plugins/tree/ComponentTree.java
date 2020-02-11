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

package org.diylc.swing.plugins.tree;

import java.awt.KeyboardFocusManager;
import java.util.EnumSet;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.plugins.config.ConfigPlugin;
import org.diylc.swing.plugins.statusbar.StatusBar;
import org.diylc.swing.tree.ComponentTreePanel;

public class ComponentTree implements IPlugIn {

  private static final Logger LOG = LogManager.getLogger(StatusBar.class);

  private IPlugInPort plugInPort;
  private ComponentTreePanel treePanel;
  private JComponent canvasPanel;

  public ComponentTree(JComponent canvasPanel) {
    this.canvasPanel = canvasPanel;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    try {
      App.ui().injectGuiComponent(getTreePanel(), SwingConstants.LEFT);
    } catch (BadPositionException e) {
      LOG.error("Could not install the component tree", e);
    }
    ConfigurationManager.addListener(
        ConfigPlugin.COMPONENT_BROWSER,
        (key, value) ->
            getTreePanel()
                .setVisible(
                    ConfigPlugin.COMPONENT_BROWSER.equals(key)
                        && ConfigPlugin.SEARCHABLE_TREE.equals(value)));
    getTreePanel()
        .setVisible(
            App.getString(ConfigPlugin.COMPONENT_BROWSER, ConfigPlugin.SEARCHABLE_TREE)
                .equals(ConfigPlugin.SEARCHABLE_TREE));

    KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher(
            (e) -> {
              if ((canvasPanel.hasFocus() || treePanel.hasFocus())
                  && e.getKeyChar() == 'q'
                  && App.getString(ConfigPlugin.COMPONENT_BROWSER, ConfigPlugin.SEARCHABLE_TREE)
                      .equals(ConfigPlugin.SEARCHABLE_TREE)) {
                getTreePanel().getSearchField().requestFocusInWindow();
                return true;
              }
              return false;
            });
  }

  public ComponentTreePanel getTreePanel() {
    if (treePanel == null) {
      treePanel = new ComponentTreePanel(); // new TreePanel(plugInPort);
    }
    return treePanel;
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

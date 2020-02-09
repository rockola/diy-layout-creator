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

package org.diylc.swing.gui.actionbar;

import java.awt.BorderLayout;
import java.util.EnumSet;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.diylc.App;
import org.diylc.common.EventType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.Project;
import org.diylc.swing.action.ActionFactory;

/**
 * Mini toolbar with common actions
 *
 * @author Branislav Stojkovic
 */
public class ActionBarPlugin implements IPlugIn {

  private IPlugInPort plugInPort;
  private JPanel actionPanel;
  private MiniToolbar miniToolbar;

  public ActionBarPlugin() {}

  public JPanel getActionPanel() {
    if (actionPanel == null) {
      actionPanel = new JPanel();
      actionPanel.setOpaque(false);
      actionPanel.setLayout(new BorderLayout());
      actionPanel.add(getMiniToolbar(), BorderLayout.EAST);
      actionPanel.setBorder(BorderFactory.createEmptyBorder());
    }
    return actionPanel;
  }

  public MiniToolbar getMiniToolbar() {
    if (miniToolbar == null) {
      miniToolbar = new MiniToolbar();
      miniToolbar.add(ActionFactory.createRotateSelectionAction(plugInPort, 1));
      miniToolbar.add(ActionFactory.createRotateSelectionAction(plugInPort, -1));
      miniToolbar.addSpacer();
      miniToolbar.add(
          ActionFactory.createMirrorSelectionAction(plugInPort, IComponentTransformer.HORIZONTAL));
      miniToolbar.add(
          ActionFactory.createMirrorSelectionAction(plugInPort, IComponentTransformer.VERTICAL));
      miniToolbar.addSpacer();
      miniToolbar.add(ActionFactory.createNudgeAction(plugInPort));
      miniToolbar.addSpacer();
      miniToolbar.add(ActionFactory.createSendToBackAction(plugInPort));
      miniToolbar.add(ActionFactory.createBringToFrontAction(plugInPort));
      miniToolbar.addSpacer();
      miniToolbar.add(ActionFactory.createGroupAction(plugInPort));
      miniToolbar.add(ActionFactory.createUngroupAction(plugInPort));
    }
    return miniToolbar;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    App.ui().injectMenuComponent(getActionPanel());
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.SELECTION_CHANGED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    if (eventType != EventType.SELECTION_CHANGED) {
      return;
    }
    boolean enabled = !plugInPort.currentProject().emptySelection();
    getMiniToolbar().setEnabled(enabled);
  }
}

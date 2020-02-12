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

package org.diylc.swing.gui.actionbar;

import java.awt.Component;
import java.awt.Cursor;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

public class MiniToolbar extends JToolBar {

  private static final long serialVersionUID = 1L;

  public MiniToolbar() {
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder());
    // note: not currently floatable as will be added as a child of
    // JMenuBar
    setFloatable(true);
    setRollover(true);
  }

  public void addButton(final Action action) {
    JButton button = new JButton();
    button.setIcon((Icon) action.getValue(AbstractAction.SMALL_ICON));
    button.setToolTipText((String) action.getValue(AbstractAction.NAME));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.addActionListener((e) -> action.actionPerformed(null));
    button.setEnabled(false);
    add(button);
  }

  @Override
  public void setEnabled(boolean enabled) {
    for (Component c : getComponents()) {
      c.setEnabled(enabled);
    }
  }

  public void addSpacer() {
    JSeparator spacer = new JSeparator();
    //    spacer.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.black));
    add(spacer);
  }
}

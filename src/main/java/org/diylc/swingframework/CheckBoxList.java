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

package org.diylc.swingframework;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class CheckBoxList extends JList {
  private static final long serialVersionUID = 1L;

  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  /**
     Construct a CheckBoxList from an array of CheckListItem instances.

     @param items Array of CheckListItems.
   */
  public CheckBoxList(CheckListItem[] items) {
    super(items);

    setCellRenderer(new CheckListRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        JList list = (JList) event.getSource();
        int index = list.locationToIndex(event.getPoint());
        if (index >= 0) {
          CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);
          // Toggle selected state
          item.setSelected(!item.isSelected());
          // Repaint cell
          list.repaint(list.getCellBounds(index, index));
        }
      }
    });
  }

  public static class CheckListItem {

    private Object value;
    private boolean isSelected = false;

    public CheckListItem(Object label) {
      this.value = label;
    }

    public boolean isSelected() {
      return isSelected;
    }

    public void setSelected(boolean isSelected) {
      this.isSelected = isSelected;
    }

    @Override
    public String toString() {
      return value.toString();
    }

    public Object getValue() {
      return value;
    }
  }

  private static class CheckListRenderer extends JCheckBox implements ListCellRenderer {

    private static final long serialVersionUID = 1L;

    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
      setEnabled(list.isEnabled());
      setSelected(((CheckListItem) value).isSelected());
      setFont(list.getFont());
      setBackground(list.getBackground());
      setForeground(list.getForeground());
      setText(value.toString());
      return this;
    }
  }
}

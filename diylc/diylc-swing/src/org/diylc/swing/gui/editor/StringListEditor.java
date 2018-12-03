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
package org.diylc.swing.gui.editor;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.annotations.DynamicList;
import org.diylc.utils.Constants;

public class StringListEditor extends JComboBox {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  private final PropertyWrapper property;

  public StringListEditor(final PropertyWrapper property) throws Exception {
    this.property = property;
    String listFunctionName = property.getGetter().getAnnotation(DynamicList.class).availableValueFunction();
    Method function = property.getOwnerObject().getClass().getMethod(listFunctionName);
    Object[] values = (Object[]) function.invoke(property.getOwnerObject());
    setModel(new DefaultComboBoxModel(values));
    setSelectedItem(property.getValue());
    addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          property.setChanged(true);
          setBackground(oldBg);
          StringListEditor.this.property.setValue(e.getItem());
        }
      }
    });
    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }
}

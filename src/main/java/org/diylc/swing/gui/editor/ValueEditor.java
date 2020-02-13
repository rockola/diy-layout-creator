/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

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

package org.diylc.swing.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JComboBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.measures.SiPrefix;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Value;
import org.diylc.swingframework.DoubleTextField;
import org.diylc.utils.Constants;

public class ValueEditor extends Container {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(MeasureEditor.class);

  private Color oldBackground;
  private DoubleTextField valueField;
  private JComboBox unitBox;
  private SiUnit unit;

  public ValueEditor(final PropertyWrapper property) {
    setLayout(new BorderLayout());
    final Value theValue = (Value) property.getValue();
    valueField = new DoubleTextField(theValue == null ? null : theValue.getValue());
    oldBackground = valueField.getBackground();
    valueField.addPropertyChangeListener(
        DoubleTextField.VALUE_PROPERTY,
        new PropertyChangeListener() {

          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            actOnChange(property);
          }
        });
    add(valueField, BorderLayout.CENTER);
    try {
      unit = property.getValueType();
      if (unit != null) {
        Map<String, SiPrefix> unitRange = unit.getRangeMap();
        unitBox = new JComboBox(unitRange.keySet().toArray());
        unitBox.setSelectedItem(theValue == null ? null : theValue.getUnitString());
        unitBox.addActionListener((evt) -> actOnChange(property));
        add(unitBox, BorderLayout.EAST);

        if (!property.isUnique()) {
          valueField.setBackground(Constants.MULTI_VALUE_COLOR);
          unitBox.setBackground(Constants.MULTI_VALUE_COLOR);
        }
      } else {
        LOG.error("No value type set for {}", property.getName());
      }
    } catch (Exception e) {
      LOG.error("Error while creating the editor", e);
      throw new RuntimeException(e);
    }
  }

  private void actOnChange(PropertyWrapper property) {
    try {
      Value value =
          Value.parse(Double.valueOf(valueField.getValue()), (String) unitBox.getSelectedItem());
      if (value != null) {
        property.setValue(value);
        property.setChanged(true);
        valueField.setBackground(oldBackground);
        unitBox.setBackground(oldBackground);
      }
    } catch (Exception e) {
      LOG.error("Error while updating property units", e);
    }
  }

  @Override
  public void requestFocus() {
    this.valueField.requestFocus();
  }

  @Override
  public boolean requestFocusInWindow() {
    return this.valueField.requestFocusInWindow();
  }

  @Override
  public synchronized void addKeyListener(KeyListener l) {
    this.valueField.addKeyListener(l);
    this.unitBox.addKeyListener(l);
  }
}

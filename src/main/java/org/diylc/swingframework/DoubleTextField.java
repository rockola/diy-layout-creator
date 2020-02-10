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

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.text.Format;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.diylc.images.Icon;

/**
 * {@link JTextField} adapted to display {@link Double}.
 * Use {@link #getValue()} and {@link #setValue(Double)} to read and write.
 *
 * @author Branislav Stojkovic
 */
public class DoubleTextField extends TextField {

  private static final long serialVersionUID = 1L;

  private Double value;

  public DoubleTextField(Double value) {
    this();
    setValue(value);
  }

  public DoubleTextField() {
    super();
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    firePropertyChange(VALUE_PROPERTY, this.value, value);
    this.value = value;
    ignoreChanges = true;
    errorLabel.setVisible(value == null);
    try {
      setText(value == null ? "" : format.format(value));
    } finally {
      ignoreChanges = false;
    }
  }

  public void textChanged() {
    if (!ignoreChanges) {
      try {
        Double newValue = null;
        if (!getText().trim().isEmpty()) {
          Object parsed = format.parseObject(getText());
          if (parsed instanceof Long) {
            newValue = ((Long) parsed).doubleValue();
          } else if (parsed instanceof Integer) {
            newValue = ((Integer) parsed).doubleValue();
          } else if (parsed instanceof Double) {
            newValue = (Double) parsed;
          } else if (parsed instanceof Float) {
            newValue = ((Float) parsed).doubleValue();
          } else {
            throw new RuntimeException("Unrecognized data type: " + parsed.getClass().getName());
          }
        }
        firePropertyChange(VALUE_PROPERTY, this.value, newValue);
        this.value = newValue;
        errorLabel.setVisible(false);
      } catch (Exception e) {
        e.printStackTrace();
        this.value = null;
        errorLabel.setVisible(true);
      }
    }
  }
}

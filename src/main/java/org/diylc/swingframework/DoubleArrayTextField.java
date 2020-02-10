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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
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
public class DoubleArrayTextField extends TextField {

  private static final long serialVersionUID = 1L;

  private Double[] value;

  public DoubleArrayTextField(Double[] value) {
    this();
    setValue(value);
  }

  public DoubleArrayTextField() {
    super();
  }

  public Double[] getValue() {
    return value;
  }

  public void setValue(Double[] value) {
    firePropertyChange(VALUE_PROPERTY, this.value, value);
    this.value = value;
    ignoreChanges = true;
    errorLabel.setVisible(value == null);
    try {
      StringJoiner sj = new StringJoiner(" / ");
      for (Double v : value) {
        sj.add(v == null ? "" : format.format(v));
      }
      setText(sj.toString());
    } finally {
      ignoreChanges = false;
    }
  }

  public void textChanged() {
    if (!ignoreChanges) {
      try {
        Double[] newValue;
        int index = getText().indexOf("/");
        int start = 0;
        List<Double> items = new ArrayList<Double>();
        while (index >= 0) {
          String part = getText().substring(start, index).trim();
          items.add(parse(part));
          start = index + 1;
          index = getText().indexOf("/", index + 1);
        }
        // get the last part
        String part = getText().substring(start).trim();
        items.add(parse(part));
        newValue = items.toArray(new Double[0]);
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

  private Double parse(String part) throws ParseException {
    if (part.isEmpty()) {
      return null;
    }
    Object parsed = format.parseObject(part);
    if (parsed instanceof Long) {
      return ((Long) parsed).doubleValue();
    } else if (parsed instanceof Integer) {
      return ((Integer) parsed).doubleValue();
    } else if (parsed instanceof Double) {
      return (Double) parsed;
    } else if (parsed instanceof Float) {
      return ((Float) parsed).doubleValue();
    } else {
      throw new RuntimeException("Unrecognized data type " + parsed.getClass().getName());
    }
  }
}

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
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.annotations.PercentEditor;
import org.diylc.swingframework.DoubleTextField;
import org.diylc.utils.Constants;

public class ByteEditor extends JPanel {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  private static final byte default100Percent = 127;

  private byte _100PercentValue;

  public ByteEditor(final PropertyWrapper property) {
    super();

    setLayout(new FlowLayout());

    try {
      PercentEditor percentEditor = property.getGetter().getAnnotation(PercentEditor.class);
      if (percentEditor == null)
        _100PercentValue = default100Percent;
      else
        _100PercentValue = percentEditor._100PercentValue();
    } catch (Exception e) {
      _100PercentValue = default100Percent;
    }

    final JSlider slider = new JSlider();
    final DoubleTextField valueField = new DoubleTextField();

    final double percentFactor = (double) default100Percent / _100PercentValue;

    slider.setMinimum(0);
    slider.setMaximum(default100Percent * 100);

    slider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        property.setChanged(true);
        setBackground(oldBg);
        slider.setBackground(oldBg);
        property.setValue(new Integer(slider.getValue() / 100).byteValue());
        valueField.setText(Integer.toString((int) Math.round(percentFactor * (double) slider.getValue()
            / default100Percent)));
      }
    });
    slider.setValue((Byte) property.getValue() * 100);

    valueField
        .setText(Integer.toString((int) Math.round(percentFactor * (double) slider.getValue() / default100Percent)));
    valueField.setColumns(3);
    valueField.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(KeyEvent e) {
        try {
          int newPosition = (int) (Double.parseDouble(valueField.getText()) * default100Percent / percentFactor);
          System.out.println("keyReleased, pos: " + newPosition);
          slider.setValue(newPosition);
        } catch (Exception ex) {
        }
      }
    });

    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
      slider.setBackground(Constants.MULTI_VALUE_COLOR);
    }

    add(slider);
    add(valueField);
    add(new JLabel("%"));
  }
}

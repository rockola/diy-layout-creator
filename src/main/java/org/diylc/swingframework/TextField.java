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

public abstract class TextField extends JTextField {

  private static final long serialVersionUID = 1L; // not sure if needed in an abstract class

  protected static final Format format = new DecimalFormat("0.#####");
  protected JLabel errorLabel;
  protected boolean ignoreChanges = false;

  public static final String VALUE_PROPERTY = "DoubleValue";

  public TextField() {
    super();
    setLayout(new BorderLayout());
    errorLabel = new JLabel(Icon.Warning.icon());
    errorLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
    add(errorLabel, BorderLayout.EAST);
    getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void changedUpdate(DocumentEvent e) {
          textChanged();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          textChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          textChanged();
        }
      });
  }

  public abstract void textChanged();
}

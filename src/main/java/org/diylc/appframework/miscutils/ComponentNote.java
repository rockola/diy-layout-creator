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

package org.diylc.appframework.miscutils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ComponentNote extends JPanel {

  private static final long serialVersionUID = 1L;

  private static int MARGIN = 4;

  private JComponent mainComponent;

  public ComponentNote(Icon icon, String text, JComponent mainComponent) {
    super(new BorderLayout());
    this.mainComponent = mainComponent;
    JLabel label = new JLabel(text);
    if (icon != null) {
      label.setIcon(icon);
    }
    add(label, BorderLayout.CENTER);

    JLabel closeLabel = new JLabel("X");
    closeLabel.setBorder(BorderFactory.createEmptyBorder(0, MARGIN, 0, 0));
    add(closeLabel, BorderLayout.EAST);
    closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    closeLabel.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            setVisible(false);
          }
        });

    setOpaque(true);
    setBackground(Color.orange);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.black),
        BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)));
    mainComponent.addComponentListener(new ComponentAdapter() {

        @Override
        public void componentMoved(ComponentEvent e) {
          updateLocation();
        }

        @Override
        public void componentResized(ComponentEvent e) {
          updateLocation();
        }
      });
    mainComponent.getParent().addComponentListener(new ComponentAdapter() {

        @Override
        public void componentResized(ComponentEvent e) {
          updateLocation();
        }
      });
    updateLocation();
  }

  private void updateLocation() {
    Rectangle mainBounds = SwingUtilities.convertRectangle(mainComponent.getParent(),
                                                           mainComponent.getBounds(),
                                                           null);
    Rectangle rootBounds = SwingUtilities.getRoot(mainComponent).getBounds();
    Dimension bounds = getPreferredSize();
    int x = mainBounds.x + (mainBounds.width - bounds.width) / 2;
    if (x + bounds.width > rootBounds.width) {
      x -= x + bounds.width - rootBounds.width + 2 * MARGIN;
    }
    int y = mainBounds.y - bounds.height - mainBounds.height - 2 * MARGIN;
    setBounds(new Rectangle(x, y, bounds.width, bounds.height));
  }
}

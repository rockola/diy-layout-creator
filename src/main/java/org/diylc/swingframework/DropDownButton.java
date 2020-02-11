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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.border.Border;

public class DropDownButton extends Box {

  private static final long serialVersionUID = 1L;
  private JButton mainButton;
  private JButton dropDownButton;
  private boolean dropDownEnabled = false;
  private boolean mainRunsDefaultMenuOption = true;
  private transient Icon enabledDownArrow;
  private transient Icon disDownArrow;
  private DropDownMenu menu;
  private MainButtonListener mainButtonListener = new MainButtonListener();

  public DropDownButton(JButton mainButton) {
    super(BoxLayout.X_AXIS);

    menu = new DropDownMenu();
    menu.getPopupMenu().addContainerListener(new MenuContainerListener());
    JMenuBar bar = new JMenuBar();
    bar.add(menu);
    bar.setMaximumSize(new Dimension(0, 100));
    bar.setMinimumSize(new Dimension(0, 1));
    bar.setPreferredSize(new Dimension(0, 1));
    add(bar);

    this.mainButton = mainButton;
    mainButton.addActionListener(mainButtonListener);
    mainButton.setBorder(new RightChoppedBorder(mainButton.getBorder(), 2));
    add(mainButton);

    enabledDownArrow = new SmallDownArrow();
    disDownArrow = new SmallDisabledDownArrow();
    dropDownButton = new JButton(disDownArrow);
    dropDownButton.setDisabledIcon(disDownArrow);
    dropDownButton.addMouseListener(new DropDownListener());
    dropDownButton.setMaximumSize(new Dimension(11, 100));
    dropDownButton.setMinimumSize(new Dimension(11, 10));
    dropDownButton.setPreferredSize(new Dimension(11, 10));
    dropDownButton.setFocusPainted(false);
    add(dropDownButton);

    setEnabled(false);
  }

  public DropDownButton() {
    this(new JButton());
  }

  public DropDownButton(Action a) {
    this(new JButton(a));
  }

  public DropDownButton(Icon icon) {
    this(new JButton(icon));
  }

  public DropDownButton(String text) {
    this(new JButton(text));
  }

  public DropDownButton(String t, Icon i) {
    this(new JButton(t, i));
  }

  public JButton getButton() {
    return mainButton;
  }

  // public JButton getDropDownButton() { return dropDownButton; }
  public JMenu getMenu() {
    return menu;
  }

  public void setEnabled(boolean enable) {
    mainButton.setEnabled(enable);
    dropDownButton.setEnabled(enable);
  }

  public boolean isEnabled() {
    return mainButton.isEnabled();
  }

  public boolean isEmpty() {
    return (menu.getItemCount() == 0);
  }

  /**
   * Set the behavior of the main button.
   *
   * @param enable if true, a click on the main button will trigger an actionPerformed() on the
   *     first item in the popup menu.
   */
  public void setRunFirstMenuOption(boolean enable) {
    mainButton.removeActionListener(mainButtonListener);
    mainRunsDefaultMenuOption = enable;
    setEnabled(mainRunsDefaultMenuOption == false || isEmpty() == false);
    if (mainRunsDefaultMenuOption) {
      mainButton.addActionListener(mainButtonListener);
    }
  }

  /**
   * @return true if a click on the main button will trigger an actionPerformed() on the first item
   *     in the popup menu.
   */
  public boolean getRunFirstMenuOption() {
    return mainRunsDefaultMenuOption;
  }

  private void setDropDownEnabled(boolean enable) {
    dropDownEnabled = enable;
    dropDownButton.setIcon(enable ? enabledDownArrow : disDownArrow);
    if (mainRunsDefaultMenuOption) {
      setEnabled(enable);
    }
  }

  /** This object responds to events on the main button. */
  private class MainButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (mainRunsDefaultMenuOption && !isEmpty()) {
        JMenuItem defaultItem = menu.getItem(0);
        if (defaultItem != null) {
          defaultItem.doClick(0);
        }
      }
    }
  }

  /** This object responds to events on the drop-down button. */
  private class DropDownListener extends MouseAdapter {
    boolean pressHidPopup = false;

    public void mouseClicked(MouseEvent e) {
      if (dropDownEnabled && !pressHidPopup) {
        menu.doClick(0);
      }
    }

    public void mousePressed(MouseEvent e) {
      if (dropDownEnabled) {
        menu.dispatchMouseEvent(e);
      }
      pressHidPopup = !menu.isPopupMenuVisible();
    }

    public void mouseReleased(MouseEvent e) {
      //
    }
  }

  /**
   * This object watches for insertion/deletion of menu items in the popup menu, and disables the
   * drop-down button when the popup menu becomes empty.
   */
  private class MenuContainerListener implements ContainerListener {
    public void componentAdded(ContainerEvent e) {
      setDropDownEnabled(true);
    }

    public void componentRemoved(ContainerEvent e) {
      setDropDownEnabled(!isEmpty());
    }
  }

  /**
   * An adapter that wraps a border object, and chops some number of pixels off the right hand side
   * of the border.
   */
  private static class RightChoppedBorder implements Border {
    private Border border;
    private int width;

    public RightChoppedBorder(Border border, int width) {
      this.border = border;
      this.width = width;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Shape clipping = g.getClip();
      g.setClip(x, y, width, height);
      border.paintBorder(c, g, x, y, width + this.width, height);
      g.setClip(clipping);
    }

    public Insets getBorderInsets(Component component) {
      Insets insets = border.getBorderInsets(component);
      return new Insets(insets.top, insets.left, insets.bottom, insets.right - this.width);
    }

    public boolean isBorderOpaque() {
      return border.isBorderOpaque();
    }
  }

  private static class DropDownMenu extends JMenu {

    private static final long serialVersionUID = 1L;

    public void dispatchMouseEvent(MouseEvent e) {
      processMouseEvent(e);
    }
  }

  /** An icon to draw a small downward-pointing arrow. */
  private static class SmallDownArrow implements Icon {

    Color arrowColor = Color.black;

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(arrowColor);
      g.drawLine(x, y, x + 4, y);
      g.drawLine(x + 1, y + 1, x + 3, y + 1);
      g.drawLine(x + 2, y + 2, x + 2, y + 2);
    }

    public int getIconWidth() {
      return 6;
    }

    public int getIconHeight() {
      return 4;
    }
  }

  /** An icon to draw a disabled small downward-pointing arrow. */
  private static class SmallDisabledDownArrow extends SmallDownArrow {

    public SmallDisabledDownArrow() {
      arrowColor = new Color(140, 140, 140);
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      super.paintIcon(c, g, x, y);
      g.setColor(Color.white);
      g.drawLine(x + 3, y + 2, x + 4, y + 1);
      g.drawLine(x + 3, y + 3, x + 5, y + 1);
    }
  }

  /*
    TODO: move to test

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setPreferredSize(new Dimension(300, 400));
    JButton b = new JButton("Test");
    b.addActionListener(
        (e) -> {
          System.out.println("hello");
        });
    DropDownButton ddb = new DropDownButton(b);
    ddb.getMenu().add(new JMenuItem("Nested 1"));
    ddb.getMenu().add(new JMenuItem("Nested 2"));
    f.getContentPane().setLayout(new FlowLayout());
    f.getContentPane().add(ddb);
    f.setVisible(true);
    f.pack();
  }
  */
}

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

package org.diylc.swing.plugins.toolbox;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.images.Icon;

/**
 * Factory that creates {@link JButton}s which display component type icons and instantiates the
 * component when clicked.
 *
 * @author Branislav Stojkovic
 */
public class ComponentButtonFactory {

  private static final Logger LOG = LogManager.getLogger(ComponentButtonFactory.class);

  public static final int MARGIN = 3;

  public static JButton create(
      final IPlugInPort plugInPort, final ComponentType componentType, final JPopupMenu menu) {

    LOG.trace("create(plugInPort, {}, popup)", componentType.getName());

    JButton button = DropDownButtonFactory.createDropDownButton(componentType.getIcon(), menu);
    button.setBorder(BorderFactory.createEmptyBorder(MARGIN + 1, MARGIN + 1, MARGIN, MARGIN));
    button.setToolTipText(
        String.format(
            "<html><b>%s</b><br>%s<br>Author: %s<br><br>"
                + "Right click to select all components of this type</html>",
            componentType.getName(), componentType.getDescription(), componentType.getAuthor()));
    button.addActionListener((e) -> plugInPort.setNewComponentTypeSlot(componentType, null, false));
    button.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
              Project project = plugInPort.currentProject();
              List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
              for (IDIYComponent<?> component : project.getComponents()) {
                if (componentType.getInstanceClass().equals(component.getClass())) {
                  newSelection.add(component);
                }
              }
              // Ctrl appends selection
              if (Utils.isMac() ? e.isControlDown() : e.isMetaDown()) {
                newSelection.addAll(project.getSelection());
              }
              project.clearSelection();
              project.setSelection(newSelection);
              plugInPort.setNewComponentTypeSlot(null, null, false);
              plugInPort.refresh();
            }
          }
        });

    button.addKeyListener(
        new KeyAdapter() {

          @Override
          public void keyPressed(KeyEvent e) {
            plugInPort.keyPressed(
                e.getKeyCode(),
                Utils.isMac() ? e.isControlDown() : e.isMetaDown(),
                e.isShiftDown(),
                e.isAltDown());
          }
        });
    return button;
  }

  private static final Pattern contributedPattern = Pattern.compile("^(.*)\\[(.*)\\]");

  public static JMenuItem createVariantItem(
      final IPlugInPort plugInPort, final Template variant, final ComponentType componentType) {

    String display = variant.getName();

    Matcher match = contributedPattern.matcher(display);
    if (match.find()) {
      String name = match.group(1);
      String owner = match.group(2);
      display = "<html>" + name + "<font color='gray'>[" + owner + "]</font></html>";
    }

    final JMenuItem item =
        new JMenuItem(display) {

          private static final long serialVersionUID = 1L;

          // Customize item size to fit the delete button
          public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 32, d.height);
          }
        };
    item.addActionListener(
        (e) -> plugInPort.setNewComponentTypeSlot(componentType, variant, false));

    boolean isDefault = variant.getName().equals(plugInPort.getDefaultVariant(componentType));

    // TODO! Do the two icons match the tooltip/action?
    JLabel label = new JLabel(isDefault ? Icon.PinGreen.icon() : Icon.PinGrey.icon());
    label.setToolTipText(isDefault ? "Remove default variant" : "Set default variant");
    label.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            // Hide the menu
            Container c = item.getParent();
            if (c != null && c instanceof JPopupMenu) {
              JPopupMenu m = (JPopupMenu) c;
              m.setVisible(false);
            }
            plugInPort.setDefaultVariant(componentType, variant.getName());
            e.consume();
          }
        });
    Border margin = new EmptyBorder(4, 0, 0, 0);
    label.setBorder(margin);
    item.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    item.add(label);

    label = new JLabel(Icon.Garbage.icon());
    label.setToolTipText("Delete variant");
    label.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            // Hide the menu
            Container c = item.getParent();
            if (c != null && c instanceof JPopupMenu) {
              JPopupMenu m = (JPopupMenu) c;
              m.setVisible(false);
            }
            int result =
                JOptionPane.showConfirmDialog(
                    SwingUtilities.getRoot(item),
                    "Are you sure you want to delete variant \"" + variant.getName() + "\"",
                    "Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
              return;
            }
            plugInPort.deleteVariant(componentType, variant.getName());
            e.consume();
          }
        });
    margin = new EmptyBorder(4, 2, 0, 0);
    label.setBorder(margin);
    item.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    item.add(label);
    return item;
  }
}

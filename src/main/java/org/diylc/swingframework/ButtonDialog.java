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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import org.diylc.App;

public abstract class ButtonDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  public static final String OK = "OK";
  public static final String CANCEL = "Cancel";
  public static final String windowClosingKey = "org.diylc.swingframework.dispatch:WINDOW_CLOSING";

  private JPanel containerPanel;
  private JPanel buttonPanel;
  private String[] buttonCaptions;
  private Map<String, JButton> buttonMap;
  private String selectedButtonCaption;

  public void closeWithEscape() {
    final JDialog dialog = this;
    Action dispatchClosing =
        new AbstractAction() {

          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent event) {
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
          }
        };
    JRootPane root = dialog.getRootPane();
    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(App.getKeyStroke("Cancel"), windowClosingKey);
    root.getActionMap().put(windowClosingKey, dispatchClosing);
  }

  public ButtonDialog(JFrame owner, String title, String[] buttonCaptions) {
    super(owner, title);

    setModal(true);
    setResizable(false);

    this.buttonCaptions = buttonCaptions;
    this.buttonMap = new HashMap<String, JButton>();

    closeWithEscape();
  }

  protected void layoutGui() {
    setContentPane(getContainerPanel());
    pack();
    setLocationRelativeTo(getParent());
  }

  public JButton getButton(String caption) {
    return buttonMap.get(caption);
  }

  public String getSelectedButtonCaption() {
    return selectedButtonCaption;
  }

  private JPanel getContainerPanel() {
    if (containerPanel == null) {
      containerPanel = new JPanel(new BorderLayout());
      containerPanel.add(getMainComponent(), BorderLayout.CENTER);
      containerPanel.add(getButtonPanel(), BorderLayout.SOUTH);
    }
    return containerPanel;
  }

  public JPanel getButtonPanel() {
    if (buttonPanel == null) {
      buttonPanel = new JPanel();
      final ButtonDialog thisDialog = this;
      for (String caption : buttonCaptions) {
        final String command = caption;
        JButton button = new JButton(caption);
        button.addActionListener(
            (e) -> {
              if (validateInput(command)) {
                selectedButtonCaption = command;
                thisDialog.setVisible(false);
              }
            });
        buttonPanel.add(button);
        buttonMap.put(command, button);
      }
      if (!buttonMap.isEmpty()) {
        getRootPane().setDefaultButton(buttonMap.get(buttonCaptions[0]));
      }
    }
    return buttonPanel;
  }

  protected abstract JComponent getMainComponent();

  protected boolean validateInput(String button) {
    return true;
  }
}

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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import org.diylc.DIYLC;

public class AboutDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  private JPanel mainPanel;
  private JPanel buttonPanel;
  private JEditorPane editorPane;

  private final String appName;
  private final Icon icon;
  private final String version;
  private final String author;
  private final URL url;
  private final String htmlContent;

  public AboutDialog(
      JFrame parent,
      String appName,
      Icon icon,
      String version,
      String author,
      URL url,
      String htmlContent) {
    super(parent, DIYLC.getString("menu.help.about"));
    this.appName = appName;
    this.icon = icon;
    this.version = version;
    this.author = author;
    this.url = url;
    this.htmlContent = htmlContent;

    setModal(true);
    setResizable(false);
    setLayout(new BorderLayout());
    add(getMainPanel(), BorderLayout.CENTER);
    add(getButtonPanel(), BorderLayout.SOUTH);

    setPreferredSize(new Dimension(320, 240));

    pack();
    setLocationRelativeTo(parent);
  }

  private JPanel getMainPanel() {
    if (mainPanel == null) {
      mainPanel = new JPanel(new GridBagLayout());
      mainPanel.setBackground(Color.white);
      mainPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.FIRST_LINE_START;
      gbc.fill = GridBagConstraints.NONE;
      gbc.insets = new Insets(10, 8, 10, 4);

      JLabel iconLabel = new JLabel(icon);

      gbc.gridheight = 2;
      gbc.weightx = 0;
      gbc.weighty = 0;
      mainPanel.add(iconLabel, gbc);

      JLabel appNameLabel = new JLabel(appName);
      appNameLabel.setForeground(Color.red.darker());
      appNameLabel.setFont(appNameLabel.getFont().deriveFont(18f).deriveFont(Font.BOLD));

      gbc.gridx = 1;
      gbc.gridy = 0;
      gbc.gridheight = 1;
      gbc.weightx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(10, 4, 0, 4);
      mainPanel.add(appNameLabel, gbc);

      JLabel versionLabel = new JLabel("version " + version);
      versionLabel.setForeground(Color.lightGray);
      versionLabel.setFont(versionLabel.getFont().deriveFont(Font.BOLD));

      gbc.gridy = 1;
      gbc.insets = new Insets(0, 4, 8, 4);
      mainPanel.add(versionLabel, gbc);

      JLabel authorLabel = new JLabel(author);

      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(1, 8, 1, 4);
      mainPanel.add(authorLabel, gbc);

      JLabel urlLabel = new LinkLabel(url);

      gbc.gridy = 3;
      mainPanel.add(urlLabel, gbc);

      // JLabel mailLabel = new LinkLabel("mailto:", mail);

      // gbc.gridy = 4;
      // mainPanel.add(mailLabel, gbc);

      String html =
          "<head><title>About</title>"
              + "<style type=\"text/css\">"
              + "body {font-family: sans-serif; font-size: 8.5px; } </style></head><body>"
              + htmlContent
              + "</body></html>";
      editorPane = new JEditorPane("text/html", html);
      editorPane.setEditable(false);
      editorPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

      JScrollPane scrollPane = new JScrollPane(editorPane);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setBorder(null);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.gridx = 0;
      gbc.gridy = 5;
      gbc.insets = new Insets(8, 4, 1, 4);
      mainPanel.add(scrollPane, gbc);
    }
    return mainPanel;
  }

  public JPanel getButtonPanel() {
    if (buttonPanel == null) {
      buttonPanel = new JPanel();
      buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));

      JButton closeButton = new JButton("Close");
      closeButton.addActionListener(
          new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              AboutDialog.this.setVisible(false);
            }
          });

      buttonPanel.add(closeButton);
    }
    return buttonPanel;
  }

  public void setEditorText(String text) {
    editorPane.setText(text);
    editorPane.setCaretPosition(0);
  }

  public void addAction(String key, String name, AbstractAction action) {
    mainPanel.getInputMap().put(KeyStroke.getKeyStroke(key), name);
    mainPanel.getActionMap().put(name, action);
  }
}

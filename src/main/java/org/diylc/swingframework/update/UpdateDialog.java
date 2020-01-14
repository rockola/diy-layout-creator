package org.diylc.swingframework.update;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.DIYLC;

public class UpdateDialog extends JDialog {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(UpdateDialog.class);

  private JEditorPane htmlLabel;

  private String htmlText;
  private String latestVersionUrl;

  public UpdateDialog(JComponent owner, String htmlText, String latestVersionUrl) {
    super(SwingUtilities.getWindowAncestor(owner));
    this.htmlText = htmlText;
    this.latestVersionUrl = latestVersionUrl;
    setTitle(latestVersionUrl == null ? "Version History" : "Update Details");

    setModal(true);

    JPanel holderPanel = new JPanel();
    holderPanel.setLayout(new BorderLayout());
    holderPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    holderPanel.add(
        new JLabel(
            latestVersionUrl == null
                ? "Most recent updates on this computer:"
                : "These updates are available for your computer:"),
        BorderLayout.NORTH);

    final JScrollPane scrollPane = new JScrollPane(getHtmlLabel());
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    holderPanel.add(scrollPane, BorderLayout.CENTER);
    holderPanel.add(createButtonPanel(), BorderLayout.SOUTH);

    setContentPane(holderPanel);

    setPreferredSize(new Dimension(480, 400));

    pack();
    setLocationRelativeTo(getOwner());

    SwingUtilities.invokeLater(
        new Runnable() {

          public void run() {
            scrollPane.getVerticalScrollBar().setValue(0);
          }
        });
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

    if (latestVersionUrl != null) {
      JButton downloadButton = new JButton("Download");
      downloadButton.addActionListener(
          new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              try {
                DIYLC.openURL(new URL(latestVersionUrl));
                UpdateDialog.this.setVisible(false);
              } catch (Exception e1) {
                JOptionPane.showMessageDialog(
                    UpdateDialog.this,
                    "Could not launch default browser. To download the latest version visit "
                        + latestVersionUrl);
                LOG.error("Could not launch default browser", e1);
              }
            }
          });
      buttonPanel.add(downloadButton);

      buttonPanel.add(Box.createHorizontalStrut(4));
    }

    JButton cancelButton = new JButton("Close");
    cancelButton.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            UpdateDialog.this.setVisible(false);
          }
        });
    buttonPanel.add(cancelButton);

    return buttonPanel;
  }

  public JEditorPane getHtmlLabel() {
    if (htmlLabel == null) {
      htmlLabel = new JEditorPane();
      htmlLabel.setEditable(false);
      htmlLabel.setContentType("text/html");
      htmlLabel.setText(htmlText);
      htmlLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    }
    return htmlLabel;
  }
}

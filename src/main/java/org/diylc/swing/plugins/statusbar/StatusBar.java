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
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.swing.plugins.statusbar;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.EdgedBalloonStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.announcements.AnnouncementProvider;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.update.UpdateChecker;
import org.diylc.appframework.update.Version;
import org.diylc.common.BadPositionException;
import org.diylc.common.ComponentType;
import org.diylc.common.Config;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.Message;
import org.diylc.core.IDIYComponent;
import org.diylc.images.Icon;
import org.diylc.swingframework.MemoryBar;
import org.diylc.swingframework.miscutils.PercentageListCellRenderer;
import org.diylc.swingframework.update.UpdateDialog;
import org.diylc.swingframework.update.UpdateLabel;

public class StatusBar extends JPanel implements IPlugIn {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(StatusBar.class);
  private static final Format sizeFormat = new DecimalFormat("0.##");

  private JComboBox zoomBox;
  private final UpdateLabel updateLabel;
  private JLabel announcementLabel;
  private JLabel recentChangesLabel;
  private MemoryBar memoryPanel;
  private JLabel statusLabel;
  private JLabel selectionSizeLabel;
  private JLabel positionLabel;
  private JLabel sizeLabel;
  private IPlugInPort plugInPort = App.ui().getPresenter();
  private AnnouncementProvider announcementProvider;
  private Point2D mousePositionIn;
  private Point2D mousePositionMm;

  // State variables
  private ComponentType componentSlot;
  private Point controlPointSlot;
  private Boolean forceInstantiate;
  private List<String> componentNamesUnderCursor;
  private List<String> selectedComponentNames;
  private List<String> stuckComponentNames;
  private String statusMessage;

  {
    recentChangesLabel =
        new JLabel(Icon.ScrollInformation.icon()) {

          private static final long serialVersionUID = 1L;

          @Override
          public Point getToolTipLocation(MouseEvent event) {
            return new Point(0, -16);
          }
        };
    recentChangesLabel.setToolTipText(getMsg("recent-changes-tooltip"));
    recentChangesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    recentChangesLabel.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            List<Version> updates = Version.getRecentUpdates();
            if (updates == null) {
              App.ui().info(getMsg("no-version-history"));
            } else {
              String html = UpdateChecker.createUpdateHtml(updates);
              UpdateDialog updateDialog =
                  new UpdateDialog(App.ui().getOwnerFrame().getRootPane(), html, null);
              updateDialog.setVisible(true);
            }
          }
        });
    updateLabel =
        new UpdateLabel(App.getVersionNumber(), Config.getUrl("update").toString()) {

          private static final long serialVersionUID = 1L;

          @Override
          public Point getToolTipLocation(MouseEvent event) {
            return new Point(0, -16);
          }
        };
    updateLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    zoomBox = new JComboBox(plugInPort.getAvailableZoomLevels());
    zoomBox.setSelectedItem(plugInPort.getZoomLevel());
    zoomBox.setFocusable(false);
    zoomBox.setRenderer(new PercentageListCellRenderer());
    zoomBox.addActionListener((e) -> plugInPort.setZoomLevel((Double) zoomBox.getSelectedItem()));
    announcementLabel =
        new JLabel(Icon.Megaphone.icon()) {

          private static final long serialVersionUID = 1L;

          @Override
          public Point getToolTipLocation(MouseEvent event) {
            return new Point(0, -16);
          }
        };
    announcementLabel.setToolTipText("Click to fetch the most recent public announcement");
    announcementLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    announcementLabel.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            App.ui()
                .executeBackgroundTask(
                    new ITask<String>() {

                      @Override
                      public String doInBackground() throws Exception {
                        return announcementProvider.getCurrentAnnouncements(true);
                      }

                      @Override
                      public void failed(Exception e) {
                        LOG.error("Error while fetching announcements", e);
                        App.ui().error(getMsg("failed-announcements"));
                      }

                      @Override
                      public void complete(String result) {
                        final String pa = getMsg("public-announcement");
                        if (result != null && result.length() > 0) {
                          App.ui().info(pa, result);
                          announcementProvider.dismissed();
                        } else {
                          App.ui().info(pa, getMsg("no-announcements"));
                        }
                      }
                    },
                    true);
          }
        });
    memoryPanel =
        new MemoryBar() {

          private static final long serialVersionUID = 1L;

          @Override
          public Point getToolTipLocation(MouseEvent event) {
            return new Point(0, -52);
          }
        };
    memoryPanel.start();
    statusLabel = new JLabel();
    statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    selectionSizeLabel = new JLabel();
    selectionSizeLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    positionLabel = new JLabel();
    positionLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    sizeLabel =
        new JLabel(Icon.Size.icon()) {

          private static final long serialVersionUID = 1L;

          @Override
          public Point getToolTipLocation(MouseEvent event) {
            return new Point(0, -16);
          }
        };
    sizeLabel.setFocusable(true);
    sizeLabel.setToolTipText(getMsg("selection-size-tooltip"));
    sizeLabel.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            Point2D[] sizes = plugInPort.calculateSelectionDimension();
            String text = refreshSelectionSize();
            if (text == null) {
              text = getMsg("empty-selection");
            }
            JOptionPane.showMessageDialog(
                SwingUtilities.getRootPane(StatusBar.this),
                text,
                "Selection Size",
                JOptionPane.INFORMATION_MESSAGE);
          }
        });
  }

  public String refreshSelectionSize() {
    Point2D[] sizes = plugInPort.calculateSelectionDimension();
    String text = null;
    if (sizes != null) {
      int nth = App.metric() ? 1 : 0;
      String format = App.metric() ? "%s x %s cm²" : "%s\" x %s\"";
      text =
          String.format(
              format, sizeFormat.format(sizes[nth].getX()), sizeFormat.format(sizes[nth].getY()));
    }
    final String sizeString = sizes == null ? "" : text;
    SwingUtilities.invokeLater(() -> selectionSizeLabel.setText(sizeString));
    return text;
  }

  private String getMsg(String name) {
    return Config.getString("message.statusbar." + name);
  }

  public StatusBar() {
    super();

    this.announcementProvider = new AnnouncementProvider();

    setLayout(new GridBagLayout());

    try {
      App.ui().injectGuiComponent(this, SwingUtilities.BOTTOM);
    } catch (BadPositionException e) {
      LOG.error("Could not install status bar", e);
    }

    App.ui()
        .executeBackgroundTask(
            new ITask<String>() {

              @Override
              public String doInBackground() throws Exception {
                Thread.sleep(1000);
                String announcements = announcementProvider.getCurrentAnnouncements(false);
                String update = updateLabel.getUpdateChecker().findNewVersionShort();

                if (update != null) {
                  String updateHtml =
                      String.format(Message.getHtml("statusbar.new-version.available"), update);
                  if (announcements == null || announcements.length() == 0) {
                    return "<html>" + updateHtml + "</html>";
                  }
                  announcements = announcements.replace("<html>", "<html>" + updateHtml + "<br>");
                }

                return announcements;
              }

              @Override
              public void failed(Exception e) {
                LOG.error("Error while fetching announcements", e);
              }

              @Override
              public void complete(String result) {
                if (result != null && result.length() > 0) {
                  new BalloonTip(
                      updateLabel,
                      result,
                      new EdgedBalloonStyle(
                          UIManager.getColor("ToolTip.background"),
                          UIManager.getColor("ToolTip.foreground")),
                      true);
                  announcementProvider.dismissed();
                }
              }
            },
            false);

    ConfigurationManager.addListener(
        Config.Flag.METRIC, (key, value) -> refreshPosition((Boolean) value));
    ConfigurationManager.addListener(
        Config.Flag.HIGHLIGHT_CONTINUITY_AREA, (key, value) -> refreshStatusText());
  }

  private void layoutComponents() {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0;
    add(statusLabel, gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    add(selectionSizeLabel, gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0;
    add(positionLabel, gbc);

    JPanel zoomPanel = new JPanel(new BorderLayout());
    zoomPanel.add(new JLabel(getMsg("zoom")), BorderLayout.WEST);
    zoomPanel.add(zoomBox, BorderLayout.CENTER);
    zoomPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0;
    add(zoomPanel, gbc);

    /*
    gbc.gridx++;
    gbc.insets = new Insets(0, 2, 0, 2);
    add(sizeLabel, gbc);
    */

    gbc.gridx++;
    add(announcementLabel, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(0, 0, 0, 0);
    add(updateLabel, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(0, 0, 0, 4);
    add(recentChangesLabel, gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = new Insets(0, 0, 0, 4);
    add(memoryPanel, gbc);

    gbc.gridx = 5;
    add(new JPanel(), gbc);
  }

  // IPlugIn

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;

    layoutComponents();
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(
        EventType.ZOOM_CHANGED,
        EventType.SLOT_CHANGED,
        EventType.AVAILABLE_CTRL_POINTS_CHANGED,
        EventType.SELECTION_CHANGED,
        EventType.STATUS_MESSAGE_CHANGED,
        EventType.MOUSE_MOVED);
  }

  private String blueFont(String string) {
    return "<font color='blue'>" + string + "</font>";
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    switch (eventType) {
      case ZOOM_CHANGED:
        if (!params[0].equals(zoomBox.getSelectedItem())) {
          final Double zoom = (Double) params[0];
          SwingUtilities.invokeLater(
              new Runnable() {

                @Override
                public void run() {
                  zoomBox.setSelectedItem(zoom);
                }
              });
        }
        break;
      case SELECTION_CHANGED:
        Collection<IDIYComponent<?>> selection = (Collection<IDIYComponent<?>>) params[0];
        Collection<String> componentNames = new HashSet<String>();
        for (IDIYComponent<?> component : selection) {
          componentNames.add(blueFont(component.getName()));
        }
        this.selectedComponentNames = new ArrayList<String>(componentNames);
        Collections.sort(this.selectedComponentNames);
        this.stuckComponentNames = new ArrayList<String>();
        Collection<IDIYComponent<?>> stuckComponents = (Collection<IDIYComponent<?>>) params[1];
        for (IDIYComponent<?> component : stuckComponents) {
          this.stuckComponentNames.add(blueFont(component.getName()));
        }
        this.stuckComponentNames.removeAll(this.selectedComponentNames);
        Collections.sort(this.stuckComponentNames);
        refreshStatusText();
        refreshSelectionSize();
        break;
      case SLOT_CHANGED:
        componentSlot = (ComponentType) params[0];
        controlPointSlot = (Point) params[1];
        forceInstantiate = params.length > 2 ? (Boolean) params[2] : null;
        refreshStatusText();
        break;
      case AVAILABLE_CTRL_POINTS_CHANGED:
        componentNamesUnderCursor = new ArrayList<String>();
        for (IDIYComponent<?> component : ((Map<IDIYComponent<?>, Integer>) params[0]).keySet()) {
          componentNamesUnderCursor.add(blueFont(component.getName()));
        }
        Collections.sort(componentNamesUnderCursor);
        refreshStatusText();
        break;
      case STATUS_MESSAGE_CHANGED:
        statusMessage = (String) params[0];
        refreshStatusText();
        break;
      case MOUSE_MOVED:
        mousePositionIn = (Point2D) params[1];
        mousePositionMm = (Point2D) params[2];
        refreshPosition(App.metric());
        break;
      default:
        LOG.error("processMessage() unknown eventType {}", eventType);
    }
  }

  private void refreshPosition(boolean metric) {
    Point2D mousePosition = metric ? mousePositionMm : mousePositionIn;
    String unit = metric ? " mm" : "\"";

    positionLabel.setText(
        mousePosition == null
            ? null
            : String.format(
                "X: %.2f%s Y: %.2f%s", mousePosition.getX(), unit, mousePosition.getY(), unit));
  }

  private void refreshStatusText() {
    String statusText = this.statusMessage;
    final int maxComponents = 15;
    final int maxStuck = 5;
    if (componentSlot == null) {
      if (componentNamesUnderCursor != null && !componentNamesUnderCursor.isEmpty()) {
        String formattedNames = Utils.toCommaString(componentNamesUnderCursor);
        statusText = "<html>Drag control point(s) of " + formattedNames + "</html>";
      } else if (selectedComponentNames != null && !selectedComponentNames.isEmpty()) {
        StringBuilder builder = new StringBuilder();
        builder.append(
            Utils.toCommaString(
                selectedComponentNames.subList(
                    0, Math.min(maxComponents, selectedComponentNames.size()))));
        if (selectedComponentNames.size() > maxComponents) {
          builder.append(" and " + (selectedComponentNames.size() - maxComponents) + " more");
        }
        if (!stuckComponentNames.isEmpty()) {
          builder.append(" (hold <b>Ctrl</b> and drag to detach from ");
          builder.append(
              Utils.toCommaString(
                  stuckComponentNames.subList(0, Math.min(maxStuck, stuckComponentNames.size()))));
          if (stuckComponentNames.size() > maxStuck) {
            builder.append(" and " + (stuckComponentNames.size() - maxStuck) + " more");
          }
          builder.append(")");
        }
        statusText = "<html>Selection: " + builder.toString() + "</html>";
      }
    } else {
      if (forceInstantiate != null && forceInstantiate) {
        statusText =
            "<html>Drag the mouse over the canvas to place a new "
                + blueFont(componentSlot.getName())
                + "</html>";
      } else {
        switch (componentSlot.getCreationMethod()) {
          case POINT_BY_POINT:
            String nth = controlPointSlot == null ? "first" : "second";
            statusText =
                "<html>Click on the canvas to set the "
                    + nth
                    + " control point of a new "
                    + blueFont(componentSlot.getName())
                    + " or press <b>Esc</b> to cancel</html>";
            break;
          case SINGLE_CLICK:
            statusText =
                "<html>Click on the canvas to create a new "
                    + blueFont(componentSlot.getName())
                    + " or press <b>Esc</b> to cancel</html>";
            break;
          default:
            LOG.error("refreshStatusText(): unknown creation method");
        }
      }
    }

    // override any other status with this when in highlight mode,
    // as we cannot do anything else
    if (App.highlightContinuityArea()) {
      statusText = getMsg("highlight-connected");
    }

    final String finalStatus = statusText;
    SwingUtilities.invokeLater(() -> statusLabel.setText(finalStatus));
  }
}

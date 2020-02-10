package org.diylc.swingframework.update;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import org.diylc.appframework.update.UpdateChecker;
import org.diylc.appframework.update.Version;
import org.diylc.appframework.update.VersionNumber;
import org.diylc.images.Icon;

public class UpdateLabel extends JLabel {

  private static final long serialVersionUID = 1L;

  private UpdateChecker updateChecker;
  private List<Version> updatedVersions;

  public UpdateLabel(final VersionNumber currentVersion, final String updateFileUrl) {
    super();
    updateChecker = new UpdateChecker(currentVersion, updateFileUrl);

    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    final UpdateLabel thisLabel = this;
    addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (updatedVersions != null && updatedVersions.size() > 0) {
            UpdateDialog updateDialog = new UpdateDialog(
                thisLabel,
                UpdateChecker.createUpdateHtml(updatedVersions),
                updatedVersions.get(0).getUrl());
            updateDialog.setVisible(true);
          } else {
            checkForUpdates();
          }
        }
      });
    checkForUpdates();
  }

  private void checkForUpdates() {
    SwingWorker<List<Version>, Void> worker = new SwingWorker<List<Version>, Void>() {

        @Override
        protected List<Version> doInBackground() throws Exception {
          return updateChecker.findNewVersions();
        }

        @Override
        protected void done() {
          Icon icon = Icon.LightBulbOff;
          String tooltipText = null;
          try {
            updatedVersions = get();
            if (updatedVersions.size() == 0) {
              // TODO get these strings from strings-<lang>.xml
              tooltipText = "No updates available, click to check again";
            } else {
              icon = Icon.LightBulbOn;
              tooltipText = "Updates are available, click to see details";
            }
          } catch (Exception e) {
            tooltipText = "Error occurred while searching for updates: " + e.getMessage();
            setCursor(Cursor.getDefaultCursor());
          }
          setIcon(icon.icon());
          setToolTipText(tooltipText);
        }
      };
    worker.execute();
  }

  public UpdateChecker getUpdateChecker() {
    return updateChecker;
  }
}

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
package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.DIYLC;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.images.Icon;
import org.diylc.plugins.cloud.model.CommentEntity;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.plugins.cloud.presenter.SearchSession;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISimpleView;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.components.HTMLTextArea;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.swing.plugins.cloud.view.CommentDialog;
import org.diylc.swing.plugins.cloud.view.UploadDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;

/**
 * Component that is capable of showing a list of {@link ProjectEntity}
 * objects. Can work in paging mode and pull one page of data at a time.
 *
 * @see {@link SearchSession}
 * @author Branislav Stojkovic
 */
public class ResultsScrollPanel extends JScrollPane {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LogManager.getLogger(ResultsScrollPanel.class);

  private JPanel resultsPanel;
  /**
   * This label goes at the end of the page. When it gets rendered we
   * know that we need to request another page.
   */
  private JLabel loadMoreLabel;

  private JLabel topLabel;
  private ISimpleView cloudUI;
  private IPlugInPort plugInPort;
  private int currentLocation;
  /** This flag tells us whether the loadMoreLabel should invoke a new data pull or not. */
  private boolean armed;

  private SearchSession searchSession;

  private boolean showEditControls;

  private static final String spinningResource;
  private static final ImageIcon spinning;

  static {
    spinningResource = "/org/diylc/images/spinning.gif";
    spinning = new ImageIcon(ResultsScrollPanel.class.getResource(spinningResource));
  }

  public ResultsScrollPanel(
      ISimpleView cloudUI,
      IPlugInPort plugInPort,
      SearchSession searchSession,
      boolean showEditControls) {
    super();
    this.cloudUI = cloudUI;
    this.plugInPort = plugInPort;
    this.searchSession = searchSession;
    this.showEditControls = showEditControls;

    this.getVerticalScrollBar().setUnitIncrement(16);
    this.setBorder(null);
    setViewportView(getResultsPanel());
  }

  public void clearPrevious() {
    currentLocation = 0;
    getResultsPanel().removeAll();
    armed = false;
  }

  public void startSearch(List<ProjectEntity> projects) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.gridx = 0;
    gbc.gridy = 10000;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100;
    gbc.weighty = 100;
    gbc.gridwidth = 3;
    resultsPanel.add(getLoadMoreLabel(), gbc);

    gbc.gridx = 3;
    gbc.gridwidth = 2;
    gbc.weightx = 0;
    gbc.insets = new Insets(0, 0, 0, 2);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.LINE_END;
    resultsPanel.add(getTopLabel(), gbc);

    addData(projects);
  }

  public void showNoMatches() {
    JLabel label =
        new JLabel(
            String.format(
                "<html><font size='4' color='#999999'>%s</font></html>",
                DIYLC.getString("cloud.search-no-match")));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    gbc.weighty = Integer.MAX_VALUE;
    getResultsPanel().add(label, gbc);
    getLoadMoreLabel().setText("");
    getLoadMoreLabel().setIcon(null);
    remove(getTopLabel());
  }

  public void addData(List<ProjectEntity> projects) {
    if (projects == null || projects.isEmpty() && currentLocation == 0) {
      showNoMatches();
    } else {
      final Point old = getViewport().getViewPosition();
      LOG.info("Adding {} projects to display.", projects.size());
      for (ProjectEntity project : projects) {
        addProjectToDisplay(project);
        this.currentLocation++;
      }
      getResultsPanel().invalidate();
      getResultsPanel().revalidate();

      // Reset scrollbar position, for some reason it jumps to the last added element
      SwingUtilities.invokeLater(
          new Runnable() {

            @Override
            public void run() {
              old.translate(0, -16);
              getViewport().setViewPosition(old);
              LOG.info("Paging mechanism is armed");
              armed = true;
            }
          });

      if (searchSession != null && searchSession.hasMoreData()) {
        getLoadMoreLabel().setText(DIYLC.getString("cloud.querying-for-more"));
        getLoadMoreLabel().setIcon(spinning);
      } else {
        getLoadMoreLabel().setText(DIYLC.getString("cloud.no-more-results"));
        getLoadMoreLabel().setIcon(null);
      }
    }
  }

  private JComponent addProjectToDisplay(final ProjectEntity project) {
    final JLabel thumbnailLabel = new JLabel(loadImage(project.getThumbnailUrl()));
    final JLabel nameLabel = new JLabel("<html><b>" + project.getName() + "</b></html>");
    nameLabel.setFont(nameLabel.getFont().deriveFont(12f));

    final JTextArea descriptionArea = new HTMLTextArea(project.getDescription());
    descriptionArea.setEditable(false);
    descriptionArea.setFont(thumbnailLabel.getFont());
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setFocusable(false);
    descriptionArea.setRequestFocusEnabled(false);
    DefaultCaret caret = (DefaultCaret) descriptionArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    descriptionArea.setEnabled(false);

    final JLabel commentLabel = new JLabel(Integer.toString(project.getCommentCount()),
                                           Icon.Messages.icon(),
                                           SwingConstants.LEFT);
    commentLabel.setToolTipText(DIYLC.getString("cloud.see-comments"));
    commentLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    commentLabel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            cloudUI.executeBackgroundTask(
                new ITask<List<CommentEntity>>() {

                  @Override
                  public List<CommentEntity> doInBackground() throws Exception {
                    return CloudPresenter.Instance.getComments(project.getId());
                  }

                  @Override
                  public void failed(Exception e) {
                    cloudUI.error(DIYLC.getString("cloud.file-not-opened"));
                  }

                  @Override
                  public void complete(List<CommentEntity> result) {
                    CommentDialog dialog = new CommentDialog(cloudUI, project, result);
                    dialog.setVisible(true);
                  }
                });
          }
        });

    final JLabel viewLabel = new JLabel(Integer.toString(project.getViewCount()),
                                        Icon.Eye.icon(),
                                        SwingConstants.LEFT);
    viewLabel.setToolTipText(DIYLC.getString("cloud.view-count"));

    final JLabel downloadLabel =
        new JLabel(
            Integer.toString(project.getDownloadCount()),
            Icon.Download.icon(),
            SwingConstants.LEFT);
    downloadLabel.setToolTipText("Download count");

    final JLabel categoryLabel =
        new JLabel(String.format("<html>Category: <b>%s</b></html>", project.getCategory()));
    final JLabel authorLabel =
        new JLabel(String.format("<html>Author: <b>%s</b></html>", project.getOwner()));
    final JLabel updatedLabel =
        new JLabel(String.format("<html>Last updated: <b>%s</b></html>", project.getUpdated()));

    final JLabel downloadButton = new JLabel(Icon.CloudDownload.icon());
    downloadButton.setFocusable(true);
    downloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    downloadButton.setToolTipText(DIYLC.getString("cloud.download-to-local-drive"));
    downloadButton.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            final File file =
                DialogFactory.getInstance()
                    .showSaveDialog(
                        (JFrame) cloudUI,
                        FileFilterEnum.DIY.getFilter(),
                        new File(project.getName() + Project.FILE_SUFFIX),
                        FileFilterEnum.DIY.getExtensions()[0],
                        null);
            if (file != null) {
              cloudUI.executeBackgroundTask(
                  new ITask<Void>() {

                    @Override
                    public Void doInBackground() throws Exception {
                      LOG.debug("Downloading project to " + file.getAbsolutePath());
                      URL website = new URL(project.getDownloadUrl());
                      ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                      FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                      fos.close();
                      return null;
                    }

                    @Override
                    public void complete(Void result) {
                      if (cloudUI.showConfirmDialog(
                              "Project downloaded to "
                                  + file.getAbsolutePath()
                                  + ".\nDo you want to open it?",
                              "Cloud",
                              IView.YES_NO_OPTION,
                              IView.INFORMATION_MESSAGE)
                          == IView.YES_OPTION) {
                        if (!plugInPort.allowFileAction()) {
                          return;
                        }

                        DIYLC
                            .ui()
                            .executeBackgroundTask(
                                new ITask<Void>() {

                                  @Override
                                  public Void doInBackground() throws Exception {
                                    LOG.debug("Opening from " + file.getAbsolutePath());
                                    plugInPort.loadProjectFromFile(file.getAbsolutePath());
                                    return null;
                                  }

                                  @Override
                                  public void complete(Void result) {
                                    DIYLC.ui().bringToFocus();
                                  }

                                  @Override
                                  public void failed(Exception e) {
                                    DIYLC.ui().error(DIYLC.getString("cloud.file-not-opened"), e);
                                  }
                                },
                                true);
                      }
                    }

                    @Override
                    public void failed(Exception e) {
                      DIYLC.ui().error(DIYLC.getString("cloud.file-not-saved"), e);
                    }
                  });
            }
          }
        });

    final JLabel spacerLabel = new JLabel();
    final JSeparator separator = new JSeparator();

    final JPanel buttonPanel = new JPanel(new FlowLayout());

    final JLabel editButton = new JLabel(Icon.CloudEdit.icon());
    editButton.setFocusable(true);
    editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    editButton.setToolTipText("Edit details without changing the project file");
    editButton.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            List<PropertyWrapper> projectProperties =
                CloudPresenter.Instance.getProjectProperties(project);
            PropertyEditorDialog editor =
                DialogFactory.getInstance()
                    .createPropertyEditorDialog(cloudUI.getOwnerFrame(),
                                                projectProperties,
                                                DIYLC.getString("cloud.edit-published-project"),
                                                true);
            editor.setVisible(true);
            if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
              // Update the project
              for (PropertyWrapper property : projectProperties) {
                try {
                  property.writeTo(project);
                } catch (Exception ex) {
                  LOG.error("Could not update the project", ex);
                  cloudUI.showMessage(
                      "Could not update the project. Detailed message is in the logs.",
                      "Error",
                      IView.ERROR_MESSAGE);
                  return;
                }
              }
              cloudUI.executeBackgroundTask(
                  new ITask<ProjectEntity>() {

                    @Override
                    public ProjectEntity doInBackground() throws Exception {
                      CloudPresenter.Instance.updateProjectDetails(
                          project, DIYLC.getVersionNumber().toString());
                      return CloudPresenter.Instance.fetchUserUploads(project.getId()).get(0);
                    }

                    @Override
                    public void failed(Exception e) {
                      cloudUI.showMessage(
                          "Could not update the project. Detailed message is in the logs.",
                          "Error",
                          IView.ERROR_MESSAGE);
                    }

                    @Override
                    public void complete(ProjectEntity result) {
                      nameLabel.setText("<html><b>" + project.getName() + "</b></html>");
                      descriptionArea.setText(result.getDescription());
                      categoryLabel.setText(
                          "<html>Category: <b>" + result.getCategory() + "</b></html>");
                      updatedLabel.setText(
                          "<html>Last updated: <b>" + result.getUpdated() + "</b></html>");
                      cloudUI.showMessage(
                          "The project has been updated successfully.",
                          "Upload Success",
                          IView.INFORMATION_MESSAGE);
                    }
                  });
            }
          }
        });

    final JLabel replaceButton = new JLabel(Icon.CloudUpload.icon());
    replaceButton.setFocusable(true);
    replaceButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    replaceButton.setToolTipText("Replace project with a new version");
    replaceButton.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (cloudUI.showConfirmDialog(String.format(DIYLC.getString("cloud.confirm-replace"),
                                                        project.getName()),
                                          "Replace Project",
                                          IView.YES_NO_OPTION,
                                          IView.QUESTION_MESSAGE) == IView.YES_OPTION) {
              final Presenter thumbnailPresenter = new Presenter();
              final File file =
                  DialogFactory.getInstance()
                      .showOpenDialog(
                          FileFilterEnum.DIY.getFilter(),
                          null,
                          FileFilterEnum.DIY.getExtensions()[0],
                          null,
                          cloudUI.getOwnerFrame());
              if (file != null) {
                LOG.info(
                    "Preparing replacement for project {} ({})",
                    project.getName(),
                    project.getId());
                cloudUI.executeBackgroundTask(
                    new ITask<String[]>() {

                      @Override
                      public String[] doInBackground() throws Exception {
                        LOG.debug("Uploading from {}", file.getAbsolutePath());
                        thumbnailPresenter.loadProjectFromFile(file.getAbsolutePath());
                        return CloudPresenter.Instance.getCategories();
                      }

                      @Override
                      public void complete(final String[] result) {
                        final UploadDialog dialog =
                            DialogFactory.getInstance()
                                .createUploadDialog(
                                    cloudUI.getOwnerFrame(), thumbnailPresenter, result, true);
                        dialog.setVisible(true);
                        if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
                          try {
                            final File thumbnailFile =
                                File.createTempFile("upload-thumbnail", ".png");
                            if (ImageIO.write(dialog.getThumbnail(), "png", thumbnailFile)) {
                              cloudUI.executeBackgroundTask(
                                  new ITask<ProjectEntity>() {

                                    @Override
                                    public ProjectEntity doInBackground() throws Exception {
                                      CloudPresenter.Instance.uploadProject(
                                          dialog.getName(),
                                          dialog.getCategory(),
                                          dialog.getDescription(),
                                          dialog.getKeywords(),
                                          DIYLC.getVersionNumber().toString(),
                                          thumbnailFile,
                                          file,
                                          project.getId());
                                      return CloudPresenter.Instance.fetchUserUploads(
                                              project.getId())
                                          .get(0);
                                    }

                                    @Override
                                    public void failed(Exception e) {
                                      cloudUI.error(DIYLC.getString("cloud.upload-error"), e);
                                    }

                                    @Override
                                    public void complete(ProjectEntity result) {
                                      nameLabel.setText(
                                          "<html><b>" + project.getName() + "</b></html>");
                                      descriptionArea.setText(result.getDescription());
                                      categoryLabel.setText(
                                          "<html>Category: <b>"
                                              + result.getCategory()
                                              + "</b></html>");
                                      updatedLabel.setText(
                                          "<html>Last updated: <b>"
                                              + result.getUpdated()
                                              + "</b></html>");
                                      thumbnailLabel.setIcon(new ImageIcon(dialog.getThumbnail()));
                                      cloudUI.info(DIYLC.getString("cloud.project-replaced"));
                                    }
                                  });
                            } else {
                              cloudUI.error(DIYLC.getString("cloud.temp-file-error"));
                              LOG.error(DIYLC.getString("cloud.temp-file-error"));
                            }
                          } catch (Exception e) {
                            cloudUI.error(DIYLC.getString("cloud.upload-error"), e);
                          }
                        }
                      }

                      @Override
                      public void failed(Exception e) {
                        cloudUI.error(DIYLC.getString("cloud.file-not-opened"), e);
                      }
                    });
              }
            }
          }
        });

    final JLabel deleteButton = new JLabel(Icon.CloudDelete.icon());
    deleteButton.setFocusable(true);
    deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    deleteButton.setToolTipText("Delete from the cloud");
    deleteButton.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            if (cloudUI.showConfirmDialog(
                    String.format(DIYLC.getString("cloud.confirm-delete"), project.getName()),
                    DIYLC.getString("cloud.delete-project"),
                    IView.YES_NO_OPTION,
                    IView.QUESTION_MESSAGE)
                == IView.YES_OPTION) {
              LOG.info("Deleting project {} ({})", project.getName(), project.getId());
              cloudUI.executeBackgroundTask(
                  new ITask<Void>() {

                    @Override
                    public Void doInBackground() throws Exception {
                      CloudPresenter.Instance.deleteProject(project.getId());
                      return null;
                    }

                    @Override
                    public void failed(Exception e) {
                      cloudUI.error(DIYLC.getString("delete-failed"));
                    }

                    @Override
                    public void complete(Void result) {
                      getResultsPanel().remove(thumbnailLabel);
                      getResultsPanel().remove(nameLabel);
                      getResultsPanel().remove(spacerLabel);
                      getResultsPanel().remove(commentLabel);
                      getResultsPanel().remove(viewLabel);
                      getResultsPanel().remove(downloadLabel);
                      getResultsPanel().remove(descriptionArea);
                      getResultsPanel().remove(categoryLabel);
                      getResultsPanel().remove(buttonPanel);
                      getResultsPanel().remove(authorLabel);
                      getResultsPanel().remove(updatedLabel);
                      getResultsPanel().remove(separator);
                    }
                  });
            }
          }
        });

    buttonPanel.setBackground(Color.white);
    buttonPanel.add(downloadButton);
    if (showEditControls) {
      buttonPanel.add(editButton);
      buttonPanel.add(replaceButton);
      buttonPanel.add(deleteButton);
    }

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridx = 0;
    gbc.gridy = currentLocation * 6;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridheight = 5;
    gbc.weightx = 0;
    getResultsPanel().add(thumbnailLabel, gbc);

    gbc.gridheight = 1;
    gbc.gridx++;
    gbc.weightx = 1000;
    gbc.insets = new Insets(2, 6, 2, 2);
    getResultsPanel().add(nameLabel, gbc);

    gbc.gridx++;
    gbc.weightx = 0.1;
    getResultsPanel().add(spacerLabel, gbc);

    gbc.gridx++;
    gbc.weightx = 0;
    getResultsPanel().add(commentLabel, gbc);

    gbc.gridx++;
    getResultsPanel().add(viewLabel, gbc);

    gbc.gridx++;
    getResultsPanel().add(downloadLabel, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    gbc.weighty = 1;
    gbc.weightx = 1;
    gbc.gridwidth = 6;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(2, 6, 2, 2);
    getResultsPanel().add(descriptionArea, gbc);

    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weighty = 0;
    getResultsPanel().add(categoryLabel, gbc);

    gbc.gridx++;
    gbc.gridheight = 3;
    gbc.gridwidth = 4;
    gbc.weightx = 0;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.anchor = GridBagConstraints.SOUTHEAST;
    getResultsPanel().add(buttonPanel, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    gbc.weightx = 1;
    gbc.gridheight = 1;
    gbc.insets = new Insets(2, 6, 2, 2);
    gbc.anchor = GridBagConstraints.NORTHWEST;
    getResultsPanel().add(authorLabel, gbc);

    gbc.gridy++;
    getResultsPanel().add(updatedLabel, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 6;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    gbc.insets = new Insets(2, 2, 2, 2);
    getResultsPanel().add(separator, gbc);

    return nameLabel;
  }

  public JLabel getTopLabel() {
    if (topLabel == null) {
      topLabel = new JLabel("<html><font color='blue'><u>To the top</u></html>");
      topLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      topLabel.addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              getViewport().setViewPosition(new Point(0, 0));
            }
          });
    }
    return topLabel;
  }

  private JLabel getLoadMoreLabel() {
    if (loadMoreLabel == null) {
      loadMoreLabel =
          new JLabel("Loading more data...") {

            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
              super.paint(g);
              if (armed && searchSession != null && searchSession.hasMoreData()) {
                // disarm immediately so we don't trigger
                // successive requests to the provider
                LOG.info("Paging mechanism is disarmed");
                armed = false;
                SwingWorker<List<ProjectEntity>, Void> worker =
                    new SwingWorker<List<ProjectEntity>, Void>() {

                      @Override
                      protected List<ProjectEntity> doInBackground() throws Exception {
                        return searchSession.requestMoreData();
                      }

                      @Override
                      protected void done() {
                        try {
                          List<ProjectEntity> newResults = get();
                          addData(newResults);
                        } catch (Exception e) {
                          cloudUI.error(DIYLC.getString("cloud.search-failed"));
                        }
                      }
                    };
                worker.execute();
              }
            }
          };
      loadMoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
      loadMoreLabel.setFont(loadMoreLabel.getFont().deriveFont(10f));
    }
    return loadMoreLabel;
  }

  private JPanel getResultsPanel() {
    if (resultsPanel == null) {
      resultsPanel = new JPanel(new GridBagLayout());
      resultsPanel.setBackground(Color.white);
    }
    return resultsPanel;
  }

  private javax.swing.Icon loadImage(String imageFile) {
    try {
      BufferedImage img = ImageIO.read(new File(imageFile));
      return new ImageIcon(img);
    } catch (Exception e) {
      return Icon.MissingImage.icon();
    }
  }
}

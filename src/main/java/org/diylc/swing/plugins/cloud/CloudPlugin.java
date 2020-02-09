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

package org.diylc.swing.plugins.cloud;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.common.Config;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.images.Icon;
import org.diylc.plugins.cloud.presenter.CloudException;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.cloud.view.ChangePasswordDialog;
import org.diylc.swing.plugins.cloud.view.LoginDialog;
import org.diylc.swing.plugins.cloud.view.UploadDialog;
import org.diylc.swing.plugins.cloud.view.UserEditDialog;
import org.diylc.swing.plugins.cloud.view.browser.CloudBrowserFrame;
import org.diylc.swing.plugins.cloud.view.browser.UploadManagerFrame;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;

public class CloudPlugin implements IPlugIn {

  private static final Logger LOG = LogManager.getLogger(CloudPlugin.class);

  private IPlugInPort plugInPort;
  private IPlugInPort thumbnailPresenter;

  private LibraryAction libraryAction;

  private LoginAction loginAction;
  private LogOutAction logOutAction;
  private CreateAccountAction createAccountAction;
  private ManageAccountAction manageAccountAction;

  private UploadAction uploadAction;
  private ChangePasswordAction changePasswordAction;
  private ManageProjectsAction manageProjectsAction;

  private CloudBrowserFrame cloudBrowser;

  private String getMsg(String key) {
    return Config.getString("message.cloud." + key);
  }

  private void menuEntry(AbstractAction action) {
    App.ui().injectMenuAction(action, getMsg("title"));
  }

  private void separator() {
    menuEntry(null);
  }

  public CloudPlugin() {
    super();

    this.thumbnailPresenter = new Presenter();

    menuEntry(getLibraryAction());
    separator();
    menuEntry(getLoginAction());
    menuEntry(getCreateAccountAction());
    separator();
    menuEntry(getUploadAction());
    menuEntry(getManageProjectsAction());
    separator();
    menuEntry(getManageAccountAction());
    menuEntry(getChangePasswordAction());
    menuEntry(getLogOutAction());

    // default state
    getUploadAction().setEnabled(false);
    getManageProjectsAction().setEnabled(false);
    getLogOutAction().setEnabled(false);
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;

    initialize();
  }

  private void initialize() {
    App.ui().executeBackgroundTask(
        new ITask<Boolean>() {

          @Override
          public Boolean doInBackground() throws Exception {
            return CloudPresenter.Instance.tryLoginWithToken();
          }

          @Override
          public void failed(Exception e) {
            LOG.error("Error while trying to login using token");
          }

          @Override
          public void complete(Boolean result) {
            try {
              if (result) {
                loggedIn();
              }
            } catch (Exception e) {
              LOG.error("Error while trying to login with token", e);
            }
          }
        },
        false);
  }

  public CloudBrowserFrame getCloudBrowser() {
    if (cloudBrowser == null) {
      cloudBrowser = new CloudBrowserFrame(plugInPort);
    }
    return cloudBrowser;
  }

  public UploadManagerFrame createUploadManagerFrame() {
    return new UploadManagerFrame(plugInPort);
  }

  public LibraryAction getLibraryAction() {
    if (libraryAction == null) {
      libraryAction = new LibraryAction();
    }
    return libraryAction;
  }

  public LoginAction getLoginAction() {
    if (loginAction == null) {
      loginAction = new LoginAction();
    }
    return loginAction;
  }

  public LogOutAction getLogOutAction() {
    if (logOutAction == null) {
      logOutAction = new LogOutAction();
    }
    return logOutAction;
  }

  public CreateAccountAction getCreateAccountAction() {
    if (createAccountAction == null) {
      createAccountAction = new CreateAccountAction();
    }
    return createAccountAction;
  }

  public ManageAccountAction getManageAccountAction() {
    if (manageAccountAction == null) {
      manageAccountAction = new ManageAccountAction();
    }
    return manageAccountAction;
  }

  public UploadAction getUploadAction() {
    if (uploadAction == null) {
      uploadAction = new UploadAction();
    }
    return uploadAction;
  }

  public ChangePasswordAction getChangePasswordAction() {
    if (changePasswordAction == null) {
      changePasswordAction = new ChangePasswordAction();
    }
    return changePasswordAction;
  }

  public ManageProjectsAction getManageProjectsAction() {
    if (manageProjectsAction == null) {
      manageProjectsAction = new ManageProjectsAction();
    }
    return manageProjectsAction;
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return null;
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}

  class LibraryAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LibraryAction() {
      super();
      putValue(AbstractAction.NAME, getMsg("search"));
      putValue(AbstractAction.SMALL_ICON, Icon.Cloud.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      getCloudBrowser().setVisible(true);
      getCloudBrowser().requestFocus();
    }
  }

  class LoginAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LoginAction() {
      super();
      putValue(AbstractAction.NAME, getMsg("log-in"));
      putValue(AbstractAction.SMALL_ICON, Icon.IdCard.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LoginDialog dialog = DialogFactory.getInstance().createLoginDialog();
      do {
        dialog.setVisible(true);
        if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
          try {
            if (CloudPresenter.Instance.logIn(dialog.getUserName(), dialog.getPassword())) {
              App.ui().info(getMsg("login-successful"), getMsg("successfully-logged-in"));
              loggedIn();
              break;
            } else {
              App.ui().error(getMsg("login-error"), getMsg("could-not-login"));
            }
          } catch (CloudException e1) {
            App.ui().error(getMsg("could-not-login"), e1);
          }
        } else {
          break;
        }
      } while (true);
    }
  }

  class LogOutAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LogOutAction() {
      super();
      putValue(AbstractAction.NAME, getMsg("log-out"));
      putValue(AbstractAction.SMALL_ICON, Icon.IdCard.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      CloudPresenter.Instance.logOut();
      loggedOut();
    }
  }

  class CreateAccountAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public CreateAccountAction() {
      super();
      putValue(AbstractAction.NAME, getMsg("create-new-account"));
      putValue(AbstractAction.SMALL_ICON, Icon.IdCardAdd.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final UserEditDialog dialog = DialogFactory.getInstance().createUserEditDialog(null);
      dialog.setVisible(true);
      if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

              @Override
              public Void doInBackground() throws Exception {
                CloudPresenter.Instance.createUserAccount(
                    dialog.getUserName(),
                    dialog.getPassword(),
                    dialog.getEmail(),
                    dialog.getWebsite(),
                    dialog.getBio());
                return null;
              }

              @Override
              public void failed(Exception e) {
                App.ui().error(getMsg("account-not-created"), e);
              }

              @Override
              public void complete(Void result) {
                App.ui().info(getMsg("account-created"));
              }
            },
            true);
      }
    }
  }

  class ManageAccountAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public ManageAccountAction() {
      super();
      putValue(AbstractAction.NAME, getMsg("manage-account"));
      putValue(AbstractAction.SMALL_ICON, Icon.IdCardEdit.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        final UserEditDialog dialog =
            DialogFactory.getInstance()
                .createUserEditDialog(CloudPresenter.Instance.getUserDetails());
        dialog.setVisible(true);
        if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
          App.ui().executeBackgroundTask(
              new ITask<Void>() {

                @Override
                public Void doInBackground() throws Exception {
                  CloudPresenter.Instance.updateUserDetails(
                      dialog.getEmail(), dialog.getWebsite(), dialog.getBio());
                  return null;
                }

                @Override
                public void failed(Exception e) {
                  App.ui().error(getMsg("account-not-updated"), e);
                }

                @Override
                public void complete(Void result) {
                  App.ui().info(getMsg("account-updated"));
                }
              },
              true);
        }
      } catch (CloudException e1) {
        App.ui().error(getMsg("could-not-connect"), e1);
      }
    }
  }

  class ChangePasswordAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public ChangePasswordAction() {
      super();
      putValue(AbstractAction.NAME, getMsg("change-password"));
      putValue(AbstractAction.SMALL_ICON, Icon.KeyEdit.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final ChangePasswordDialog dialog = DialogFactory.getInstance().createChangePasswordDialog();
      dialog.setVisible(true);
      if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

              @Override
              public Void doInBackground() throws Exception {
                CloudPresenter.Instance.updatePassword(
                    dialog.getOldPassword(), dialog.getNewPassword());
                return null;
              }

              @Override
              public void failed(Exception e) {
                App.ui().error(getMsg("password-update-failed"), e);
              }

              @Override
              public void complete(Void result) {
                App.ui().info(getMsg("password-updated"));
              }
            },
            true);
      }
    }
  }

  class UploadAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public UploadAction() {
      super();
      putValue(AbstractAction.NAME, getMsg("upload-project"));
      putValue(AbstractAction.SMALL_ICON, Icon.CloudUp.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("UploadAction triggered");

      final File[] files = DialogFactory.getInstance().showOpenMultiDialog(
          FileFilterEnum.DIY.getFilter(),
          null,
          FileFilterEnum.DIY.getExtensions()[0],
          null,
          App.ui().getOwnerFrame());
      if (files != null && files.length > 0) {
        List<ITask<String[]>> tasks = new ArrayList<ITask<String[]>>();
        final ListIterator<ITask<String[]>> taskIterator = tasks.listIterator();

        for (final File file : files) {
          taskIterator.add(
              new ITask<String[]>() {

                @Override
                public String[] doInBackground() throws Exception {
                  LOG.debug("Uploading from " + file.getAbsolutePath());
                  thumbnailPresenter.loadProject(file.getAbsolutePath());
                  return CloudPresenter.Instance.getCategories();
                }

                @Override
                public void complete(final String[] result) {
                  final UploadDialog dialog =
                      DialogFactory.getInstance()
                          .createUploadDialog(
                              App.ui().getOwnerFrame(), thumbnailPresenter, result, false);
                  dialog.setVisible(true);
                  if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
                    try {
                      final File thumbnailFile = File.createTempFile("upload-thumbnail", ".png");
                      if (ImageIO.write(dialog.getThumbnail(), "png", thumbnailFile)) {
                        App.ui().executeBackgroundTask(
                            new ITask<Void>() {

                              @Override
                              public Void doInBackground() throws Exception {
                                CloudPresenter.Instance.uploadProject(
                                    dialog.getName(),
                                    dialog.getCategory(),
                                    dialog.getDescription(),
                                    dialog.getKeywords(),
                                    App.getVersionNumber().toString(),
                                    thumbnailFile,
                                    file,
                                    null);
                                return null;
                              }

                              @Override
                              public void failed(Exception e) {
                                App.ui().error(getMsg("upload-error"), e);
                              }

                              @Override
                              public void complete(Void result) {
                                App.ui().info(getMsg("upload-success"),
                                              getMsg("project-uploaded"));
                                synchronized (taskIterator) {
                                  if (taskIterator.hasPrevious()) {
                                    App.ui().executeBackgroundTask(
                                        taskIterator.previous(),
                                        true);
                                  }
                                }
                              }
                            },
                            true);
                      } else {
                        App.ui().error(getMsg("upload-error"), getMsg("temp-file-error"));
                      }
                    } catch (Exception e) {
                      App.ui().error(getMsg("upload-error"), e);
                    }
                  }
                }

                @Override
                public void failed(Exception e) {
                  App.ui().error(getMsg("file-not-opened"), e);
                }
              });
        }

        synchronized (taskIterator) {
          if (taskIterator.hasPrevious()) {
            App.ui().executeBackgroundTask(taskIterator.previous(), true);
          }
        }
      }
    }
  }

  class ManageProjectsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public ManageProjectsAction() {
      super();
      putValue(AbstractAction.NAME, getMsg("manage-my-uploads"));
      putValue(AbstractAction.SMALL_ICON, Icon.CloudGear.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      UploadManagerFrame frame = createUploadManagerFrame();
      frame.setVisible(true);
    }
  }

  public void loggedIn() {
    getLoginAction().setEnabled(false);
    getCreateAccountAction().setEnabled(false);

    getLogOutAction().setEnabled(true);
    getManageAccountAction().setEnabled(true);
    getUploadAction().setEnabled(true);
    getManageProjectsAction().setEnabled(true);
  }

  public void loggedOut() {
    getLoginAction().setEnabled(true);
    getCreateAccountAction().setEnabled(true);

    getLogOutAction().setEnabled(false);
    getManageAccountAction().setEnabled(false);
    getUploadAction().setEnabled(false);
    getManageProjectsAction().setEnabled(false);
  }
}

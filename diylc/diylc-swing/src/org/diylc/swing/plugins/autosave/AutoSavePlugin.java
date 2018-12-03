/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.plugins.autosave;

import java.io.File;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.DummyView;

public class AutoSavePlugin implements IPlugIn {

  private static final String AUTO_SAVE_FILE_NAME = Utils.getUserDataDirectory("diylc") + "autoSave.diy";

  protected static final long autoSaveFrequency = 60 * 1000;

  private ExecutorService executor;

  private IPlugInPort plugInPort;
  private IView view;
  private long lastSave = 0;

  public AutoSavePlugin(IView view) {
    this.view = view;
    executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        boolean wasAbnormal = ConfigurationManager.getInstance().readBoolean(IPlugInPort.ABNORMAL_EXIT_KEY, false);
        Date lastHeartbeat = (Date) ConfigurationManager.getInstance().readObject(IPlugInPort.HEARTBEAT, new Date());
        long msSinceHeartbeat = new Date().getTime() - lastHeartbeat.getTime();
        File autoSaved = new File(AUTO_SAVE_FILE_NAME);
        if (autoSaved.exists())
          // try to figure out if another instance is running. Only pull auto-saved file if there's no recent heartbeat 
          if (wasAbnormal && msSinceHeartbeat > autoSaveFrequency) {
            IPlugInPort testPresenter = new Presenter(new DummyView());
            testPresenter.loadProjectFromFile(AUTO_SAVE_FILE_NAME);
            // Only prompt if there is something saved in the
            // auto-saved file.
            if (!testPresenter.getCurrentProject().getComponents().isEmpty()) {
              int decision =
                  view.showConfirmDialog(
                      "It appears that application was not closed normally in the previous session. Do you want to open the last auto-saved file?",
                      "Auto-Save", IView.YES_NO_OPTION, IView.QUESTION_MESSAGE);
              if (decision == IView.YES_OPTION) {
                AutoSavePlugin.this.plugInPort.loadProjectFromFile(AUTO_SAVE_FILE_NAME);
              }
            }
          } else
            autoSaved.delete();
        // Set abnormal flag to true, GUI side of the app must flip to
        // false when app closes regularly.
        ConfigurationManager.getInstance().writeValue(IPlugInPort.ABNORMAL_EXIT_KEY, true);
      }
    });
    // write heartbeat periodically
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          Thread.sleep(autoSaveFrequency);
        } catch (InterruptedException e) {
        }
        ConfigurationManager.getInstance().writeValue(IPlugInPort.HEARTBEAT, new Date());
      }
    }).start();
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.PROJECT_MODIFIED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    if (eventType == EventType.PROJECT_MODIFIED) {
      if (System.currentTimeMillis() - lastSave > autoSaveFrequency) {
        executor.execute(new Runnable() {

          @Override
          public void run() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            lastSave = System.currentTimeMillis();
            plugInPort.saveProjectToFile(AUTO_SAVE_FILE_NAME, true);
          }
        });
      }
    }
  }
}

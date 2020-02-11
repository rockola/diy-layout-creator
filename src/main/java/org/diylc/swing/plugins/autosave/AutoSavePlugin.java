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

package org.diylc.swing.plugins.autosave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.Project;

public class AutoSavePlugin implements IPlugIn {

  private static final Logger LOG = LogManager.getLogger(AutoSavePlugin.class);
  private static final String AUTO_SAVE_PATH = Utils.getUserDataDirectory() + "backup";

  public static final long BACKUP_FREQ_MS = 60 * 1000;
  public static final int MAX_TOTAL_SIZE_MB = 64;

  private ExecutorService executor;
  private IPlugInPort plugInPort;
  private long lastSave = 0;

  public AutoSavePlugin() {
    executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    File dir = new File(AUTO_SAVE_PATH);
    if (!dir.exists()) { // create the directory if needed
      boolean success = dir.mkdirs();
      if (!success) {
        String msg = String.format("Directories not created for path {}", AUTO_SAVE_PATH);
        LOG.error(msg); // throw new Exception(msg);
      }
    }
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.PROJECT_MODIFIED, EventType.PROJECT_LOADED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    switch (eventType) {
      case PROJECT_MODIFIED:
        if (System.currentTimeMillis() - lastSave > BACKUP_FREQ_MS) {
          executor.execute(
              new Runnable() {

                @Override
                public void run() {
                  Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

                  lastSave = System.currentTimeMillis();
                  String fileName = generateBackupFileName(plugInPort.getCurrentFileName());
                  plugInPort.saveProjectToFile(fileName, true);
                  cleanupExtra();
                }
              });
        }
        break;
      case PROJECT_LOADED:
        String fileName = (String) params[2];
        if (fileName != null) {
          String backupName = generateBackupFileName(fileName);
          try {
            copyFileUsingStream(new File(fileName), new File(backupName));
            LOG.info("Copied loaded file to {}", backupName);
          } catch (IOException e) {
            LOG.error("Could not copy the loaded file to backup", e);
          }
        }
        break;
      default:
    }
  }

  private static void copyFileUsingStream(File source, File dest) throws IOException {
    InputStream is = null;
    OutputStream os = null;
    try {
      is = new FileInputStream(source);
      os = new FileOutputStream(dest);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
    } finally {
      try {
        is.close();
      } catch (NullPointerException e) {
        LOG.error("copyFileUsingStream: Tried to close input stream but it was already null", e);
      }
      try {
        os.close();
      } catch (NullPointerException e) {
        LOG.error("copyFileUsingStream: Tried to close output stream but it was already null", e);
      }
    }
  }

  private String formatBackupFileName(String name, Date date, int nth) {
    return AUTO_SAVE_PATH
        + File.separator
        + name
        + "."
        + new SimpleDateFormat("yyyyMMdd-HHmmss").format(date)
        + (nth < 2 ? "" : "-" + nth)
        + Project.FILE_SUFFIX;
  }

  private String generateBackupFileName(String baseFileName) {
    if (baseFileName == null) {
      baseFileName = "Untitled";
    }
    File file = new File(baseFileName);
    String name = file.getName();

    // remove extension
    if (name.toLowerCase().endsWith(Project.FILE_SUFFIX)) {
      name = name.substring(0, name.length() - 4);
    }

    // append date and time
    Date date = new Date();
    file = new File(formatBackupFileName(name, date, 1));
    // make sure that it doesn't already exist
    int i = 1;
    String backupFileName = null;
    do {
      if (i > 999) {
        // 999 is a magic number pulled out of a hat
        // arbitrarily constraining us to <1000 backup files
        // by the same name in the same directory
        //
        // To be fixed if someone files a bug report
        LOG.error(
            "Could not generate backup file name from {} after {} tries, last one tried was {}",
            baseFileName,
            i,
            backupFileName);
        App.ui().requestBugReport(App.getString("project.autosave-failed"));
        return null;
      }
      backupFileName = formatBackupFileName(name, date, i);
      file = new File(backupFileName);
      i++;
    } while (file.exists());

    return file.getAbsolutePath();
  }

  private void cleanupExtra() {
    File[] files = new File(AUTO_SAVE_PATH).listFiles();
    if (files == null) {
      return;
    }
    // sort files by date
    Arrays.sort(
        files,
        new Comparator<File>() {

          @Override
          public int compare(File o1, File o2) {
            return Long.compare(o1.lastModified(), o2.lastModified());
          }
        });
    long totalSize = 0;
    long maxTotalSize = MAX_TOTAL_SIZE_MB * 1024 * 1024;
    for (File f : files) {
      totalSize += f.length();
    }
    int i = 0;
    while (i < files.length && totalSize > maxTotalSize) {
      totalSize -= files[i].length();
      LOG.info("Maximum backup size exceeded. Deleting old backup file {}", files[i].getName());
      boolean success = files[i].delete();
      if (!success) {
        LOG.error("Could not delete autosave file {} '{}'", i, files[i].getName());
      }
      i++;
    }
  }
}

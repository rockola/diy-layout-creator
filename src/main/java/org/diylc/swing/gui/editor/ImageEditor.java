/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

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

package org.diylc.swing.gui.editor;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JButton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.IOUtils;

import org.diylc.common.PropertyWrapper;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class ImageEditor extends JButton {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(ImageEditor.class);
  private static final String title = "Click to load image file";

  public ImageEditor(final PropertyWrapper property) {
    super(property.isUnique() ? title : "(multi value) " + title);
    addActionListener((e) -> {
        File file = DialogFactory.getInstance().showOpenDialog(
            FileFilterEnum.IMAGES.getFilter(),
            null,
            FileFilterEnum.IMAGES.getExtensions()[0],
            null);
        if (file != null) {
          try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = IOUtils.toByteArray(fis);
            property.setValue(byteArray);
            property.setChanged(true);
          } catch (FileNotFoundException e1) {
            LOG.error("File not found, path is " + file.getAbsolutePath(), e1);
          } catch (IOException e1) {
            LOG.error("IO exception for " + file.getAbsolutePath(), e1);
          }
        }
      });
  }
}

/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2020 held jointly by the individual authors.

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
package org.diylc.core;

import java.io.File;
import java.util.List;
import java.util.Set;
import org.diylc.common.PropertyWrapper;

/**
 * Base interface for the main GUI component.
 *
 * @author Branislav Stojkovic
 */
public interface IView {

  int ERROR_MESSAGE = 0;
  int INFORMATION_MESSAGE = 1;
  int WARNING_MESSAGE = 2;
  int QUESTION_MESSAGE = 3;
  int PLAIN_MESSAGE = -1;
  int DEFAULT_OPTION = -1;
  int YES_NO_OPTION = 0;
  int YES_NO_CANCEL_OPTION = 1;
  int OK_CANCEL_OPTION = 2;
  int YES_OPTION = 0;
  int NO_OPTION = 1;
  int CANCEL_OPTION = 2;
  int OK_OPTION = 0;

  String CHECK_BOX_MENU_ITEM = "org.diylc.checkBoxMenuItem";
  String RADIO_BUTTON_GROUP_KEY = "org.diylc.radioButtonGroup";

  void showMessage(String message, String title, int messageType);

  int showConfirmDialog(String message, String title, int optionType, int messageType);

  boolean editProperties(
      List<PropertyWrapper> properties, Set<PropertyWrapper> defaultedProperties);

  File promptFileSave();
}

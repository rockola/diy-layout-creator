/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2020 held jointly by the individual authors.

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

package org.diylc.images;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Icon {
  About,
  Add,
  App,
  AppMedium,
  AppSmall,
  ApplicationEdit,
  Arrow,
  BOM,
  Back,
  BlackBoard,
  BranchAdd,
  BriefcaseAdd,
  BriefcaseInto,
  Bug,
  CSV,
  Chest,
  Cloud,
  CloudBg,
  CloudBig,
  CloudDelete,
  CloudDownload,
  CloudEdit,
  CloudGear,
  CloudUp,
  CloudUpload,
  CloudWait,
  CoffeebeanEdit,
  Component,
  ComponentAdd,
  Copy,
  Cut,
  Dashboard,
  DataFind,
  Delete,
  DiskBlue,
  DocumentEdit,
  DocumentPlain,
  DocumentPlainYellow,
  DocumentsGear,
  Donate,
  Download,
  EditComponent,
  ElementInto,
  ElementsSelection,
  Error,
  Excel,
  Exit,
  Eye,
  Faq,
  Find,
  FitToSize,
  FlipHorizontal,
  FlipVertical,
  Folder,
  FolderOpen,
  Form,
  FormAdd,
  Front,
  Garbage,
  Gears,
  Group,
  Guitar,
  HTML,
  Hammer,
  Help,
  History,
  IconLarge,
  IconMedium,
  IconSmall,
  IdCard,
  IdCardAdd,
  IdCardEdit,
  Image,
  JarBeanInto,
  KeyEdit,
  LightBulbOff,
  LightBulbOn,
  MagicWand,
  Manual,
  Megaphone,
  Messages,
  MissingImage,
  MoveSmall,
  NavLeftBlue,
  NavRightBlue,
  NavigateCheck,
  Node,
  NotebookAdd,
  PDF,
  Paste,
  Pens,
  PhotoScenery,
  PinGreen,
  PinGrey,
  Plugin,
  Print,
  Redo,
  RotateCCW,
  RotateCW,
  SaveAs,
  Scientist,
  Screwdriver,
  ScrollInformation,
  Scroll_Center,
  Scroll_E,
  Scroll_N,
  Scroll_NE,
  Scroll_NW,
  Scroll_S,
  Scroll_SE,
  Scroll_SW,
  Scroll_W,
  SearchBox,
  Selection,
  Size,
  Sort,
  Spinning,
  Splash,
  SplashCeramic,
  SplashElectrolytic,
  SplashFilm,
  SplashResistor,
  StarBlue,
  StarGrey,
  TraceMask,
  Undo,
  Ungroup,
  Upload,
  Warning,
  Web,
  WindowColors,
  WindowGear,
  Wrench,
  ZoomSmall;

  private static final Logger LOG = LogManager.getLogger(Icon.class);
  private static final String PNG_SUFFIX = ".png";

  private static List<String> iconDirectories = new ArrayList<>();

  static {
    iconDirectories.add("/icons/MaterialDesign/18px/");
    iconDirectories.add("/icons/material.io/");
    iconDirectories.add("");
  }

  private ImageIcon imageIcon;

  private URL resourcePng() {
    String resourceID = "icon." + this.toString();
    URL resource = null;
    String r = null;
    for (String iconDirectory : iconDirectories) {
      r = iconDirectory + org.diylc.App.getString(resourceID) + PNG_SUFFIX;
      resource = Icon.class.getResource(r);
      if (resource != null) {
        break;
      }
      LOG.trace("Didn't find icon for {} in {}", resourceID, r);
    }
    if (resource == null) {
      LOG.error("{} not found in icons! Tried resource {}", this, resourceID);
    } else {
      LOG.debug("Asked for {}, found {} in icons", resourceID, r);
    }
    return resource;
  }

  public ImageIcon imageIcon() {
    if (imageIcon == null) {
      // this icon hasn't been requested yet, let's load it from
      // resources
      try {
        ImageIcon i = new ImageIcon(resourcePng());
        // let's store the icon for future reference
        LOG.debug("storing ImageIcon for {}", this.toString());
        imageIcon = i;
      } catch (NullPointerException e) {
        LOG.error("Cannot create ImageIcon for {}", this.toString());
        throw e;
      }
    }
    return imageIcon;
  }

  public javax.swing.Icon icon() {
    return imageIcon();
  }

  public Image image() {
    ImageIcon r = imageIcon();
    if (r == null) {
      LOG.error(this.toString() + ".image() not found");
      // TODO throw exception?
      return null;
    }
    return r.getImage();
  }
}

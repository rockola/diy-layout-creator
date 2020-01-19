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
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Icon {
  App,
  AppMedium,
  AppSmall,
  ApplicationEdit,
  Arrow,
  BranchAdd,
  BriefcaseInto,
  CloudBg,
  CloudDelete,
  CloudGear,
  CloudUpload,
  CloudWait,
  Component,
  Cut,
  DataFind,
  Delete,
  DocumentEdit,
  Donate,
  Error,
  Excel,
  Exit,
  FitToSize,
  FlipHorizontal,
  Form,
  FormAdd,
  Garbage,
  Guitar,
  History,
  IconSmall,
  IconMedium,
  IconLarge,
  IdCard,
  KeyEdit,
  LightBulbOff,
  MagicWand,
  Megaphone,
  Messages,
  MoveSmall,
  NavRightBlue,
  NavigateCheck,
  PhotoScenery,
  PinGreen,
  RotateCCW,
  Screwdriver,
  ScrollInformation,
  Selection,
  Size,
  Sort,
  Splash,
  SplashElectrolytic,
  SplashResistor,
  StarBlue,
  Ungroup,
  Upload,
  WindowColors,
  About,
  Add,
  BOM,
  Back,
  BlackBoard,
  BriefcaseAdd,
  Bug,
  CSV,
  Chest,
  Cloud,
  CloudBig,
  CloudDownload,
  CloudEdit,
  CloudUp,
  CoffeebeanEdit,
  ComponentAdd,
  Copy,
  Dashboard,
  DiskBlue,
  DocumentPlain,
  DocumentPlainYellow,
  DocumentsGear,
  Download,
  EditComponent,
  ElementInto,
  ElementsSelection,
  Eye,
  Faq,
  Find,
  FlipVertical,
  FolderOut,
  Front,
  Gears,
  Group,
  HTML,
  Hammer,
  Help,
  IdCardAdd,
  IdCardEdit,
  Image,
  JarBeanInto,
  LightBulbOn,
  Manual,
  MissingImage,
  NavLeftBlue,
  Node,
  NotebookAdd,
  PDF,
  Paste,
  Pens,
  PinGrey,
  Plugin,
  Print,
  Redo,
  RotateCW,
  SaveAs,
  Scientist,
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
  Spinning,
  SplashCeramic,
  SplashFilm,
  StarGrey,
  TraceMask,
  Undo,
  Warning,
  Web,
  WindowGear,
  Wrench,
  ZoomSmall;

  private static final Logger LOG = LogManager.getLogger(Icon.class);

  private static HashMap<Icon, ImageIcon> icons = new HashMap<>();

  private String resourcePNG() {
    String resourceID = "icon." + this.toString();
    String r = org.diylc.App.getString(resourceID) + ".png";
    if (r == null || r.isEmpty()) {
      LOG.error("{} not found in icons! Tried resource {}", this, resourceID);
    } else {
      LOG.debug("Asked for {}, looking for {} in icons...", resourceID, r);
    }
    return r;
  }

  public ImageIcon imageIcon() {
    ImageIcon i = icons.get(this);
    if (i == null) {
      // this icon hasn't been requested yet, let's load it from
      // resources
      try {
        i = new ImageIcon(Icon.class.getResource(resourcePNG()));
        // let's store the icon for future reference
        LOG.debug("storing ImageIcon for {}", this.toString());
        icons.put(this, i);
      } catch (NullPointerException e) {
        LOG.error("Cannot create ImageIcon for {}", this.toString());
        throw e;
      }
    }
    return i;
  }

  public javax.swing.Icon icon() {
    return imageIcon();
  }

  public Image image() {
    Image r = null;
    try {
      r = ImageIO.read(Icon.class.getResourceAsStream(resourcePNG()));
    } catch (IOException e) {
      LOG.error(this.toString() + ".image() failed", e);
    }
    return r;
  }
}

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
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.DIYLC;

public enum Icons {
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
    FormGreen,
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

    private String resourcePNG() {
	return DIYLC.getString("icon." + this.toString()) + ".png";
    }

    public ImageIcon imageIcon() {
	return new ImageIcon(Icons.class.getResource(resourcePNG()));
    }

    public Icon icon() {
	return imageIcon();
    }

    public Image image() {
	Image r = null;
	try {
	    r = ImageIO.read(Icons.class.getResourceAsStream(resourcePNG()));
	} catch (IOException e) {
	    LogManager.getLogger(Icons.class).error(this.toString() + ".image() failed", e);
	}
	return r;
    }
};

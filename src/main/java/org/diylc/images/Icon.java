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
import javax.swing.ImageIcon;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.common.Config;

public final class Icon {
    private Icon() { }

    private static String resourcePNG(String name) {
	return Config.getString("icon." + name) + ".png";
    }

    public static ImageIcon imageIcon(String name) {
	return new ImageIcon(Icon.class.getResource(resourcePNG(name)));
    }

    public static javax.swing.Icon icon(String name) {
	return imageIcon(name);
    }

    public static Image image(String name) {
	Image r = null;
	try {
	    r = ImageIO.read(Icon.class.getResourceAsStream(resourcePNG(name)));
	} catch (IOException e) {
	    LogManager.getLogger(Icon.class).error("image(" + name + ") failed", e);	    
	}
	return r;
    }

    public static ImageIcon imageIcon(Icons ic) { return imageIcon(ic.toString()); }
    public static javax.swing.Icon icon(Icons ic) { return icon(ic.toString()); }
    public static Image image(Icons ic) { return image(ic.toString()); }
}

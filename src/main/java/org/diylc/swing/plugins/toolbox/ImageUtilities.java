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

package org.diylc.swing.plugins.toolbox;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.net.URL;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.diylc.appframework.miscutils.Utils;

public final class ImageUtilities {
  static final String TOOLTIP_SEPAR = "<br>";

  private ImageUtilities() {}

  public static final Icon image2Icon(Image image) {
    if (image instanceof ToolTipImage) {
      return ((ToolTipImage) image).getIcon();
    }
    return new ImageIcon(image);
  }

  public static final Image icon2Image(Icon icon) {
    if (icon instanceof ImageIcon) {
      return ((ImageIcon) icon).getImage();
    }
    ToolTipImage image = new ToolTipImage("", icon.getIconWidth(), icon.getIconHeight(), 2);
    Graphics g = image.getGraphics();
    icon.paintIcon(new JLabel(), g, 0, 0);
    g.dispose();
    return image;
  }

  public static final String getImageToolTip(Image image) {
    if (image instanceof ToolTipImage) {
      return ((ToolTipImage) image).toolTipText;
    }
    return "";
  }

  public static Icon createDisabledIcon(Icon icon) {
    assert (icon != null);
    return new LazyDisabledIcon(ImageUtilities.icon2Image(icon));
  }

  public static Image createDisabledImage(Image image) {
    assert (image != null);
    return LazyDisabledIcon.createDisabledImage(image);
  }

  static final Image toBufferedImage(Image img) {
    new ImageIcon(img, "");
    if (img.getHeight(null) * img.getWidth(null) > 576) {
      return img;
    }
    BufferedImage rep = ImageUtilities.createBufferedImage(img.getWidth(null),
                                                           img.getHeight(null));
    Graphics2D g = rep.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
    img.flush();
    return rep;
  }

  static final BufferedImage createBufferedImage(int width, int height) {
    if (Utils.isMac()) {
      return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
    }
    ColorModel model = ImageUtilities.colorModel(Transparency.TRANSLUCENT);
    BufferedImage buffImage = new BufferedImage(
        model,
        model.createCompatibleWritableRaster(width, height),
        model.isAlphaPremultiplied(),
        null);
    return buffImage;
  }

  private static ColorModel colorModel(int transparency) {
    ColorModel model;
    try {
      model = GraphicsEnvironment
              .getLocalGraphicsEnvironment()
              .getDefaultScreenDevice()
              .getDefaultConfiguration()
              .getColorModel(transparency);
    } catch (HeadlessException he) {
      model = ColorModel.getRGBdefault();
    }
    return model;
  }

  static {
    ImageIO.setUseCache(false);
  }

  private static class DisabledButtonFilter extends RGBImageFilter {
    DisabledButtonFilter() {
      this.canFilterIndexColorModel = true;
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
      // TODO lots of magic numbers here - what is 8947848?
      return (rgb & -16777216)
          + 8947848
          + ((rgb >> 16 & 255) >> 2 << 16)
          + ((rgb >> 8 & 255) >> 2 << 8)
          + ((rgb & 255) >> 2);
    }

    public void setProperties(Hashtable<?, ?> props) {
      props = (Hashtable<?, ?>) props.clone();
      this.consumer.setProperties(props);
    }
  }

  private static class LazyDisabledIcon implements Icon {
    private static final RGBImageFilter DISABLED_BUTTON_FILTER = new DisabledButtonFilter();
    private Image img;
    private Icon disabledIcon;

    public LazyDisabledIcon(Image img) {
      assert (null != img);
      this.img = img;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      this.getDisabledIcon().paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
      return this.getDisabledIcon().getIconWidth();
    }

    @Override
    public int getIconHeight() {
      return this.getDisabledIcon().getIconHeight();
    }

    private synchronized Icon getDisabledIcon() {
      if (this.disabledIcon == null) {
        this.disabledIcon = new ImageIcon(LazyDisabledIcon.createDisabledImage(this.img));
      }
      return this.disabledIcon;
    }

    static Image createDisabledImage(Image img) {
      FilteredImageSource prod = new FilteredImageSource(img.getSource(), DISABLED_BUTTON_FILTER);
      return Toolkit.getDefaultToolkit().createImage(prod);
    }
  }

  private static class ToolTipImage extends BufferedImage implements Icon {
    final String toolTipText;
    ImageIcon imageIcon;
    final URL url;

    public ToolTipImage(
        String toolTipText,
        ColorModel cm,
        WritableRaster raster,
        boolean isRasterPremultiplied,
        Hashtable<?, ?> properties,
        URL url) {
      super(cm, raster, isRasterPremultiplied, properties);
      this.toolTipText = toolTipText;
      this.url = url;
    }

    public ToolTipImage(String toolTipText, int width, int height, int imageType) {
      super(width, height, imageType);
      this.toolTipText = toolTipText;
      this.url = null;
    }

    synchronized ImageIcon getIcon() {
      if (this.imageIcon == null) {
        this.imageIcon = new ImageIcon(this);
      }
      return this.imageIcon;
    }

    @Override
    public int getIconHeight() {
      return super.getHeight();
    }

    @Override
    public int getIconWidth() {
      return super.getWidth();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.drawImage(this, x, y, null);
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
      if ("url".equals(name)) {
        if (this.url != null) {
          return this.url;
        }
        return this.imageIcon.getImage().getProperty("url", observer);
      }
      return super.getProperty(name, observer);
    }
  }
}

/*
  DIY Layout Creator (DIYLC). Copyright (c) 2009-2019 held jointly by
  the individual authors.

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

package org.diylc.components.misc;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import javax.swing.ImageIcon;
import org.apache.poi.util.IOUtils;
import org.diylc.appframework.miscutils.IconImageConverter;
import org.diylc.common.Config;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PercentEditor;

@ComponentDescriptor(
    name = "Image",
    author = "Branislav Stojkovic",
    category = "Misc",
    description = "User defined image",
    instanceNamePrefix = "Img",
    zOrder = IDIYComponent.COMPONENT,
    flexibleZOrder = true,
    bomPolicy = BomPolicy.NEVER_SHOW)
public class Image extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;
  private static final ImageIcon ICON;
  private static final byte DEFAULT_SCALE = 25;

  public static final String DEFAULT_TEXT = Config.getString("components.double-click-to-edit");

  private Point point = new Point(0, 0);
  private byte[] data;
  private Byte scale;
  private byte newScale = DEFAULT_SCALE;
  @XStreamConverter(IconImageConverter.class)
  @Deprecated
  private ImageIcon image;

  static {
    String name = "/org/diylc/images/image-icon.png";
    java.net.URL imgURL = Image.class.getResource(name);
    if (imgURL != null) {
      ICON = new ImageIcon(imgURL, name);
    } else {
      byte[] iconData = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAK3RFWHRDcmVhdGlvbiBUaW1lAE1vIDI5IFNlcCAyMDAzIDE1OjE1OjUyICswMTAwvjqHlwAAAAd0SU1FB9YFFw0yKjOOuzAAAAAJcEhZcwAACvAAAArwAUKsNJgAAAAEZ0FNQQAAsY8L/GEFAAACMElEQVR42p1Tz2sTURD+3u4GN92aEKUVizWJYKSHGnKr8T8QQRDBm/ZP8NS7/0EF0YtCjx5qLz0JQgteRHptkZZWsM3GtptEbX7sZnffc+ZlY3LUDjv7fs7MN9/MExiIwPlEieXNZt8WMqWkoqWij0Yp+Qy8p5X3YtpWUt8J/ABLj28V6NKxRcbiyUIGv/0IQgloP9o19DzWa5HMBdIpgbWNGkd/RLpqKYrWJuP1HYnvX7cwV7kLFQdkFZNDSabqrzKScjGTIMQsqW0wJBYnbqA0O4Uv75cRuNvImV1kjA4yZo9Gmos2sqTkFYmNoX86R5pkTYra9VCtVtE43Eb0sw7HCHG1/gk5/wgTlsSk6AxSTIKyWJqUsI8jbwuV6TKUFaM4swCz10J57R6MbkOnc3LnOdwb9zVB4w50Cn2EeNlaxIvdJXjNPVwmAm8fvIVx6SJlehPIz+Hzt1eovKvAC1rEwaiOlmSahcB8KQ9f7uPZzlPsfWhjMTeDN/kSTGcCH5s1PDipAxGXxxySOEohIh9nQUD8pFEsTsOZcrBy4GJl14VjpdDphyCQIKiIdYuMccBljIiZU4Jmqh78KEKgQhSu2XBtH51TsvTpJnEMKkJIpVXjCJgQ50IWr8v7uqOZZT6W3FTEGHdCJAeoOd3JdI4AeCMHQbcXr2/WLDWEpiMkbS1Hra2SNeQPhGe/2FYOH9F10oekhWT9Lw+LjQ+5lfmyTXolGf9HmJnjYbRzP+c/c5s3Rp+EAfUAAAAASUVORK5CYII=");
      ICON = new ImageIcon(iconData);
    }
  }

  {
    image = new ImageIcon(ICON.getImage());
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  public Image() {
    try {
      data = IOUtils.toByteArray(Image.class.getResourceAsStream("/org/diylc/images/image.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    double s = 1d * getScale() / DEFAULT_SCALE;
    Shape clip = g2d.getClip().getBounds();
    if (!clip.intersects(
        new Rectangle2D.Double(
            point.getX(),
            point.getY(),
            getImage().getIconWidth() * s,
            getImage().getIconHeight() * s))) {
      return;
    }

    Composite oldComposite = setTransparency(g2d);
    g2d.scale(s, s);
    int x = (int) (point.x / s);
    int y = (int) (point.y / s);
    g2d.drawImage(getImage().getImage(), x, y, null);
    g2d.setComposite(oldComposite);

    if (isSelected()) {
      g2d.scale(1 / s, 1 / s);
      g2d.setColor(SELECTION_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.drawRect(
          point.x,
          point.y,
          (int) (getImage().getIconWidth() * s),
          (int) (getImage().getIconHeight() * s));
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.drawImage(ICON.getImage(), point.x, point.y, null);
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public Point getControlPoint(int index) {
    return point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.point.setLocation(point);
  }

  public ImageIcon getImage() {
    if (image != null) {
      /*
           REMOVED for use of sun.awt.image

           TODO: Figure out how - and at which stage - to handle old
           files containing images //ola 20191220

         // when loading old files, convert the stored image to
         // byte array and then then discard it, we won't be
         // needing it anymore

         BufferedImage bi = ((ToolkitImage) image.getImage()).getBufferedImage();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try {
      ImageIO.write(bi, "png", baos);
      // make it official
      data = baos.toByteArray();
         } catch (IOException e) {
         }
         // don't save back to the file
         */
    }

    return image;
  }

  @EditableProperty(name = "Image")
  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
    this.image = new ImageIcon(data);
  }

  @PercentEditor(_100PercentValue = 25)
  @EditableProperty(defaultable = false)
  public byte getScale() {
    if (scale != null) {
      newScale = (byte) (scale / 2);
      scale = null;
    }
    return newScale;
  }

  public void setScale(byte scale) {
    this.newScale = scale;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {
    //
  }
}

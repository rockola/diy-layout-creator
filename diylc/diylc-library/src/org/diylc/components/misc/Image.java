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
package org.diylc.components.misc;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

import org.diylc.appframework.miscutils.IconImageConverter;
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

import com.thoughtworks.xstream.annotations.XStreamConverter;

@ComponentDescriptor(name = "Image", author = "Branislav Stojkovic", category = "Misc",
    description = "User defined image", instanceNamePrefix = "Img", zOrder = IDIYComponent.COMPONENT,
    flexibleZOrder = true, stretchable = false, bomPolicy = BomPolicy.NEVER_SHOW)
public class Image extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;
  public static String DEFAULT_TEXT = "Double click to edit text";
  private static ImageIcon ICON;
  private static byte DEFAULT_SCALE = 50;

  static {
    String name = "image.png";
    java.net.URL imgURL = Image.class.getResource(name);
    if (imgURL != null) {
      ICON = new ImageIcon(imgURL, name);
    }
  }

  private Point point = new Point(0, 0);
  @XStreamConverter(IconImageConverter.class)
  private ImageIcon image = ICON;
  private byte scale = DEFAULT_SCALE;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    double s = 1d * scale / DEFAULT_SCALE;
    Shape clip = g2d.getClip().getBounds();
    if (!clip.intersects(new Rectangle2D.Double(point.getX(), point.getY(), image.getIconWidth() * s, image
        .getIconHeight() * s))) {
      return;
    }
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    g2d.scale(s, s);
    g2d.drawImage(image.getImage(), (int) (point.x / s), (int) (point.y / s), null);
    if (componentState == ComponentState.SELECTED) {
      g2d.setComposite(oldComposite);
      g2d.scale(1 / s, 1 / s);
      g2d.setColor(SELECTION_COLOR);
      g2d.drawRect(point.x, point.y, (int) (image.getIconWidth() * s), (int) (image.getIconHeight() * s));
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

  @EditableProperty(defaultable = false)
  public ImageIcon getImage() {
    return image;
  }

  public void setImage(ImageIcon image) {
    this.image = image;
  }

  @PercentEditor(_100PercentValue = 50)
  @EditableProperty(defaultable = false)
  public byte getScale() {
    return scale;
  }

  public void setScale(byte scale) {
    this.scale = scale;
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
  public void setValue(Void value) {}
}

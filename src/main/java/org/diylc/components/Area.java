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

package org.diylc.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import org.diylc.core.measures.Size;

public class Area extends java.awt.geom.Area {

  private static final long serialVersionUID = 1L;

  public static final int MAX_ALPHA = 127;

  private Color fillColor;
  private Color drawColor;

  public Area() {
    super();
  }

  public Area(Shape s) {
    super(s);
  }

  public Color getFillColor() {
    return fillColor;
  }

  public Color getDrawColor() {
    return drawColor;
  }

  public void setFillColor(Color fillColor) {
    this.fillColor = fillColor;
  }

  public void setDrawColor(Color drawColor) {
    this.drawColor = drawColor;
  }

  public Area subtract(Area area) {
    super.subtract(area);
    return this;
  }

  public static Point point(double x, double y) {
    return new Point((int) x, (int) y);
  }

  public static Point point(int x, double y) {
    return new Point(x, (int) y);
  }

  public static Point point(double x, int y) {
    return new Point((int) x, y);
  }

  public static Point point(int x, int y) {
    return new Point(x, y);
  }

  public static Point point(Point p) {
    return new Point(p);
  }

  public static Graphics2D rotate(Graphics2D g2d, double theta, Point center) {
    g2d.rotate(theta, center.x, center.y);
    return g2d;
  }

  public static Area roundedPolygon(Point[] points, double[] radii) {
    return new Area(new RoundedPolygon(points, radii));
  }

  /**
     Create an oval area (ellipse).

     @param center Circle center.
     @param diameterH Horizontal diameter.
     @param diameterV Vertical diameter.
     @return new Area
  */
  public static Area oval(Point center, double diameterH, double diameterV) {
    return oval(center.x, center.y, diameterH, diameterV);
  }

  public static Area oval(int x, int y, double diameterH, double diameterV) {
    return new Area(new Ellipse2D.Double(
        x - diameterH / 2,
        y - diameterV / 2,
        diameterH,
        diameterV));
  }

  /**
     Create a circular area (circle).

     @param centerX X coordinate of circle center.
     @param centerY Y coordinate of circle center.
     @param diameter Circle diameter.
     @return new Area
   */
  public static Area circle(double centerX, double centerY, double diameter) {
    return new Area(new Ellipse2D.Double(
        centerX - diameter / 2,
        centerY - diameter / 2,
        diameter,
        diameter));
  }

  /**
     Create a circular area (circle).

     @param center Circle center as a Point.
     @param diameter Circle diameter.
     @return new Area
   */
  public static Area circle(Point center, double diameter) {
    return circle(center.x, center.y, diameter);
  }

  public static Area circle(Point center, int diameter) {
    return circle(center.x, center.y, (double) diameter);
  }

  public static Area circle(Point center, Size diameter) {
    return circle(center.x, center.y, diameter.asPixels());
  }

  public static Area circle(Point2D center, double diameter) {
    return circle(center.getX(), center.getY(), diameter);
  }

  /**
   * Create a ring (circular area with a round hole in the middle).
   *
   * <p>Also known as a donut.
   *
   * @param centerX X coordinate of ring center.
   * @param centerY Y coordinate of ring center.
   * @param outerDiameter Outer diameter.
   * @param innerDiameter Inner diameter.
   * @return new Area
   */
  public static Area ring(
      double centerX, double centerY, double outerDiameter, double innerDiameter) {
    Area outer = circle(centerX, centerY, outerDiameter);
    // Area.subtract() could return the area for chaining, but it doesn't, so here we are
    outer.subtract(Area.circle(centerX, centerY, innerDiameter));
    return outer;
  }

  public static Area ring(Point center, double outerDiameter, double innerDiameter) {
    return ring(center.x, center.y, outerDiameter, innerDiameter);
  }

  /**
     Create a rounded rectangle.

     @param x The X coordinate of this rounded rectangle.
     @param y The Y coordinate of this rounded rectangle.
     @param width The width of this rounded rectangle.
     @param height The height of this rounded rectangle.
     @param cornerWidth The width of the arc that rounds off the corners.
     @param cornerHeight The height of the arc that rounds off the corners.
  */
  public static Area roundRect(
      double x, double y, double width, double height, double cornerWidth, double cornerHeight) {
    return new Area(new RoundRectangle2D.Double(
        x,
        y,
        width,
        height,
        cornerWidth,
        cornerHeight));
  }

  /**
   * Create a rounded rectangle with circular arc corners.
   *
   * <p>The width and height of the arc that rounds off the corners are equal.
   *
   * @param x The X coordinate of this rounded rectangle.
   * @param y The Y coordinate of this rounded rectangle.
   * @param width The width of this rounded rectangle.
   * @param height The height of this rounded rectangle.
   * @param corner The width and height of the arc that rounds off the corners.
  */
  public static Area roundRect(double x, double y, double width, double height, double corner) {
    return roundRect(x, y, width, height, corner, corner);
  }

  public static Area centeredRoundRect(
      double x, double y, double width, double height, double corner) {
    return roundRect(x - width / 2, y - height / 2, width, height, corner, corner);
  }

  public static Area centeredRoundRect(Point center, double width, double height, double corner) {
    return centeredRoundRect(center.x, center.y, width, height, corner);
  }

  public static Area centeredRoundRect(Point center, Size width, Size height, Size corner) {
    return centeredRoundRect(center, width.asPixels(), height.asPixels(), corner.asPixels());
  }

  /**
     Create a rectangle.

     @param x The X coordinate of this rectangle.
     @param y The Y coordinate of this rectangle.
     @param width The width of this rectangle.
     @param height The height of this rectangle.
  */
  public static Area rect(double x, double y, double width, double height) {
    return new Area(new Rectangle2D.Double(x, y, width, height));
  }

  public static Area rect(Point upperLeft, double width, double height) {
    return rect(upperLeft.x, upperLeft.y, width, height);
  }

  public static Area rect(Point upperLeft, Dimension dim) {
    return rect(upperLeft.x, upperLeft.y, dim.getWidth(), dim.getHeight());
  }

  /**
     Create a centered rectangle.

     @param x The X coordinate of the center of this rectangle.
     @param y The Y coordinate of the center this rectangle.
     @param width The width of this rectangle.
     @param height The height of this rectangle.
  */
  public static Area centeredRect(double x, double y, double width, double height) {
    return rect(x - width / 2, y - height / 2, width, height);
  }

  /**
     Create a centered rectangle.

     @param center The center of this rectangle as a Point.
     @param width The width of this rectangle.
     @param height The height of this rectangle.
  */
  public static Area centeredRect(Point center, double width, double height) {
    return rect(center.x - width / 2, center.y - height / 2, width, height);
  }

  /**
     Create a centered square.

     @param center The center of this rectangle as a Point.
     @param size The height and width of this square.
  */
  public static Area centeredSquare(Point center, double size) {
    return centeredRect(center, size, size);
  }

  public static Area centeredSquare(int x, int y, int size) {
    return centeredSquare(new Point(x, y), (double) size);
  }

  /**
     Create a rectangle with a round hole in the middle.

     @param x The X coordinate of this rectangle.
     @param y The Y coordinate of this rectangle.
     @param width The width of this rectangle.
     @param height The height of this rectangle.
     @param holeDiameter The diameter of the hole in this rectangle.
  */
  public static Area centeredRectWithRoundHole(
      double x, double y, double width, double height, double holeDiameter) {
    Area rect = centeredRect(x, y, width, height);
    rect.subtract(Area.circle(x, y, holeDiameter));
    return rect;
  }

  /**
   * Fill this area.
   *
   * @param g2d Graphics context.
   * @param fillColor Color for filling.
   */
  public void fill(Graphics2D g2d, Color fillColor) {
    g2d.setColor(this.fillColor != null ? this.fillColor : fillColor);
    g2d.fill(this);
  }

  /**
   * Draw the border of this area.
   *
   * @param g2d Graphics context.
   * @param borderColor Color for drawing.
   */
  public void draw(Graphics2D g2d, Color borderColor) {
    g2d.setColor(this.drawColor != null ? this.drawColor : borderColor);
    g2d.draw(this);
  }

  /**
   * Fill this area and draw a border around it.
   *
   * @param g2d Graphics context.
   * @param fillColor Color for filling.
   * @param borderColor Color for drawing the border.
   */
  public void fillDraw(Graphics2D g2d, Color fillColor, Color borderColor) {
    g2d.setColor(this.fillColor != null ? this.fillColor : fillColor);
    g2d.fill(this);
    g2d.setColor(this.drawColor != null ? this.drawColor : borderColor);
    g2d.draw(this);
  }

  /**
   * Fill this possibly transparent area and draw a border around it.
   *
   * @param g2d Graphics context.
   * @param fillColor Color for filling.
   * @param borderColor Color for drawing the border.
   * @param alpha Transparency 0 <= alpha <= MAX_ALPHA
   */
  public void fillDraw(Graphics2D g2d, Color fillColor, Color borderColor, int alpha) {
    final Composite oldComposite = g2d.getComposite();
    alpha = Math.max(0, Math.min(MAX_ALPHA, alpha));
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(fillColor);
    g2d.fill(this);
    g2d.setComposite(oldComposite);
    g2d.setColor(borderColor);
    g2d.draw(this);
  }



  /**
   * Draw or fill this area.
   *
   * @param g2d Graphics context.
   * @param draw Draw if true, fill if false.
   */
  public void drawOrFill(Graphics2D g2d, boolean draw) {
    if (draw) {
      g2d.draw(this);
    } else {
      g2d.fill(this);
    }
  }
}

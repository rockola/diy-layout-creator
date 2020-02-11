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
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.swingframework.ruler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;
import javax.swing.JComponent;
import org.diylc.App;
import org.diylc.components.AbstractComponent;

/**
 * {@link JComponent} that renders ruler. It features configurable units (cm or in), orientation and
 * ability to indicate cursor position.
 *
 * @author Branislav Stojkovic
 */
public class Ruler extends JComponent {

  private static final long serialVersionUID = 1L;

  public static final Color COLOR = Color.decode("#C0FF3E");
  public static final Color SELECTION_COLOR = AbstractComponent.SELECTION_COLOR;
  public static final Color CURSOR_COLOR = Color.blue;

  public static final int PIXELS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();
  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  public static final int SIZE = 18;

  public int orientation;
  private boolean metric;
  // private float increment;
  private double unitSize;
  private int indicatorValue = -1;
  private float ticksPerUnit;
  private Graphics bufferGraphics;
  private Image bufferImage;
  private GraphicsConfiguration screenGraphicsConfiguration;
  private double zoomLevel = 1d;
  private double cmSpacing;
  private double inSpacing;
  private double zeroLocation = 0;
  private Rectangle2D selectionRect = null;

  public Ruler(int orientation, boolean metric) {
    this(orientation, metric, 0, 0);
  }

  public Ruler(int orientation, boolean metric, double cmSpacing, double inSpacing) {
    this.orientation = orientation;
    this.metric = metric;
    this.cmSpacing = cmSpacing;
    this.inSpacing = inSpacing;
    setIncrementAndUnits();

    addComponentListener(
        new ComponentAdapter() {

          @Override
          public void componentResized(ComponentEvent e) {
            invalidateBuffer();
          }
        });
  }

  public boolean isHorizontal() {
    return orientation == HORIZONTAL;
  }

  protected void createBufferImage() {
    if (App.hardwareAcceleration()) {
      bufferImage =
          getScreenGraphicsConfiguration().createCompatibleVolatileImage(getWidth(), getHeight());
      ((VolatileImage) bufferImage).validate(screenGraphicsConfiguration);
    } else {
      bufferImage = createImage(getWidth(), getHeight());
    }

    bufferGraphics = bufferImage.getGraphics();
  }

  public void invalidateBuffer() {
    bufferImage = null;
  }

  public void setSelectionRect(Rectangle2D selectionRect) {
    this.selectionRect = selectionRect;
    repaint();
  }

  public void setZoomLevel(double zoomLevel) {
    this.zoomLevel = zoomLevel;
    setIncrementAndUnits();
    repaint();
  }

  public void setMetric(boolean metric) {
    this.metric = metric;
    setIncrementAndUnits();
    repaint();
  }

  public void setZeroLocation(double zeroLocation) {
    this.zeroLocation = zeroLocation;
    repaint();
  }

  /**
   * Changes cursor position. If less than zero, indicator will not be rendered. For horizontal
   * ruler this should be X coordinate of mouse position, and Y for vertical.
   *
   * @param indicatorValue
   */
  public void setIndicatorValue(int indicatorValue) {
    this.indicatorValue = indicatorValue;
  }

  private GraphicsConfiguration getScreenGraphicsConfiguration() {
    if (screenGraphicsConfiguration == null) {
      GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
      screenGraphicsConfiguration = devices[0].getDefaultConfiguration();
    }
    return screenGraphicsConfiguration;
  }

  private void setIncrementAndUnits() {
    // TODO ticksPerUnit should be user settable
    // also, ticksPerUnit for imperial was 10 (not 12)
    ticksPerUnit = isMetric() ? 4 : 12;
    unitSize =
        zoomLevel
            * (isMetric()
                ? (cmSpacing == 0 ? PIXELS_PER_INCH / 2.54f : cmSpacing)
                : (inSpacing == 0 ? PIXELS_PER_INCH : inSpacing));
  }

  public boolean isMetric() {
    return this.metric;
  }

  public void setPreferredHeight(int ph) {
    setPreferredSize(new Dimension(SIZE, ph));
  }

  public void setPreferredWidth(int pw) {
    setPreferredSize(new Dimension(pw, SIZE));
  }

  protected void paintComponent(Graphics g) {
    if (bufferImage == null) {
      createBufferImage();
    }
    if (bufferGraphics == null) {
      return;
    }
    Rectangle clipRect = g.getClipBounds();
    bufferGraphics.setColor(COLOR);
    bufferGraphics.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);

    // Do the ruler labels in a small font that's black.
    bufferGraphics.setFont(new Font("SansSerif", Font.PLAIN, 10));
    bufferGraphics.setColor(Color.black);

    // Use clipping bounds to calculate first and last tick locations.
    int reference = isHorizontal() ? clipRect.x : clipRect.y;
    int firstUnit = (int) Math.floor(reference / unitSize);
    double offset = Math.floor(zeroLocation * unitSize);
    double start = offset + Math.floor(reference / unitSize) * unitSize;
    int tickLength = 0;
    String text = null;
    double increment = unitSize / ticksPerUnit;
    int x = 0;
    int i = (int) -offset;
    int tickLimit = isHorizontal() ? clipRect.x + clipRect.width : clipRect.y + clipRect.height;

    // ticks and labels
    while (x < tickLimit) {
      if ((ticksPerUnit <= 1) || (i % Math.round(ticksPerUnit) == 0)) {
        tickLength = 10;
        text = Integer.toString(firstUnit + Math.round(i / ticksPerUnit));
      } else {
        tickLength = 7;
        if (isMetric()) {
          tickLength -= 2 * (i % Math.round(ticksPerUnit) % 2);
        } else if (i % Math.round(ticksPerUnit) != 5) {
          tickLength -= 2;
        }
        text = null;
      }

      x = (int) (start + i * increment);

      if (tickLength != 0) {
        int startX = isHorizontal() ? x : SIZE - 1;
        int startY = isHorizontal() ? SIZE - 1 : x;
        int endX = isHorizontal() ? startX : SIZE - tickLength - 1;
        int endY = isHorizontal() ? SIZE - tickLength - 1 : startY;

        bufferGraphics.drawLine(startX, startY, endX, endY);

        if (text != null) {
          int textX =
              isHorizontal()
                  ? startX + 2
                  : SIZE
                      - (int)
                          bufferGraphics
                              .getFontMetrics()
                              .getStringBounds(text, bufferGraphics)
                              .getWidth()
                      - 2;
          int textY = isHorizontal() ? 15 : startY + 10;

          bufferGraphics.drawString(text, textX, textY);
        }
      }
      i++;
    }

    // highlight value
    if (indicatorValue >= 0 && indicatorValue < (isHorizontal() ? getWidth() : getHeight())) {
      int startX = isHorizontal() ? indicatorValue : 0;
      int startY = isHorizontal() ? 0 : indicatorValue;
      int endX = isHorizontal() ? startX : SIZE - 1;
      int endY = isHorizontal() ? SIZE - 1 : startY;

      bufferGraphics.setColor(CURSOR_COLOR);
      bufferGraphics.drawLine(startX, startY, endX, endY);
    }

    // selection
    if (selectionRect != null) {
      int startX = isHorizontal() ? (int) selectionRect.getX() : 0;
      int startY = isHorizontal() ? 0 : (int) selectionRect.getY();
      int endX = isHorizontal() ? startX : SIZE - 1;
      int endY = isHorizontal() ? SIZE - 1 : startY;

      bufferGraphics.setColor(SELECTION_COLOR);
      bufferGraphics.drawLine(startX, startY, endX, endY);

      startX = isHorizontal() ? (int) (selectionRect.getX() + selectionRect.getWidth()) : 0;
      startY = isHorizontal() ? 0 : (int) (selectionRect.getY() + selectionRect.getHeight());
      endX = isHorizontal() ? startX : SIZE - 1;
      endY = isHorizontal() ? SIZE - 1 : startY;

      bufferGraphics.drawLine(startX, startY, endX, endY);
    }

    // lines
    bufferGraphics.setColor(Color.black);
    if (orientation == HORIZONTAL) {
      bufferGraphics.drawLine(0, SIZE - 1, getWidth(), SIZE - 1);
    } else {
      bufferGraphics.drawLine(SIZE - 1, 0, SIZE - 1, getHeight());
    }

    // Draw the buffer
    g.drawImage(bufferImage, 0, 0, this);
  }

  @Override
  public void update(Graphics g) {
    paint(g);
  }
}

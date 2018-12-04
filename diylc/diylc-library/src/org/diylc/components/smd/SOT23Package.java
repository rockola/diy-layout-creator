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
//package org.diylc.components.smd;
//
//import java.awt.AlphaComposite;
//import java.awt.Color;
//import java.awt.Composite;
//import java.awt.FontMetrics;
//import java.awt.Graphics2D;
//import java.awt.Point;
//import java.awt.Rectangle;
//import java.awt.geom.Area;
//import java.awt.geom.Rectangle2D;
//import java.awt.geom.RoundRectangle2D;
//
//import org.diylc.appframework.miscutils.ConfigurationManager;
//import org.diylc.common.Display;
//import org.diylc.common.IPlugInPort;
//import org.diylc.common.ObjectCache;
//import org.diylc.common.Orientation;
//import org.diylc.components.AbstractTransparentComponent;
//import org.diylc.core.ComponentState;
//import org.diylc.core.IDIYComponent;
//import org.diylc.core.IDrawingObserver;
//import org.diylc.core.Project;
//import org.diylc.core.Theme;
//import org.diylc.core.VisibilityPolicy;
//import org.diylc.core.annotations.ComponentDescriptor;
//import org.diylc.core.annotations.EditableProperty;
//import org.diylc.core.annotations.KeywordPolicy;
//import org.diylc.core.annotations.PositiveMeasureValidator;
//import org.diylc.core.measures.Size;
//import org.diylc.core.measures.SizeUnit;
//import org.diylc.utils.Constants;
//
//@ComponentDescriptor(name = "SOT-23 Case", author = "Branislav Stojkovic", category = "SMD", instanceNamePrefix = "C",
//    description = "Small outline transistor for SMD", stretchable = false, zOrder = IDIYComponent.COMPONENT,
//    keywordPolicy = KeywordPolicy.SHOW_VALUE)
//public class SOT23Package extends AbstractTransparentComponent<String> {
//
//  private static final long serialVersionUID = 1L;
//
//  public static Color BODY_COLOR = Color.gray;
//  public static Color BORDER_COLOR = Color.gray.darker();
//
//  public static Color PIN_COLOR = Color.decode("#00B2EE");
//  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
//  public static Color LABEL_COLOR = Color.white;
//  public static int EDGE_RADIUS = 4;
//  public static Size PIN_SIZE = new Size(0.8d, SizeUnit.mm);
//  
//  public static Size LENGTH = new Size(3d, SizeUnit.mm);
//  public static Size WIDTH = new Size(1.4d, SizeUnit.mm);
//  
//  public static Size PIN_LENGTH = new Size(0.6d, SizeUnit.mm);
//  public static Size PIN_WIDTH = new Size(0.4d, SizeUnit.mm);
//
//  protected String value;
//  private Orientation orientation = Orientation.DEFAULT;
//
//  private SOT23Variation variation = SOT23Variation._3;
//  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0)};
//  protected Display display = Display.NAME;
//  protected Color bodyColor = BODY_COLOR;
//  protected Color borderColor = BORDER_COLOR;
//  private Color labelColor = LABEL_COLOR;
//  transient private Area[] body;
//
//  public SOT23Package() {
//    super();
//    updateControlPoints();
//  }
//
//  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
//  public String getValue() {
//    return value;
//  }
//
//  public void setValue(String value) {
//    this.value = value;
//  }
//
//  @EditableProperty
//  public Orientation getOrientation() {
//    return orientation;
//  }
//
//  public void setOrientation(Orientation orientation) {
//    this.orientation = orientation;
//    updateControlPoints();
//    // Reset body shape.
//    body = null;
//  }
//
//  @EditableProperty
//  public Display getDisplay() {
//    if (display == null) {
//      display = Display.VALUE;
//    }
//    return display;
//  }
//
//  public void setDisplay(Display display) {
//    this.display = display;
//  }
//
//  @EditableProperty(name = "Contacts")
//  public SOT23Variation getVariation() {
//    return variation;
//  }
//
//  public void setVariation(SOT23Variation variation) {
//    this.variation = variation;
//    updateControlPoints();
//    // Reset body shape.
//    body = null;
//  }
//
//  @Override
//  public int getControlPointCount() {
//    return controlPoints.length;
//  }
//
//  @Override
//  public Point getControlPoint(int index) {
//    return controlPoints[index];
//  }
//
//  @Override
//  public boolean isControlPointSticky(int index) {
//    return true;
//  }
//
//  @Override
//  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
//    return VisibilityPolicy.NEVER;
//  }
//
//  @Override
//  public void setControlPoint(Point point, int index) {
//    controlPoints[index].setLocation(point);
//    body = null;
//  }
//
//  private void updateControlPoints() {
//    Point firstPoint = controlPoints[0];
//    controlPoints[0] = firstPoint;
//    int pinSize = (int) PIN_SIZE.convertToPixels();
//    int smdLength = (int) LENGTH.convertToPixels();
//    int pinSpacing = smdLength - pinSize;
//
//    // Update control points.
//    int dx1;
//    int dy1;
//    switch (orientation) {
//      case DEFAULT:
//        dx1 = 0;
//        dy1 = pinSpacing;
//        break;
//      case _90:
//        dx1 = -pinSpacing;
//        dy1 = 0;
//        break;
//      case _180:
//        dx1 = 0;
//        dy1 = -pinSpacing;
//        break;
//      case _270:
//        dx1 = pinSpacing;
//        dy1 = 0;
//        break;
//      default:
//        throw new RuntimeException("Unexpected orientation: " + orientation);
//    }
//    controlPoints[1] = new Point(firstPoint.x + dx1, firstPoint.y + dy1);
//  }
//
//  public Area[] getBody() {
//    if (body == null) {
//      body = new Area[2];
//      int x = controlPoints[0].x;
//      int y = controlPoints[0].y;      
//      int smdLength = (int) LENGTH.convertToPixels();
//      int smdWidth = (int) WIDTH.convertToPixels();
//      int pinSize = (int) PIN_SIZE.convertToPixels();
//      int width;
//      int height;
//
//      // create main body
//      switch (orientation) {
//        case DEFAULT:
//          width = smdWidth;
//          height = smdLength;
//          x = controlPoints[0].x - smdWidth / 2;
//          y = controlPoints[0].y - pinSize / 2;
//          break;
//        case _90:
//          width = smdLength;
//          height = smdWidth;
//          x = controlPoints[1].x - pinSize / 2;
//          y = controlPoints[1].y - smdWidth / 2;
//          break;
//        case _180:
//          width = smdWidth;
//          height = smdLength;
//          x = controlPoints[1].x - smdWidth / 2;
//          y = controlPoints[1].y - pinSize / 2;
//          break;
//        case _270:
//          width = smdLength;
//          height = smdWidth;
//          x = controlPoints[0].x - pinSize / 2;
//          y = controlPoints[0].y - smdWidth / 2;
//          break;
//        default:
//          throw new RuntimeException("Unexpected orientation: " + orientation);
//      }
//      Area mainArea = new Area(new RoundRectangle2D.Double(x, y, width, height, EDGE_RADIUS, EDGE_RADIUS));
//
//      // create contact area
//      Area contactArea = new Area();
//      if (width > height) {
//        contactArea.add(new Area(new Rectangle2D.Double(x, y, pinSize, height)));
//        contactArea.add(new Area(new Rectangle2D.Double(x + width - pinSize, y, pinSize, height)));
//      } else {
//        contactArea.add(new Area(new Rectangle2D.Double(x, y, width, pinSize)));
//        contactArea.add(new Area(new Rectangle2D.Double(x, y + height - pinSize, width, pinSize)));
//      }
//      contactArea.intersect(mainArea);
//
//      mainArea.subtract(contactArea);
//      body[0] = mainArea;
//      body[1] = contactArea;
//
//    }
//    return body;
//  }
//
//  @Override
//  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
//      IDrawingObserver drawingObserver) {
//    if (checkPointsClipped(g2d.getClip())) {
//      return;
//    }
//
//    // draw main area
//    Area mainArea = getBody()[0];
//    Composite oldComposite = g2d.getComposite();
//    if (alpha < MAX_ALPHA) {
//      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
//    }
//    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
//    g2d.fill(mainArea);
//    g2d.setComposite(oldComposite);
//
//    Color finalBorderColor;
//    if (outlineMode) {
//      Theme theme =
//          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
//      finalBorderColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
//              : theme.getOutlineColor();
//    } else {
//      finalBorderColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
//              : getBorderColor();
//    }
//    g2d.setColor(finalBorderColor);
//    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
//    g2d.draw(mainArea);
//
//
//    // draw contact area
//    Area contactArea = getBody()[1];
//    if (!outlineMode) {
//      g2d.setColor(PIN_COLOR);
//      if (alpha < MAX_ALPHA) {
//        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
//      }
//      g2d.fill(contactArea);
//      g2d.setComposite(oldComposite);
//    }
//
//    if (outlineMode) {
//      Theme theme =
//          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
//      finalBorderColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
//              : theme.getOutlineColor();
//    } else {
//      finalBorderColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
//              : PIN_BORDER_COLOR;
//    }
//    g2d.setColor(finalBorderColor);
//    g2d.draw(contactArea);
//
//    // Draw label.
//    g2d.setFont(LABEL_FONT);
//    Color finalLabelColor;
//    if (outlineMode) {
//      Theme theme =
//          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
//      finalLabelColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
//              : theme.getOutlineColor();
//    } else {
//      finalLabelColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
//              : getLabelColor();
//    }
//    g2d.setColor(finalLabelColor);
//    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
//    String label = "";
//    label = (getDisplay() == Display.NAME) ? getName() : getValue().toString();
//    if (getDisplay() == Display.NONE) {
//      label = "";
//    }
//    if (getDisplay() == Display.BOTH) {
//      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
//    }
//
//    Rectangle textTarget = mainArea.getBounds();
//    int length = textTarget.height > textTarget.width ? textTarget.height : textTarget.width;
//
//    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
//    int textHeight = (int) (rect.getHeight());
//    int textWidth = (int) (rect.getWidth());
//
//    do {
//      g2d.setFont(g2d.getFont().deriveFont(g2d.getFont().getSize2D() - 1));
//      fontMetrics = g2d.getFontMetrics(g2d.getFont());
//      rect = fontMetrics.getStringBounds(label, g2d);
//      textHeight = (int) (rect.getHeight());
//      textWidth = (int) (rect.getWidth());
//    } while (textWidth > length && g2d.getFont().getSize2D() > 2);
//
//    double centerX = textTarget.getX() + textTarget.getWidth() / 2;
//    double centerY = textTarget.getY() + textTarget.getHeight() / 2;
//    g2d.translate(centerX, centerY);
//
//    switch (orientation) {
//      case DEFAULT:
//        g2d.rotate(Math.PI / 2);
//        break;
//      case _90:
//        g2d.rotate(Math.PI);
//        break;
//      case _180:
//        g2d.rotate(Math.PI * 3 / 2);
//        break;
//      case _270:
//        break;
//      default:
//        throw new RuntimeException("Unexpected orientation: " + orientation);
//    }
//
//    // Center text horizontally and vertically
//    int x = -textWidth / 2;
//    int y = -textHeight / 2 + fontMetrics.getAscent() - 1;
//    g2d.drawString(label, x, y);
//  }
//
//  @Override
//  public void drawIcon(Graphics2D g2d, int width, int height) {
//    int radius = 4 * width / 32;
//    int contactSize = 4 * width / 32;
//    int thickness = getClosestOdd(width / 2);
//    g2d.rotate(Math.PI / 4, width / 2, height / 2);
//    RoundRectangle2D rect =
//        new RoundRectangle2D.Double((width - thickness) / 2, 4 * width / 32, thickness, height - 8 * width / 32,
//            radius, radius);
//    g2d.setColor(getBodyColor());
//    g2d.fill(rect);
//    g2d.setColor(getBorderColor());
//    g2d.draw(rect);
//    Area contactArea = new Area();
//    contactArea.add(new Area(new Rectangle2D.Double((width - thickness) / 2, 4 * width / 32, thickness, contactSize)));
//    contactArea.add(new Area(new Rectangle2D.Double((width - thickness) / 2, height - 8 * width / 32, thickness,
//        contactSize)));
//    contactArea.intersect(new Area(rect));
//    g2d.setColor(PIN_COLOR);
//    g2d.fill(contactArea);
//  }
//
//  @EditableProperty(name = "Body")
//  public Color getBodyColor() {
//    return bodyColor;
//  }
//
//  public void setBodyColor(Color bodyColor) {
//    this.bodyColor = bodyColor;
//  }
//
//  @EditableProperty(name = "Border")
//  public Color getBorderColor() {
//    return borderColor;
//  }
//
//  public void setBorderColor(Color borderColor) {
//    this.borderColor = borderColor;
//  }
//
//  @EditableProperty(name = "Label")
//  public Color getLabelColor() {
//    if (labelColor == null) {
//      labelColor = LABEL_COLOR;
//    }
//    return labelColor;
//  }
//
//  public void setLabelColor(Color labelColor) {
//    this.labelColor = labelColor;
//  }
//
//  public static enum SOT23Variation {
//
//    _3, _5, _6, _8;
//
//    @Override
//    public String toString() {
//      return name().replace("_", "");
//    }
//
//    public int getValue() {
//      return Integer.parseInt(toString());
//    }
//  }
//}

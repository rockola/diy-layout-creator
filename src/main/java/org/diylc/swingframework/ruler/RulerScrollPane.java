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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.diylc.images.Icon;
import org.diylc.swingframework.IDrawingProvider;

/**
 * Improved version of {@link JScrollPane} that features:
 *
 * <ul>
 *   <li>horizontal and vertical rulers with cursor position indicators
 *   <li>button in the top-left corner to choose ruler units
 *   <li>button in the bottom-right corner to navigate through the viewport
 * </ul>
 *
 * <p>Note: when doing drag'n'drop in the viewport component, sometimes Java doesn't fire <code>
 *  mouseDragged</code> events. To overcome this, component can notify about drag movements by
 * firing property change events. Property name is "dragPoint" and x, y coordinates are stored as
 * oldValue and newValue respectively.
 *
 * <p>TODO: something more elegant.
 *
 * @author Branislav Stojkovic
 */
public class RulerScrollPane extends JScrollPane {

  private static final long serialVersionUID = 1L;

  private static double MOUSE_SCROLL_SPEED = 0.8;

  private Ruler horizontalRuler;
  private Ruler verticalRuler;
  private JButton unitButton;
  private JButton navigateButton;
  private Corner topRightCorner;
  private Corner bottomLeftCorner;
  private List<IRulerListener> listeners;
  private boolean mouseScrollMode = false;
  private Point mouseScrollPrevLocation = null;

  public RulerScrollPane(Component view) {
    this(view, new ComponentThumbnailProvider(view));
  }

  public RulerScrollPane(Component view, final IDrawingProvider provider) {
    this(view, provider, 0, 0);
  }

  public RulerScrollPane(
      final Component view, final IDrawingProvider provider, double cmSpacing, double inSpacing) {
    super(view);

    horizontalRuler = new Ruler(Ruler.HORIZONTAL, true, cmSpacing, inSpacing);
    verticalRuler = new Ruler(Ruler.VERTICAL, true, cmSpacing, inSpacing);
    setColumnHeaderView(horizontalRuler);
    setRowHeaderView(verticalRuler);

    listeners = new ArrayList<IRulerListener>();

    view.addMouseListener(
        new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            if (mouseScrollMode) {
              mouseScrollMode = false;
              view.setCursor(Cursor.getDefaultCursor());
              e.consume();
            } else if (e.getButton() == MouseEvent.BUTTON2) {
              mouseScrollPrevLocation = null;
              mouseScrollMode = true;
              view.setCursor(CursorLoader.ScrollCenter.getCursor());
              e.consume();
            }
          }
        });

    view.addMouseMotionListener(
        new MouseAdapter() {

          @Override
          public void mouseMoved(MouseEvent e) {
            if (mouseScrollMode) {
              if (mouseScrollPrevLocation != null) {
                int dx = (int) ((e.getPoint().x - mouseScrollPrevLocation.x) * MOUSE_SCROLL_SPEED);
                int dy = (int) ((e.getPoint().y - mouseScrollPrevLocation.y) * MOUSE_SCROLL_SPEED);
                getHorizontalScrollBar().setValue(getHorizontalScrollBar().getValue() + dx);
                getVerticalScrollBar().setValue(getVerticalScrollBar().getValue() + dy);

                if (Math.abs(dx) > 2 * Math.abs(dy)) {
                  view.setCursor(
                      dx > 0 ? CursorLoader.ScrollE.getCursor() : CursorLoader.ScrollW.getCursor());
                } else if (Math.abs(dy) > 2 * Math.abs(dx)) {
                  view.setCursor(
                      dy > 0 ? CursorLoader.ScrollS.getCursor() : CursorLoader.ScrollN.getCursor());
                } else if (dx > 0 && dy > 0) {
                  view.setCursor(CursorLoader.ScrollSE.getCursor());
                } else if (dx > 0 && dy < 0) {
                  view.setCursor(CursorLoader.ScrollNE.getCursor());
                } else if (dx < 0 && dy < 0) {
                  view.setCursor(CursorLoader.ScrollNW.getCursor());
                } else if (dx < 0 && dy > 0) {
                  view.setCursor(CursorLoader.ScrollSW.getCursor());
                }
                // else view.setCursor(CursorLoader.ScrollCenter.getCursor());
              }
              mouseScrollPrevLocation = e.getPoint();
              e.consume();
            }
          }
        });

    view.addComponentListener(
        new ComponentAdapter() {

          @Override
          public void componentResized(ComponentEvent e) {
            updateRulerSize(e.getComponent().getWidth(), e.getComponent().getHeight());
          }
        });
    updateRulerSize(view.getWidth(), view.getHeight());

    unitButton = new JButton("cm");
    unitButton.setToolTipText("Toggle metric/imperial system");
    unitButton.setMargin(new Insets(0, 0, 0, 0));
    unitButton.setFont(unitButton.getFont().deriveFont(9f));
    unitButton.setFocusable(false);
    unitButton.addActionListener(
        (e) -> {
          boolean flip = !horizontalRuler.isMetric();
          horizontalRuler.setMetric(flip);
          verticalRuler.setMetric(flip);
          unitButton.setText(flip ? "in" : "cm");
          for (IRulerListener listener : listeners) {
            listener.unitsChanged(horizontalRuler.isMetric());
          }
        });

    navigateButton = new JButton(Icon.MoveSmall.icon());
    navigateButton.setToolTipText("Auto-scroll");
    navigateButton.setFocusable(false);
    navigateButton.setMargin(new Insets(0, 0, 0, 0));
    final RulerScrollPane thisPane = this;
    navigateButton.addActionListener(
        (e) -> {
          NavigateDialog navigateDialog = new NavigateDialog(thisPane, provider);
          navigateDialog.setVisible(true);
          navigateDialog.setLocationRelativeTo(navigateButton);
        });

    topRightCorner = new Corner(Ruler.HORIZONTAL);
    bottomLeftCorner = new Corner(Ruler.VERTICAL);

    setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, unitButton);
    setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, topRightCorner);
    setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, bottomLeftCorner);
    setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, navigateButton);

    view.addPropertyChangeListener(
        (evt) -> {
          if (evt.getPropertyName().equals("dragPoint")) {
            horizontalRuler.setIndicatorValue(((Long) evt.getOldValue()).intValue());
            horizontalRuler.repaint();
            verticalRuler.setIndicatorValue(((Long) evt.getNewValue()).intValue());
            verticalRuler.repaint();
          }
        });

    view.addMouseMotionListener(
        new MouseAdapter() {

          private void execute(int horizontalValue, int verticalValue) {
            horizontalRuler.setIndicatorValue(horizontalValue);
            horizontalRuler.repaint();
            verticalRuler.setIndicatorValue(verticalValue);
            verticalRuler.repaint();
          }

          @Override
          public void mouseMoved(MouseEvent e) {
            execute(e.getX(), e.getY());
          }

          @Override
          public void mouseDragged(MouseEvent e) {
            execute(e.getX(), e.getY());
          }

          @Override
          public void mouseExited(MouseEvent e) {
            execute(-1, -1);
          }
        });

    setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    getHorizontalScrollBar().setUnitIncrement(50);
    setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    getVerticalScrollBar().setUnitIncrement(50);
  }

  public void setRulerVisible(boolean visible) {
    setColumnHeaderView(visible ? horizontalRuler : null);
    setRowHeaderView(visible ? verticalRuler : null);
    setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, visible ? unitButton : null);
    setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, visible ? topRightCorner : null);
    setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, visible ? bottomLeftCorner : null);
  }

  public void setSelectionRectangle(Rectangle2D rect) {
    horizontalRuler.setSelectionRect(rect);
    verticalRuler.setSelectionRect(rect);
  }

  public void invalidateBuffers() {
    horizontalRuler.invalidateBuffer();
    verticalRuler.invalidateBuffer();
  }

  public void setZeroLocation(Point2D location) {
    this.horizontalRuler.setZeroLocation(location.getX());
    this.verticalRuler.setZeroLocation(location.getY());
  }

  public boolean addUnitListener(IRulerListener e) {
    return listeners.add(e);
  }

  public boolean removeUnitListener(IRulerListener o) {
    return listeners.remove(o);
  }

  public void setZoomLevel(double zoomLevel) {
    horizontalRuler.setZoomLevel(zoomLevel);
    verticalRuler.setZoomLevel(zoomLevel);
  }

  public void setMetric(boolean isMetric) {
    horizontalRuler.setMetric(isMetric);
    verticalRuler.setMetric(isMetric);
    unitButton.setText(isMetric ? "cm" : "in");

    for (IRulerListener listener : listeners) {
      listener.unitsChanged(isMetric);
    }
  }

  public boolean isMouseScrollMode() {
    return mouseScrollMode;
  }

  protected void updateRulerSize(int width, int height) {
    horizontalRuler.setPreferredWidth(width);
    horizontalRuler.invalidate();
    verticalRuler.setPreferredHeight(height);
    verticalRuler.invalidate();
  }

  private static class Corner extends JComponent {

    private static final long serialVersionUID = 1L;
    private final int orientation;

    public Corner(int orientation) {
      this.orientation = orientation;
    }

    protected void paintComponent(Graphics g) {
      g.setColor(Ruler.COLOR);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(Color.black);
      if (orientation == Ruler.HORIZONTAL) {
        g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
      } else if (orientation == Ruler.VERTICAL) {
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);
      }
    }
  }
}

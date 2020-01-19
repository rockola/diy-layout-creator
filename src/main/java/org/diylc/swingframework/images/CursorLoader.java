package org.diylc.swingframework.images;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Image;

import org.diylc.images.Icon;

public enum CursorLoader {
  ScrollCenter(Icon.Scroll_Center),
  ScrollN(Icon.Scroll_N),
  ScrollNE(Icon.Scroll_NE),
  ScrollE(Icon.Scroll_E),
  ScrollSE(Icon.Scroll_SE),
  ScrollS(Icon.Scroll_S),
  ScrollSW(Icon.Scroll_SW),
  ScrollW(Icon.Scroll_W),
  ScrollNW(Icon.Scroll_NW);

  private Icon icon;
  private Cursor cursor;

  CursorLoader(Icon icon) {
    this.icon = icon;
    this.cursor = icon.image() == null
                  ? Cursor.getDefaultCursor()
                  : Toolkit.getDefaultToolkit().createCustomCursor(
                        icon.image(),
                        new Point(
                            icon.image().getWidth(null) / 2,
                            icon.image().getHeight(null) / 2),
                        "custom:" + name());
  }

  public Cursor getCursor() {
    return this.cursor;
  }
}

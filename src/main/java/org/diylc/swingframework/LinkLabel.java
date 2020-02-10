/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2010-2020 held jointly by the individual authors.

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

package org.diylc.swingframework;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;

/**
 * {@link JLabel} customized to show hyperlinks. Foreground color is
 * set to blue by default and link is underlined.
 *
 * @author Branislav Stojkovic
 */
public class LinkLabel extends JLabel {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(LinkLabel.class);

  /**
   * Creates a hyperlink with the specified url.
   *
   * @param url
   */
  public LinkLabel(final URL url) {
    super("<html><u>" + url.toString() + "</u></html>");
    setForeground(Color.blue);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          try {
            App.openUrl(url);
          } catch (Exception e1) {
            LOG.error("Could not launch default browser", e1);
          }
        }
      });
  }

  @Deprecated
  public LinkLabel() {
    super();
  }

  @Deprecated
  public LinkLabel(Icon image, int horizontalAlignment) {
    super(image, horizontalAlignment);
  }

  @Deprecated
  public LinkLabel(Icon image) {
    super(image);
  }

  @Deprecated
  public LinkLabel(String text, Icon icon, int horizontalAlignment) {
    super(text, icon, horizontalAlignment);
  }

  @Deprecated
  public LinkLabel(String text, int horizontalAlignment) {
    super(text, horizontalAlignment);
  }

  @Deprecated
  public LinkLabel(String text) {
    super(text);
  }
}

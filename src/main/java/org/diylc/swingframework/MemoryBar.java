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

package org.diylc.swingframework;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.Format;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.Message;

/**
 * {@link JComponent} that draws current memory usage as a vertical bar. Details are provided in the
 * tooltip. Click on the component will run the garbage collector. Memory usage information is read
 * periodically.
 *
 * @author Branislav Stojkovic
 */
public class MemoryBar extends JComponent {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(MemoryBar.class);
  /** Milliseconds between memory bar updates. */
  private static final int DELAY = 10000;

  private static final Format format = new DecimalFormat("0.00");
  private static final double THRESHOLD = 0.1d;

  private long totalMemory = 0;
  private long freeMemory = 0;
  private long maxMemory = 0;
  private double percentFree = 0;

  private Thread thread;

  public MemoryBar() {
    super();
    setPreferredSize(new Dimension(16, 20));
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public void start() {
    final MemoryBar bar = this;
    thread =
        new Thread("DIYLC Free Memory") {
          @Override
          public void run() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            final String tooltipPattern =
                Message.getHtml("message.memorybar.tooltip", true, "<br>");
            while (true) {
              totalMemory = Runtime.getRuntime().totalMemory();
              freeMemory = Runtime.getRuntime().freeMemory();
              maxMemory = Runtime.getRuntime().maxMemory();
              percentFree = (double) freeMemory / totalMemory;
              final String tooltipText =
                  String.format(
                      tooltipPattern,
                      format.format(convertToMb(freeMemory)),
                      format.format(convertToMb(totalMemory)),
                      format.format(convertToMb(maxMemory)));
              if (percentFree < THRESHOLD) {
                LOG.debug("memory: {}", tooltipText);
              }
              SwingUtilities.invokeLater(
                  () -> {
                    bar.setToolTipText(tooltipText);
                    bar.repaint();
                  });
              try {
                Thread.sleep(DELAY);
              } catch (InterruptedException e) {
                // ignore
              }
            }
          }
        };
    thread.start();
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    g2d.setColor(getBackground());
    g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

    int barHeight = (int) ((1 - percentFree) * getHeight());
    g2d.setColor(
        percentFree < THRESHOLD ? Color.red : UIManager.getColor("List.selectionBackground"));
    g2d.fillRect(0, getHeight() - barHeight - 1, getWidth() - 1, barHeight);

    g2d.setColor(UIManager.getColor("Button.shadow"));
    g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
  }

  private double convertToMb(long size) {
    return (double) size / 1024 / 1024;
  }

  public void dispose() {
    if (thread != null) {
      thread.interrupt();
    }
  }
}

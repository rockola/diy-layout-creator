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

package org.diylc;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.SplashScreen;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.diylc.common.Config;
import org.diylc.images.FontLoader;
import org.diylc.images.Icon;

/**
   Application splash screen. Displayed briefly in the beginning of
   application execution. Disappears by itself when application is
   ready for user input.
 */
public class Splash {

  private Thread thread;

  private ImageIcon ceramic = null;
  private ImageIcon electrolytic = null;
  private ImageIcon film = null;
  private ImageIcon resistor = null;
  private ImageIcon splash = null;
  private Font font = null;
  private String appVersion = null;

  /**
     Create a Splash instance. This object is instantiated once, in
     the initialization section of App class. start() is called right
     after instantiation. This results in the splash animation
     starting as soon as possible.
   */
  public Splash() {
    final SplashScreen splashScreen = SplashScreen.getSplashScreen();
    final Graphics2D g = splashScreen.createGraphics();
    if (g == null) {
      return; // when would this happen?
    }

    ceramic = Icon.SplashCeramic.imageIcon();
    electrolytic = Icon.SplashElectrolytic.imageIcon();
    film = Icon.SplashFilm.imageIcon();
    resistor = Icon.SplashResistor.imageIcon();
    splash = Icon.Splash.imageIcon();

    font = FontLoader.getFont("Jura", Font.BOLD, 24);
    appVersion = Config.getString("app.version");

    thread = new Thread(() -> {
      for (int i = 90; i >= 0; i--) {
        if (splashScreen.isVisible()) {
          final int frame = i;
          SwingUtilities.invokeLater(() -> {
            renderSplashFrame(splashScreen, g, frame);
            if (splashScreen.isVisible()) {
              splashScreen.update();
            }
          });
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            // do nothing
          }
        }
      }
    });
  }

  /**
     Start the splash animation.
   */
  public void start() {
    if (thread != null) {
      thread.start();
    }
  }

  private Point resistorTarget = new Point(112, 114);
  private Point filmTarget = new Point(233, 113);
  private Point electrolyticTarget = new Point(261, 23);
  private Point ceramicTarget = new Point(352, 22);
  private Point textTarget = new Point(25, 82);

  private int pxPerFrame = 3;
  private int textPxPerFrame = 2;

  /**
     Render one frame of the splash animation.

     @param splashScreen The splash screen.
     @param g2d Graphics context.
     @param frame Ordinal of animation frame.
   */
  public void renderSplashFrame(SplashScreen splashScreen, Graphics2D g2d, int frame) {
    Graphics2D g = (Graphics2D) g2d.create();
    g.rotate(Math.toRadians(frame));
    g.setComposite(AlphaComposite.Clear);
    splash.paintIcon(null, g, 0, 0);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f - frame * 0.007f));
    resistor.paintIcon(null, g, resistorTarget.x - pxPerFrame * frame, resistorTarget.y);
    film.paintIcon(null, g, filmTarget.x, filmTarget.y + pxPerFrame * frame);
    electrolytic.paintIcon(
        null, g, electrolyticTarget.x, electrolyticTarget.y - pxPerFrame * frame);
    ceramic.paintIcon(null, g, ceramicTarget.x + pxPerFrame * frame, ceramicTarget.y);
    g.setComposite(AlphaComposite.Src);
    g.setFont(font);
    g.setColor(Color.RED);
    g.drawString(
        appVersion, textTarget.x - textPxPerFrame * frame, textTarget.y - textPxPerFrame * frame);
    g.dispose();
  }
}

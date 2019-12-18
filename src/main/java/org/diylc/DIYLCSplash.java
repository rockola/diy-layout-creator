package org.diylc;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.SplashScreen;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.diylc.images.IconLoader;

public class DIYLCSplash {

    private Thread t;

    public DIYLCSplash(final SplashScreen splashScreen) {
	if (splashScreen == null)
	    return; // when/why would this happen?
	final Graphics2D g = splashScreen.createGraphics();
	if (g == null)
	    return; // when would this happen?

	resistor = (ImageIcon) IconLoader.SplashResistor.getIcon();
	film = (ImageIcon) IconLoader.SplashFilm.getIcon();
	ceramic = (ImageIcon) IconLoader.SplashCeramic.getIcon();
	electrolytic = (ImageIcon) IconLoader.SplashElectrolytic.getIcon();
	splash = (ImageIcon) IconLoader.Splash.getIcon();
    
	t = new Thread(new Runnable() {

		@Override
		public void run() {
		    for (int i = 90; i >= 0; i--) {
			if (!splashScreen.isVisible())
			    return;
			final int frame = i;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
				    renderSplashFrame(splashScreen, g, frame);
				    if (splashScreen.isVisible())
					splashScreen.update();
				}
			    });
			try {
			    Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		    }
		}
	    });    
    }
  
    public void start() {
	if (t != null)
	    t.start();
    }

    private ImageIcon resistor = null;
    private ImageIcon film = null;
    private ImageIcon ceramic = null;
    private ImageIcon electrolytic = null;
    private ImageIcon splash = null;

    private Point resistorTarget = new Point(112, 114);
    private Point filmTarget = new Point(233, 113);
    private Point electrolyticTarget = new Point(261, 23);
    private Point ceramicTarget = new Point(352, 22);

    private int pxPerFrame = 3;

    public void renderSplashFrame(SplashScreen splashScreen,
				  Graphics2D g2d,
				  int frame) {
	Graphics2D g = (Graphics2D) g2d.create();
	g.setComposite(AlphaComposite.Clear);
	splash.paintIcon(null, g, 0, 0);
	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						  0.7f - frame * 0.007f));
	resistor.paintIcon(null, g,
			   resistorTarget.x - pxPerFrame * frame,
			   resistorTarget.y);
	film.paintIcon(null, g,
		       filmTarget.x,
		       filmTarget.y + pxPerFrame * frame);
	electrolytic.paintIcon(null, g,
			       electrolyticTarget.x,
			       electrolyticTarget.y - pxPerFrame * frame);
	ceramic.paintIcon(null, g,
			  ceramicTarget.x + pxPerFrame * frame,
			  ceramicTarget.y);
	g.dispose();
    }
}

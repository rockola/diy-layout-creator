package org.diylc.components;

import java.awt.Graphics2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;

public abstract class AbstractMultiPartComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(AbstractMultiPartComponent.class);

  public abstract Area[] getBody();

  public void drawSelectionOutline(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    if (isSelectedOrDragging()) {
      g2d.setColor(SELECTION_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.draw(getBodyOutline());
    }
  }

  public Area getBodyOutline() {
    Area[] body = getBody();
    Area outline = new Area();
    for (Area b : body) {
      LOG.trace("Body size {}: {} area", body.length, b == null ? "Not drawing" : "Drawing");
      if (b != null) {
        outline.add(b);
      }
    }
    return outline;
  }
}

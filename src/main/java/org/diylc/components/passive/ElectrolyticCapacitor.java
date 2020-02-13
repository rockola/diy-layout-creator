package org.diylc.components.passive;

import java.awt.Color;
import org.diylc.core.annotations.EditableProperty;

public abstract class ElectrolyticCapacitor extends AbstractCapacitor {

  private static final long serialVersionUID = 1L;

  public static final Color BODY_COLOR = Color.decode("#6B6DCE");
  public static final Color BORDER_COLOR = BODY_COLOR.darker();
  public static final Color MARKER_COLOR = Color.decode("#8CACEA");
  public static final Color TICK_COLOR = Color.white;

  protected Color markerColor = MARKER_COLOR;
  protected Color tickColor = TICK_COLOR;

  public ElectrolyticCapacitor() {
    super();
    polarized = true;
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
    this.labelColor = TICK_COLOR;
  }

  @EditableProperty(name = "Marker")
  public Color getMarkerColor() {
    return markerColor;
  }

  public void setMarkerColor(Color coverColor) {
    this.markerColor = coverColor;
  }

  @EditableProperty(name = "Tick")
  public Color getTickColor() {
    return tickColor;
  }

  public void setTickColor(Color tickColor) {
    this.tickColor = tickColor;
  }

  @EditableProperty(name = "Polarized")
  public boolean getPolarized() {
    return polarized;
  }

  public void setPolarized(boolean polarized) {
    this.polarized = polarized;
  }
}

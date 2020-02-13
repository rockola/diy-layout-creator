/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

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

package org.diylc.components.electromechanical;

import com.google.common.collect.ListMultimap;
import java.awt.Point;
import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.Area;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.Unmarshaller;
import org.diylc.parsing.XmlNode;
import org.diylc.utils.Constants;

@Unmarshaller("jack")
public abstract class AbstractJack extends AbstractMultiPartComponent {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(AbstractJack.class);

  protected JackType type = JackType.MONO;
  protected Orientation orientation = Orientation.DEFAULT;
  protected Point[] controlPoints;
  protected Integer angle = 0;
  protected boolean showLabels = false;

  protected transient Area[] body;

  @EditableProperty
  public JackType getType() {
    return type;
  }

  public void setType(JackType type) {
    this.type = type;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  protected abstract void updateControlPoints();

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
  }

  @EditableProperty(name = "Labels")
  public boolean getShowLabels() {
    return showLabels;
  }

  public void setShowLabels(boolean showLabels) {
    this.showLabels = showLabels;
  }

  public void setShowLabels(String showLabels) {
    setShowLabels(Boolean.parseBoolean(showLabels));
  }

  @EditableProperty
  public Integer getAngle() {
    if (angle == null) {
      angle = orientation == null ? 0 : orientation.toInt();
    }
    return angle;
  }

  public void setAngle(Integer angle) {
    this.angle = angle;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  @Override
  public void setAngle(String angle) {
    setAngle(Integer.parseInt(angle));
  }

  protected double getTheta() {
    return Math.toRadians(getAngle());
  }

  @Override
  public String getControlPointNodeName(int index) {
    switch (index) {
      case 0:
        return "Tip";
      case 1:
        return "Sleeve";
      case 2:
        if (getType().isStereo()) {
          return "Ring";
        } else if (getType().isSwitched()) {
          return "Shunt";
        }
        break;
      default:
        // if none of the above, let's return null and let the caller figure it out
    }
    return null;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index < 2 || getType().isStereo();
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  protected enum JackSize {
    QUARTER_INCH(6.35), // 1/4"
    MINI(3.5),
    SUBMINI(2.5);

    private double diameter;
    private String displayName;

    JackSize(double dia) {
      diameter = dia;
      displayName =
          String.format("%s (%.2f mm)", WordUtils.capitalize(name().replace('_', ' ')), diameter);
    }

    public double getDiameter() {
      return diameter;
    }

    public double getImperialDiameter() {
      return diameter / Constants.MM_PER_INCH;
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  public enum JackType {
    MONO,
    STEREO,
    SWITCHED,
    SWITCHED_STEREO;

    @Override
    public String toString() {
      return WordUtils.capitalize(name().replace("_", " "));
    }

    public boolean isMono() {
      return this == MONO || this == SWITCHED;
    }

    public boolean isStereo() {
      return this == STEREO || this == SWITCHED_STEREO;
    }

    public boolean isSwitched() {
      return this == SWITCHED || this == SWITCHED_STEREO;
    }
  }

  // ----------------------------------------------------------------
  // Unmarshal jacks
  // ----------------------------------------------------------------
  protected ListMultimap<String, XmlNode> fillFields(ListMultimap<String, XmlNode> contents) {
    LOG.trace("fillFields(...)");
    contents = super.fillFields(contents);
    contents = fillField(contents, "angle", AbstractComponent::setAngle);
    contents = fillField(contents, "showLabels", AbstractComponent::setShowLabels);
    return contents;
  }

  public static AbstractJack unmarshal(XmlNode xml) {
    AbstractJack jack = null;

    if (xml.nodeIsA("jack")) {
      Map<String, ComponentType> componentTypes = ComponentType.getUnmarshalledTypes("jack");
      String type = xml.attributes.get("type");
      ComponentType foundType = componentTypes.get(type == null ? "" : type);
      if (foundType != null) {
        try {
          jack = (AbstractJack) foundType.getInstanceClass().newInstance();
          jack.fillFields(xml);
        } catch (InstantiationException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      // TODO handle legacy formats
      // TODO raise exception if xml could not be read
    }

    return jack;
  }
}

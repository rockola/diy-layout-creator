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
package org.diylc.netlist;

import org.diylc.core.IDIYComponent;

/**
 * Represents a single node in a {@link Netlist}, uniquely defined by a component and a control point.
 * 
 * @author Branislav Stojkovic
 */
public class Node implements Comparable<Node> {

  private IDIYComponent<?> component;
  private int pointIndex;

  public Node(IDIYComponent<?> component, int pointIndex) {
    super();
    this.component = component;
    this.pointIndex = pointIndex;
  }

  public IDIYComponent<?> getComponent() {
    return component;
  }

  public int getPointIndex() {
    return pointIndex;
  }
  
  public String getDisplayName() {
    return component.getControlPointNodeName(pointIndex);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((component == null) ? 0 : component.hashCode());
    result = prime * result + pointIndex;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Node other = (Node) obj;
    if (component == null) {
      if (other.component != null)
        return false;
    } else if (!component.equals(other.component))
      return false;
    if (pointIndex != other.pointIndex)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return component.getName() + "." + getDisplayName() /*+ " @ (" + component.getControlPoint(pointIndex).getX() + ":" + component.getControlPoint(pointIndex).getY() + ")"*/;
  }

  @Override
  public int compareTo(Node o) {
    return toString().compareToIgnoreCase(o.toString());
  }
}

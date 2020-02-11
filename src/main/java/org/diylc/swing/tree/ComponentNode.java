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

package org.diylc.swing.tree;

import java.util.Enumeration;
import java.util.StringJoiner;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.core.Template;

public class ComponentNode extends DefaultMutableTreeNode {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(ComponentNode.class);

  protected boolean isVisible;

  public ComponentNode() {
    this(null);
  }

  public ComponentNode(Object userObject) {
    this(userObject, true, true);
  }

  public ComponentNode(Object userObject, boolean allowsChildren, boolean isVisible) {
    super(userObject, allowsChildren);
    this.isVisible = isVisible;
  }

  public TreeNode getChildAt(int index, boolean filterIsActive) {
    if (!filterIsActive) {
      return super.getChildAt(index);
    }
    if (children == null) {
      throw new ArrayIndexOutOfBoundsException("node has no children");
    }

    int realIndex = -1;
    int visibleIndex = -1;
    Enumeration e = children.elements();
    while (e.hasMoreElements()) {
      ComponentNode node = (ComponentNode) e.nextElement();
      if (node.isVisible()) {
        visibleIndex++;
      }
      realIndex++;
      if (visibleIndex == index) {
        return (TreeNode) children.elementAt(realIndex);
      }
    }

    // throw new ArrayIndexOutOfBoundsException(String.format("index %d unmatched", index));
    return (TreeNode) children.elementAt(index);
  }

  public int getChildCount(boolean filterIsActive) {
    if (!filterIsActive) {
      return super.getChildCount();
    }
    if (children == null) {
      return 0;
    }

    int count = 0;
    Enumeration e = children.elements();
    while (e.hasMoreElements()) {
      ComponentNode node = (ComponentNode) e.nextElement();
      if (node.isVisible()) {
        count++;
      }
    }

    return count;
  }

  /** @return previous state of isVisible */
  public boolean setVisible(boolean visible) {
    boolean previous = this.isVisible;
    this.isVisible = visible;
    return previous;
  }

  public boolean isVisible() {
    return isVisible;
  }

  public boolean isCategory() {
    return getUserObject() instanceof String;
  }

  public ComponentType getComponentType() {
    Object o = getUserObject();
    ComponentType type = o instanceof ComponentType ? (ComponentType) o : null;
    if (type == null && o instanceof Template && parent instanceof ComponentNode) {
      type = ((ComponentNode) parent).getComponentType();
    }
    return type;
  }

  public Template getVariant() {
    Object o = getUserObject();
    return o instanceof Template ? (Template) o : null;
  }

  @Override
  public String toString() {
    Object o = getUserObject();
    if (o == null) {
      return "";
    }
    if (o instanceof String) {
      return (String) o;
    }
    if (o instanceof ComponentType) {
      ComponentType type = (ComponentType) o;
      return type.getName();
    }
    if (o instanceof Template) {
      Template type = (Template) o;
      return type.getName();
    }
    throw new RuntimeException("unknown userObject " + o.getClass());
  }
}

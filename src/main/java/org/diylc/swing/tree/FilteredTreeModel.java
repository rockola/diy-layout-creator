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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilteredTreeModel extends DefaultTreeModel {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(FilteredTreeModel.class);

  private String filter;

  public FilteredTreeModel(final TreeNode root) {
    super(root);
    this.filter = "";
  }

  public void setFilter(final String filter) {
    this.filter = filter.toLowerCase();
  }

  private boolean recursiveMatch(final Object node, final String filter) {

    boolean matches = false;
    if (node instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) node;
      TreePath path = new TreePath(theNode.getPath());
      // LOG.trace("recursiveMatch({}, {}) node path {}", node, filter, path);
      matches = path.toString().toLowerCase().contains(filter);
    } else {
      matches = node.toString().toLowerCase().contains(filter);
    }

    int childCount = getChildCount(node);
    for (int i = 0; i < childCount; i++) {
      Object child = getChild(node, i);
      matches |= recursiveMatch(child, filter);
    }

    return matches;
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    int count = 0;
    int childCount = super.getChildCount(parent);
    for (int i = 0; i < childCount; i++) {
      Object child = super.getChild(parent, i);
      if (recursiveMatch(child, filter)) {
        if (count == index) {
          return child;
        }
        count++;
      }
    }
    return null;
  }

  @Override
  public int getChildCount(final Object parent) {
    int count = 0;
    int childCount = super.getChildCount(parent);
    for (int i = 0; i < childCount; i++) {
      Object child = super.getChild(parent, i);
      if (recursiveMatch(child, filter)) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object childToFind) {
    int childCount = super.getChildCount(parent);
    int index = -1;
    for (int i = 0; i < childCount; i++) {
      Object child = super.getChild(parent, i);
      if (recursiveMatch(child, filter) && childToFind.equals(child)) {
        index = i;
        break;
      }
    }
    return index;
  }

  public void addNode(ComponentNode parent, ComponentNode node) {
    insertNodeInto(node, parent, parent.getChildCount());
    if (parent == getRoot()) {
      nodeStructureChanged((TreeNode) getRoot());
    }
  }
}

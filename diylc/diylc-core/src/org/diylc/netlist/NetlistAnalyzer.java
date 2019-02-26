/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.core.IDIYComponent;

public class NetlistAnalyzer {

  public NetlistAnalyzer() {}

  public Tree constructTreeBetween(Netlist netlist, Node nodeA, Node nodeB) {
    Tree tree = new Tree(TreeConnectionType.Parallel);
    connectNodes(netlist, nodeA, nodeB, tree.getChildren(), new Tree(TreeConnectionType.Series), new HashSet<Node>());

    // System.out.println(netlist.getSwitchSetup());
    // System.out.println(tree);
    //
    // System.out.println("opposite:");
    // Tree tree1 = new Tree(TreeConnectionType.Parallel);
    // connectNodes(netlist, nodeB, nodeA, tree1.getChildren(), new Tree(TreeConnectionType.Series),
    // new HashSet<Node>());
    // System.out.println(tree1);

    return tree;
  }

  protected void connectNodes(Netlist netlist, Node nodeA, Node nodeB, List<Tree> concurrentPaths, Tree currentPath,
      Set<Node> visited) {
    if (nodeA == nodeB) {
      concurrentPaths.add(currentPath);
      return;
    }

    Group groupA = findGroup(netlist, nodeA);

    if (groupA == null)
      return;

    if (groupA.getNodes().contains(nodeB)) {
      concurrentPaths.add(currentPath);
      return;
    }

    visited.addAll(groupA.getNodes());

    List<Tree> newConcurrentPaths = new ArrayList<Tree>();

    for (Node n : groupA.getNodes()) {
      IDIYComponent<?> c = n.getComponent();
      for (int i = 0; i < c.getControlPointCount(); i++) {
        if (i != n.getPointIndex() && c.getInternalLinkName(i, n.getPointIndex()) != null) {
          TreeLeaf l = new TreeLeaf(c, i, n.getPointIndex());
          Node newNodeA = new Node(c, i);
          if (!visited.contains(newNodeA)) {
            // visited.add(n);
            Tree newCurrentPath = null;
            try {
              newCurrentPath = (Tree) currentPath.clone();
            } catch (CloneNotSupportedException e) {
            }
            Tree newItem = new Tree(l);
            if (!newCurrentPath.getChildren().contains(newItem))
              newCurrentPath.getChildren().add(newItem);
            // if (!currentPath.contains(l))
            // newCurrentPath.add(l);
            Set<Node> newVisited = new HashSet<Node>(visited);
            connectNodes(netlist, newNodeA, nodeB, newConcurrentPaths, newCurrentPath, newVisited);
          }
        }
      }
    }

    if (newConcurrentPaths.size() == 0)
      return;


    if (newConcurrentPaths.size() == 1) {
      if (newConcurrentPaths.get(0).getConnectionType() == TreeConnectionType.Series) {
        currentPath.getChildren().clear();
        currentPath.getChildren().addAll(newConcurrentPaths.get(0).getChildren());
      } else {
        currentPath.getChildren().add(newConcurrentPaths.get(0));
      }
      concurrentPaths.add(currentPath);
      return;
    }

    mergePaths(newConcurrentPaths, false);
    concurrentPaths.addAll(newConcurrentPaths);
  }

  protected void mergePaths(List<Tree> paths, boolean backward) {
    Map<Tree, Integer> uniques = new HashMap<Tree, Integer>();
    for (Tree t : paths) {
      if (t.getConnectionType() != TreeConnectionType.Series)
        continue;
      Tree key = t.getChildren().get(backward ? t.getChildren().size() - 1 : 0);
      Integer count = uniques.get(key);
      uniques.put(key, count == null ? 1 : count + 1);
    }
    
    List<Tree> unmerged = new ArrayList<Tree>(paths);

    for (Map.Entry<Tree, Integer> e : uniques.entrySet()) {
      if (e.getValue() > 1) {
        List<Tree> pathsToMerge = new ArrayList<Tree>();
        for (Tree t : paths) {
          Tree key = t.getChildren().get(backward ? t.getChildren().size() - 1 : 0);
          if (key.equals(e.getKey()))
            pathsToMerge.add(t);
        }
        unmerged.removeAll(pathsToMerge);

        Tree jointStart = new Tree(new ArrayList<Tree>(), TreeConnectionType.Series);
        Tree jointFinish = new Tree(new ArrayList<Tree>(), TreeConnectionType.Series);
        
        Tree unique = e.getKey();

        for (int i = 0; i < pathsToMerge.get(0).getChildren().size(); i++) {
          boolean canMerge = true;
          unique = pathsToMerge.get(0).getChildren().get(backward ? pathsToMerge.get(0).getChildren().size() - i - 1 : i);
          for (Tree t : pathsToMerge) {            
            if (t.getChildren().size() <= i || !t.getChildren().get(backward ? t.getChildren().size() - i - 1 : i).equals(unique)) {
              canMerge = false;
              break;
            }
          }
          if (canMerge)
            (backward ? jointFinish : jointStart).getChildren().add(unique);
          else
            break;
        }        
        
        for (int i = 0; i < pathsToMerge.get(0).getChildren().size(); i++) {
          boolean canMerge = true;
          unique = pathsToMerge.get(0).getChildren().get(backward ? i : pathsToMerge.get(0).getChildren().size() - i - 1);
          for (Tree t : pathsToMerge) {            
            if (t.getChildren().size() <= i || !t.getChildren().get(backward ? 0 : t.getChildren().size() - 1).equals(unique)) {
              canMerge = false;
              break;
            }
          }
          if (canMerge)
            (backward ? jointStart : jointFinish).getChildren().add(unique);
          else
            break;
        }

        if (!jointStart.getChildren().isEmpty() || !jointFinish.getChildren().isEmpty()) {
          Tree mergedTree = new Tree(new ArrayList<Tree>(), TreeConnectionType.Series);
          Tree parallelSection = new Tree(new ArrayList<Tree>(pathsToMerge), TreeConnectionType.Parallel);
          if (!jointStart.getChildren().isEmpty()) {
            mergedTree.getChildren().add(jointStart);
            for (Tree t : parallelSection.getChildren()) {
              t.trimChildrenLeft(jointStart.getChildren().size());
            }
          }
          mergedTree.getChildren().add(parallelSection);
          if (!jointFinish.getChildren().isEmpty()) {
            mergedTree.getChildren().add(jointFinish);
            for (Tree t : parallelSection.getChildren()) {
              t.trimChildrenRight(jointFinish.getChildren().size());
            }
          }
          paths.removeAll(pathsToMerge);
          paths.add(mergedTree);          
        }
      }
    }
    
    // try in the opposite direction for paths we haven't merged
    if (!backward && !unmerged.isEmpty()) {
      paths.removeAll(unmerged);
      mergePaths(unmerged, true);
      paths.addAll(unmerged);
    }
  }

  protected void findAllPaths(Netlist netlist, Node nodeA, Node nodeB, List<TreeLeaf> currentPath,
      List<List<TreeLeaf>> allPaths, Set<Node> visited) {
    if (nodeA == nodeB) {
      allPaths.add(currentPath);
      return;
    }

    // System.out.println("Finding path between " + nodeA + " and " + nodeB);

    Group groupA = findGroup(netlist, nodeA);

    if (groupA == null)
      return;

    if (groupA.getNodes().contains(nodeB)) {
      allPaths.add(currentPath);
      return;
    }

    for (Node n : groupA.getNodes()) {
      IDIYComponent<?> c = n.getComponent();
      for (int i = 0; i < c.getControlPointCount(); i++) {
        if (i != n.getPointIndex() && c.getInternalLinkName(i, n.getPointIndex()) != null) {
          TreeLeaf l = new TreeLeaf(c, i, n.getPointIndex());
          Node newNodeA = new Node(c, i);
          if (!visited.contains(newNodeA)) {
            List<TreeLeaf> newCurrentPath = new ArrayList<TreeLeaf>(currentPath);
            if (!currentPath.contains(l))
              newCurrentPath.add(l);
            Set<Node> newVisited = new HashSet<Node>(visited);
            newVisited.add(nodeA);
            newVisited.add(newNodeA);
            findAllPaths(netlist, newNodeA, nodeB, newCurrentPath, allPaths, newVisited);
          }
        }
      }
    }
  }

  protected Group findGroup(Netlist netlist, Node node) {
    for (Group g : netlist.getGroups())
      if (g.getNodes().contains(node))
        return g;
    return null;
  }

  protected List<Group> trace(Set<String> typeNames, String nodeName, Netlist netlist) {
    List<Group> res = new ArrayList<Group>();
    for (Group g : netlist.getGroups()) {
      for (Node n : g.getNodes()) {
        if (typeNames.contains(n.getComponent().getClass().getCanonicalName())
            && (nodeName == null || n.getDisplayName().equalsIgnoreCase(nodeName))) {
          res.add(g);
          break;
        }
      }
    }
    return res;
  }

  protected List<Node> find(Set<String> typeNames, String nodeName, Group group) {
    List<Node> res = new ArrayList<Node>();

    for (Node n : group.getNodes()) {
      if (typeNames.contains(n.getComponent().getClass().getCanonicalName())
          && (nodeName == null || n.getDisplayName().equalsIgnoreCase(nodeName))) {
        res.add(n);
      }
    }

    return res;
  }

  protected List<Group> findGroups(Set<String> typeNames, String nodeName, Netlist netlist) {
    List<Group> res = new ArrayList<Group>();
    for (Group g : netlist.getGroups()) {
      for (Node n : g.getNodes()) {
        if (typeNames.contains(n.getComponent().getClass().getCanonicalName())
            && (nodeName == null || n.getDisplayName().equalsIgnoreCase(nodeName))) {
          res.add(g);
          break;
        }
      }
    }
    return res;
  }

  protected List<Group> findGroups(Set<String> typeNames, String nodeName, Netlist netlist,
      Collection<IDIYComponent<?>> belongTo) {
    List<Group> res = new ArrayList<Group>();
    for (Group g : netlist.getGroups()) {
      for (Node n : g.getNodes()) {
        if ((belongTo == null || belongTo.contains(n.getComponent()))
            && (typeNames.contains(n.getComponent().getClass().getCanonicalName()))
            && (nodeName == null || n.getDisplayName().equalsIgnoreCase(nodeName))) {
          res.add(g);
          break;
        }
      }
    }
    return res;
  }

  protected List<Node> find(Set<String> typeNames, String nodeName, Netlist netlist) {
    List<Node> res = new ArrayList<Node>();
    for (Group g : netlist.getGroups()) {
      for (Node n : g.getNodes()) {
        if (typeNames.contains(n.getComponent().getClass().getCanonicalName())
            && (nodeName == null || n.getDisplayName().equalsIgnoreCase(nodeName))) {
          res.add(n);
        }
      }
    }
    return res;
  }

  protected List<Node> find(Set<String> typeNames, String nodeName, Netlist netlist, List<Node> inGroupWith) {
    List<Node> res = new ArrayList<Node>();
    for (Group g : netlist.getGroups()) {

      boolean inGroup;
      if (inGroupWith == null) {
        inGroup = true;
      } else {
        inGroup = true;
        for (Node n1 : inGroupWith)
          if (!g.getNodes().contains(n1)) {
            inGroup = false;
            break;
          }
      }

      for (Node n : g.getNodes()) {
        if (inGroup && typeNames.contains(n.getComponent().getClass().getCanonicalName())
            && (nodeName == null || n.getDisplayName().equalsIgnoreCase(nodeName))) {
          res.add(n);
        }
      }
    }
    return res;
  }

  protected List<Node> find(Set<String> typeNames, String nodeName, Netlist netlist,
      Collection<IDIYComponent<?>> belongTo) {
    List<Node> res = new ArrayList<Node>();
    for (Group g : netlist.getGroups()) {

      for (Node n : g.getNodes()) {
        if ((belongTo == null || belongTo.contains(n.getComponent()))
            && typeNames.contains(n.getComponent().getClass().getCanonicalName())
            && (nodeName == null || n.getDisplayName().equalsIgnoreCase(nodeName))) {
          res.add(n);
        }
      }
    }
    return res;
  }

  protected boolean allMatch(Collection<Node> nodes, String name) {
    for (Node n : nodes)
      if (!n.getDisplayName().equalsIgnoreCase(name))
        return false;

    return true;
  }

  protected boolean allComponentsMatch(Collection<Node> nodes, Set<IDIYComponent<?>> components) {
    Set<IDIYComponent<?>> extracted = extractComponents(nodes);
    return extracted.containsAll(components);
  }

  protected Set<IDIYComponent<?>> extractComponents(Collection<Node> nodes) {
    Set<IDIYComponent<?>> components = new HashSet<IDIYComponent<?>>();
    for (Node n : nodes)
      components.add(n.getComponent());
    return components;
  }

  protected String extractName(IDIYComponent<?> c) {
    return c.getName() + " " + c.getValueForDisplay();
  }

  protected List<String> extractNames(Collection<IDIYComponent<?>> components) {
    List<String> names = new ArrayList<String>();
    for (IDIYComponent<?> c : components)
      names.add(c.getName() + " " + c.getValueForDisplay());
    Collections.sort(names);
    return names;
  }

  protected Netlist simplify(Netlist netlist, Set<Node> nodesToMerge, Collection<Node> nodesToPurge) {
    Netlist res = new Netlist();
    res.getSwitchSetup().addAll(netlist.getSwitchSetup());

    Group merged = new Group();

    // merge
    for (Group g : netlist.getGroups()) {
      boolean needsMerging = false;
      for (Node n : nodesToMerge) {
        if (g.getNodes().contains(n)) {
          needsMerging = true;
          break;
        }
      }
      if (needsMerging) {
        for (Node n : g.getNodes()) {
          if (!nodesToMerge.contains(n))
            merged.getNodes().add(n);
        }
      } else {
        res.getGroups().add(g.clone());
      }
    }

    if (!merged.getNodes().isEmpty())
      res.getGroups().add(merged);

    // purge
    for (Group g : res.getGroups()) {
      g.getNodes().removeAll(nodesToPurge);
    }

    System.out.println("Before:\n" + netlist);
    System.out.println("After:\n" + res);

    return res;
  }

  protected boolean intersect(List<Group> groups, Node node) {
    for (Group g : groups)
      if (g.getNodes().contains(node))
        return true;
    return false;
  }

  protected Node intersect(List<Group> groups, Group group) {
    for (Group g : groups)
      for (Node node : group.getNodes())
        if (g.getNodes().contains(node))
          return node;
    return null;
  }
}

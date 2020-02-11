/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2020 held jointly by the individual authors.

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

package org.diylc.swing.plugins.edit;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.swing.AbstractAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.common.EventType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.ComponentTransferable;
import org.diylc.core.ExpansionMode;
import org.diylc.images.Icon;
import org.diylc.swing.action.ActionFactory;

public class EditMenuPlugin implements IPlugIn, ClipboardOwner {

  private static final Logger LOG = LogManager.getLogger(EditMenuPlugin.class);

  private IPlugInPort plugInPort;
  private Clipboard clipboard;

  private Map<String, AbstractAction> actions = new HashMap<>();
  private Map<String, AbstractAction> editActions = new LinkedHashMap<>();
  private Map<String, AbstractAction> transformActions = new LinkedHashMap<>();
  private Map<String, AbstractAction> renumberActions = new LinkedHashMap<>();
  private Map<String, AbstractAction> expandActions = new LinkedHashMap<>();
  private Map<String, AbstractAction> saveAsActions = new LinkedHashMap<>();

  private List<String> actionsToRefresh;

  // private UndoHandler<Project> undoHandler;

  public EditMenuPlugin() {

    // TODO rethink this
    plugInPort = App.ui().getPresenter();

    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    /*
    undoHandler = new UndoHandler<Project>(new IUndoListener<Project>() {

        @Override
        public void actionPerformed(Project currentState) {
          plugInPort.loadProject(currentState, false, null);
        }
      });
    */
    clipboard.addFlavorListener(
        new FlavorListener() {

          @Override
          public void flavorsChanged(FlavorEvent e) {
            refreshActions();
          }
        });

    editActions.put("undo", ActionFactory.createUndoAction());
    editActions.put("redo", ActionFactory.createRedoAction());
    editActions.put("cut", ActionFactory.createCutAction(plugInPort, clipboard, this));
    editActions.put("copy", ActionFactory.createCopyAction(plugInPort, clipboard, this));
    editActions.put("paste", ActionFactory.createPasteAction(plugInPort, clipboard));
    editActions.put("duplicate", ActionFactory.createDuplicateAction(plugInPort));
    editActions.put("select-all", ActionFactory.createSelectAllAction(plugInPort));
    editActions.put("edit-selection", ActionFactory.createEditSelectionAction(plugInPort));
    editActions.put("delete-selection", ActionFactory.createDeleteSelectionAction(plugInPort));

    transformActions.put(
        "rotate-clockwise", ActionFactory.createRotateSelectionAction(plugInPort, 1));
    transformActions.put(
        "rotate-counterclockwise", ActionFactory.createRotateSelectionAction(plugInPort, -1));
    transformActions.put(
        "mirror-horizontally",
        ActionFactory.createMirrorSelectionAction(plugInPort, IComponentTransformer.HORIZONTAL));
    transformActions.put(
        "mirror-vertically",
        ActionFactory.createMirrorSelectionAction(plugInPort, IComponentTransformer.VERTICAL));
    transformActions.put("nudge", ActionFactory.createNudgeAction(plugInPort));
    transformActions.put("send-to-back", ActionFactory.createSendToBackAction(plugInPort));
    transformActions.put("bring-to-front", ActionFactory.createBringToFrontAction(plugInPort));
    transformActions.put("group", ActionFactory.createGroupAction(plugInPort));
    transformActions.put("ungroup", ActionFactory.createUngroupAction(plugInPort));

    renumberActions.put("renumber-x-axis", ActionFactory.createRenumberAction(plugInPort, true));
    renumberActions.put("renumber-y-axis", ActionFactory.createRenumberAction(plugInPort, false));

    expandActions.put(
        "expand-all", ActionFactory.createExpandSelectionAction(plugInPort, ExpansionMode.ALL));
    expandActions.put(
        "expand-immediate",
        ActionFactory.createExpandSelectionAction(plugInPort, ExpansionMode.IMMEDIATE));
    expandActions.put(
        "expand-same-type",
        ActionFactory.createExpandSelectionAction(plugInPort, ExpansionMode.SAME_TYPE));

    saveAsActions.put("save-as-template", ActionFactory.createSaveAsTemplateAction(plugInPort));
    saveAsActions.put("save-as-block", ActionFactory.createSaveAsBlockAction(plugInPort));

    actionsToRefresh =
        Arrays.asList(
            "cut",
            "copy",
            "duplicate",
            "edit-selection",
            "delete-selection",
            "group",
            "ungroup",
            "expand-all",
            "expand-immediate",
            "expand-same-type",
            "nudge",
            "send-to-back",
            "bring-to-front",
            "rotate-clockwise",
            "rotate-counterclockwise",
            "mirror-horizontally",
            "mirror-vertically",
            "save-as-template",
            "save-as-block");
  }

  private void separator(String menuTitle) {
    App.ui().injectMenuAction(null, menuTitle);
  }

  private void addActions(
      String menuTitle, Map<String, AbstractAction> actionMap, Queue<String> separatorsAfter) {
    for (Map.Entry<String, AbstractAction> entry : actionMap.entrySet()) {
      String key = entry.getKey();
      AbstractAction action = entry.getValue();
      // add this action to map of all edit actions
      actions.put(key, action);
      App.ui().injectMenuAction(action, menuTitle);
      // check for separators ----------------
      if (separatorsAfter != null
          && !separatorsAfter.isEmpty()
          && key.equals(separatorsAfter.peek())) {
        separator(menuTitle);
        separatorsAfter.remove();
      }
    }
  }

  private static String editTitle() {
    return App.getString("menu.edit.title");
  }

  private static String transformTitle() {
    return App.getString("menu.edit.transform-selection");
  }

  private static String renumberTitle() {
    return App.getString("menu.edit.renumber-selection");
  }

  private static String expandTitle() {
    return App.getString("menu.edit.expand-selection");
  }

  private static String saveAsTitle() {
    return App.getString("menu.edit.save-selection");
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    // Edit menu
    LinkedList<String> separatorsAfter = new LinkedList<>();
    separatorsAfter.add("redo");
    separatorsAfter.add("duplicate");
    addActions(editTitle(), editActions, separatorsAfter);
    //
    App.ui().injectSubmenu(transformTitle(), Icon.MagicWand, editTitle());
    addActions(transformTitle(), transformActions, null);
    // addEditAction(getSaveAsTemplateAction());
    separatorsAfter.clear();
    separatorsAfter.add("rotate-counterclockwise");
    separatorsAfter.add("mirror-vertically");
    separatorsAfter.add("nudge");
    separatorsAfter.add("bring-to-front");
    App.ui().injectSubmenu(renumberTitle(), Icon.Sort, editTitle());
    addActions(renumberTitle(), renumberActions, null);
    App.ui().injectSubmenu(expandTitle(), Icon.BranchAdd, editTitle());
    addActions(expandTitle(), expandActions, null);
    App.ui().injectSubmenu(saveAsTitle(), Icon.DiskBlue, editTitle());
    addActions(saveAsTitle(), saveAsActions, null);
    // ----------------------------------------------------------------
    separator(editTitle());
    App.ui().injectMenuAction(ActionFactory.createEditProjectAction(plugInPort), editTitle());

    refreshActions();
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(
        EventType.SELECTION_CHANGED, EventType.PROJECT_MODIFIED, EventType.PROJECT_LOADED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    switch (eventType) {
      case SELECTION_CHANGED:
        refreshActions();
        break;
      case PROJECT_MODIFIED:
        // undoHandler.stateChanged((Project) params[0], (Project) params[1], (String) params[2]);
        actions.get("undo").setEnabled(true); // TODO only for debugging!
        actions.get("redo").setEnabled(true); // TODO only for debugging!
        break;
      case PROJECT_LOADED:
        if ((Boolean) params[1]) {
          // TODOTODOTODO undoHandler.reset();
        }
        break;
      default:
        LOG.debug("{} event type unknown", eventType);
    }
  }

  private void refreshActions() {
    boolean enabled = !plugInPort.currentProject().emptySelection();
    try {
      actions
          .get("paste")
          .setEnabled(clipboard.isDataFlavorAvailable(ComponentTransferable.listFlavor));
    } catch (IllegalStateException e) {
      // clipboard unavailable
      actions.get("paste").setEnabled(false);
    }
    for (String key : actionsToRefresh) {
      LOG.trace("refreshActions {}", key);
      actions.get(key).setEnabled(enabled);
    }
  }

  // ClipboardOwner

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    refreshActions();
  }
}

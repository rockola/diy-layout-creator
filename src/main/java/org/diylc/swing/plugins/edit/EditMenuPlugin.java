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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.swing.AbstractAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.DIYLC;
import org.diylc.appframework.undo.IUndoListener;
import org.diylc.appframework.undo.UndoHandler;
import org.diylc.common.EventType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.ComponentTransferable;
import org.diylc.core.ExpansionMode;
import org.diylc.core.Project;
import org.diylc.images.Icon;
import org.diylc.swing.action.ActionFactory;
import org.diylc.swing.action.ActionFactoryAction;

public class EditMenuPlugin implements IPlugIn, ClipboardOwner {

  private static final Logger LOG = LogManager.getLogger(EditMenuPlugin.class);

  private static final String EDIT_TITLE = "Edit";
  private static final String TRANSFORM_TITLE = "Transform Selection";
  private static final String RENUMBER_TITLE = "Renumber Selection";
  private static final String EXPAND_TITLE = "Expand Selection";

  private IPlugInPort plugInPort;
  private Clipboard clipboard;

  private ActionFactoryAction cutAction;
  private ActionFactoryAction copyAction;
  private ActionFactoryAction pasteAction;
  private ActionFactoryAction duplicateAction;
  private ActionFactoryAction editSelectionAction;
  private ActionFactoryAction deleteSelectionAction;
  private ActionFactoryAction groupAction;
  private ActionFactoryAction ungroupAction;
  private ActionFactoryAction sendToBackAction;
  private ActionFactoryAction bringToFrontAction;
  private ActionFactoryAction nudgeAction;
  private ActionFactoryAction renumberXAxisAction;
  private ActionFactoryAction renumberYAxisAction;
  private ActionFactoryAction expandSelectionAllAction;
  private ActionFactoryAction expandSelectionImmediateAction;
  private ActionFactoryAction expandSelectionSameTypeAction;
  private ActionFactoryAction saveAsTemplateAction;
  private ActionFactoryAction saveAsBlockAction;
  private ActionFactoryAction rotateClockwiseAction;
  private ActionFactoryAction rotateCounterClockwiseAction;
  private ActionFactoryAction mirrorHorizontallyAction;
  private ActionFactoryAction mirrorVerticallyAction;

  private UndoHandler<Project> undoHandler;

  public EditMenuPlugin() {

    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    undoHandler =
        new UndoHandler<Project>(
            new IUndoListener<Project>() {

              @Override
              public void actionPerformed(Project currentState) {
                plugInPort.loadProject(currentState, false, null);
              }
            });
    clipboard.addFlavorListener(
        new FlavorListener() {

          @Override
          public void flavorsChanged(FlavorEvent e) {
            refreshActions();
          }
        });
  }

  public ActionFactoryAction getCutAction() {
    if (cutAction == null) {
      cutAction = ActionFactory.createCutAction(plugInPort, clipboard, this);
    }
    return cutAction;
  }

  public ActionFactoryAction getCopyAction() {
    if (copyAction == null) {
      copyAction = ActionFactory.createCopyAction(plugInPort, clipboard, this);
    }
    return copyAction;
  }

  public ActionFactoryAction getPasteAction() {
    if (pasteAction == null) {
      pasteAction = ActionFactory.createPasteAction(plugInPort, clipboard);
    }
    return pasteAction;
  }

  public ActionFactoryAction getDuplicateAction() {
    if (duplicateAction == null) {
      duplicateAction = ActionFactory.createDuplicateAction(plugInPort);
    }
    return duplicateAction;
  }

  public ActionFactoryAction getEditSelectionAction() {
    if (editSelectionAction == null) {
      editSelectionAction = ActionFactory.createEditSelectionAction(plugInPort);
    }
    return editSelectionAction;
  }

  public ActionFactoryAction getDeleteSelectionAction() {
    if (deleteSelectionAction == null) {
      deleteSelectionAction = ActionFactory.createDeleteSelectionAction(plugInPort);
    }
    return deleteSelectionAction;
  }

  public ActionFactoryAction getGroupAction() {
    if (groupAction == null) {
      groupAction = ActionFactory.createGroupAction(plugInPort);
    }
    return groupAction;
  }

  public ActionFactoryAction getUngroupAction() {
    if (ungroupAction == null) {
      ungroupAction = ActionFactory.createUngroupAction(plugInPort);
    }
    return ungroupAction;
  }

  public ActionFactoryAction getSendToBackAction() {
    if (sendToBackAction == null) {
      sendToBackAction = ActionFactory.createSendToBackAction(plugInPort);
    }
    return sendToBackAction;
  }

  public ActionFactoryAction getBringToFrontAction() {
    if (bringToFrontAction == null) {
      bringToFrontAction = ActionFactory.createBringToFrontAction(plugInPort);
    }
    return bringToFrontAction;
  }

  public ActionFactoryAction getNudgeAction() {
    if (nudgeAction == null) {
      nudgeAction = ActionFactory.createNudgeAction(plugInPort);
    }
    return nudgeAction;
  }

  public ActionFactoryAction getRenumberXAxisAction() {
    if (renumberXAxisAction == null) {
      renumberXAxisAction = ActionFactory.createRenumberAction(plugInPort, true);
    }
    return renumberXAxisAction;
  }

  public ActionFactoryAction getRenumberYAxisAction() {
    if (renumberYAxisAction == null) {
      renumberYAxisAction = ActionFactory.createRenumberAction(plugInPort, false);
    }
    return renumberYAxisAction;
  }

  public ActionFactoryAction getExpandSelectionAllAction() {
    if (expandSelectionAllAction == null) {
      expandSelectionAllAction =
          ActionFactory.createExpandSelectionAction(plugInPort, ExpansionMode.ALL);
    }
    return expandSelectionAllAction;
  }

  public ActionFactoryAction getExpandSelectionImmediateAction() {
    if (expandSelectionImmediateAction == null) {
      expandSelectionImmediateAction =
          ActionFactory.createExpandSelectionAction(plugInPort, ExpansionMode.IMMEDIATE);
    }
    return expandSelectionImmediateAction;
  }

  public ActionFactoryAction getExpandSelectionSameTypeAction() {
    if (expandSelectionSameTypeAction == null) {
      expandSelectionSameTypeAction =
          ActionFactory.createExpandSelectionAction(plugInPort, ExpansionMode.SAME_TYPE);
    }
    return expandSelectionSameTypeAction;
  }

  public ActionFactoryAction getSaveAsTemplateAction() {
    if (saveAsTemplateAction == null) {
      saveAsTemplateAction = ActionFactory.createSaveAsTemplateAction(plugInPort);
    }
    return saveAsTemplateAction;
  }

  public ActionFactoryAction getSaveAsBlockAction() {
    if (saveAsBlockAction == null) {
      saveAsBlockAction = ActionFactory.createSaveAsBlockAction(plugInPort);
    }
    return saveAsBlockAction;
  }

  public ActionFactoryAction getRotateClockwiseAction() {
    if (rotateClockwiseAction == null) {
      rotateClockwiseAction = ActionFactory.createRotateSelectionAction(plugInPort, 1);
    }
    return rotateClockwiseAction;
  }

  public ActionFactoryAction getRotateCounterclockwiseAction() {
    if (rotateCounterClockwiseAction == null) {
      rotateCounterClockwiseAction = ActionFactory.createRotateSelectionAction(plugInPort, -1);
    }
    return rotateCounterClockwiseAction;
  }

  public ActionFactoryAction getMirrorHorizontallyAction() {
    if (mirrorHorizontallyAction == null) {
      mirrorHorizontallyAction =
          ActionFactory.createMirrorSelectionAction(plugInPort, IComponentTransformer.HORIZONTAL);
    }
    return mirrorHorizontallyAction;
  }

  public ActionFactoryAction getMirrorVerticallyAction() {
    if (mirrorVerticallyAction == null) {
      mirrorVerticallyAction =
          ActionFactory.createMirrorSelectionAction(plugInPort, IComponentTransformer.VERTICAL);
    }
    return mirrorVerticallyAction;
  }

  private void addActions(List<AbstractAction> actions, String menuTitle) {
    for (AbstractAction action : actions) {
      DIYLC.ui().injectMenuAction(action, menuTitle);
    }
  }

  private void separator(String menuTitle) {
    DIYLC.ui().injectMenuAction(null, menuTitle);
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    addActions(
        new ArrayList<AbstractAction>(Arrays.asList(
            undoHandler.getUndoAction(),
            undoHandler.getRedoAction(),
            null, // ----------------
            getCutAction(),
            getCopyAction(),
            getPasteAction(),
            getDuplicateAction(),
            null, // ----------------
            ActionFactory.createSelectAllAction(plugInPort),
            getEditSelectionAction(),
            getDeleteSelectionAction())),
        EDIT_TITLE);
    //
    DIYLC.ui().injectSubmenu(TRANSFORM_TITLE, Icon.MagicWand, EDIT_TITLE);
    // addEditAction(getSaveAsTemplateAction());
    addActions(
        new ArrayList<AbstractAction>(Arrays.asList(
            getRotateClockwiseAction(),
            getRotateCounterclockwiseAction(),
            null, // ----------------
            getMirrorHorizontallyAction(),
            getMirrorVerticallyAction(),
            null, // ----------------
            getNudgeAction(),
            null, // ----------------
            getSendToBackAction(),
            getBringToFrontAction(),
            null, // ----------------
            getGroupAction(),
            getUngroupAction())),
        TRANSFORM_TITLE);
    // ----------------------------------------------------------------
    separator(EDIT_TITLE);
    DIYLC.ui().injectSubmenu(RENUMBER_TITLE, Icon.Sort, EDIT_TITLE);
    addActions(
        new ArrayList<AbstractAction>(Arrays.asList(
            getRenumberXAxisAction(),
            getRenumberYAxisAction())),
        RENUMBER_TITLE);
    DIYLC.ui().injectSubmenu(EXPAND_TITLE, Icon.BranchAdd, EDIT_TITLE);
    addActions(
        new ArrayList<AbstractAction>(Arrays.asList(
            getExpandSelectionAllAction(),
            getExpandSelectionImmediateAction(),
            getExpandSelectionSameTypeAction())),
        EXPAND_TITLE);
    // ----------------------------------------------------------------
    separator(EDIT_TITLE);
    DIYLC.ui().injectMenuAction(ActionFactory.createEditProjectAction(plugInPort), EDIT_TITLE);

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
        undoHandler.stateChanged((Project) params[0], (Project) params[1], (String) params[2]);
        break;
      case PROJECT_LOADED:
        if ((Boolean) params[1]) {
          undoHandler.reset();
        }
        break;
      default:
        LOG.debug("{} event type unknown", eventType);
    }
  }

  private void refreshActions() {
    boolean enabled = !plugInPort.getCurrentProject().emptySelection();
    getCutAction().setEnabled(enabled);
    getCopyAction().setEnabled(enabled);
    getDuplicateAction().setEnabled(enabled);
    try {
      getPasteAction().setEnabled(
          clipboard.isDataFlavorAvailable(ComponentTransferable.listFlavor));
    } catch (Exception e) {
      getPasteAction().setEnabled(false);
    }
    getEditSelectionAction().setEnabled(enabled);
    getDeleteSelectionAction().setEnabled(enabled);
    getGroupAction().setEnabled(enabled);
    getExpandSelectionAllAction().setEnabled(enabled);
    getExpandSelectionImmediateAction().setEnabled(enabled);
    getExpandSelectionSameTypeAction().setEnabled(enabled);
    getNudgeAction().setEnabled(enabled);
    getUngroupAction().setEnabled(enabled);
    getSendToBackAction().setEnabled(enabled);
    getBringToFrontAction().setEnabled(enabled);
    getRotateClockwiseAction().setEnabled(enabled);
    getRotateCounterclockwiseAction().setEnabled(enabled);
    getMirrorHorizontallyAction().setEnabled(enabled);
    getMirrorVerticallyAction().setEnabled(enabled);
    getSaveAsTemplateAction().setEnabled(enabled);
    getSaveAsBlockAction().setEnabled(enabled);
  }

  // ClipboardOwner

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    refreshActions();
  }
}

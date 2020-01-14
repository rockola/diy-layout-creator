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
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.appframework.undo;

import java.awt.event.ActionEvent;
import java.util.Stack;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.images.Icon;

/**
 * Utility that handles undo/redo operations. Use {@link
 * #getUndoAction()} and {@link #getRedoAction()} to obtain instances
 * of actions that may be used for menus, buttons, etc. Action states
 * are updated automatically, i.e. they are enabled and disabled when
 * they need to be.
 *
 * @author Branislav Stojkovic
 * @param <T> type of entities that actions are performed upon
 */
public class UndoHandler<T> {

  private static final Logger LOG = LogManager.getLogger(UndoHandler.class);

  public static final int MAX_STACK_SIZE = 32; // arbitrary

  private Stack<Change> undoStack = new Stack<Change>();
  private Stack<Change> redoStack = new Stack<Change>();
  private IUndoListener<T> listener;

  private UndoAction undoAction = new UndoAction();
  private RedoAction redoAction = new RedoAction();

  public UndoHandler(IUndoListener<T> listener) {
    super();
    this.listener = listener;
    refreshActions();
  }

  public UndoAction getUndoAction() {
    return undoAction;
  }

  public RedoAction getRedoAction() {
    return redoAction;
  }

  public void reset() {
    LOG.debug("Resetting Undo and Redo");
    undoStack.clear();
    redoStack.clear();
    refreshActions();
  }

  /**
   * This method is called when entity change occurs.
   *
   * @param previousState
   * @param currentState
   * @param changeDescription
   */
  public void stateChanged(T previousState, T currentState, String changeDescription) {
    LOG.info("Undo state changed");
    undoStack.push(new Change(previousState, currentState, changeDescription));
    redoStack.clear();
    refreshActions();
  }

  class UndoAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public UndoAction() {
      super();
      putValue(Action.NAME, "Undo");
      putValue(AbstractAction.ACCELERATOR_KEY, App.getKeyStroke("Undo"));
      putValue(AbstractAction.SMALL_ICON, Icon.Undo.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (undoStack.isEmpty()) {
        LOG.debug("No Undo actions available");
      } else {
        LOG.info("Performing Undo");
        Change currentState = undoStack.pop();
        redoStack.push(currentState);
        listener.actionPerformed(currentState.before);
        refreshActions();
      }
    }
  }

  class RedoAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public RedoAction() {
      super();
      putValue(Action.NAME, "Redo");
      putValue(AbstractAction.ACCELERATOR_KEY, App.getKeyStroke("Redo"));
      putValue(AbstractAction.SMALL_ICON, Icon.Redo.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (redoStack.isEmpty()) {
        LOG.debug("No Redo actions available");
      } else {
        LOG.info("Performing Redo");
        Change currentState = redoStack.pop();
        undoStack.push(currentState);
        listener.actionPerformed(currentState.after);
        refreshActions();
      }
    }
  }

  private void refreshActions() {
    getUndoAction().setEnabled(!undoStack.isEmpty());
    getUndoAction().putValue(Action.NAME,
                             "Undo" + (undoStack.isEmpty()
                                       ? ""
                                       : " " + undoStack.peek().changeDescription));
    getRedoAction().setEnabled(!redoStack.isEmpty());
    getRedoAction().putValue(Action.NAME,
                             "Redo" + (redoStack.isEmpty()
                                       ? ""
                                       : " " + redoStack.peek().changeDescription));
    while (undoStack.size() > MAX_STACK_SIZE) {
      undoStack.remove(0);
    }
    while (redoStack.size() > MAX_STACK_SIZE) {
      redoStack.remove(0);
    }
  }

  class Change {
    T before;
    T after;
    String changeDescription;

    public Change(T before, T after, String changeDescription) {
      super();
      this.before = before;
      this.after = after;
      this.changeDescription = changeDescription;
    }
  }
}

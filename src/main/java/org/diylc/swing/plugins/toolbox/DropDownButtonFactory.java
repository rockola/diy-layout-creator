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

package org.diylc.swing.plugins.toolbox;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DropDownButtonFactory {

  private static final Logger LOG = LogManager.getLogger(DropDownButtonFactory.class);

  public static final String PROP_DROP_DOWN_MENU = "dropDownMenu";

  private DropDownButtonFactory() {
    //
  }

  public static JButton createDropDownButton(Icon icon, JPopupMenu dropDownMenu) {
    return new DropDownButton(icon, dropDownMenu);
  }

  private static class DropDownButton extends JButton {

    private static final long serialVersionUID = 1L;
    private static final String ICON_NORMAL = "normal";
    private static final String ICON_PRESSED = "pressed";
    private static final String ICON_ROLLOVER = "rollover";
    private static final String ICON_ROLLOVER_SELECTED = "rolloverSelected";
    private static final String ICON_SELECTED = "selected";
    private static final String ICON_DISABLED = "disabled";
    private static final String ICON_DISABLED_SELECTED = "disabledSelected";
    private static final String ICON_ROLLOVER_LINE = "rolloverLine";
    private static final String ICON_ROLLOVER_SELECTED_LINE = "rolloverSelectedLine";

    private boolean mouseInButton = false;
    private boolean mouseInArrowArea = false;
    private Map<String, Icon> regIcons = new HashMap<String, Icon>(5);
    private Map<String, Icon> arrowIcons = new HashMap<String, Icon>(5);
    private transient PopupMenuListener menuListener =
        new PopupMenuListener() {

          @Override
          public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            //
          }

          @Override
          public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (e.getSource() instanceof DropDownButton) {
              DropDownButton button = (DropDownButton) e.getSource();
              if (button.getModel() instanceof Model) {
                ((Model) button.getModel())._release();
              }
              JPopupMenu menu;
              if ((menu = button.getPopupMenu()) != null) {
                menu.removePopupMenuListener(this);
              }
            }
          }

          @Override
          public void popupMenuCanceled(PopupMenuEvent e) {
            //
          }
        };

    public DropDownButton(Icon icon, JPopupMenu popup) {
      // Parameters.notNull((CharSequence) "icon", (Object) icon);
      assert (icon != null);
      putClientProperty("dropDownMenu", popup);
      setIcon(icon);
      setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) icon));
      resetIcons();
      addPropertyChangeListener(
          "dropDownMenu", (e) -> ((DropDownButton) e.getSource()).resetIcons());
      addMouseMotionListener(
          new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
              DropDownButton button = (DropDownButton) e.getSource();
              if (button.getPopupMenu() != null) {
                button.mouseInArrowArea = button.isInArrowArea(e.getPoint());
                button.updateRollover();
              }
            }
          });
      addMouseListener(
          new MouseAdapter() {
            private boolean popupMenuOperation;

            @Override
            public void mousePressed(MouseEvent e) {
              DropDownButton button = (DropDownButton) e.getSource();
              this.popupMenuOperation = false;
              JPopupMenu menu = button.getPopupMenu();
              if (menu != null && button.getModel() instanceof Model) {
                Model model = (Model) button.getModel();
                if (!model._isPressed()) {
                  if (button.isInArrowArea(e.getPoint()) && menu.getComponentCount() > 0) {
                    model._press();
                    menu.addPopupMenuListener(button.getMenuListener());
                    menu.show(button, 0, button.getHeight());
                    this.popupMenuOperation = true;
                  }
                } else {
                  model._release();
                  menu.removePopupMenuListener(button.getMenuListener());
                  this.popupMenuOperation = true;
                }
              }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
              if (this.popupMenuOperation) {
                this.popupMenuOperation = false;
                e.consume();
              }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
              DropDownButton button = (DropDownButton) e.getSource();
              button.mouseInButton = true;
              if (button.hasPopupMenu()) {
                button.mouseInArrowArea = button.isInArrowArea(e.getPoint());
                button.updateRollover();
              }
            }

            @Override
            public void mouseExited(MouseEvent e) {
              DropDownButton button = (DropDownButton) e.getSource();
              button.mouseInButton = false;
              button.mouseInArrowArea = false;
              if (button.hasPopupMenu()) {
                button.updateRollover();
              }
            }
          });
      this.setModel(new Model());
    }

    private PopupMenuListener getMenuListener() {
      return menuListener;
    }

    private void updateRollover() {
      super.setRolloverIcon(rolloverIcon(false));
      super.setRolloverSelectedIcon(rolloverIcon(true));
    }

    private Icon rolloverIcon(boolean selected) {
      LOG.trace("rolloverIcon({})", selected);
      String rollover = selected ? ICON_ROLLOVER_SELECTED : ICON_ROLLOVER;
      String line = selected ? ICON_ROLLOVER_SELECTED_LINE : ICON_ROLLOVER_LINE;
      Icon icon = arrowIcons.get(mouseInArrowArea ? rollover : line);
      if (icon == null) {
        Icon orig = regIcons.get(rollover);
        if (orig == null) {
          orig = regIcons.get(ICON_ROLLOVER);
        }
        if (orig == null) {
          orig = regIcons.get(ICON_NORMAL);
        }
        LOG.trace(
            "rolloverIcon({}) creating IconWithArrow (mouse%s in arrow area)",
            selected, mouseInArrowArea ? "" : " not");
        icon = new IconWithArrow(orig, !mouseInArrowArea);
        arrowIcons.put(mouseInArrowArea ? rollover : line, icon);
      }
      return icon;
    }

    private void resetIcons() {
      Icon icon = this.regIcons.get(ICON_NORMAL);
      if (icon != null) {
        this.setIcon(icon);
      }
      if ((icon = this.regIcons.get(ICON_PRESSED)) != null) {
        this.setPressedIcon(icon);
      }
      if ((icon = this.regIcons.get(ICON_ROLLOVER)) != null) {
        this.setRolloverIcon(icon);
      }
      if ((icon = this.regIcons.get(ICON_ROLLOVER_SELECTED)) != null) {
        this.setRolloverSelectedIcon(icon);
      }
      if ((icon = this.regIcons.get(ICON_SELECTED)) != null) {
        this.setSelectedIcon(icon);
      }
      if ((icon = this.regIcons.get(ICON_DISABLED)) != null) {
        this.setDisabledIcon(icon);
      }
      if ((icon = this.regIcons.get(ICON_DISABLED_SELECTED)) != null) {
        this.setDisabledSelectedIcon(icon);
      }
    }

    JPopupMenu getPopupMenu() {
      Object menu = this.getClientProperty("dropDownMenu");
      if (menu instanceof JPopupMenu) {
        return (JPopupMenu) menu;
      }
      return null;
    }

    boolean hasPopupMenu() {
      return this.getPopupMenu() != null;
    }

    private boolean isInArrowArea(Point p) {
      return (p.getLocation().x
          >= getWidth() - IconWithArrow.getArrowAreaWidth() - getInsets().right);
    }

    @Override
    public void setIcon(Icon icon) {
      assert (icon != null);
      final Icon arrow = updateIcons(icon, ICON_NORMAL);
      arrowIcons.remove(ICON_ROLLOVER_LINE);
      arrowIcons.remove(ICON_ROLLOVER_SELECTED_LINE);
      arrowIcons.remove(ICON_ROLLOVER);
      arrowIcons.remove(ICON_ROLLOVER_SELECTED);
      super.setIcon(hasPopupMenu() ? arrow : icon);
    }

    private Icon updateIcons(Icon orig, String iconType) {
      ImageIcon arrow = null;
      if (orig == null) {
        regIcons.remove(iconType);
        arrowIcons.remove(iconType);
      } else {
        regIcons.put(iconType, orig);
        arrow = new ImageIcon(ImageUtilities.icon2Image((Icon) new IconWithArrow(orig, false)));
        arrowIcons.put(iconType, arrow);
      }
      return arrow;
    }

    @Override
    public void setPressedIcon(Icon icon) {
      Icon arrow = updateIcons(icon, ICON_PRESSED);
      super.setPressedIcon(hasPopupMenu() ? arrow : icon);
    }

    @Override
    public void setSelectedIcon(Icon icon) {
      Icon arrow = this.updateIcons(icon, ICON_SELECTED);
      super.setSelectedIcon(hasPopupMenu() ? arrow : icon);
    }

    @Override
    public void setRolloverIcon(Icon icon) {
      Icon arrow = this.updateIcons(icon, ICON_ROLLOVER);
      this.arrowIcons.remove(ICON_ROLLOVER_LINE);
      this.arrowIcons.remove(ICON_ROLLOVER_SELECTED_LINE);
      super.setRolloverIcon(hasPopupMenu() ? arrow : icon);
    }

    @Override
    public void setRolloverSelectedIcon(Icon icon) {
      Icon arrow = this.updateIcons(icon, ICON_ROLLOVER_SELECTED);
      this.arrowIcons.remove(ICON_ROLLOVER_SELECTED_LINE);
      super.setRolloverSelectedIcon(hasPopupMenu() ? arrow : icon);
    }

    @Override
    public void setDisabledIcon(Icon icon) {
      Icon arrow = this.updateIcons(icon, ICON_DISABLED);
      super.setDisabledIcon(hasPopupMenu() ? arrow : icon);
    }

    @Override
    public void setDisabledSelectedIcon(Icon icon) {
      Icon arrow = this.updateIcons(icon, ICON_DISABLED_SELECTED);
      super.setDisabledSelectedIcon(hasPopupMenu() ? arrow : icon);
    }

    @Override
    public void setText(String text) {
      //
    }

    @Override
    public String getText() {
      return null;
    }

    private class Model extends DefaultButtonModel {

      private static final long serialVersionUID = 1L;

      private boolean _pressed;

      private Model() {
        this._pressed = false;
      }

      @Override
      public void setPressed(boolean b) {
        if (DropDownButton.this.mouseInArrowArea || this._pressed) {
          return;
        }
        super.setPressed(b);
      }

      public void _press() {
        if (this.isPressed() || !this.isEnabled()) {
          return;
        }
        this.stateMask |= 5;
        this.fireStateChanged();
        this._pressed = true;
      }

      public void _release() {
        this._pressed = false;
        DropDownButton.this.mouseInArrowArea = false;
        this.setArmed(false);
        this.setPressed(false);
        this.setRollover(false);
        this.setSelected(false);
      }

      public boolean _isPressed() {
        return this._pressed;
      }

      @Override
      protected void fireStateChanged() {
        if (this._pressed) {
          return;
        }
        super.fireStateChanged();
      }

      @Override
      public void setArmed(boolean b) {
        if (this._pressed) {
          return;
        }
        super.setArmed(b);
      }

      @Override
      public void setEnabled(boolean b) {
        if (this._pressed) {
          return;
        }
        super.setEnabled(b);
      }

      @Override
      public void setSelected(boolean b) {
        if (this._pressed) {
          return;
        }
        super.setSelected(b);
      }

      @Override
      public void setRollover(boolean b) {
        if (this._pressed) {
          return;
        }
        super.setRollover(b);
      }
    }
  }
}

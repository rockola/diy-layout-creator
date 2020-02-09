/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2020 held jointly by the individual authors.

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
package org.diylc.swing.gui;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.StringJoiner;
import javax.swing.KeyStroke;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "binding")
public class Binding implements Serializable {

  @XmlElement(name = "keycode")
  @XmlJavaTypeAdapter(KeyCodeAdapter.class)
  private Integer keycode;

  @XmlElement(name = "modifier")
  @XmlJavaTypeAdapter(KeyModifierAdapter.class)
  private Integer modifier;

  @XmlElement(name = "action")
  private String action;

  public Binding() {}

  public Binding(Integer keycode, Integer modifier, String action) {
    this.keycode = keycode;
    this.modifier = modifier;
    this.action = action;
  }

  private static boolean hasMenu(int i) {
    // Java >8: (i & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0
    return (i & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
  }

  private static boolean hasAlt(int i) {
    return (i & KeyEvent.ALT_DOWN_MASK) != 0;
  }

  private static boolean hasShift(int i) {
    return (i & KeyEvent.SHIFT_DOWN_MASK) != 0;
  }

  public static String modifierToString(Integer m) {
    if (m == null) {
      return "";
    }
    int i = m.intValue();
    StringJoiner sj = new StringJoiner(" ");
    if (hasMenu(i)) {
      sj.add("Menu");
    }
    if (hasAlt(i)) {
      sj.add("Alt");
    }
    if (hasShift(i)) {
      sj.add("Shift");
    }
    return sj.toString();
  }

  public String toString() {
    return String.format(
        "<Binding keycode %d modifier %s action %s />",
        this.getKeyCode(),
        modifierToString(this.getModifier()),
        this.getAction());
  }

  public void setKeyCode(Integer keycode) {
    this.keycode = keycode;
  }

  public int getKeyCode() {
    if (this.keycode == null) {
      throw new RuntimeException("keycode is null " + this.toString());
    }
    return this.keycode.intValue();
  }

  public boolean hasKeyCode(int c) {
    return getKeyCode() == c;
  }

  public boolean hasKeyCode(KeyEvent e) {
    return getKeyCode() == e.getKeyCode();
  }

  public void setModifier(Integer modifier) {
    this.modifier = modifier;
  }

  public int getModifier() {
    if (this.modifier == null) {
      throw new RuntimeException("modifier is null " + this.toString());
    }
    return this.modifier.intValue();
  }

  /**
   * Compare modifier to this modifier. Should not be compared with ==
   * in case new modifier flags are added.
   *
   * @param m Other modifier
   */
  public boolean hasModifier(int m) {
    return (this.getModifier() & m) == this.getModifier();
  }

  public boolean hasModifier(KeyEvent e) {
    return hasModifier(e.getModifiersEx());
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getAction() {
    return this.action;
  }

  public KeyStroke getKeyStroke() {
    return KeyStroke.getKeyStroke(getKeyCode(), getModifier());
  }
}

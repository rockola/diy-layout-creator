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

import java.util.Map;
import static java.util.Map.entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class KeyModifierAdapter extends XmlAdapter<String, Integer> {

    private static final Logger LOG = LogManager.getLogger(KeyModifierAdapter.class);

    public KeyModifierAdapter() { }

    @Override
    public Integer unmarshal(String v) throws Exception {
        int i = 0;
        for (String s : v.split("\\s+")) { // split at whitespace
            if (s.equals("Menu")) {
                i = i | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
            } else if (s.equals("Alt")) {
                i = i | KeyEvent.ALT_DOWN_MASK;
            } else if (s.equals("Shift")) {
                i = i | KeyEvent.SHIFT_DOWN_MASK;
            }
        }
        return Integer.valueOf(i);
    }

    @Override
    public String marshal(Integer v) throws Exception {
        return Binding.modifierToString(v);
    }
}

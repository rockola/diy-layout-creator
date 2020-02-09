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

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KeyCodeAdapter extends XmlAdapter<String, Integer> {
  private static final Logger LOG = LogManager.getLogger(KeyCodeAdapter.class);

  public KeyCodeAdapter() {}

  private static Map<String, Integer> keyCodes =
      new HashMap<String, Integer>() {
        {
          put("ENTER", KeyEvent.VK_ENTER);
          put("BACK_SPACE", KeyEvent.VK_BACK_SPACE);
          put("TAB", KeyEvent.VK_TAB);
          put("CANCEL", KeyEvent.VK_CANCEL);
          put("CLEAR", KeyEvent.VK_CLEAR);
          put("SHIFT", KeyEvent.VK_SHIFT);
          put("CONTROL", KeyEvent.VK_CONTROL);
          put("ALT", KeyEvent.VK_ALT);
          put("PAUSE", KeyEvent.VK_PAUSE);
          put("CAPS_LOCK", KeyEvent.VK_CAPS_LOCK);
          put("ESCAPE", KeyEvent.VK_ESCAPE);
          put("SPACE", KeyEvent.VK_SPACE);
          put("PAGE_UP", KeyEvent.VK_PAGE_UP);
          put("PAGE_DOWN", KeyEvent.VK_PAGE_DOWN);
          put("END", KeyEvent.VK_END);
          put("HOME", KeyEvent.VK_HOME);
          put("LEFT", KeyEvent.VK_LEFT);
          put("UP", KeyEvent.VK_UP);
          put("RIGHT", KeyEvent.VK_RIGHT);
          put("DOWN", KeyEvent.VK_DOWN);
          put("COMMA", KeyEvent.VK_COMMA);
          put("MINUS", KeyEvent.VK_MINUS);
          put("PERIOD", KeyEvent.VK_PERIOD);
          put("SLASH", KeyEvent.VK_SLASH);
          put("0", KeyEvent.VK_0);
          put("1", KeyEvent.VK_1);
          put("2", KeyEvent.VK_2);
          put("3", KeyEvent.VK_3);
          put("4", KeyEvent.VK_4);
          put("5", KeyEvent.VK_5);
          put("6", KeyEvent.VK_6);
          put("7", KeyEvent.VK_7);
          put("8", KeyEvent.VK_8);
          put("9", KeyEvent.VK_9);
          put("SEMICOLON", KeyEvent.VK_SEMICOLON);
          put("EQUALS", KeyEvent.VK_EQUALS);
          put("A", KeyEvent.VK_A);
          put("B", KeyEvent.VK_B);
          put("C", KeyEvent.VK_C);
          put("D", KeyEvent.VK_D);
          put("E", KeyEvent.VK_E);
          put("F", KeyEvent.VK_F);
          put("G", KeyEvent.VK_G);
          put("H", KeyEvent.VK_H);
          put("I", KeyEvent.VK_I);
          put("J", KeyEvent.VK_J);
          put("K", KeyEvent.VK_K);
          put("L", KeyEvent.VK_L);
          put("M", KeyEvent.VK_M);
          put("N", KeyEvent.VK_N);
          put("O", KeyEvent.VK_O);
          put("P", KeyEvent.VK_P);
          put("Q", KeyEvent.VK_Q);
          put("R", KeyEvent.VK_R);
          put("S", KeyEvent.VK_S);
          put("T", KeyEvent.VK_T);
          put("U", KeyEvent.VK_U);
          put("V", KeyEvent.VK_V);
          put("W", KeyEvent.VK_W);
          put("X", KeyEvent.VK_X);
          put("Y", KeyEvent.VK_Y);
          put("Z", KeyEvent.VK_Z);
          put("OPEN_BRACKET", KeyEvent.VK_OPEN_BRACKET);
          put("BACK_SLASH", KeyEvent.VK_BACK_SLASH);
          put("CLOSE_BRACKET", KeyEvent.VK_CLOSE_BRACKET);
          put("NUMPAD0", KeyEvent.VK_NUMPAD0);
          put("NUMPAD1", KeyEvent.VK_NUMPAD1);
          put("NUMPAD2", KeyEvent.VK_NUMPAD2);
          put("NUMPAD3", KeyEvent.VK_NUMPAD3);
          put("NUMPAD4", KeyEvent.VK_NUMPAD4);
          put("NUMPAD5", KeyEvent.VK_NUMPAD5);
          put("NUMPAD6", KeyEvent.VK_NUMPAD6);
          put("NUMPAD7", KeyEvent.VK_NUMPAD7);
          put("NUMPAD8", KeyEvent.VK_NUMPAD8);
          put("NUMPAD9", KeyEvent.VK_NUMPAD9);
          put("MULTIPLY", KeyEvent.VK_MULTIPLY);
          put("ADD", KeyEvent.VK_ADD);
          put("SEPARATER", KeyEvent.VK_SEPARATER);
          put("SEPARATOR", KeyEvent.VK_SEPARATOR);
          put("SUBTRACT", KeyEvent.VK_SUBTRACT);
          put("DECIMAL", KeyEvent.VK_DECIMAL);
          put("DIVIDE", KeyEvent.VK_DIVIDE);
          put("DELETE", KeyEvent.VK_DELETE);
          put("NUM_LOCK", KeyEvent.VK_NUM_LOCK);
          put("SCROLL_LOCK", KeyEvent.VK_SCROLL_LOCK);
          put("F1", KeyEvent.VK_F1);
          put("F2", KeyEvent.VK_F2);
          put("F3", KeyEvent.VK_F3);
          put("F4", KeyEvent.VK_F4);
          put("F5", KeyEvent.VK_F5);
          put("F6", KeyEvent.VK_F6);
          put("F7", KeyEvent.VK_F7);
          put("F8", KeyEvent.VK_F8);
          put("F9", KeyEvent.VK_F9);
          put("F10", KeyEvent.VK_F10);
          put("F11", KeyEvent.VK_F11);
          put("F12", KeyEvent.VK_F12);
          put("F13", KeyEvent.VK_F13);
          put("F14", KeyEvent.VK_F14);
          put("F15", KeyEvent.VK_F15);
          put("F16", KeyEvent.VK_F16);
          put("F17", KeyEvent.VK_F17);
          put("F18", KeyEvent.VK_F18);
          put("F19", KeyEvent.VK_F19);
          put("F20", KeyEvent.VK_F20);
          put("F21", KeyEvent.VK_F21);
          put("F22", KeyEvent.VK_F22);
          put("F23", KeyEvent.VK_F23);
          put("F24", KeyEvent.VK_F24);
          put("PRINTSCREEN", KeyEvent.VK_PRINTSCREEN);
          put("INSERT", KeyEvent.VK_INSERT);
          put("HELP", KeyEvent.VK_HELP);
          put("META", KeyEvent.VK_META);
          put("BACK_QUOTE", KeyEvent.VK_BACK_QUOTE);
          put("QUOTE", KeyEvent.VK_QUOTE);
          put("KP_UP", KeyEvent.VK_KP_UP);
          put("KP_DOWN", KeyEvent.VK_KP_DOWN);
          put("KP_LEFT", KeyEvent.VK_KP_LEFT);
          put("KP_RIGHT", KeyEvent.VK_KP_RIGHT);
          put("DEAD_GRAVE", KeyEvent.VK_DEAD_GRAVE);
          put("DEAD_ACUTE", KeyEvent.VK_DEAD_ACUTE);
          put("DEAD_CIRCUMFLEX", KeyEvent.VK_DEAD_CIRCUMFLEX);
          put("DEAD_TILDE", KeyEvent.VK_DEAD_TILDE);
          put("DEAD_MACRON", KeyEvent.VK_DEAD_MACRON);
          put("DEAD_BREVE", KeyEvent.VK_DEAD_BREVE);
          put("DEAD_ABOVEDOT", KeyEvent.VK_DEAD_ABOVEDOT);
          put("DEAD_DIAERESIS", KeyEvent.VK_DEAD_DIAERESIS);
          put("DEAD_ABOVERING", KeyEvent.VK_DEAD_ABOVERING);
          put("DEAD_DOUBLEACUTE", KeyEvent.VK_DEAD_DOUBLEACUTE);
          put("DEAD_CARON", KeyEvent.VK_DEAD_CARON);
          put("DEAD_CEDILLA", KeyEvent.VK_DEAD_CEDILLA);
          put("DEAD_OGONEK", KeyEvent.VK_DEAD_OGONEK);
          put("DEAD_IOTA", KeyEvent.VK_DEAD_IOTA);
          put("DEAD_VOICED_SOUND", KeyEvent.VK_DEAD_VOICED_SOUND);
          put("DEAD_SEMIVOICED_SOUND", KeyEvent.VK_DEAD_SEMIVOICED_SOUND);
          put("AMPERSAND", KeyEvent.VK_AMPERSAND);
          put("ASTERISK", KeyEvent.VK_ASTERISK);
          put("QUOTEDBL", KeyEvent.VK_QUOTEDBL);
          put("LESS", KeyEvent.VK_LESS);
          put("GREATER", KeyEvent.VK_GREATER);
          put("BRACELEFT", KeyEvent.VK_BRACELEFT);
          put("BRACERIGHT", KeyEvent.VK_BRACERIGHT);
          put("AT", KeyEvent.VK_AT);
          put("COLON", KeyEvent.VK_COLON);
          put("CIRCUMFLEX", KeyEvent.VK_CIRCUMFLEX);
          put("DOLLAR", KeyEvent.VK_DOLLAR);
          put("EURO_SIGN", KeyEvent.VK_EURO_SIGN);
          put("EXCLAMATION_MARK", KeyEvent.VK_EXCLAMATION_MARK);
          put("INVERTED_EXCLAMATION_MARK", KeyEvent.VK_INVERTED_EXCLAMATION_MARK);
          put("LEFT_PARENTHESIS", KeyEvent.VK_LEFT_PARENTHESIS);
          put("NUMBER_SIGN", KeyEvent.VK_NUMBER_SIGN);
          put("PLUS", KeyEvent.VK_PLUS);
          put("RIGHT_PARENTHESIS", KeyEvent.VK_RIGHT_PARENTHESIS);
          put("UNDERSCORE", KeyEvent.VK_UNDERSCORE);
          put("WINDOWS", KeyEvent.VK_WINDOWS);
          put("CONTEXT_MENU", KeyEvent.VK_CONTEXT_MENU);
          put("FINAL", KeyEvent.VK_FINAL);
          put("CONVERT", KeyEvent.VK_CONVERT);
          put("NONCONVERT", KeyEvent.VK_NONCONVERT);
          put("ACCEPT", KeyEvent.VK_ACCEPT);
          put("MODECHANGE", KeyEvent.VK_MODECHANGE);
          put("KANA", KeyEvent.VK_KANA);
          put("KANJI", KeyEvent.VK_KANJI);
          put("ALPHANUMERIC", KeyEvent.VK_ALPHANUMERIC);
          put("KATAKANA", KeyEvent.VK_KATAKANA);
          put("HIRAGANA", KeyEvent.VK_HIRAGANA);
          put("FULL_WIDTH", KeyEvent.VK_FULL_WIDTH);
          put("HALF_WIDTH", KeyEvent.VK_HALF_WIDTH);
          put("ROMAN_CHARACTERS", KeyEvent.VK_ROMAN_CHARACTERS);
          put("ALL_CANDIDATES", KeyEvent.VK_ALL_CANDIDATES);
          put("PREVIOUS_CANDIDATE", KeyEvent.VK_PREVIOUS_CANDIDATE);
          put("CODE_INPUT", KeyEvent.VK_CODE_INPUT);
          put("JAPANESE_KATAKANA", KeyEvent.VK_JAPANESE_KATAKANA);
          put("JAPANESE_HIRAGANA", KeyEvent.VK_JAPANESE_HIRAGANA);
          put("JAPANESE_ROMAN", KeyEvent.VK_JAPANESE_ROMAN);
          put("KANA_LOCK", KeyEvent.VK_KANA_LOCK);
          put("INPUT_METHOD_ON_OFF", KeyEvent.VK_INPUT_METHOD_ON_OFF);
          put("CUT", KeyEvent.VK_CUT);
          put("COPY", KeyEvent.VK_COPY);
          put("PASTE", KeyEvent.VK_PASTE);
          put("UNDO", KeyEvent.VK_UNDO);
          put("AGAIN", KeyEvent.VK_AGAIN);
          put("FIND", KeyEvent.VK_FIND);
          put("PROPS", KeyEvent.VK_PROPS);
          put("STOP", KeyEvent.VK_STOP);
          put("COMPOSE", KeyEvent.VK_COMPOSE);
          put("ALT_GRAPH", KeyEvent.VK_ALT_GRAPH);
          put("BEGIN", KeyEvent.VK_BEGIN);
          put("UNDEFINED", KeyEvent.VK_UNDEFINED);
        }
      };

  public static Map<String, Integer> getAllKeycodes() {
    return keyCodes;
  }

  @Override
  public Integer unmarshal(String v) throws Exception {
    Integer r = keyCodes.get(v);
    if (r == null) {
      LOG.error("unmarshal({}) keycode not found", v);
    }

    return r;
  }

  // not being used ATM
  @Override
  public String marshal(Integer v) throws Exception {
    for (Map.Entry<String, Integer> e : keyCodes.entrySet()) {
      if (e.getValue().equals(v)) {
        return e.getKey();
      }
    }
    return ""; // TODO: not found, throw Exception?
  }
}

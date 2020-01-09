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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.KeyStroke;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "keymap")
public class Keymap {
  private static final Logger LOG = LogManager.getLogger(Keymap.class);

  private static Keymap defaultKeymap;

  @XmlElement(name = "binding")
  private List<Binding> bindings = null;

  private Map<String, Binding> actionBindingMap;

  public Keymap() {
    // bindings = new List<Binding>();
    actionBindingMap = new HashMap<String, Binding>();
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(
        String.format(
            "<Keymap bindings.size() %d actionBindingMap.size() %d>\n",
            bindings.size(), actionBindingMap.size()));
    for (Binding bi : bindings) {
      b.append(bi.toString() + "\n");
    }
    b.append("</Keymap>\n");
    return b.toString();
  }

  public List<Binding> getBindings() {
    return bindings;
  }

  public void setBindings(List<Binding> bindings) {
    this.bindings = bindings;
  }

  public Binding getBindingForAction(String actionName) {
    return actionBindingMap.get(actionName);
  }

  public KeyStroke getKeyForAction(String actionName) {
    Binding b = getBindingForAction(actionName);
    if (b == null) {
      LOG.error(
          "getKeyForAction({}): no binding found among {} bindings",
          actionName,
          actionBindingMap.size());
      return null;
    }

    return KeyStroke.getKeyStroke(b.getKeyCode(), b.getModifier());
  }

  public KeyStroke stroke(String actionName) {
    if (actionName == null) // perfectly OK to pass null here
    return null;
    return getKeyForAction(actionName);
  }

  public static Keymap getDefaultKeymap() throws JAXBException {
    if (defaultKeymap == null) defaultKeymap = readDefaultKeymap();
    return defaultKeymap;
  }

  public static Keymap readDefaultKeymap() throws JAXBException {
    Keymap defaultKeymap = null;
    try {
      String defaultKeymapResource = "/org/diylc/action/keymap.xml";
      JAXBContext jaxbContext = JAXBContext.newInstance(Keymap.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      defaultKeymap =
          (Keymap)
              jaxbUnmarshaller.unmarshal(Keymap.class.getResourceAsStream(defaultKeymapResource));
      // initialize mapping from action name to binding
      for (Binding b : defaultKeymap.getBindings()) {
        LOG.debug(String.format("Adding binding %s", b.toString()));
        defaultKeymap.actionBindingMap.put(b.getAction(), b);
      }
      LOG.debug(KeyCodeAdapter.getAllKeycodes().toString());
    } catch (JAXBException e) {
      LOG.error("Could not load default keymap", e);
      throw e;
    }
    return defaultKeymap;
  }
}

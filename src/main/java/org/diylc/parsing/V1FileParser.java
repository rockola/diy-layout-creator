/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

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

package org.diylc.parsing;

import java.awt.Color;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.boards.AbstractBoard;
import org.diylc.components.boards.BlankBoard;
import org.diylc.components.boards.PerfBoard;
import org.diylc.components.boards.VeroBoard;
import org.diylc.components.connectivity.CopperTrace;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.connectivity.Jumper;
import org.diylc.components.connectivity.SolderPad;
import org.diylc.components.connectivity.TraceCut;
import org.diylc.components.electromechanical.MiniToggleSwitch;
import org.diylc.components.electromechanical.ToggleSwitchType;
import org.diylc.components.misc.BillOfMaterials;
import org.diylc.components.misc.Label;
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.components.passive.RadialElectrolytic;
import org.diylc.components.passive.RadialFilmCapacitor;
import org.diylc.components.passive.Resistor;
import org.diylc.components.passive.Taper;
import org.diylc.components.passive.TrimmerPotentiometer;
import org.diylc.components.passive.TrimmerPotentiometer.TrimmerType;
import org.diylc.components.semiconductors.DiodePlastic;
import org.diylc.components.semiconductors.DualInlineIc;
import org.diylc.components.semiconductors.Led;
import org.diylc.components.semiconductors.SingleInlineIc;
import org.diylc.components.semiconductors.TransistorTO92;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.Value;
import org.diylc.presenter.ComparatorFactory;
import org.diylc.utils.Constants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class V1FileParser implements IOldFileParser {

  private static final Logger LOG = LogManager.getLogger(V1FileParser.class);

  private static final Size V1_GRID_SPACING = Size.in(0.1);
  private static final Map<String, Color> V1_COLOR_MAP = new HashMap<String, Color>();

  static {
    V1_COLOR_MAP.put("red", Color.red);
    V1_COLOR_MAP.put("blue", Color.blue);
    V1_COLOR_MAP.put("white", Color.white);
    V1_COLOR_MAP.put("green", Color.green.darker());
    V1_COLOR_MAP.put("black", Color.black);
    V1_COLOR_MAP.put("yellow", Color.yellow);
  }

  @Override
  public boolean canParse(String version) {
    return version == null || version.trim().isEmpty();
  }

  public Project parseFile(Element root, List<String> warnings) {
    Project project = new Project();
    project.setTitle(root.getAttribute("Project"));
    project.setAuthor(root.getAttribute("Credits"));
    project.setGridSpacing(V1_GRID_SPACING);
    project.setDescription("Automatically converted from V1 format.");
    String type = root.getAttribute("Type");

    // Create the board.
    int width = Integer.parseInt(root.getAttribute("Width")) + 1;
    int height = Integer.parseInt(root.getAttribute("Height")) + 1;
    int boardWidth = (int) (width * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue());
    int boardHeight = (int) (height * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue());
    int projectWidth = (int) project.getWidth().convertToPixels();
    int projectHeight = (int) project.getHeight().convertToPixels();
    int x = (projectWidth - boardWidth) / 2;
    int y = (projectHeight - boardHeight) / 2;
    AbstractBoard board;
    if (type.equalsIgnoreCase("pcb")) {
      board = new BlankBoard();
      board.setBoardColor(Color.white);
      board.setBorderColor(Color.black);
    } else if (type.equalsIgnoreCase("perfboard")) {
      board = new PerfBoard();
    } else if (type.equalsIgnoreCase("stripboard")) {
      board = new VeroBoard();
    } else {
      throw new IllegalArgumentException("Unrecognized board type: " + type);
    }
    board.setName("Main board");
    Point referencePoint = project.getGrid().snapToGrid(new Point(x, y));
    board.setControlPoint(referencePoint, 0);
    board.setControlPoint(
        project.getGrid().snapToGrid(new Point(x + boardWidth, y + boardHeight)), 1);
    project.getComponents().add(board);

    NodeList childNodes = root.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = node.getNodeName();
        String nameAttr = node.getAttributes().getNamedItem("Name").getNodeValue();
        Node valueNode = node.getAttributes().getNamedItem("Value");
        String valueAttr = valueNode == null ? null : valueNode.getNodeValue();
        int x1Attr = Integer.parseInt(node.getAttributes().getNamedItem("X1").getNodeValue());
        int y1Attr = Integer.parseInt(node.getAttributes().getNamedItem("Y1").getNodeValue());
        Point point1 = convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr);
        Point point2 = null;
        Integer x2Attr = null;
        Integer y2Attr = null;
        Color color = null;
        if (node.getAttributes().getNamedItem("Color") != null) {
          String colorAttr = node.getAttributes().getNamedItem("Color").getNodeValue();
          color = V1_COLOR_MAP.get(colorAttr.toLowerCase());
        }
        if (node.getAttributes().getNamedItem("X2") != null
            && node.getAttributes().getNamedItem("Y2") != null) {
          x2Attr = Integer.parseInt(node.getAttributes().getNamedItem("X2").getNodeValue());
          y2Attr = Integer.parseInt(node.getAttributes().getNamedItem("Y2").getNodeValue());
          point2 = convertV1CoordinatesToV3Point(referencePoint, x2Attr, y2Attr);
        }
        AbstractComponent component = null;
        if (nodeName.equalsIgnoreCase("text")) {
          LOG.debug("Recognized " + nodeName);
          Label label = new Label();
          label.setName(nameAttr);
          if (color != null) {
            label.setColor(color);
          }
          label.setStringValue(valueAttr);
          label.setHorizontalAlignment(HorizontalAlignment.LEFT);
          label.setVerticalAlignment(VerticalAlignment.CENTER);
          label.setControlPoint(convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr), 0);
          component = label;
        } else if (nodeName.equalsIgnoreCase("pad")) {
          LOG.debug("Recognized " + nodeName);
          SolderPad pad = new SolderPad();
          pad.setName(nameAttr);
          if (color != null) {
            pad.setLeadColor(color);
          }
          pad.setControlPoint(convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr), 0);
          component = pad;
        } else if (nodeName.equalsIgnoreCase("cut")) {
          LOG.debug("Recognized " + nodeName);
          TraceCut cut = new TraceCut();
          cut.setCutBetweenHoles(false);
          cut.setName(nameAttr);
          cut.setControlPoint(convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr), 0);
          component = cut;
        } else if (nodeName.equalsIgnoreCase("trace")) {
          LOG.debug("Recognized " + nodeName);
          CopperTrace trace = new CopperTrace();
          trace.setName(nameAttr);
          if (color != null) {
            trace.setLeadColor(color);
          }
          trace.setControlPoint(point1, 0);
          trace.setControlPoint(point2, 1);
          component = trace;
        } else if (nodeName.equalsIgnoreCase("jumper")) {
          LOG.debug("Recognized " + nodeName);
          Jumper jumper = new Jumper();
          jumper.setName(nameAttr);
          jumper.setControlPoint(point1, 0);
          jumper.setControlPoint(point2, 1);
          component = jumper;
        } else if (nodeName.equalsIgnoreCase("wire")) {
          LOG.debug("Recognized " + nodeName);
          HookupWire wire = new HookupWire();
          long seed = Long.parseLong(node.getAttributes().getNamedItem("Seed").getNodeValue());
          Random r = new Random(seed);
          randSeed = seed;
          int d = (int) Math.round(Math.hypot(point1.x - point2.x, point1.y - point2.y) / 2);
          int x2 = (int) (point1.x + Math.round((point2.x - point1.x) * 0.40) + myRandom(d, r));
          int y2 = (int) (point1.y + Math.round((point2.y - point1.y) * 0.40) + myRandom(d, r));
          int x3 = (int) (point1.x + Math.round((point2.x - point1.x) * 0.60) + myRandom(d, r));
          int y3 = (int) (point1.y + Math.round((point2.y - point1.y) * 0.60) + myRandom(d, r));

          wire.setName(nameAttr);
          String colorAttr = node.getAttributes().getNamedItem("Color").getNodeValue();
          wire.setLeadColor(parseV1Color(colorAttr));
          wire.setControlPoint(point1, 0);
          wire.setControlPoint(new Point(x2, y2), 1);
          wire.setControlPoint(new Point(x3, y3), 2);
          wire.setControlPoint(point2, 3);
          component = wire;
        } else if (nodeName.equalsIgnoreCase("resistor")) {
          LOG.debug("Recognized " + nodeName);
          Resistor resistor = new Resistor();
          resistor.setName(nameAttr);
          try {
            resistor.setValue(Value.parse(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          resistor.setLength(Size.mm(6.35));
          resistor.setWidth(Size.mm(2.2));
          resistor.setControlPoint(point1, 0);
          resistor.setControlPoint(point2, 1);
          component = resistor;
        } else if (nodeName.equalsIgnoreCase("capacitor")) {
          LOG.debug("Recognized " + nodeName);
          RadialFilmCapacitor capacitor = new RadialFilmCapacitor();
          capacitor.setName(nameAttr);
          try {
            capacitor.setValue(Value.parse(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          capacitor.setLength(Size.mm(6));
          capacitor.setWidth(Size.mm(2));
          capacitor.setControlPoint(point1, 0);
          capacitor.setControlPoint(point2, 1);
          component = capacitor;
        } else if (nodeName.equalsIgnoreCase("electrolyte")) {
          LOG.debug("Recognized " + nodeName);
          RadialElectrolytic capacitor = new RadialElectrolytic();
          capacitor.setName(nameAttr);
          try {
            capacitor.setValue(Value.parse(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          try {
            String sizeAttr = node.getAttributes().getNamedItem("Size").getNodeValue();
            if (sizeAttr.equalsIgnoreCase("small")) {
              capacitor.setLength(Size.mm(3.5));
            } else if (sizeAttr.equalsIgnoreCase("medium")) {
              capacitor.setLength(Size.mm(5));
            } else if (sizeAttr.equalsIgnoreCase("large")) {
              capacitor.setLength(Size.mm(7));
            } else {
              capacitor.setLength(Size.mm(4));
            }
          } catch (Exception e) {
            capacitor.setLength(Size.mm(5));
            LOG.debug("Could not set size of " + nameAttr);
          }
          capacitor.setControlPoint(point1, 0);
          capacitor.setControlPoint(point2, 1);
          component = capacitor;
        } else if (nodeName.equalsIgnoreCase("diode")) {
          LOG.debug("Recognized " + nodeName);
          DiodePlastic capacitor = new DiodePlastic();
          capacitor.setName(nameAttr);
          try {
            capacitor.setValue(Value.parse(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          capacitor.setLength(Size.mm(6));
          capacitor.setWidth(Size.mm(2));
          capacitor.setControlPoint(point1, 0);
          capacitor.setControlPoint(point2, 1);
          component = capacitor;
        } else if (nodeName.equalsIgnoreCase("led")) {
          LOG.debug("Recognized " + nodeName);
          Led led = new Led();
          led.setName(nameAttr);
          led.setStringValue(valueAttr);
          led.setBodyColor(Color.red);
          led.setBorderColor(Color.red.darker());
          led.setLength(Size.mm(3));
          led.setControlPoint(point1, 0);
          led.setControlPoint(point2, 1);
          component = led;
        } else if (nodeName.equalsIgnoreCase("transistor")) {
          LOG.debug("Recognized " + nodeName);
          TransistorTO92 transistor = new TransistorTO92();
          transistor.setName(nameAttr);
          try {
            transistor.setStringValue(valueAttr);
          } catch (Exception e) {
            LOG.debug("Could not set value of {}", nameAttr);
          }
          transistor.setControlPoint(point1, 0);
          if (point1.y > point2.y) {
            transistor.setOrientation(Orientation._180);
          } else if (point1.y < point2.y) {
            transistor.setOrientation(Orientation.DEFAULT);
          } else if (point1.x < point2.x) {
            transistor.setOrientation(Orientation._270);
          } else if (point1.x > point2.x) {
            transistor.setOrientation(Orientation._90);
          }
          // capacitor.setControlPoint(point2, 1);
          component = transistor;
        } else if (nodeName.equalsIgnoreCase("ic")) {
          LOG.debug("Recognized " + nodeName);
          DualInlineIc ic = new DualInlineIc();
          int pinCount = 8;
          int rowSpace = 3;
          if (x1Attr < x2Attr && y1Attr < y2Attr) {
            pinCount = (y2Attr - y1Attr + 1) * 2;
            rowSpace = x2Attr - x1Attr;
            ic.setOrientation(Orientation.DEFAULT);
          } else if (x1Attr > x2Attr && y1Attr < y2Attr) {
            pinCount = (x1Attr - x2Attr + 1) * 2;
            rowSpace = y2Attr - y1Attr;
            ic.setOrientation(Orientation._90);
          } else if (x1Attr > x2Attr && y1Attr > y2Attr) {
            rowSpace = x1Attr - x2Attr;
            pinCount = (y1Attr - y2Attr + 1) * 2;
            ic.setOrientation(Orientation._180);
          } else if (x1Attr < x2Attr && y1Attr > y2Attr) {
            rowSpace = y1Attr - y2Attr;
            pinCount = (x2Attr - x1Attr + 1) * 2;
            ic.setOrientation(Orientation._270);
          }
          ic.setRowSpacing(Size.mm(0.1 * rowSpace));
          ic.setPinCount(DualInlineIc.defaultPinCount().setPins(pinCount));
          ic.setName(nameAttr);
          // Translate control points.
          for (int j = 0; j < ic.getControlPointCount(); j++) {
            Point p = new Point(ic.getControlPoint(j));
            p.translate(point1.x, point1.y);
            ic.setControlPoint(p, j);
          }
          ic.setStringValue(valueAttr);
          component = ic;
        } else if (nodeName.equalsIgnoreCase("switch")) {
          LOG.debug("Recognized " + nodeName);
          MiniToggleSwitch sw = new MiniToggleSwitch();
          int sizeX = Math.abs(x1Attr - x2Attr);
          int sizeY = Math.abs(y1Attr - y2Attr);
          ToggleSwitchType switchType = null;
          OrientationHV orientation = null;
          if (Math.min(sizeX, sizeY) == 0 && Math.max(sizeX, sizeY) == 1) {
            switchType = ToggleSwitchType.SPST;
            orientation = sizeX < sizeY ? OrientationHV.VERTICAL : OrientationHV.HORIZONTAL;
          }
          if (Math.min(sizeX, sizeY) == 0 && Math.max(sizeX, sizeY) == 2) {
            switchType = ToggleSwitchType.SPDT;
            orientation = sizeX < sizeY ? OrientationHV.VERTICAL : OrientationHV.HORIZONTAL;
          }
          if (Math.min(sizeX, sizeY) == 1 && Math.max(sizeX, sizeY) == 2) {
            switchType = ToggleSwitchType.DPDT;
            orientation = sizeX < sizeY ? OrientationHV.VERTICAL : OrientationHV.HORIZONTAL;
          }
          if (Math.min(sizeX, sizeY) == 2 && Math.max(sizeX, sizeY) == 2) {
            switchType = ToggleSwitchType._3PDT;
            orientation = OrientationHV.VERTICAL;
          }
          if (Math.min(sizeX, sizeY) == 2 && Math.max(sizeX, sizeY) == 3) {
            switchType = ToggleSwitchType._4PDT;
            orientation = sizeX < sizeY ? OrientationHV.HORIZONTAL : OrientationHV.VERTICAL;
          }
          if (Math.min(sizeX, sizeY) == 2 && Math.max(sizeX, sizeY) == 4) {
            switchType = ToggleSwitchType._5PDT;
            orientation = sizeX < sizeY ? OrientationHV.HORIZONTAL : OrientationHV.VERTICAL;
          }

          if (switchType == null || orientation == null) {
            String message = "Unsupported toggle switch dimensions";
            LOG.debug(message);
            if (!warnings.contains(message)) {
              warnings.add(message);
            }
          } else {
            sw.setName(nameAttr);
            sw.setOrientation(orientation);
            sw.setSwitchType(switchType);
            sw.setSpacing(Size.in(0.1));
            // compensate for potential negative coordinates after the type and orientation have
            // been set. Make sure that the top left corner is at (0, 0)
            int dx = 0;
            int dy = 0;
            for (int j = 0; j < sw.getControlPointCount(); j++) {
              Point p = new Point(sw.getControlPoint(j));
              if (p.x < 0 && p.x < dx) {
                dx = p.x;
              }
              if (p.y < 0 && p.y < dy) {
                dy = p.y;
              }
            }
            // Translate control points.
            for (int j = 0; j < sw.getControlPointCount(); j++) {
              Point p = new Point(sw.getControlPoint(j));
              p.translate(Math.min(point1.x, point2.x) - dx, Math.min(point1.y, point2.y) - dy);
              sw.setControlPoint(p, j);
            }
            component = sw;
          }
        } else if (nodeName.equalsIgnoreCase("lineic")) {
          LOG.debug("Recognized " + nodeName);
          SingleInlineIc ic = new SingleInlineIc();
          int pinCount = 8;
          if (x1Attr == x2Attr && y1Attr < y2Attr) {
            pinCount = y2Attr - y1Attr + 1;
            ic.setOrientation(Orientation.DEFAULT);
          } else if (x1Attr > x2Attr && y1Attr == y2Attr) {
            pinCount = x1Attr - x2Attr + 1;
            ic.setOrientation(Orientation._90);
          } else if (x1Attr == x2Attr && y1Attr > y2Attr) {
            pinCount = y1Attr - y2Attr + 1;
            ic.setOrientation(Orientation._180);
          } else if (x1Attr < x2Attr && y1Attr == y2Attr) {
            pinCount = x2Attr - x1Attr + 1;
            ic.setOrientation(Orientation._270);
          }
          ic.setPinCount(SingleInlineIc.defaultPinCount().setPins(pinCount));
          ic.setName(nameAttr);
          // Translate control points.
          for (int j = 0; j < ic.getControlPointCount(); j++) {
            Point p = new Point(ic.getControlPoint(j));
            p.translate(point1.x, point1.y);
            ic.setControlPoint(p, j);
          }
          ic.setStringValue(valueAttr);
          component = ic;
        } else if (nodeName.equalsIgnoreCase("pot")) {
          LOG.debug("Recognized " + nodeName);
          PotentiometerPanel pot = new PotentiometerPanel();
          pot.setBodyDiameter(Size.mm(14));
          pot.setSpacing(Size.in(0.2));
          pot.setName(nameAttr);
          try {
            pot.setValue(Value.parse(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          String taperAttr = node.getAttributes().getNamedItem("Taper").getNodeValue();
          if ("Linear".equals(taperAttr)) {
            pot.setTaper(Taper.LIN);
          } else if ("Audio".equals(taperAttr)) {
            pot.setTaper(Taper.LOG);
          } else if ("Reverse Audio".equals(taperAttr)) {
            pot.setTaper(Taper.REV_LOG);
          }
          // Pin spacing, we'll need to move pot around a bit.
          int delta = Constants.PIXELS_PER_INCH / 5;
          if (x1Attr < x2Attr) {
            pot.setOrientation(Orientation.DEFAULT);
            for (int j = 0; j < pot.getControlPointCount(); j++) {
              Point p = new Point(pot.getControlPoint(j));
              p.translate(point1.x - delta, point1.y);
              pot.setControlPoint(p, j);
            }
          } else if (x1Attr > x2Attr) {
            pot.setOrientation(Orientation._180);
            for (int j = 0; j < pot.getControlPointCount(); j++) {
              Point p = new Point(pot.getControlPoint(j));
              p.translate(point1.x + delta, point1.y);
              pot.setControlPoint(p, j);
            }
          } else if (y1Attr < y2Attr) {
            pot.setOrientation(Orientation._90);
            for (int j = 0; j < pot.getControlPointCount(); j++) {
              Point p = new Point(pot.getControlPoint(j));
              p.translate(point1.x, point1.y - delta);
              pot.setControlPoint(p, j);
            }
          } else if (y1Attr > y2Attr) {
            pot.setOrientation(Orientation._270);
            for (int j = 0; j < pot.getControlPointCount(); j++) {
              Point p = new Point(pot.getControlPoint(j));
              p.translate(point1.x, point1.y + delta);
              pot.setControlPoint(p, j);
            }
          }
          component = pot;
        } else if (nodeName.equalsIgnoreCase("trimmer")) {
          LOG.debug("Recognized " + nodeName);
          TrimmerPotentiometer trimmer = new TrimmerPotentiometer();
          trimmer.setName(nameAttr);
          int sizeX = Math.abs(x1Attr - x2Attr);
          int sizeY = Math.abs(y1Attr - y2Attr);
          TrimmerType trimmerType = null;
          Orientation orientation = null;
          int dx = 0;
          int dy = 0;
          // determine type by size
          if (Math.min(sizeX, sizeY) == 0 && Math.max(sizeX, sizeY) == 1) {
            trimmerType = TrimmerType.VERTICAL_INLINE;

            if (y1Attr > y2Attr) {
              orientation = Orientation.DEFAULT;
              dy = -1;
            } else if (x1Attr > x2Attr) {
              orientation = Orientation._90;
              dx = 1;
            } else if (y1Attr < y2Attr) {
              orientation = Orientation._180;
              dy = 1;
            } else if (x1Attr < x2Attr) {
              orientation = Orientation._270;
              dx = -1;
            }
          } else if (Math.min(sizeX, sizeY) == 1 && Math.max(sizeX, sizeY) == 1) {
            trimmerType = TrimmerType.VERTICAL_OFFSET;

            if (x1Attr > x2Attr && y1Attr > y2Attr) {
              orientation = Orientation.DEFAULT;
              dx = -1;
              dy = -1;
            } else if (x1Attr < x2Attr && y1Attr > y2Attr) {
              orientation = Orientation._90;
              dx = 1;
              dy = -1;
            } else if (x1Attr < x2Attr && y1Attr < y2Attr) {
              orientation = Orientation._180;
              dx = 1;
              dy = 1;
            } else if (x1Attr > x2Attr && y1Attr < y2Attr) {
              orientation = Orientation._270;
              dx = -1;
              dy = 1;
            }
          } else if (Math.min(sizeX, sizeY) == 1 && Math.max(sizeX, sizeY) == 2) {
            trimmerType = TrimmerType.VERTICAL_OFFSET_BIG_GAP;

            if (x1Attr > x2Attr && y1Attr > y2Attr) {
              if (sizeX == 2) {
                orientation = Orientation.DEFAULT;
                dx = -2;
                dy = -1;
              } else {
                orientation = Orientation._90;
                dx = 1;
                dy = -2;
              }
            } else if (x1Attr < x2Attr && y1Attr > y2Attr) {
              if (sizeX == 2) {
                orientation = Orientation._180;
                dx = 2;
                dy = 1;
              } else {
                orientation = Orientation._90;
                dx = 1;
                dy = -2;
              }
            } else if (x1Attr < x2Attr && y1Attr < y2Attr) {
              if (sizeX == 2) {
                orientation = Orientation._180;
                dx = 2;
                dy = 1;
              } else {
                orientation = Orientation._270;
                dx = -1;
                dy = 2;
              }
            } else if (x1Attr > x2Attr && y1Attr < y2Attr) {
              if (sizeX == 2) {
                orientation = Orientation.DEFAULT;
                dx = -2;
                dy = -1;
              } else {
                orientation = Orientation._270;
                dx = -1;
                dy = 2;
              }
            }
          } else if (Math.min(sizeX, sizeY) == 1 && Math.max(sizeX, sizeY) == 4) {
            trimmerType = TrimmerType.FLAT_LARGE;

            if (x1Attr > x2Attr && y1Attr > y2Attr) {
              if (sizeX == 4) {
                orientation = Orientation.DEFAULT;
                dx = -4;
                dy = -1;
              } else {
                orientation = Orientation._90;
                dx = 1;
                dy = -4;
              }
            } else if (x1Attr < x2Attr && y1Attr > y2Attr) {
              if (sizeX == 4) {
                orientation = Orientation._180;
                dx = 4;
                dy = 1;
              } else {
                orientation = Orientation._90;
                dx = 1;
                dy = -4;
              }
            } else if (x1Attr < x2Attr && y1Attr < y2Attr) {
              if (sizeX == 4) {
                orientation = Orientation._180;
                dx = 4;
                dy = 1;
              } else {
                orientation = Orientation._270;
                dx = -1;
                dy = 4;
              }
            } else if (x1Attr > x2Attr && y1Attr < y2Attr) {
              if (sizeX == 4) {
                orientation = Orientation.DEFAULT;
                dx = -4;
                dy = -1;
              } else {
                orientation = Orientation._270;
                dx = -1;
                dy = 4;
              }
            }
          }

          if (trimmerType == null || orientation == null) {
            String message = "Unsupported trimmer dimensions";
            LOG.debug(message);
            if (!warnings.contains(message)) {
              warnings.add(message);
            }
          } else {
            try {
              trimmer.setValue(Value.parse(valueAttr));
            } catch (Exception e) {
              LOG.debug("Could not set value of " + nameAttr);
            }
            trimmer.setType(trimmerType);
            trimmer.setOrientation(orientation);
            // scale nudges
            dx *= V1_GRID_SPACING.convertToPixels();
            dy *= V1_GRID_SPACING.convertToPixels();
            // Translate control points.
            for (int j = 0; j < trimmer.getControlPointCount(); j++) {
              Point p = new Point(trimmer.getControlPoint(j));
              p.translate(point1.x + dx, point1.y + dy);
              trimmer.setControlPoint(p, j);
            }
            component = trimmer;
          }
        } else {
          String message = "Could not recognize component type " + nodeName;
          LOG.debug(message);
          if (!warnings.contains(message)) {
            warnings.add(message);
          }
        }
        if (component != null) {
          component.setDisplay(Display.NAME);
          if (component instanceof AbstractTransparentComponent) {
            ((AbstractTransparentComponent) component).setAlpha(100);
          }
          project.getComponents().add(component);
        }
      }
    }

    int minY = y;
    for (AbstractComponent c : project.getComponents()) {
      for (int i = 0; i < c.getControlPointCount(); i++) {
        Point p = c.getControlPoint(i);
        if (p.y < minY) {
          minY = p.y;
        }
      }
    }

    // Add title and credits
    Label titleLabel = new Label();
    titleLabel.setColor(Color.blue);
    titleLabel.setFontSize(24);
    titleLabel.setStringValue(project.getTitle());
    titleLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
    titleLabel.setControlPoint(
        project
            .getGrid()
            .snapToGrid(
                new Point(
                    x + boardWidth / 2,
                    (int) (minY - Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue() * 5))),
        0);
    project.getComponents().add(titleLabel);

    Label creditsLabel = new Label();
    creditsLabel.setFontSize(16);
    creditsLabel.setStringValue(project.getAuthor());
    creditsLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
    creditsLabel.setControlPoint(
        project
            .getGrid()
            .snapToGrid(
                new Point(
                    x + boardWidth / 2,
                    (int) (minY - Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue() * 4))),
        0);
    project.getComponents().add(creditsLabel);

    // Add BOM at the bottom
    BillOfMaterials bom = new BillOfMaterials();
    int bomSize = (int) bom.getSize().convertToPixels();
    bom.setControlPoint(
        project
            .getGrid()
            .snapToGrid(
                new Point(
                    x + (boardWidth - bomSize) / 2,
                    (int) (y + boardHeight + 2 * V1_GRID_SPACING.convertToPixels()))),
        0);
    project.getComponents().add(bom);

    // Sort by z-order
    Collections.sort(
        project.getComponents(), ComparatorFactory.getInstance().getComponentZOrderComparator());
    return project;
  }

  private Point convertV1CoordinatesToV3Point(Point reference, int x, int y) {
    Point point = new Point(reference);
    point.translate(
        (int) (x * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue()),
        (int) (y * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue()));
    return point;
  }

  private Color parseV1Color(String color) {
    if (color.toLowerCase().equals("brown")) {
      return new Color(139, 69, 19);
    }
    try {
      Field field = Color.class.getDeclaredField(color.toLowerCase());
      return (Color) field.get(null);
    } catch (Exception e) {
      LOG.error("Could not parse color \"" + color + "\"", e);
      return Color.black;
    }
  }

  private int myRandom(int range, Random r) {
    range = (range * 2) / 3;
    int rand = r.nextInt(range) - range / 2;
    if (Math.abs(rand) < range / 3) {
      rand = myRandom(range, r);
    }
    return rand;
  }

  private long randSeed = 0;

  /*
  private int randInt(int range) {
    long newSeed = randSeed * 0x08088405 + 1;
    randSeed = newSeed;
    return (int) ((long) newSeed * range >> 32);
  }
  */
}

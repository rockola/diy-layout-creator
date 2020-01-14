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

package org.diylc.parsing;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.boards.AbstractBoard;
import org.diylc.components.boards.BlankBoard;
import org.diylc.components.boards.PerfBoard;
import org.diylc.components.boards.VeroBoard;
import org.diylc.components.connectivity.AWG;
import org.diylc.components.connectivity.CopperTrace;
import org.diylc.components.connectivity.CurvedTrace;
import org.diylc.components.connectivity.Eyelet;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.connectivity.Line;
import org.diylc.components.connectivity.SolderPad;
import org.diylc.components.connectivity.TraceCut;
import org.diylc.components.electromechanical.MiniToggleSwitch;
import org.diylc.components.electromechanical.ToggleSwitchType;
import org.diylc.components.misc.GroundSymbol;
import org.diylc.components.passive.AxialElectrolyticCapacitor;
import org.diylc.components.passive.AxialFilmCapacitor;
import org.diylc.components.passive.CapacitorSymbol;
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.components.passive.PotentiometerSymbol;
import org.diylc.components.passive.RadialCeramicDiskCapacitor;
import org.diylc.components.passive.RadialElectrolytic;
import org.diylc.components.passive.RadialFilmCapacitor;
import org.diylc.components.passive.Resistor;
import org.diylc.components.passive.ResistorSymbol;
import org.diylc.components.semiconductors.BJTSymbol;
import org.diylc.components.semiconductors.DIL_IC;
import org.diylc.components.semiconductors.DIL_IC.PinCount;
import org.diylc.components.semiconductors.DiodePlastic;
import org.diylc.components.semiconductors.DiodeSymbol;
import org.diylc.components.semiconductors.ICPointCount;
import org.diylc.components.semiconductors.ICSymbol;
import org.diylc.components.semiconductors.LEDSymbol;
import org.diylc.components.semiconductors.TransistorTO92;
import org.diylc.components.shapes.Ellipse;
import org.diylc.components.shapes.Rectangle;
import org.diylc.components.tube.PentodeSymbol;
import org.diylc.components.tube.TriodeSymbol;
import org.diylc.components.tube.TubeSocket;
import org.diylc.components.tube.TubeSocket.Base;
import org.diylc.core.Project;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.Presenter;
import org.diylc.utils.Constants;

import org.nfunk.jep.JEP;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class V2FileParser implements IOldFileParser {

  private static final Logger LOG = LogManager.getLogger(V2FileParser.class);
  private static final Size V2_GRID_SPACING = new Size(0.1d, SizeUnit.in);

  @Override
  public boolean canParse(String version) {
    return version.equals("2.0");
  }

  public static Size parseString(String text) {
    JEP parser = new JEP();
    parser.addStandardConstants();
    parser.addStandardFunctions();
    parser.setImplicitMul(true);
    parser.addConstant("mm", Constants.PIXELS_PER_INCH / 25.4f);
    parser.addConstant("cm", Constants.PIXELS_PER_INCH / 2.54f);
    parser.addConstant("in", Constants.PIXELS_PER_INCH * 1f);
    parser.addConstant("grid", Constants.PIXELS_PER_INCH * 0.1f);
    parser.addConstant("degree", Constants.DEGREES_PER_RADIAN);
    parser.addConstant("deg", Constants.DEGREES_PER_RADIAN);
    parser.addConstant("px", 1f);
    parser.parseExpression(text);

    Double value = parser.getValue(); // in pixels
    boolean metric = App.getBoolean(IPlugInPort.Key.METRIC, true);

    return new Size(
        value / Constants.PIXELS_PER_INCH * (metric ? 25.4f : 1.0f),
        metric ? SizeUnit.mm : SizeUnit.in);
  }

  @Override
  public Project parseFile(Element root, List<String> warnings) {
    Project project = new Project();
    String projectName = root.getAttribute("projectName");
    String credits = root.getAttribute("credits");
    String width = root.getAttribute("width");
    String height = root.getAttribute("height");
    Size wp = parseString(width);
    Size hp = parseString(height);

    project.setTitle(projectName);
    project.setAuthor(credits);
    project.setGridSpacing(V2_GRID_SPACING);
    project.setDescription("V2FileParser");
    project.setWidth(wp);
    project.setHeight(hp);

    NodeList childNodes = root.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if (node.getNodeName().equalsIgnoreCase("component")) {
          LOG.debug(node.getAttributes().getNamedItem("name").getNodeValue());
          NodeList unuci = node.getChildNodes();
          Node properties = unuci.item(1);
          Node points = unuci.item(3);

          NodeList propertyList = properties.getChildNodes();
          NodeList pointList = points.getChildNodes();
          Color cl = null;
          String comName = "";
          String shape = "circle";
          String display = "";
          double angle = 0;
          double distance = 1;
          double value = -9999;
          String value_s = "";
          int transparency = 100;
          int pins = 6;
          Size sizePro = new Size(5.0, SizeUnit.mm);
          Size thicknessPro = new Size(5.0, SizeUnit.mm);
          Size diameterPro = new Size(5.0, SizeUnit.mm);
          Size lengthPro = new Size(0.0, SizeUnit.mm);
          Size bodyPro = new Size(5.0, SizeUnit.mm);
          Size spacingPro = new Size(0.0, SizeUnit.mm);
          Size radiusPro = new Size(1.0, SizeUnit.mm);
          CapacitanceUnit cp = CapacitanceUnit.nF;
          ResistanceUnit ru = ResistanceUnit.K;

          for (int j = 0; j < propertyList.getLength(); j++) {
            if (propertyList.item(j).getNodeName().equalsIgnoreCase("property")) {
              // Note: propertyName is transformed to lowercase for comparison purposes
              String propertyName = propertyList.item(j).getAttributes()
                                    .getNamedItem("name").getNodeValue().toLowerCase();
              String nodeValue = propertyList.item(j).getAttributes()
                                 .getNamedItem("value").getNodeValue();
              switch (propertyName) {
                case "color":
                  String color = nodeValue;
                  String hexR = color.substring(0, 2);
                  int valueR = Integer.parseInt(hexR, 16);
                  String hexG = color.substring(2, 4);
                  int valueG = Integer.parseInt(hexG, 16);
                  String hexB = color.substring(4, 6);
                  int valueB = Integer.parseInt(hexB, 16);
                  cl = new Color(valueR, valueG, valueB);
                  break;
                case "name":
                  comName = nodeValue;
                  break;
                case "thickness":
                  thicknessPro = parseString(nodeValue);
                  break;
                case "size":
                  sizePro = parseString(nodeValue);
                  break;
                case "shape":
                  shape = nodeValue;
                  break;
                case "diameter":
                  diameterPro = parseString(nodeValue);
                  break;
                case "length":
                  lengthPro = parseString(nodeValue);
                  break;
                case "display":
                  display = nodeValue;
                  break;
                case "body":
                  bodyPro = parseString(nodeValue);
                  break;
                case "spacing":
                  spacingPro = parseString(nodeValue);
                  break;
                case "angle":
                  Size anglePro = parseString(nodeValue);
                  angle = anglePro.getValue() * Constants.PIXELS_PER_INCH
                          / 25.4f
                          / Constants.DEGREES_PER_RADIAN;
                  angle = Math.floor(angle);
                  break;
                case "distance":
                  distance = Double.parseDouble(nodeValue.replaceAll("[^0-9.]", ""));
                  break;
                case "radius":
                  radiusPro = parseString(nodeValue);
                  break;
                case "value":
                  String pr8 = nodeValue;
                  if (pr8 != "") {
                    value_s = pr8;
                    String pr9 = pr8.replaceAll("[^0-9.]", "");
                    String pr10 = pr8.replaceAll("[^a-zA-Z]", "");
                    if (pr10.equals("pF")) {
                      cp = CapacitanceUnit.pF;
                    } else if (pr10.equals("nF")) {
                      cp = CapacitanceUnit.nF;
                    } else if (pr10.equals("F")) {
                      cp = CapacitanceUnit.F;
                    } else if (pr10.equals("mF")) {
                      cp = CapacitanceUnit.mF;
                    } else if (pr10.equals("uF")) {
                      cp = CapacitanceUnit.uF;
                    } else if (pr10.equals("K")) {
                      ru = ResistanceUnit.K;
                    } else if (pr10.equals("M")) {
                      ru = ResistanceUnit.M;
                    } else if (pr10.equals("R")) {
                      ru = ResistanceUnit.R;
                    }
                    try {
                      value = Double.parseDouble(pr9);
                    } catch (Exception e) {
                      /* TODO: maybe we should actually do something
                         when parsing fails, such as notify the user
                         that there's something wrong with the project
                         file
                      */
                      LOG.debug("threw exception parsing " + pr9 + " as Double", e);
                    }
                  }
                  break;
                case "transparency":
                  transparency = (int) (Double.parseDouble(nodeValue.replaceAll("[^0-9.]", ""))
                                        * 100);
                  break;
                case "pins":
                  pins = Integer.parseInt(nodeValue.replaceAll("[^0-9.]", ""));
                  break;
                default:
                  LOG.error("unknown property name {}", propertyName);
              }
            } // property
          }

          ArrayList<Point> tacke = new ArrayList<Point>();

          for (int k = 1; k < pointList.getLength(); k += 2) {
            int iks = Integer.parseInt(
                pointList.item(k).getAttributes().getNamedItem("x").getNodeValue());
            int ipsilon = Integer.parseInt(
                pointList.item(k).getAttributes().getNamedItem("y").getNodeValue());
            tacke.add(new Point(iks, ipsilon));
          }

          String lcNodeName =
              node.getAttributes().getNamedItem("name").getNodeValue().toLowerCase();
          AbstractBoard board = null;
          AbstractLeadedComponent trace = null;
          AbstractLeadedComponent capacitor = null;
          TubeSocket ts = null;
          Point point;
          int x;
          int y;
          switch (lcNodeName) {
            case "blank board":
              board = new BlankBoard();
              if (cl != null) {
                board.setBoardColor(cl);
              }
              board.setBorderColor(Color.black);
              if (comName != "") {
                board.setName(comName);
              } else {
                board.setName("Main board");
              }
              board.setControlPoint(tacke.get(0), 0);
              board.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(board);
              break;
            case "perfboard":
              PerfBoard perf = new PerfBoard();
              perf.setBoardColor(Color.white);
              perf.setBorderColor(Color.black);
              perf.setName(comName != "" ? comName : "Main board");
              perf.setSpacing(new Size(0.07, SizeUnit.in));
              perf.setControlPoint(tacke.get(0), 0);
              perf.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(perf);
              break;
            case "copper trace":
              CopperTrace copperTrace = new CopperTrace();
              copperTrace.setName(comName != "" ? comName : "t");
              if (cl != null) {
                copperTrace.setLeadColor(cl);
              }
              copperTrace.setThickness(thicknessPro);
              copperTrace.setControlPoint(tacke.get(0), 0);
              copperTrace.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(copperTrace);
              break;
            case "copper trace curved":
              CurvedTrace curvedTrace = new CurvedTrace();
              curvedTrace.setName(comName != "" ? comName : "t");
              if (cl != null) {
                curvedTrace.setLeadColor(cl);
              }
              curvedTrace.setThickness(thicknessPro);
              curvedTrace.setControlPoint(tacke.get(0), 0);
              curvedTrace.setControlPoint(tacke.get(1), 1);
              curvedTrace.setControlPoint(tacke.get(2), 2);
              curvedTrace.setControlPoint(tacke.get(3), 3);
              project.getComponents().add(curvedTrace);
              break;
            case "eyelet":
              Eyelet eyelet = new Eyelet();
              if (comName != "") {
                eyelet.setName(comName);
              } else {
                eyelet.setName("eyelet");
              }
              eyelet.setSize(sizePro);
              eyelet.setControlPoint(tacke.get(0), 0);
              project.getComponents().add(eyelet);
              break;
            case "solder pad":
              SolderPad pad = new SolderPad();
              if (comName != "") {
                pad.setName(comName);
              } else {
                pad.setName("pad");
              }
              pad.setSize(sizePro);
              if (shape.equals("square")) {
                pad.setType(SolderPad.Type.SQUARE);
              } else {
                pad.setType(SolderPad.Type.ROUND);
              }
              pad.setControlPoint(tacke.get(0), 0);
              project.getComponents().add(pad);
              break;
            case "stripboard":
              VeroBoard vero = new VeroBoard();
              if (comName != "") {
                vero.setName(comName);
              } else {
                vero.setName("Main board");
              }
              vero.setControlPoint(tacke.get(0), 0);
              vero.setControlPoint(tacke.get(1), 1);
              vero.setSpacing(new Size(0.08, SizeUnit.in));
              project.getComponents().add(vero);
              break;
            case "trace cut":
              TraceCut cut = new TraceCut();
              if (comName != "") {
                cut.setName(comName);
              } else {
                cut.setName("cut");
              }
              cut.setControlPoint(tacke.get(0), 0);
              project.getComponents().add(cut);
              break;
            case "switch":
              MiniToggleSwitch sw = new MiniToggleSwitch();
              sw.setValue(ToggleSwitchType.DPDT);
              if (comName != "") {
                sw.setName(comName);
              } else {
                sw.setName("sw");
              }
              x = (int) (tacke.get(0).getX() + 20);
              y = (int) (tacke.get(0).getY() + 10);
              point = new Point(x, y);
              sw.setSpacing(new Size(0.1, SizeUnit.in));
              sw.setControlPoint(point, 0);
              x = (int) (tacke.get(0).getX() + 40);
              y = (int) (tacke.get(0).getY() + 10);
              point = new Point(x, y);
              sw.setControlPoint(point, 1);
              x = (int) (tacke.get(0).getX() + 20);
              y = (int) (tacke.get(0).getY() + 30);
              point = new Point(x, y);
              sw.setControlPoint(point, 2);
              x = (int) (tacke.get(0).getX() + 40);
              y = (int) (tacke.get(0).getY() + 30);
              point = new Point(x, y);
              sw.setControlPoint(point, 3);
              x = (int) (tacke.get(0).getX() + 20);
              y = (int) (tacke.get(0).getY() + 50);
              point = new Point(x, y);
              sw.setControlPoint(point, 4);
              x = (int) (tacke.get(0).getX() + 40);
              y = (int) (tacke.get(0).getY() + 50);
              point = new Point(x, y);
              sw.setControlPoint(point, 5);
              project.getComponents().add(sw);
              break;
            case "wire":
              HookupWire hw = new HookupWire();
              if (comName != "") {
                hw.setName(comName);
              } else {
                hw.setName("hookup_wire");
              }
              ArrayList<Double> awgt = new ArrayList<Double>();
              awgt.add(2 * 3.264);
              awgt.add(2 * 2.588);
              awgt.add(2 * 2.053);
              awgt.add(2 * 1.628);
              awgt.add(2 * 1.291);
              awgt.add(2 * 1.024);
              awgt.add(2 * 0.812);
              awgt.add(2 * 0.644);
              awgt.add(2 * 0.511);
              awgt.add(2 * 0.405);
              awgt.add(2 * 0.321);
              awgt.add(2 * 0.255);
              int num = 0;
              for (int q = 1; q < awgt.size(); q++) {
                if (thicknessPro.getValue() <= awgt.get(q - 1)
                    && thicknessPro.getValue() > awgt.get(q)) {
                  num = q - 1;
                }
              }
              num = num * 2 + 8;
              switch (num) {
                case 8: hw.setGauge(AWG._8); break;
                case 10: hw.setGauge(AWG._10); break;
                case 12: hw.setGauge(AWG._12); break;
                case 14: hw.setGauge(AWG._14); break;
                case 16: hw.setGauge(AWG._16); break;
                case 18: hw.setGauge(AWG._18); break;
                case 20: hw.setGauge(AWG._20); break;
                case 22: hw.setGauge(AWG._22); break;
                case 24: hw.setGauge(AWG._24); break;
                case 26: hw.setGauge(AWG._26); break;
                case 28: hw.setGauge(AWG._28); break;
                case 30: hw.setGauge(AWG._30); break;
                default:
                  LOG.error("Unknown gauge {}", num);
              }
              hw.setLeadColor(cl);
              hw.setControlPoint(tacke.get(0), 0);
              hw.setControlPoint(tacke.get(1), 1);
              hw.setControlPoint(tacke.get(2), 2);
              hw.setControlPoint(tacke.get(3), 3);
              project.getComponents().add(hw);
              break;
            case "electrolytic (axial)":
              AxialElectrolyticCapacitor aec = new AxialElectrolyticCapacitor();
              if (comName != "") {
                aec.setName(comName);
              } else {
                aec.setName("A_E_C");
              }
              aec.setAlpha((byte) transparency);
              if (value != -9999) {
                aec.setValue(new Capacitance(value, cp));
              }
              aec.setWidth(diameterPro);
              aec.setLength(lengthPro);
              if (display.equals("Name")) {
                aec.setDisplay(Display.NAME);
              } else {
                aec.setDisplay(Display.VALUE);
              }
              aec.setControlPoint(tacke.get(0), 0);
              aec.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(aec);
              break;
            case "capacitor (axial)":
              AxialFilmCapacitor afc = new AxialFilmCapacitor();
              if (comName != "") {
                afc.setName(comName);
              }
              afc.setAlpha((byte) transparency);
              if (value != -9999) {
                afc.setValue(new Capacitance(value, cp));
              } else {
                afc.setName("A_F_C");
              }
              if (display.equals("Name")) {
                afc.setDisplay(Display.NAME);
              } else {
                afc.setDisplay(Display.VALUE);
              }
              afc.setWidth(thicknessPro);
              afc.setLength(lengthPro);
              afc.setControlPoint(tacke.get(0), 0);
              afc.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(afc);
              break;
            case "capacitor (ceramic)":
              RadialCeramicDiskCapacitor rcdc = new RadialCeramicDiskCapacitor();
              if (comName != "") {
                rcdc.setName(comName);
              } else {
                rcdc.setName("Radial_Ceramic_Capacitor");
              }
              rcdc.setAlpha((byte) transparency);
              if (value != -9999) {
                rcdc.setValue(new Capacitance(value, cp));
              }
              if (display.equals("Name")) {
                rcdc.setDisplay(Display.NAME);
              } else {
                rcdc.setDisplay(Display.VALUE);
              }
              rcdc.setWidth(thicknessPro);
              rcdc.setLength(lengthPro);
              rcdc.setControlPoint(tacke.get(0), 0);
              rcdc.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(rcdc);
              break;
            case "capacitor (radial)":
              RadialFilmCapacitor rfc = new RadialFilmCapacitor();
              if (comName != "") {
                rfc.setName(comName);
              } else {
                rfc.setName("Radial_Film_Capacitor");
              }
              rfc.setAlpha((byte) transparency);
              if (value != -9999) {
                rfc.setValue(new Capacitance(value, cp));
              }
              if (display.equals("Name")) {
                rfc.setDisplay(Display.NAME);
              } else {
                rfc.setDisplay(Display.VALUE);
              }
              rfc.setWidth(thicknessPro);
              rfc.setLength(lengthPro);
              rfc.setControlPoint(tacke.get(0), 0);
              rfc.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(rfc);
              break;
            case "electrolytic (radial)":
              RadialElectrolytic re = new RadialElectrolytic();
              if (comName != "") {
                re.setName(comName);
              } else {
                re.setName("Radial_Electrolytic_Capacitor");
              }
              if (value != -9999) {
                re.setValue(new Capacitance(value, cp));
              }
              re.setAlpha((byte) transparency);
              re.setLength(diameterPro);
              re.setControlPoint(tacke.get(0), 0);
              re.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(re);
              break;
            case "potentiometer lug":
              PotentiometerPanel panel = new PotentiometerPanel();
              if (comName != "") {
                panel.setName(comName);
              } else {
                panel.setName("potentiometer_panel");
              }
              if (value != -9999) {
                panel.setValue(new Resistance(value, ru));
              }
              panel.setAlpha((byte) transparency);
              if (angle > 45 && angle <= 135) {
                panel.setOrientation(Orientation._90);
              } else if (angle > 135 && angle <= 225) {
                panel.setOrientation(Orientation._180);
              } else if (angle > 225 && angle <= 315) {
                panel.setOrientation(Orientation._270);
              }
              x = (int) (tacke.get(0).getX() - 20);
              y = (int) tacke.get(0).getY();
              point = new Point(x, y);
              panel.setControlPoint(point, 0);
              panel.setBodyDiameter(bodyPro);
              panel.setSpacing(spacingPro);
              panel.setLugDiameter(new Size(0.1, SizeUnit.in));
              project.getComponents().add(panel);
              break;
            case "resistor":
            case "resistor standing":
              Resistor resistor = new Resistor();
              if (comName != "") {
                resistor.setName(comName);
              } else {
                resistor.setName("resistor");
              }
              if (value != -9999) {
                resistor.setValue(new Resistance(value, ru));
              }
              resistor.setAlpha((byte) transparency);
              resistor.setWidth(diameterPro);
              if (lengthPro.getValue() != 0) {
                resistor.setLength(lengthPro);
              }
              resistor.setControlPoint(tacke.get(0), 0);
              resistor.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(resistor);
              break;
            case "capacitor symbol":
              CapacitorSymbol cap = new CapacitorSymbol();
              if (comName != "") {
                cap.setName(comName);
              } else {
                cap.setName("cap_simbol");
              }
              if (value != -9999) {
                cap.setValue(new Capacitance(value, cp));
              }
              cap.setLength(new Size(distance, SizeUnit.mm));
              cap.setWidth(lengthPro);
              cap.setBodyColor(cl);
              cap.setBorderColor(cl);
              cap.setControlPoint(tacke.get(0), 0);
              cap.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(cap);
              break;
            case "diode symbol":
              DiodeSymbol dio = new DiodeSymbol();
              if (comName != "") {
                dio.setName(comName);
              } else {
                dio.setName("dp");
              }
              if (value_s != "") {
                dio.setValue(value_s);
              }
              dio.setBodyColor(cl);
              dio.setWidth(sizePro);
              dio.setControlPoint(tacke.get(0), 0);
              dio.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(dio);
              break;
            case "ground symbol":
              GroundSymbol gs = new GroundSymbol();
              if (comName != "") {
                gs.setName(comName);
              } else {
                gs.setName("gs");
              }
              gs.setSize(sizePro);
              gs.setControlPoint(tacke.get(0), 0);
              project.getComponents().add(gs);
              break;
            case "led symbol":
              LEDSymbol ls = new LEDSymbol();
              if (comName != "") {
                ls.setName(comName);
              } else {
                ls.setName("ls");
              }
              if (value_s != "") {
                ls.setValue(value_s);
              }
              ls.setBodyColor(cl);
              ls.setWidth(sizePro);
              ls.setControlPoint(tacke.get(0), 0);
              ls.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(ls);
              break;
            case "pentode":
              PentodeSymbol ps = new PentodeSymbol();
              if (comName != "") {
                ps.setName(comName);
              } else {
                ps.setName("ps");
              }
              ps.setColor(cl);
              if (value_s != "") ps.setValue(value_s);
              x = (int) (tacke.get(0).getX() - 70);
              y = (int) tacke.get(0).getY();
              point = new Point(x, y);
              ps.setControlPoint(point, 0);
              project.getComponents().add(ps);
              break;
            case "electrolytic symbol":
              CapacitorSymbol cs = new CapacitorSymbol();
              if (comName != "") {
                cs.setName(comName);
              } else {
                cs.setName("cs");
              }
              if (value != -9999) {
                cs.setValue(new Capacitance(value, cp));
              }
              cs.setLength(new Size(distance, SizeUnit.mm));
              cs.setWidth(lengthPro);
              cs.setPolarized(true);
              cs.setBorderColor(cl);
              cs.setControlPoint(tacke.get(0), 0);
              cs.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(cs);
              break;
            case "opamp symbol":
              ICSymbol ic = new ICSymbol();
              if (comName != "") {
                ic.setName(comName);
              } else {
                ic.setName("ics");
              }
              if (value_s != "") ic.setValue(value_s);
              ic.setAlpha((byte) transparency);
              x = (int) (tacke.get(0).getX() - 70);
              y = (int) tacke.get(0).getY() - 20;
              point = new Point(x, y);
              ic.setControlPoint(point, 0);
              ic.setIcPointCount(ICPointCount._3);
              project.getComponents().add(ic);
              break;
            case "potentiometer symbol":
              PotentiometerSymbol potsym = new PotentiometerSymbol();
              if (comName != "") {
                potsym.setName(comName);
              } else {
                potsym.setName("ps");
              }
              potsym.setOrientation(Orientation._270);
              if (value_s != "") {
                potsym.setValue(value_s);
              }
              potsym.setColor(cl);
              x = (int) (tacke.get(0).getX() + 40);
              y = (int) tacke.get(0).getY() + 40;
              point = new Point(x, y);
              potsym.setControlPoint(point, 0);
              x = (int) (tacke.get(1).getX() + 40);
              y = (int) tacke.get(1).getY() + 40;
              point = new Point(x, y);
              potsym.setControlPoint(point, 1);
              project.getComponents().add(potsym);
              break;
            case "resistor symbol":
              ResistorSymbol rs = new ResistorSymbol();
              if (comName != "") {
                rs.setName(comName);
              } else {
                rs.setName("rs");
              }
              if (value != -9999) {
                rs.setValue(new Resistance(value, ru));
              }
              rs.setWidth(sizePro);
              rs.setLeadColor(cl);
              rs.setBorderColor(cl);
              rs.setBodyColor(cl);
              rs.setControlPoint(tacke.get(0), 0);
              rs.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(rs);
              break;
            case "triode":
              TriodeSymbol triodesym = new TriodeSymbol();
              if (comName != "") {
                triodesym.setName(comName);
              } else {
                triodesym.setName("ts");
              }
              if (value_s != "") {
                triodesym.setValue(value_s);
              }
              triodesym.setColor(cl);
              x = (int) (tacke.get(0).getX() - 30);
              y = (int) tacke.get(0).getY();
              point = new Point(x, y);
              triodesym.setControlPoint(point, 0);
              project.getComponents().add(triodesym);
              break;
            case "diode":
              DiodePlastic dp = new DiodePlastic();
              if (comName != "") {
                dp.setName(comName);
              } else {
                dp.setName("dp");
              }
              if (value_s != "") {
                dp.setValue(value_s);
              }
              dp.setAlpha((byte) transparency);
              if (display.equals("Name")) {
                dp.setDisplay(Display.NAME);
              } else {
                dp.setDisplay(Display.VALUE);
              }
              dp.setLength(lengthPro);
              dp.setWidth(diameterPro);
              dp.setControlPoint(tacke.get(0), 0);
              dp.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(dp);
              break;
            case "ic dil":
              DIL_IC dil = new DIL_IC();
              if (comName != "") {
                dil.setName(comName);
              } else {
                dil.setName("dil");
              }
              switch (pins) {
                case 4: dil.setPinCount(PinCount._4); break;
                case 6: dil.setPinCount(PinCount._6); break;
                case 8: dil.setPinCount(PinCount._8); break;
                case 10: dil.setPinCount(PinCount._10); break;
                case 12: dil.setPinCount(PinCount._12); break;
                case 14: dil.setPinCount(PinCount._14); break;
                case 16: dil.setPinCount(PinCount._16); break;
                case 18: dil.setPinCount(PinCount._18); break;
                case 20: dil.setPinCount(PinCount._20); break;
                case 22: dil.setPinCount(PinCount._22); break;
                case 24: dil.setPinCount(PinCount._24); break;
                case 26: dil.setPinCount(PinCount._26); break;
                case 28: dil.setPinCount(PinCount._28); break;
                case 30: dil.setPinCount(PinCount._30); break;
                case 32: dil.setPinCount(PinCount._32); break;
                case 34: dil.setPinCount(PinCount._34); break;
                case 36: dil.setPinCount(PinCount._36); break;
                case 38: dil.setPinCount(PinCount._38); break;
                case 40: dil.setPinCount(PinCount._40); break;
                case 42: dil.setPinCount(PinCount._42); break;
                case 44: dil.setPinCount(PinCount._44); break;
                case 46: dil.setPinCount(PinCount._46); break;
                case 48: dil.setPinCount(PinCount._48); break;
                case 50: dil.setPinCount(PinCount._50); break;
                default:
                  LOG.error("pin count error, cannot handle {} pins", pins);
              }
              dil.setAlpha((byte) transparency);
              if (value_s != "") {
                dil.setValue(value_s);
              }
              dil.setRowSpacing(spacingPro);
              dil.setControlPoint(tacke.get(0), 0);
              dil.setPinSpacing(new Size(0.1d, SizeUnit.in));
              project.getComponents().add(dil);
              break;
            case "transistor":
              TransistorTO92 trans = new TransistorTO92();
              if (comName != "") {
                trans.setName(comName);
              } else {
                trans.setName("tr");
              }
              trans.setAlpha((byte) transparency);
              if (value_s != "") {
                trans.setValue(value_s);
              }
              trans.setPinSpacing(new Size(0.1, SizeUnit.in));
              Point point0 = null;
              Point point1 = null;
              // note: code used to just compare angle with ints with
              // the equality operator, so this should be equally ok/not ok
              switch ((int) angle) {
                case 0:
                  x = (int) (tacke.get(0).getX() - 20);
                  y = (int) (tacke.get(0).getY());
                  point0 = new Point(x, y);
                  x = (int) (tacke.get(1).getX() - 20);
                  y = (int) (tacke.get(1).getY());
                  point1 = new Point(x, y);
                  break;
                case 90:
                  x = (int) (tacke.get(0).getX());
                  y = (int) (tacke.get(0).getY() - 20);
                  point0 = new Point(x, y);
                  x = (int) (tacke.get(1).getX());
                  y = (int) (tacke.get(1).getY() - 20);
                  point1 = new Point(x, y);
                  break;
                case 180:
                  x = (int) (tacke.get(0).getX() + 20);
                  y = (int) (tacke.get(0).getY());
                  point0 = new Point(x, y);
                  x = (int) (tacke.get(1).getX() + 20);
                  y = (int) (tacke.get(1).getY());
                  point1 = new Point(x, y);
                  break;
                case 270:
                  x = (int) (tacke.get(0).getX());
                  y = (int) (tacke.get(0).getY() + 20);
                  point0 = new Point(x, y);
                  x = (int) (tacke.get(1).getX());
                  y = (int) (tacke.get(1).getY() + 20);
                  point1 = new Point(x, y);
                default:
                  LOG.error("Unknown transistor angle {}", angle);
              }
              if (point0 != null) {
                trans.setControlPoint(point0, 0);
                trans.setControlPoint(point1, 1);
              }
              Orientation orientation = Orientation._270;
              if (angle > 45 && angle <= 135) {
                orientation = Orientation.DEFAULT;
              } else if (angle > 135 && angle <= 225) {
                orientation = Orientation._90;
              } else if (angle > 225 && angle <= 315) {
                orientation = Orientation._180;
              }
              trans.setOrientation(orientation);
              project.getComponents().add(trans);
              break;
            case "ellipse":
              Ellipse el = new Ellipse();
              el.setName("elipse");
              el.setAlpha((byte) transparency);
              el.setColor(cl);
              el.setControlPoint(tacke.get(0), 0);
              el.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(el);
              break;
            case "line":
              Line ln = new Line();
              ln.setName("ln");
              ln.setColor(cl);
              ln.setControlPoint(tacke.get(0), 0);
              ln.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(ln);
              break;
            case "rectangle":
              Rectangle rec = new Rectangle();
              rec.setName("rec");
              rec.setColor(cl);
              rec.setAlpha((byte) transparency);
              rec.setEdgeRadius(radiusPro);
              rec.setControlPoint(tacke.get(0), 0);
              rec.setControlPoint(tacke.get(1), 1);
              project.getComponents().add(rec);
              break;
            case "noval tube socket":
              ts = new TubeSocket();
              if (comName != "") {
                ts.setName(comName);
              } else {
                ts.setName("ts");
              }
              ts.setAlpha((byte) transparency);
              if (value_s != "") {
                ts.setValue(value_s);
              }
              ts.setAngle((int) angle);
              ts.setControlPoint(tacke.get(0), 0);
              ts.setBase(Base.B9A);
              project.getComponents().add(ts);
              break;
            case "bjt transistor":
              BJTSymbol bjt = new BJTSymbol();
              if (comName != "") {
                bjt.setName(comName);
              } else {
                bjt.setName("bjt");
              }
              bjt.setColor(cl);
              if (value_s != "") {
                bjt.setValue(value_s);
              }
              if (angle <= 45 || angle > 315) {
                bjt.setOrientation(Orientation.DEFAULT);
              } else if (angle > 45 && angle <= 135) {
                bjt.setOrientation(Orientation._90);
              } else if (angle > 135 && angle <= 225) {
                bjt.setOrientation(Orientation._180);
              } else if (angle > 225 && angle <= 315) {
                bjt.setOrientation(Orientation._270);
              }
              x = (int) (tacke.get(0).getX() - 20);
              y = (int) tacke.get(0).getY();
              point = new Point(x, y);
              bjt.setControlPoint(point, 0);
              project.getComponents().add(bjt);
              break;
            case "octal tube socket":
              ts = new TubeSocket();
              if (comName != "") {
                ts.setName(comName);
              } else {
                ts.setName("ts");
              }
              ts.setAlpha((byte) transparency);
              if (value_s != "") {
                ts.setValue(value_s);
              }
              ts.setAngle((int) angle);
              ts.setControlPoint(tacke.get(0), 0);
              ts.setBase(Base.OCTAL);
              project.getComponents().add(ts);
              break;
            case "7-pin tube socket":
              ts = new TubeSocket();
              if (comName != "") {
                ts.setName(comName);
              } else {
                ts.setName("ts");
              }
              ts.setAlpha((byte) transparency);
              if (value_s != "") {
                ts.setValue(value_s);
              }
              ts.setAngle((int) angle);
              ts.setControlPoint(tacke.get(0), 0);
              ts.setBase(Base.B7G);
              project.getComponents().add(ts);
              break;
            default:
              LOG.error("Don't know what to do with {}", lcNodeName);
          }
        } else {
          LOG.error("Unrecognized node name found: {}", node.getNodeName());
        }
      }
    }
    return project;
  }
}

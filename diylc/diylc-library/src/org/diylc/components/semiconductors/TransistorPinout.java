package org.diylc.components.semiconductors;

public enum TransistorPinout {

  BJT_EBC, BJT_CBE, JFET_DSG, JFET_GSD, JFET_DGS, JFET_SGD, MOSFET_DSG, MOSFET_GSD, MOSFET_DGS, MOSFET_SGD;
  
  @Override
  public String toString() {
    String[] parts = name().split("_");
    StringBuilder sb = new StringBuilder();    
    for (int i = 0; i < parts[1].length(); i++) {
      if (i > 0)
        sb.append("-");
      sb.append(parts[1].charAt(i));
    }
    sb.append("     ").append(parts[0]);
    return sb.toString();
  };
}

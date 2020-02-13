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

package org.diylc.components.electromechanical.iec;

import java.awt.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.components.Area;

public abstract class Iec60320 extends IecSocket {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(Iec60320.class);

  protected static final Point reference = new Point(0, 0);

  public enum CouplerType {
    CONNECTOR, // i.e. plug
    INLET // i.e. socket
  }

  /** Type of coupler (male connector/plug or female inlet/socket). */
  private final CouplerType couplerType;
  /** Coupler main area (plug for connectors, opening for inlets). */
  protected Area coupler;
  /** Area surrounding the main area. */
  protected Area courtyard;
  /** Area of mounting ears (optional). */
  protected Area mount;

  private Area[] body;

  protected Iec60320(CouplerType type) {
    super();
    this.couplerType = type;
    this.coupler = coupler;
    this.courtyard = courtyard;
    this.mount = mount;
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[3];
      body[0] = mount;
      body[1] = courtyard;
      body[2] = coupler;
    }
    return body;
  }
}

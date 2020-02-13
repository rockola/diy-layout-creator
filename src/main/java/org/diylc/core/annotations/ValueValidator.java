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

package org.diylc.core.annotations;

import org.diylc.core.IPropertyValidator;
import org.diylc.core.ValidationException;
import org.diylc.core.measures.Value;

/**
 * Validates instances of {@link Value} and makes sure that the value is positive.
 *
 * @author Branislav Stojkovic
 */
public class ValueValidator implements IPropertyValidator {

  @Override
  public void validate(Object object) throws ValidationException {
    if (object != null) {
      if (object instanceof Value) {
        Value value = (Value) object;
        if (value.getValue() < 0) {
          throw new ValidationException("Must be greater or equal to zero.");
        }
      } else {
        throw new ValidationException("Wrong data type, Value expected.");
      }
    }
  }
}

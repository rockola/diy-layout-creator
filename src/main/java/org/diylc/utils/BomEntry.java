/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.utils;

public class BomEntry {

  private String type;
  private String name;
  private String value;
  private Integer quantity;
  private String notes;

  public BomEntry(String type, String name, String value, int quantity) {
    super();
    this.type = type;
    this.name = name;
    this.value = value;
    this.quantity = quantity;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((notes == null) ? 0 : notes.hashCode());
    result = prime * result + quantity;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this != obj) {
      if (obj == null || (obj.getClass() != this.getClass())) {
        return false;
      } else {
        BomEntry other = (BomEntry) obj;

        if ((type == null && other.type != null)
            || (type != null && !type.equals(other.type))
            || (name == null && other.name != null)
            || (name != null && !name.equals(other.name))
            || (notes == null && other.notes != null)
            || (notes != null && !notes.equals(other.notes))
            || (quantity == null && other.quantity != null)
            || (quantity != null && !quantity.equals(other.quantity))
            || (value == null && other.value != null)
            || (value != null && value.equals(other.value))) {
          return false;
        }
      }
    }
    return true;
  }
}

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
package org.diylc.common;

import java.io.IOException;

public interface IBlockProcessor {

  public static final String BLOCKS_KEY = "buildingBlocks";

  void saveSelectionAsBlock(String blockName);

  void loadBlock(String blockName) throws InvalidBlockException;
  
  void deleteBlock(String blockName);
  
  int importBlocks(String fileName) throws IOException;

  public class BlockAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 1L;

  }

  public class InvalidBlockException extends Exception {

    private static final long serialVersionUID = 1L;

  }
}

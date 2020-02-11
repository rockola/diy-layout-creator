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
  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc;

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Command line argument handler. */
public class CommandLineArguments {

  private static final Logger LOG = LogManager.getLogger(CommandLineArguments.class);

  private CommandLine commandLine;

  /** @param args Command line arguments to be parsed. */
  public CommandLineArguments(String[] args) {
    Options options = new Options();
    // no options for now
    CommandLineParser parser = new DefaultParser();
    try {
      commandLine = parser.parse(options, args);
    } catch (ParseException e) {
      LOG.error("Parsing failed", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Get non-option command line arguments.
   *
   * @return list of arguments that were not parsed, i.e. filenames
   */
  public List<String> filenames() {
    return commandLine.getArgList();
  }
}

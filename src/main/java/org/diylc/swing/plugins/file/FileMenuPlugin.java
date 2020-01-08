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
package org.diylc.swing.plugins.file;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.DIYLC;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.EventType;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.Icon;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.IDynamicSubmenuHandler;

/**
 * Entry point class for File management utilities.
 * 
 * @author Branislav Stojkovic
 */
public class FileMenuPlugin implements IPlugIn, IDynamicSubmenuHandler {

    private static final Logger LOG = LogManager.getLogger(FileMenuPlugin.class);

    private static final String FILE_TITLE = "File";
    private static final String TRACE_MASK_TITLE = "Trace Mask";
    private static final String INTEGRATION_TITLE = "Integration";
    private static final String ANALYZE_TITLE = "Analyze";

    private ProjectDrawingProvider drawingProvider;
    private TraceMaskDrawingProvider traceMaskDrawingProvider;
    private IPlugInPort plugInPort;

    public FileMenuPlugin() { super(); }

    private void addAction(AbstractAction action, String submenuTitle) {
	DIYLC.ui().injectMenuAction(action, submenuTitle);
    }
    private void addAction(AbstractAction action) {
	DIYLC.ui().injectMenuAction(action, FILE_TITLE);
    }

    @Override
    public void connect(IPlugInPort plugInPort) {
	this.plugInPort = plugInPort;
	this.drawingProvider = new ProjectDrawingProvider(plugInPort, false, true, false);
	this.traceMaskDrawingProvider = new TraceMaskDrawingProvider(plugInPort);

	addAction(ActionFactory.createNewAction(plugInPort));
	addAction(ActionFactory.createOpenAction(plugInPort));
	addAction(ActionFactory.createImportAction(plugInPort));
	addAction(ActionFactory.createSaveAction(plugInPort));
	addAction(ActionFactory.createSaveAsAction(plugInPort));
	DIYLC.ui().injectDynamicSubmenu("Recent Files",
					Icon.History,
					FILE_TITLE,
					this);
	addAction(null);

	// Export / Print
	addAction(ActionFactory.createExportPDFAction(plugInPort,
						      drawingProvider,
						      ""));
	addAction(ActionFactory.createExportPNGAction(plugInPort,
						      drawingProvider,
						      ""));
	addAction(ActionFactory.createPrintAction(drawingProvider,
						  DIYLC.getKeyStroke("Print")));

	// Trace mask
	DIYLC.ui().injectSubmenu(TRACE_MASK_TITLE, Icon.TraceMask, FILE_TITLE);
	addAction(ActionFactory.createExportPDFAction(plugInPort,
						      traceMaskDrawingProvider,
						      " (mask)"),
		  TRACE_MASK_TITLE);
	addAction(ActionFactory.createExportPNGAction(plugInPort,
						      traceMaskDrawingProvider,
						      " (mask)"),
		  TRACE_MASK_TITLE);
	addAction(ActionFactory.createPrintAction(traceMaskDrawingProvider,
						  DIYLC.getKeyStroke("Print Trace Mask")),
		  TRACE_MASK_TITLE);

	// Analyze
	DIYLC.ui().injectSubmenu(ANALYZE_TITLE, Icon.Scientist, FILE_TITLE);
	addAction(ActionFactory.createBomAction(plugInPort), ANALYZE_TITLE);
	addAction(ActionFactory.createGenerateNetlistAction(plugInPort), ANALYZE_TITLE);
    
	List<INetlistAnalyzer> summarizers = plugInPort.getNetlistAnalyzers();
	if (summarizers != null) {
	    for(INetlistAnalyzer summarizer : summarizers)
		addAction(ActionFactory.createSummarizeNetlistAction(plugInPort, summarizer),
			  ANALYZE_TITLE);
	}
	addAction(null);

	// Integration
	DIYLC.ui().injectSubmenu(INTEGRATION_TITLE, Icon.Node, FILE_TITLE);
	addAction(ActionFactory.createImportBlocksAction(plugInPort), INTEGRATION_TITLE);
	addAction(ActionFactory.createExportBlocksAction(), INTEGRATION_TITLE);
	addAction(null, INTEGRATION_TITLE);
	addAction(ActionFactory.createImportVariantsAction(plugInPort), INTEGRATION_TITLE);
	addAction(ActionFactory.createExportVariantsAction(plugInPort), INTEGRATION_TITLE);
	//
	addAction(ActionFactory.createTemplateDialogAction());
	addAction(null);

	// Quit
	addAction(ActionFactory.createExitAction(plugInPort));
    }

    @Override
    public EnumSet<EventType> getSubscribedEventTypes() {
	return null;
    }

    @Override
    public void processMessage(EventType eventType, Object... params) {
	// Do nothing.
    }

    // Dynamic menu for recent files

    @Override
    public void onActionPerformed(String name) {
	LOG.info("Opening recent file: " + name);
	if (!plugInPort.allowFileAction()) {
	    LOG.info("Aborted opening recent file");
	    return;
	}
	this.plugInPort.loadProjectFromFile(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getAvailableItems() {
	return (List<String>) DIYLC.getObject(IPlugInPort.RECENT_FILES_KEY, null);
    }
}

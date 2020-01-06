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
import org.diylc.images.IconLoader;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.IDynamicSubmenuHandler;
import org.diylc.swing.ISwingUI;

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

    private ISwingUI swingUI;
    private IPlugInPort plugInPort;

    public FileMenuPlugin(ISwingUI swingUI) {
	super();
	this.swingUI = swingUI;
    }

    private void addAction(AbstractAction action) {
	swingUI.injectMenuAction(action, FILE_TITLE);
    }

    @Override
    public void connect(IPlugInPort plugInPort) {
	this.plugInPort = plugInPort;
	this.drawingProvider = new ProjectDrawingProvider(plugInPort, false, true, false);
	this.traceMaskDrawingProvider = new TraceMaskDrawingProvider(plugInPort);

	ActionFactory actionFactory = ActionFactory.getInstance();
	addAction(actionFactory.createNewAction(plugInPort));
	addAction(actionFactory.createOpenAction(plugInPort, swingUI));
	addAction(actionFactory.createImportAction(plugInPort, swingUI));
	addAction(actionFactory.createSaveAction(plugInPort, swingUI));
	addAction(actionFactory.createSaveAsAction(plugInPort, swingUI));
	swingUI.injectDynamicSubmenu("Recent Files", IconLoader.History.getIcon(), FILE_TITLE, this);
	addAction(null);
	addAction(actionFactory.createExportPDFAction(plugInPort,
						      drawingProvider,
						      swingUI,
						      ""));
	addAction(actionFactory.createExportPNGAction(plugInPort,
						      drawingProvider,
						      swingUI,
						      ""));
	addAction(actionFactory.createPrintAction(drawingProvider,
						  DIYLC.getKeyStroke("Print")));
	swingUI.injectSubmenu(TRACE_MASK_TITLE, IconLoader.TraceMask.getIcon(), FILE_TITLE);
	swingUI.injectMenuAction(actionFactory.createExportPDFAction(plugInPort,
								     traceMaskDrawingProvider,
								     swingUI, " (mask)"),
				 TRACE_MASK_TITLE);
	swingUI.injectMenuAction(actionFactory.createExportPNGAction(plugInPort,
								     traceMaskDrawingProvider,
								     swingUI, " (mask)"),
				 TRACE_MASK_TITLE);
	swingUI.injectMenuAction(actionFactory.createPrintAction(traceMaskDrawingProvider,
								 DIYLC.getKeyStroke("Print Trace Mask")),
				 TRACE_MASK_TITLE);
	swingUI.injectSubmenu(ANALYZE_TITLE, IconLoader.Scientist.getIcon(), FILE_TITLE);
	swingUI.injectMenuAction(actionFactory.createBomAction(plugInPort), ANALYZE_TITLE);
	swingUI.injectMenuAction(actionFactory.createGenerateNetlistAction(plugInPort, swingUI),
				 ANALYZE_TITLE);
    
	List<INetlistAnalyzer> summarizers = plugInPort.getNetlistAnalyzers();
	if (summarizers != null) {
	    for(INetlistAnalyzer summarizer : summarizers)
		swingUI.injectMenuAction(actionFactory.createSummarizeNetlistAction(plugInPort,
										    swingUI,
										    summarizer),
					 ANALYZE_TITLE);
	}
    
	addAction(null);
	swingUI.injectSubmenu(INTEGRATION_TITLE, IconLoader.Node.getIcon(), FILE_TITLE);
	swingUI.injectMenuAction(actionFactory.createImportBlocksAction(swingUI, plugInPort),
				 INTEGRATION_TITLE);
	swingUI.injectMenuAction(actionFactory.createExportBlocksAction(swingUI),
				 INTEGRATION_TITLE);
	swingUI.injectMenuAction(null, INTEGRATION_TITLE);
	swingUI.injectMenuAction(actionFactory.createImportVariantsAction(swingUI, plugInPort),
				 INTEGRATION_TITLE);
	swingUI.injectMenuAction(actionFactory.createExportVariantsAction(swingUI, plugInPort),
				 INTEGRATION_TITLE);    
	addAction(null);
	addAction(actionFactory.createExitAction(plugInPort));
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

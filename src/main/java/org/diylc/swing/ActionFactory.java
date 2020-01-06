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
package org.diylc.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.DIYLC;
import org.diylc.common.BuildingBlockPackage;
import org.diylc.common.ComponentType;
import org.diylc.common.Config;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.common.VariantPackage;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.Theme;
import org.diylc.core.measures.Nudge;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.images.IconLoader;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Summary;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.swing.plugins.config.ConfigPlugin;
import org.diylc.swing.plugins.edit.ComponentTransferable;
import org.diylc.swing.plugins.file.BomDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.CheckBoxListDialog;
import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;
import org.diylc.swingframework.TextDialog;
import org.diylc.utils.BomEntry;

import org.diylc.appframework.Serializer;

public class ActionFactory {

    private static final Logger LOG = LogManager.getLogger(ActionFactory.class);

    private static ActionFactory instance;

    public static ActionFactory getInstance() {
	if (instance == null) {
	    instance = new ActionFactory();
	}
	return instance;
    }

    private ActionFactory() {}

    private static String getMsg(String key) { return Config.getString("message.actionFactory." + key); }

    // File menu actions.

    public NewAction createNewAction(IPlugInPort plugInPort) {
	return new NewAction(plugInPort);
    }

    public OpenAction createOpenAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	return new OpenAction(plugInPort, swingUI);
    }

    public ImportAction createImportAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	return new ImportAction(plugInPort, swingUI);
    }

    public SaveAction createSaveAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	return new SaveAction(plugInPort, swingUI);
    }

    public SaveAsAction createSaveAsAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	return new SaveAsAction(plugInPort, swingUI);
    }

    public CreateBomAction createBomAction(IPlugInPort plugInPort) {
	return new CreateBomAction(plugInPort);
    }

    public ExportPDFAction createExportPDFAction(IPlugInPort plugInPort,
						 IDrawingProvider drawingProvider,
						 ISwingUI swingUI,
						 String defaultSuffix) {
	return new ExportPDFAction(plugInPort, drawingProvider, swingUI, defaultSuffix);
    }

    public ExportPNGAction createExportPNGAction(IPlugInPort plugInPort,
						 IDrawingProvider drawingProvider,
						 ISwingUI swingUI,
						 String defaultSuffix) {
	return new ExportPNGAction(plugInPort, drawingProvider, swingUI, defaultSuffix);
    }

    public PrintAction createPrintAction(IDrawingProvider drawingProvider,
					 KeyStroke acceleratorKey) {
	return new PrintAction(drawingProvider, acceleratorKey);
    }
  
    public ExportVariantsAction createExportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
	return new ExportVariantsAction(swingUI, plugInPort);
    }
  
    public ImportVariantsAction createImportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
	return new ImportVariantsAction(swingUI, plugInPort);
    }
  
    public ExportBlocksAction createExportBlocksAction(ISwingUI swingUI) {
	return new ExportBlocksAction(swingUI);
    }
  
    public ImportBlocksAction createImportBlocksAction(ISwingUI swingUI, IPlugInPort plugInPort) {
	return new ImportBlocksAction(swingUI, plugInPort);
    }

    public ExitAction createExitAction(IPlugInPort plugInPort) {
	return new ExitAction(plugInPort);
    }

    // Edit menu actions.

    public CutAction createCutAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
	return new CutAction(plugInPort, clipboard, clipboardOwner);
    }

    public CopyAction createCopyAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
	return new CopyAction(plugInPort, clipboard, clipboardOwner);
    }

    public PasteAction createPasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
	return new PasteAction(plugInPort, clipboard);
    }

    public DuplicateAction createDuplicateAction(IPlugInPort plugInPort) {
	return new DuplicateAction(plugInPort);
    }

    public SelectAllAction createSelectAllAction(IPlugInPort plugInPort) {
	return new SelectAllAction(plugInPort);
    }

    public GroupAction createGroupAction(IPlugInPort plugInPort) {
	return new GroupAction(plugInPort);
    }

    public UngroupAction createUngroupAction(IPlugInPort plugInPort) {
	return new UngroupAction(plugInPort);
    }

    public EditProjectAction createEditProjectAction(IPlugInPort plugInPort) {
	return new EditProjectAction(plugInPort);
    }

    public EditSelectionAction createEditSelectionAction(IPlugInPort plugInPort) {
	return new EditSelectionAction(plugInPort);
    }

    public DeleteSelectionAction createDeleteSelectionAction(IPlugInPort plugInPort) {
	return new DeleteSelectionAction(plugInPort);
    }

    public SaveAsTemplateAction createSaveAsTemplateAction(IPlugInPort plugInPort) {
	return new SaveAsTemplateAction(plugInPort);
    }

    public SaveAsBlockAction createSaveAsBlockAction(IPlugInPort plugInPort) {
	return new SaveAsBlockAction(plugInPort);
    }

    public ExpandSelectionAction createExpandSelectionAction(IPlugInPort plugInPort, ExpansionMode expansionMode) {
	return new ExpandSelectionAction(plugInPort, expansionMode);
    }

    public RotateSelectionAction createRotateSelectionAction(IPlugInPort plugInPort, int direction) {
	return new RotateSelectionAction(plugInPort, direction);
    }

    public MirrorSelectionAction createMirrorSelectionAction(IPlugInPort plugInPort, int direction) {
	return new MirrorSelectionAction(plugInPort, direction);
    }

    public SendToBackAction createSendToBackAction(IPlugInPort plugInPort) {
	return new SendToBackAction(plugInPort);
    }

    public BringToFrontAction createBringToFrontAction(IPlugInPort plugInPort) {
	return new BringToFrontAction(plugInPort);
    }

    public NudgeAction createNudgeAction(IPlugInPort plugInPort) {
	return new NudgeAction(plugInPort);
    }

    // Config actions.

    public ConfigAction createConfigAction(IPlugInPort plugInPort, String title, String configKey, boolean defaultValue) {
	return new ConfigAction(plugInPort, title, configKey, defaultValue);
    }

    public ConfigAction createConfigAction(IPlugInPort plugInPort, String title, String configKey, boolean defaultValue,
					   String tipKey) {
	return new ConfigAction(plugInPort, title, configKey, defaultValue, tipKey);
    }

    public ThemeAction createThemeAction(IPlugInPort plugInPort, Theme theme) {
	return new ThemeAction(plugInPort, theme);
    }

    public ComponentBrowserAction createComponentBrowserAction(String browserType) {
	return new ComponentBrowserAction(browserType);
    }

    public RenumberAction createRenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
	return new RenumberAction(plugInPort, xAxisFirst);
    }
  
    public GenerateNetlistAction createGenerateNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	return new GenerateNetlistAction(plugInPort, swingUI);
    }
  
    public SummarizeNetlistAction createSummarizeNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI, INetlistAnalyzer summarizer) {
	return new SummarizeNetlistAction(plugInPort, swingUI, summarizer);
    }

    // File menu actions.

    public static class NewAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public NewAction(IPlugInPort pp) {
	    super(pp, null, "New", "New", IconLoader.DocumentPlain.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("NewAction triggered");
	    if (!plugInPort.allowFileAction()) {
		return;
	    }
	    plugInPort.createNewProject();
	    List<PropertyWrapper> properties = plugInPort.getProperties(plugInPort.getCurrentProject());
	    PropertyEditorDialog editor =
		DialogFactory.getInstance().createPropertyEditorDialog(properties, "Edit Project", true);
	    editor.setVisible(true);
	    if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
		plugInPort.applyProperties(plugInPort.getCurrentProject(), properties);
	    }
	    // Save default values.
	    for (PropertyWrapper property : editor.getDefaultedProperties()) {
		if (property.getValue() != null) {
		    plugInPort.setDefaultPropertyValue(Project.class, property.getName(), property.getValue());
		}
	    }
	}
    }

    public static class OpenAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public OpenAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	    super(plugInPort, swingUI, "Open", "Open", 
		  IconLoader.FolderOut.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("OpenAction triggered");
	    if (!plugInPort.allowFileAction()) {
		return;
	    }
	    final File file =
		DialogFactory.getInstance().showOpenDialog(FileFilterEnum.DIY.getFilter(), null,
							   FileFilterEnum.DIY.getExtensions()[0], null);
	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Void>() {

			@Override
			public Void doInBackground() throws Exception {
			    LOG.debug("Opening from " + file.getAbsolutePath());
			    plugInPort.loadProjectFromFile(file.getAbsolutePath());
			    return null;
			}

			@Override
			public void complete(Void result) {}

			@Override
			public void failed(Exception e) {
			    swingUI.showMessage("Could not open file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
			}
		    }, true);
	    }
	}
    }

    public static class ImportAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private Presenter presenter;

	public ImportAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	    super(plugInPort, swingUI, "Import", "Import", IconLoader.ElementInto.getIcon());
	    this.presenter = new Presenter(new IView() {

		    @Override
		    public int showConfirmDialog(String message, String title, int optionType, int messageType) {
			return JOptionPane.showConfirmDialog(null, message, title, optionType, messageType);
		    }

		    @Override
		    public void showMessage(String message, String title, int messageType) {
			JOptionPane.showMessageDialog(null, message, title, messageType);
		    }

		    @Override
		    public File promptFileSave() {
			return null;
		    }

		    @Override
		    public boolean editProperties(List<PropertyWrapper> properties, Set<PropertyWrapper> defaultedProperties) {
			return false;
		    }
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("ImportAction triggered");

	    final File file =
		DialogFactory.getInstance().showOpenDialog(FileFilterEnum.DIY.getFilter(), null,
							   FileFilterEnum.DIY.getExtensions()[0], null);
	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Void>() {

			@Override
			public Void doInBackground() throws Exception {
			    LOG.debug("Opening from " + file.getAbsolutePath());
			    // Load project in temp presenter
			    presenter.loadProjectFromFile(file.getAbsolutePath());
			    // Grab all components and paste them into the main
			    // presenter
			    plugInPort.pasteComponents(presenter.getCurrentProject().getComponents(), false);
			    // Cleanup components in the temp presenter, don't need
			    // them anymore
			    presenter.selectAll(0);
			    presenter.deleteSelectedComponents();
			    return null;
			}

			@Override
			public void complete(Void result) {}

			@Override
			public void failed(Exception e) {
			    swingUI.showMessage("Could not open file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
			}
		    }, true);
	    }
	}
    }

    public static class SaveAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public SaveAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	    super(plugInPort, swingUI, "Save", "Save", IconLoader.DiskBlue.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("SaveAction triggered");
	    if (plugInPort.getCurrentFileName() == null) {
		final File file =
		    DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
							       FileFilterEnum.DIY.getFilter(), null,
							       FileFilterEnum.DIY.getExtensions()[0], null);
		if (file != null) {
		    swingUI.executeBackgroundTask(new ITask<Void>() {

			    @Override
			    public Void doInBackground() throws Exception {
				LOG.debug("Saving to " + file.getAbsolutePath());
				plugInPort.saveProjectToFile(file.getAbsolutePath(), false);
				return null;
			    }

			    @Override
			    public void complete(Void result) {}

			    @Override
			    public void failed(Exception e) {
				swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
			    }
			}, true);
		}
	    } else {
		swingUI.executeBackgroundTask(new ITask<Void>() {

			@Override
			public Void doInBackground() throws Exception {
			    LOG.debug("Saving to " + plugInPort.getCurrentFileName());
			    plugInPort.saveProjectToFile(plugInPort.getCurrentFileName(), false);
			    return null;
			}

			@Override
			public void complete(Void result) {}

			@Override
			public void failed(Exception e) {
			    swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
			}
		    }, true);
	    }
	}
    }

    public static class SaveAsAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public SaveAsAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	    super(plugInPort, swingUI, "Save As", "Save As",
		  IconLoader.DiskBlue.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("SaveAsAction triggered");
	    final File file =
		DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
							   FileFilterEnum.DIY.getFilter(), null,
							   FileFilterEnum.DIY.getExtensions()[0], null);
	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Void>() {

			@Override
			public Void doInBackground() throws Exception {
			    LOG.debug("Saving to " + file.getAbsolutePath());
			    plugInPort.saveProjectToFile(file.getAbsolutePath(), false);
			    return null;
			}

			@Override
			public void complete(Void result) {}

			@Override
			public void failed(Exception e) {
			    swingUI.showMessage("Could not save to file. " + e.getMessage(),
						"Error",
						ISwingUI.ERROR_MESSAGE);
			}
		    }, true);
	    }
	}
    }

    public static class CreateBomAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public CreateBomAction(IPlugInPort plugInPort) {
	    super(plugInPort, "Create B.O.M.", IconLoader.BOM.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("CreateBomAction triggered");
	    List<BomEntry> bom =
		org.diylc.utils.BomMaker.getInstance().createBom(plugInPort.getCurrentProject().getComponents());
      
	    String initialFileName = null;
	    String currentFile = plugInPort.getCurrentFileName();
	    if (currentFile != null) {
		File cFile = new File(currentFile);        
		initialFileName = cFile.getName().replaceAll("(?i)\\.diy", "") + " BOM";
	    }
      
	    BomDialog dialog = DialogFactory.getInstance().createBomDialog(bom, initialFileName);
	    dialog.setVisible(true);
	}
    }

    public static class ExportPDFAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private IDrawingProvider drawingProvider;
	private String defaultSuffix;

	public ExportPDFAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider,
			       ISwingUI swingUI, String defaultSuffix) {
	    super(plugInPort, swingUI, "Export to PDF", IconLoader.PDF.getIcon());
	    this.drawingProvider = drawingProvider;
	    this.defaultSuffix = defaultSuffix;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("ExportPDFAction triggered");
      
	    File initialFile = null;
	    String currentFile = plugInPort.getCurrentFileName();
	    if (currentFile != null) {
		File cFile = new File(currentFile);        
		initialFile = new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".pdf");
	    }
      
	    final File file =
		DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.PDF.getFilter(), initialFile,
							   FileFilterEnum.PDF.getExtensions()[0], null);
	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Void>() {

			@Override
			public Void doInBackground() throws Exception {
			    LOG.debug("Exporting to " + file.getAbsolutePath());
			    DrawingExporter.getInstance().exportPDF(ExportPDFAction.this.drawingProvider, file);
			    return null;
			}

			@Override
			public void complete(Void result) {}

			@Override
			public void failed(Exception e) {
			    swingUI.showMessage("Could not export to PDF. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
			}
		    }, true);
	    }
	}
    }

    public static class ExportPNGAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private IDrawingProvider drawingProvider;
	private String defaultSuffix;

	public ExportPNGAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider,
			       ISwingUI swingUI, String defaultSuffix) {
	    super(plugInPort, swingUI, "Export to PNG", IconLoader.Image.getIcon());
	    this.drawingProvider = drawingProvider;
	    this.defaultSuffix = defaultSuffix;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("ExportPNGAction triggered");
      
	    File initialFile = null;
	    String currentFile = plugInPort.getCurrentFileName();
	    if (currentFile != null) {
		File cFile = new File(currentFile);        
		initialFile = new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".png");
	    }
      
	    final File file =
		DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.PNG.getFilter(), initialFile,
							   FileFilterEnum.PNG.getExtensions()[0], null);
	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Void>() {

			@Override
			public Void doInBackground() throws Exception {
			    LOG.debug("Exporting to " + file.getAbsolutePath());
			    DrawingExporter.getInstance().exportPNG(ExportPNGAction.this.drawingProvider, file);
			    return null;
			}

			@Override
			public void complete(Void result) {}

			@Override
			public void failed(Exception e) {
			    swingUI.showMessage("Could not export to PNG. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
			}
		    }, true);
	    }
	}
    }

    public static class PrintAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private IDrawingProvider drawingProvider;

	public PrintAction(IDrawingProvider drawingProvider,
			   KeyStroke acceleratorKey) {
	    super(null, null, "Print...", acceleratorKey, IconLoader.Print.getIcon());
	    this.drawingProvider = drawingProvider;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("PrintAction triggered");
	    try {
		DrawingExporter.getInstance().print(this.drawingProvider);
	    } catch (PrinterException e1) {
		e1.printStackTrace();
	    }
	}
    }
  
    public static class ExportVariantsAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private Map<String, ComponentType> typeMap =
	    new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);

	public ExportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
	    super(plugInPort, swingUI, "Export Variants");
      
	    Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
	    for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet())
		for (ComponentType type : entry.getValue()) {
		    typeMap.put(type.getInstanceClass().getCanonicalName(), type);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("ExportVariantsAction triggered");
      
	    Map<String, List<Template>> selectedVariants;
      
	    try {
		Map<String, List<Template>> variantMap =
		    (Map<String, List<Template>>) DIYLC.getObject(IPlugInPort.TEMPLATES_KEY);
		if (variantMap == null || variantMap.isEmpty()) {
		    swingUI.error(getMsg("no-variants"));
		    return;
		}
        
		List<ComponentType> types = new ArrayList<ComponentType>();
		for (String className : variantMap.keySet()) {
		    ComponentType type = typeMap.get(className);
		    if (type != null)            
			types.add(type);
		    else
			LOG.warn("Could not find type for: " + className);
		}
        
		Collections.sort(types, new Comparator<ComponentType>() {
  
			@Override
			public int compare(ComponentType o1, ComponentType o2) {
			    return o1.toString().compareToIgnoreCase(o2.toString());
			}});
        
		CheckBoxListDialog dialog = new CheckBoxListDialog(swingUI.getOwnerFrame(), "Export Variants", types.toArray());
        
		dialog.setVisible(true);      
        
		if (dialog.getSelectedButtonCaption() != "OK")
		    return;
        
		Object[] selected = dialog.getSelectedOptions();
        
		if (selected.length == 0) {
		    swingUI.error(getMsg("no-variants-selected"));
		    return;      
		}
        
		selectedVariants = new HashMap<String, List<Template>>();
		for (Object key : selected) {
		    ComponentType type = (ComponentType) key;
		    String clazz = type.getInstanceClass().getCanonicalName();
		    List<Template> variants = variantMap.get(clazz);
		    if (variants != null)
			selectedVariants.put(clazz, variants);
		}
	    } catch (Exception ex) {
		LOG.error("Error preparing variants for export", ex);
		swingUI.error(getMsg("export-variants"),
			      getMsg("variant-export-failed"));
		return;
	    }

	    final VariantPackage variantPkg = new VariantPackage(selectedVariants,
								 System.getProperty("user.name"));

	    File initialFile =
		new File(variantPkg.getOwner() == null
			 ? "variants.xml"
			 : ("variants by " + variantPkg.getOwner().toLowerCase() + ".xml"));

	    final File file =
		DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
							   FileFilterEnum.XML.getFilter(),
							   initialFile,
							   FileFilterEnum.XML.getExtensions()[0],
							   null);

	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Void>() {

			@Override
			public Void doInBackground() throws Exception {
			    LOG.debug("Exporting variants to " + file.getAbsolutePath());

			    try {
				Serializer.toFile(file, variantPkg);

				LOG.info("Exported variants succesfully");
			    } catch (IOException e) {
				LOG.error("Could not export variants", e);
			    }

			    return null;
			}

			@Override
			public void complete(Void result) {
			    swingUI.info(getMsg("success"),
					 String.format(getMsg("variants-exported"),
						       file.getName()));
			}

			@Override
			public void failed(Exception e) {
			    swingUI.error(getMsg("variant-export-failed") + e.getMessage());
			}
		    }, true);
	    }
	}
    }
  
    public static class ImportVariantsAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public ImportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
	    super(plugInPort, swingUI, "Import Variants");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("ImportVariantsAction triggered");

	    final File file =
		DialogFactory.getInstance().showOpenDialog(FileFilterEnum.XML.getFilter(),
							   null,
							   FileFilterEnum.XML.getExtensions()[0],
							   null,
							   swingUI.getOwnerFrame());

	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Integer>() {

			@Override
			public Integer doInBackground() throws Exception {
			    return plugInPort.importVariants(file.getAbsolutePath());
			}

			@Override
			public void complete(Integer result) {
			    swingUI.info(getMsg("success"),
					 String.format(getMsg("variants-imported"),
						       result,
						       file.getName()));
			}

			@Override
			public void failed(Exception e) {
			    swingUI.error(getMsg("variant-import-failed") + e.getMessage());
			}
		    }, true);
	    }
	}
    }
  
    public static class ExportBlocksAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private ISwingUI swingUI;

	public ExportBlocksAction(ISwingUI swingUI) {
	    super();
	    this.swingUI = swingUI;
	    putValue(AbstractAction.NAME, "Export Building Blocks");
	    // putValue(AbstractAction.SMALL_ICON, IconLoader.Print.getIcon());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("ExportBuildingBlocksAction triggered");
      
	    Map<String, List<IDIYComponent<?>>> selectedBlocks;
      
	    try {
		Map<String, List<IDIYComponent<?>>> blocks =
		    (Map<String, List<IDIYComponent<?>>>) DIYLC.getObject(IPlugInPort.BLOCKS_KEY);
		if (blocks == null || blocks.isEmpty()) {
		    swingUI.error(getMsg("no-building-blocks"));
		    return;
		}
        
		String[] options = blocks.keySet().toArray(new String[0]);
        
		Arrays.sort(options, new Comparator<String>() {
  
			@Override
			public int compare(String o1, String o2) {
			    return o1.compareToIgnoreCase(o2);
			}});
        
		CheckBoxListDialog dialog = new CheckBoxListDialog(swingUI.getOwnerFrame(),
								   getMsg("export-building-blocks"),
								   options);
        
		dialog.setVisible(true);      
        
		if (dialog.getSelectedButtonCaption() != "OK")
		    return;
        
		Object[] selected = dialog.getSelectedOptions();
        
		if (selected.length == 0) {
		    swingUI.error(getMsg("no-building-blocks-selected"));
		    return;      
		}
        
		selectedBlocks = new HashMap<String, List<IDIYComponent<?>>>();
		for (Object key : selected) {
		    selectedBlocks.put(key.toString(), blocks.get(key));
		}
	    } catch (Exception ex) {
		LOG.error("Error preparing building blocks for export", ex);
		swingUI.error(getMsg("export-building-blocks"),
			      getMsg("building-block-export-failed"));
		return;
	    }
      
	    final BuildingBlockPackage variantPkg =
		new BuildingBlockPackage(selectedBlocks, System.getProperty("user.name"));

	    File initialFile =
		new File(variantPkg.getOwner() == null
			 ? "building blocks.xml" :
			 ("building blocks by " + variantPkg.getOwner().toLowerCase() + ".xml"));

	    final File file =
		DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
							   FileFilterEnum.XML.getFilter(),
							   initialFile,
							   FileFilterEnum.XML.getExtensions()[0],
							   null);

	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Void>() {

			@Override
			public Void doInBackground() throws Exception {
			    LOG.debug("Exporting variants to "
				      + file.getAbsolutePath());
			    try {
				Serializer.toFile(file, variantPkg);
				LOG.info("Exported building blocks succesfully");
			    } catch (IOException e) {
				LOG.error("Could not export building blocks", e);
			    }

			    return null;
			}

			@Override
			public void complete(Void result) {
			    swingUI.info(getMsg("success"),
					 String.format(getMsg("building-blocks-exported"),
						       file.getName()));
				}

			@Override
			public void failed(Exception e) {
			    swingUI.error(getMsg("building-block-export-failed")
					  + e.getMessage());
			}
		    }, true);
	    }
	}
    }
  
    public static class ImportBlocksAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public ImportBlocksAction(ISwingUI swingUI, IPlugInPort plugInPort) {
	    super(plugInPort, swingUI, getMsg("import-building-blocks"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("ImportBlocksAction triggered");
      
	    final File file =
		DialogFactory.getInstance().showOpenDialog(FileFilterEnum.XML.getFilter(),
							   null,
							   FileFilterEnum.XML.getExtensions()[0],
							   null,
							   swingUI.getOwnerFrame());

	    if (file != null) {
		swingUI.executeBackgroundTask(new ITask<Integer>() {

			@Override
			public Integer doInBackground() throws Exception {
			    return plugInPort.importBlocks(file.getAbsolutePath());
			}

			@Override
			public void complete(Integer result) {
			    swingUI.info(getMsg("success"),
					 String.format(getMsg("building-blocks-imported"),
						       result,
						       file.getName()));
			}

			@Override
			public void failed(Exception e) {
			    swingUI.error(getMsg("building-block-import-failed")
					  + e.getMessage());
			}
		    }, true);
	    }
	}
    }

    public static class ExitAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public ExitAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, getMsg("exit"), "Quit", IconLoader.Exit.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("ExitAction triggered");
	    if (plugInPort.allowFileAction()) {
		System.exit(0);
	    }
	}
    }

    // Edit menu actions.

    public static class CutAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private Clipboard clipboard;
	private ClipboardOwner clipboardOwner;

	public CutAction(IPlugInPort plugInPort,
			 Clipboard clipboard, ClipboardOwner clipboardOwner) {
	    super(plugInPort, null, getMsg("cut"), "Cut", IconLoader.Cut.getIcon());
	    this.clipboard = clipboard;
	    this.clipboardOwner = clipboardOwner;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Cut triggered");
	    clipboard.setContents(new ComponentTransferable(cloneComponents(plugInPort.getSelectedComponents())),
				  clipboardOwner);
	    plugInPort.deleteSelectedComponents();
	}
    }

    public static class PasteAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private Clipboard clipboard;

	public PasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
	    super(plugInPort, null, getMsg("paste"), "Paste", IconLoader.Paste.getIcon());
	    this.clipboard = clipboard;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Paste triggered");
	    try {
		List<IDIYComponent<?>> components =
		    (List<IDIYComponent<?>>) clipboard.getData(ComponentTransferable.listFlavor);
		plugInPort.pasteComponents(cloneComponents(components), false);
	    } catch (Exception ex) {
		LOG.error("Coule not paste.", ex);
	    }
	}
    }

    public static class CopyAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private Clipboard clipboard;
	private ClipboardOwner clipboardOwner;

	public CopyAction(IPlugInPort plugInPort,
			  Clipboard clipboard, ClipboardOwner clipboardOwner) {
	    super(plugInPort, null, getMsg("copy"), "Copy", IconLoader.Copy.getIcon());
	    this.clipboard = clipboard;
	    this.clipboardOwner = clipboardOwner;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Copy triggered");
	    clipboard.setContents(new ComponentTransferable(cloneComponents(plugInPort.getSelectedComponents())),
				  clipboardOwner);
	}
    }


    public static class DuplicateAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public DuplicateAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, getMsg("duplicate"), "Duplicate", 
		  IconLoader.DocumentsGear.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Duplicate triggered");
	    try {
		plugInPort.duplicateSelection();
	    } catch (Exception ex) {
		LOG.error("Coule not duplicate.", ex);
	    }
	}
    }

    private static List<IDIYComponent<?>> cloneComponents(Collection<IDIYComponent<?>> components) {
	List<IDIYComponent<?>> result = new ArrayList<IDIYComponent<?>>(components.size());
	for (IDIYComponent<?> component : components) {
	    try {
		result.add(component.clone());
	    } catch (CloneNotSupportedException e) {
		throw new RuntimeException(e);
	    }
	}
	return result;
    }

    public static class SelectAllAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public SelectAllAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, "Select All", "Select All",
		  IconLoader.Selection.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Select All triggered");
	    plugInPort.selectAll(0);
	}
    }

    public static class GroupAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public GroupAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, "Group Selection", "Group",
		  IconLoader.Group.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Group Selection triggered");
	    plugInPort.groupSelectedComponents();
	}
    }

    public static class UngroupAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public UngroupAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, getMsg("ungroup-selection"), "Ungroup",
		  IconLoader.Ungroup.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Ungroup Selection triggered");
	    plugInPort.ungroupSelectedComponents();
	}
    }

    public static class EditProjectAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public EditProjectAction(IPlugInPort plugInPort) {
	    super(plugInPort, getMsg("edit-project-settings"),
		  IconLoader.DocumentEdit.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Edit Project triggered");
	    List<PropertyWrapper> properties = plugInPort.getProperties(plugInPort.getCurrentProject());
	    PropertyEditorDialog editor =
		DialogFactory.getInstance().createPropertyEditorDialog(properties, "Edit Project", true);
	    editor.setVisible(true);
	    if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
		plugInPort.applyProperties(plugInPort.getCurrentProject(), properties);
	    }
	    // Save default values.
	    for (PropertyWrapper property : editor.getDefaultedProperties()) {
		if (property.getValue() != null) {
		    plugInPort.setDefaultPropertyValue(Project.class, property.getName(), property.getValue());
		}
	    }
	}
    }

    public static class NudgeAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public NudgeAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, getMsg("nudge"), "Nudge",
		  IconLoader.FitToSize.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Nudge triggered");
	    Nudge n = new Nudge();
	    boolean metric = DIYLC.getBoolean(Presenter.METRIC_KEY, true);
	    if (metric) {
		n.setxOffset(new Size(0d, SizeUnit.mm));
		n.setyOffset(new Size(0d, SizeUnit.mm));
	    } else {
		n.setxOffset(new Size(0d, SizeUnit.in));
		n.setyOffset(new Size(0d, SizeUnit.in));
	    }
	    List<PropertyWrapper> properties = plugInPort.getProperties(n);
	    PropertyEditorDialog editor =
		DialogFactory.getInstance().createPropertyEditorDialog(properties, "Nudge Selection", false);
	    editor.setVisible(true);
	    if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
		plugInPort.applyProperties(n, properties);
		plugInPort.nudgeSelection(n.getxOffset(), n.getyOffset(), n.getAffectStuckComponents());
	    }
	}
    }

    public static class EditSelectionAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public EditSelectionAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, getMsg("edit-selection"), "Edit Selection",
		  IconLoader.EditComponent.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Edit Selection triggered");
	    List<PropertyWrapper> properties = plugInPort.getMutualSelectionProperties();
	    if (properties == null || properties.isEmpty()) {
		LOG.info("Nothing to edit");
		return;
	    }
	    plugInPort.editSelection();
	}
    }

    public static class DeleteSelectionAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public DeleteSelectionAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, getMsg("delete-selection"), "Delete",
		  IconLoader.Delete.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Delete Selection triggered");
	    plugInPort.deleteSelectedComponents();
	}
    }

    public static class SaveAsTemplateAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public SaveAsTemplateAction(IPlugInPort plugInPort) {
	    super(plugInPort, getMsg("save-as-variant"),
		  IconLoader.BriefcaseAdd.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Save as template triggered");
	    String templateName =
		JOptionPane.showInputDialog(null,
					    getMsg("variant-name"),
					    getMsg("save-as-variant"),
					    JOptionPane.PLAIN_MESSAGE);
	    if (templateName != null && !templateName.trim().isEmpty()) {
		plugInPort.saveSelectedComponentAsVariant(templateName);
	    }
	}
    }

    public static class SaveAsBlockAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public SaveAsBlockAction(IPlugInPort plugInPort) {
	    super(plugInPort, getMsg("save-as-building-block"),
		  IconLoader.ComponentAdd.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Save as building block triggered");
	    String templateName =
		JOptionPane.showInputDialog(null,
					    "Block name:",
					    getMsg("save-as-building-block"),
					    JOptionPane.PLAIN_MESSAGE);
	    if (templateName != null && !templateName.trim().isEmpty()) {
		plugInPort.saveSelectionAsBlock(templateName);
	    }
	}
    }

    public static class ExpandSelectionAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private ExpansionMode expansionMode;

	public ExpandSelectionAction(IPlugInPort plugInPort, ExpansionMode expansionMode) {
	    super(plugInPort, (expansionMode == ExpansionMode.ALL
			       ? getMsg("all-connected")
			       : (expansionMode == ExpansionMode.IMMEDIATE
				  ? getMsg("immediate-only")
				  : (expansionMode == ExpansionMode.SAME_TYPE
				     ? getMsg("same-type-only")
				     : "ERROR" // how do we throw an exception here?
				     ))));
	    this.expansionMode = expansionMode;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Expand Selection triggered: " + expansionMode);
	    plugInPort.expandSelection(expansionMode);
	}
    }

    public static class RotateSelectionAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private int direction;

	public RotateSelectionAction(IPlugInPort plugInPort, int direction) {
	    super(plugInPort, null,
		  getMsg(direction > 0 ? "rotate-clockwise" : "rotate-counterclockwise"),
		  direction > 0 ? "Rotate Clockwise" : "Rotate Counterclockwise",
		  direction > 0
		  ? IconLoader.RotateCW.getIcon()
		  : IconLoader.RotateCCW.getIcon());
	    this.direction = direction;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Rotate Selection triggered: " + direction);
	    plugInPort.rotateSelection(direction);
	}
    }

    public static class MirrorSelectionAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private int direction;

	public MirrorSelectionAction(IPlugInPort plugInPort, int direction) {
	    super(plugInPort, null,
		  getMsg(direction == IComponentTransformer.HORIZONTAL
			 ? "mirror-horizontally"
			 : "mirror-vertically"),
		  direction == IComponentTransformer.HORIZONTAL
		  ? "Mirror Horizontally"
		  : "Mirror Vertically",
		  direction == IComponentTransformer.HORIZONTAL
		  ? IconLoader.FlipHorizontal.getIcon()
		  : IconLoader.FlipVertical.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Mirror Selection triggered: " + direction);
	    plugInPort.mirrorSelection(direction);
	}
    }

    public static class SendToBackAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	public SendToBackAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, getMsg("send-to-back"), "Send to Back",
		  IconLoader.Back.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Send to Back triggered");
	    plugInPort.sendSelectionToBack();
	}
    }

    public static class BringToFrontAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private IPlugInPort plugInPort;

	public BringToFrontAction(IPlugInPort plugInPort) {
	    super(plugInPort, null, getMsg("bring-to-front"), "Bring to Front",
		  IconLoader.Front.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info("Bring to Front triggered");
	    plugInPort.bringSelectionToFront();
	}
    }

    public static class ConfigAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private String configKey;
	private String tipKey;

	public ConfigAction(IPlugInPort plugInPort, String title, String configKey,
			    boolean defaultValue, String tipKey) {
	    super(plugInPort, title);
	    this.configKey = configKey;
	    this.tipKey = tipKey;
	    putValue(IView.CHECK_BOX_MENU_ITEM, true);
	    putValue(AbstractAction.SELECTED_KEY, DIYLC.getBoolean(configKey, defaultValue));
	}

	public ConfigAction(IPlugInPort plugInPort, String title, String configKey,
			    boolean defaultValue) {
	    this(plugInPort, title, configKey, defaultValue, null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info(getValue(AbstractAction.NAME) + " triggered");
	    DIYLC.putValue(configKey, getValue(AbstractAction.SELECTED_KEY));
	    if ((Boolean) getValue(AbstractAction.SELECTED_KEY) && tipKey != null
		&& !DIYLC.getBoolean(tipKey + ".dismissed", false)) {
		DialogFactory.getInstance().createInfoDialog(tipKey).setVisible(true);
	    }
	    plugInPort.refresh();
	}
    }

    public static class ThemeAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private Theme theme;

	public ThemeAction(IPlugInPort plugInPort, Theme theme) {
	    super(plugInPort, theme.getName());
	    this.theme = theme;
	    putValue(IView.RADIO_BUTTON_GROUP_KEY, "theme");
	    putValue(AbstractAction.SELECTED_KEY,
		     plugInPort.getSelectedTheme().getName().equals(theme.getName()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info(getValue(AbstractAction.NAME) + " triggered");
	    plugInPort.setSelectedTheme(theme);
	}
    }

    public static class ComponentBrowserAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private String browserType;

	public ComponentBrowserAction(String browserType) {
	    super(null, browserType);
	    this.browserType = browserType;
	    putValue(IView.RADIO_BUTTON_GROUP_KEY, "componentBrowser");
	    putValue(AbstractAction.SELECTED_KEY,
		     browserType.equals(DIYLC.getString(ConfigPlugin.COMPONENT_BROWSER,
							ConfigPlugin.SEARCHABLE_TREE)));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info(getValue(AbstractAction.NAME) + " triggered");
	    DIYLC.putValue(ConfigPlugin.COMPONENT_BROWSER, browserType);
	}
    }


    public static class RenumberAction extends ActionFactoryAction {

	private static final long serialVersionUID = 1L;

	private boolean xAxisFirst;

	public RenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
	    super(plugInPort, xAxisFirst ? "Top-to-Bottom" : "Left-to-Right");
	    this.xAxisFirst = xAxisFirst;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    LOG.info(getValue(AbstractAction.NAME) + " triggered");
	    plugInPort.renumberSelectedComponents(xAxisFirst);
	}
    }
  
    public static class GenerateNetlistAction extends ActionFactoryAction {
    
	private static final long serialVersionUID = 1L;

	public GenerateNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI) {
	    super(plugInPort, swingUI, "Generate DIYLC Netlist",
		  IconLoader.Web.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    swingUI.executeBackgroundTask(new ITask<List<Netlist>>() {

		    @Override
		    public List<Netlist> doInBackground() throws Exception {
			return plugInPort.extractNetlists(true);
		    }

		    @Override
		    public void failed(Exception e) {
			swingUI.showMessage("Failed to generate the netlist: " + e.getMessage(), "DIYLC Netlist", ISwingUI.INFORMATION_MESSAGE);
		    }

		    @Override
		    public void complete(List<Netlist> res) {
			if (res == null) {
			    swingUI.showMessage("The generated netlist is empty, nothing to show.", "DIYLC Netlist", ISwingUI.INFORMATION_MESSAGE);
			    return;
			}
			StringBuilder sb = new StringBuilder("<html>");
          
			for (Netlist netlist : res) {        
			    sb.append("<p style=\"font-family: " + new JLabel().getFont().getName() + "; font-size: 9px\"><b>Switch configuration: ").
				append(netlist.getSwitchSetup()).append("</b><br><br>Connected node groups:<br>");        
			    for (Group v : netlist.getSortedGroups()) {
				sb.append("&nbsp;&nbsp;").append(v.getSortedNodes()).append("<br>");          
			    }
			    sb.append("</p><br><hr>");
			}
			sb.append("</html>");
			new TextDialog(swingUI.getOwnerFrame().getRootPane(), sb.toString(), "DIYLC Netlist", new Dimension(600, 480)).setVisible(true);
		    }        
		}, true);
	}    
    }
  
    public static class SummarizeNetlistAction extends ActionFactoryAction {
    
	private static final long serialVersionUID = 1L;

	private INetlistAnalyzer summarizer;

	public SummarizeNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI,
				      INetlistAnalyzer summarizer) {
	    super(plugInPort, swingUI, summarizer.getName(),
		  Enum.valueOf(IconLoader.class, summarizer.getIconName()).getIcon());
	    this.summarizer = summarizer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
      
	    swingUI.executeBackgroundTask(new ITask<List<Summary>>() {

		    @Override
		    public List<Summary> doInBackground() throws Exception {
			List<Netlist> netlists = plugInPort.extractNetlists(true);
			if (netlists == null || netlists.isEmpty()) {
			    throw new Exception(getMsg("empty-netlist"));
			}
                    
			return summarizer.summarize(netlists, null);
		    }

		    @Override
		    public void failed(Exception e) {
			swingUI.showMessage(e.getMessage(),
					    summarizer.getName(),
					    ISwingUI.INFORMATION_MESSAGE);
		    }

		    @Override
		    public void complete(List<Summary> res) {
			if (res == null) {
			    swingUI.showMessage(getMsg("empty-netlist-summary"),
						summarizer.getName(),
						ISwingUI.INFORMATION_MESSAGE);
			    return;
			}
			StringBuilder sb = new StringBuilder("<html>");
          
			for (Summary summary : res) {        
			    sb.append("<p style=\"font-family: ").
				append(summarizer.getFontName()).
				append("; font-size: 9px\">");
            
			    if (res.size() > 1)
				sb.append("<b>Switch configuration: ").
				    append(summary.getNetlist().getSwitchSetup()).
				    append("</b><br><br>");        
            
			    sb.append(summary.getSummary());
            
			    sb.append("</p><br>");
            
			    if (res.size() > 1)
				sb.append("<hr>");
			}
			sb.append("</html>");
			new TextDialog(swingUI.getOwnerFrame().getRootPane(),
				       sb.toString(),
				       summarizer.getName(),
				       new Dimension(600, 480)).setVisible(true);
		    }        
		}, true);
	}    
    }
}

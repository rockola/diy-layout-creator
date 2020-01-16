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

package org.diylc.swing.action;

import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.appframework.Serializer;
import org.diylc.common.BuildingBlockPackage;
import org.diylc.common.ComponentType;
import org.diylc.common.Config;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.common.VariantPackage;
import org.diylc.core.ComponentTransferable;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.Theme;
import org.diylc.core.measures.Nudge;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.images.Icon;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Summary;
import org.diylc.presenter.Presenter;
import org.diylc.presenter.ProjectFileManager;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.swing.plugins.config.ConfigPlugin;
import org.diylc.swing.plugins.file.BomDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.CheckBoxListDialog;
import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.TextDialog;
import org.diylc.swingframework.export.DrawingExporter;
import org.diylc.utils.BomEntry;
import org.diylc.utils.BomMaker;

public class ActionFactory {

  private static final Logger LOG = LogManager.getLogger(ActionFactory.class);

  private static ActionFactory instance;

  private static ActionFactory getInstance() {
    if (instance == null) {
      instance = new ActionFactory();
    }
    return instance;
  }

  private ActionFactory() {}

  private static String getMsg(String key) {
    return Config.getString("message.actionFactory." + key);
  }

  // File menu actions.

  public static NewAction createNewAction(IPlugInPort plugInPort) {
    return new NewAction(plugInPort);
  }

  public static OpenAction createOpenAction(IPlugInPort plugInPort) {
    return new OpenAction(plugInPort);
  }

  public static ImportAction createImportAction(IPlugInPort plugInPort) {
    return new ImportAction(plugInPort);
  }

  public static SaveAction createSaveAction(IPlugInPort plugInPort) {
    return new SaveAction(plugInPort);
  }

  public static SaveAsAction createSaveAsAction(IPlugInPort plugInPort) {
    return new SaveAsAction(plugInPort);
  }

  public static CreateBomAction createBomAction(IPlugInPort plugInPort) {
    return new CreateBomAction(plugInPort);
  }

  public static ExportPDFAction createExportPDFAction(
      IPlugInPort plugInPort, IDrawingProvider drawingProvider, String defaultSuffix) {
    return new ExportPDFAction(plugInPort, drawingProvider, defaultSuffix);
  }

  public static ExportPNGAction createExportPNGAction(
      IPlugInPort plugInPort, IDrawingProvider drawingProvider, String defaultSuffix) {
    return new ExportPNGAction(plugInPort, drawingProvider, defaultSuffix);
  }

  public static PrintAction createPrintAction(
      IDrawingProvider drawingProvider, KeyStroke acceleratorKey) {
    return new PrintAction(drawingProvider, acceleratorKey);
  }

  public static ExportVariantsAction createExportVariantsAction(IPlugInPort plugInPort) {
    return new ExportVariantsAction(plugInPort);
  }

  public static ImportVariantsAction createImportVariantsAction(IPlugInPort plugInPort) {
    return new ImportVariantsAction(plugInPort);
  }

  public static ExportBlocksAction createExportBlocksAction() {
    return new ExportBlocksAction();
  }

  public static ImportBlocksAction createImportBlocksAction(IPlugInPort plugInPort) {
    return new ImportBlocksAction(plugInPort);
  }

  public static TemplateDialogAction createTemplateDialogAction() {
    return new TemplateDialogAction();
  }

  public static ExitAction createExitAction(IPlugInPort plugInPort) {
    return new ExitAction(plugInPort);
  }

  // Edit menu actions.

  public static CutAction createCutAction(
      IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
    return new CutAction(plugInPort, clipboard, clipboardOwner);
  }

  public static CopyAction createCopyAction(
      IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
    return new CopyAction(plugInPort, clipboard, clipboardOwner);
  }

  public static PasteAction createPasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
    return new PasteAction(plugInPort, clipboard);
  }

  public static DuplicateAction createDuplicateAction(IPlugInPort plugInPort) {
    return new DuplicateAction(plugInPort);
  }

  public static SelectAllAction createSelectAllAction(IPlugInPort plugInPort) {
    return new SelectAllAction(plugInPort);
  }

  public static GroupAction createGroupAction(IPlugInPort plugInPort) {
    return new GroupAction(plugInPort);
  }

  public static UngroupAction createUngroupAction(IPlugInPort plugInPort) {
    return new UngroupAction(plugInPort);
  }

  public static EditProjectAction createEditProjectAction(IPlugInPort plugInPort) {
    return new EditProjectAction(plugInPort);
  }

  public static EditSelectionAction createEditSelectionAction(IPlugInPort plugInPort) {
    return new EditSelectionAction(plugInPort);
  }

  public static DeleteSelectionAction createDeleteSelectionAction(IPlugInPort plugInPort) {
    return new DeleteSelectionAction(plugInPort);
  }

  public static SaveAsTemplateAction createSaveAsTemplateAction(IPlugInPort plugInPort) {
    return new SaveAsTemplateAction(plugInPort);
  }

  public static SaveAsBlockAction createSaveAsBlockAction(IPlugInPort plugInPort) {
    return new SaveAsBlockAction(plugInPort);
  }

  public static ExpandSelectionAction createExpandSelectionAction(
      IPlugInPort plugInPort, ExpansionMode expansionMode) {
    return new ExpandSelectionAction(plugInPort, expansionMode);
  }

  public static RotateSelectionAction createRotateSelectionAction(
      IPlugInPort plugInPort, int direction) {
    return new RotateSelectionAction(plugInPort, direction);
  }

  public static MirrorSelectionAction createMirrorSelectionAction(
      IPlugInPort plugInPort, int direction) {
    return new MirrorSelectionAction(plugInPort, direction);
  }

  public static SendToBackAction createSendToBackAction(IPlugInPort plugInPort) {
    return new SendToBackAction(plugInPort);
  }

  public static BringToFrontAction createBringToFrontAction(IPlugInPort plugInPort) {
    return new BringToFrontAction(plugInPort);
  }

  public static NudgeAction createNudgeAction(IPlugInPort plugInPort) {
    return new NudgeAction(plugInPort);
  }

  // Config actions.

  public static ConfigAction createConfigAction(
      IPlugInPort plugInPort, String title, String configKey, boolean defaultValue) {
    return new ConfigAction(plugInPort, title, configKey, defaultValue);
  }

  public static ConfigAction createConfigAction(IPlugInPort plugInPort,
                                                String title,
                                                String configKey,
                                                boolean defaultValue,
                                                String tipKey) {
    return new ConfigAction(plugInPort, title, configKey, defaultValue, tipKey);
  }

  public static ThemeAction createThemeAction(IPlugInPort plugInPort, Theme theme) {
    return new ThemeAction(plugInPort, theme);
  }

  public static ComponentBrowserAction createComponentBrowserAction(String browserType) {
    return new ComponentBrowserAction(browserType);
  }

  public static RenumberAction createRenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
    return new RenumberAction(plugInPort, xAxisFirst);
  }

  public static GenerateNetlistAction createGenerateNetlistAction(IPlugInPort plugInPort) {
    return new GenerateNetlistAction(plugInPort);
  }

  public static SummarizeNetlistAction createSummarizeNetlistAction(
      IPlugInPort plugInPort, INetlistAnalyzer summarizer) {
    return new SummarizeNetlistAction(plugInPort, summarizer);
  }

  // File menu actions.

  /**
     Creates a new project. Not undoable.
   */
  public static class NewAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public NewAction(IPlugInPort pp) {
      super(pp, "New", "New", Icon.DocumentPlain.icon());
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
          plugInPort.setDefaultPropertyValue(
              Project.class, property.getName(), property.getValue());
        }
      }
    }
  }

  /**
     Opens an existing project. Not undoable.
  */
  public static class OpenAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public OpenAction(IPlugInPort plugInPort) {
      super(plugInPort, "Open", "Open", Icon.FolderOut.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("OpenAction triggered");
      if (!plugInPort.allowFileAction()) {
        return;
      }
      final File file = DialogFactory.getInstance().showOpenDialog(
          FileFilterEnum.DIY.getFilter(),
          FileFilterEnum.DIY.getExtensions()[0]);
      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

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
                App.ui().error("Could not open file. " + e.getMessage());
              }
            },
            true);
      }
    }
  }

  /**
     Import all components from another project into the current project.
     TODO: Undoable.
   */
  public static class ImportAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public ImportAction(IPlugInPort plugInPort) {
      super(plugInPort, "Import", "Import", Icon.ElementInto.icon());
      // TODO: reimplement!
      /*
         this.presenter = new Presenter(new IView() {

          @Override
          public int showConfirmDialog(String message, String title,
      				 int optionType, int messageType) {
      	return JOptionPane.showConfirmDialog(null, message, title,
      					     optionType, messageType);
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
          public boolean editProperties(List<PropertyWrapper> properties,
      				  Set<PropertyWrapper> defaultedProperties) {
      	return false;
          }
      });
         */
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ImportAction triggered");

      final File file = DialogFactory.getInstance().showOpenDialog(
          FileFilterEnum.DIY.getFilter(),
          FileFilterEnum.DIY.getExtensions()[0]);
      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

              @Override
              public Void doInBackground() throws Exception {
                LOG.debug("Opening from " + file.getAbsolutePath());
                // Get project BUT do not load
                Project p = ProjectFileManager.getProjectFromFile(file.getAbsolutePath());
                // Grab all components and paste them into
                // the main presenter (i.e. current project)
                plugInPort.pasteComponents(p.getComponents(), false);
                return null;
              }

              @Override
              public void complete(Void result) {}

              @Override
              public void failed(Exception e) {
                App.ui().error("Could not open file. " + e.getMessage());
              }
            },
            true);
      }
    }
  }

  /**
     Save project. Not undoable.
  */
  public static class SaveAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public SaveAction(IPlugInPort plugInPort) {
      super(plugInPort, "Save", "Save", Icon.DiskBlue.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("SaveAction triggered");
      if (plugInPort.getCurrentFileName() == null) {
        final File file = DialogFactory.getInstance().showSaveDialog(
            FileFilterEnum.DIY.getFilter(),
            FileFilterEnum.DIY.getExtensions()[0]);
        if (file != null) {
          App.ui().executeBackgroundTask(
              new ITask<Void>() {

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
                  App.ui().error("Could not save to file. " + e.getMessage());
                }
              },
              true);
        }
      } else {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

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
                App.ui().error("Could not save to file. " + e.getMessage());
              }
            },
            true);
      }
    }
  }

  /**
     Save project under new name. Not undoable.
  */
  public static class SaveAsAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public SaveAsAction(IPlugInPort plugInPort) {
      super(plugInPort, "Save As", "Save As", Icon.DiskBlue.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("SaveAsAction triggered");
      final File file = DialogFactory.getInstance().showSaveDialog(
          FileFilterEnum.DIY.getFilter(),
          FileFilterEnum.DIY.getExtensions()[0]);
      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

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
                App.ui().error("Could not save to file. " + e.getMessage());
              }
            },
            true);
      }
    }
  }

  /**
     Create bill of materials. Not undoable.
  */
  public static class CreateBomAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public CreateBomAction(IPlugInPort plugInPort) {
      super(plugInPort, "Create B.O.M.", Icon.BOM.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("CreateBomAction triggered");
      List<BomEntry> bom =
          BomMaker.getInstance().createBom(plugInPort.getCurrentProject().getComponents());

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

  /**
     Export project to PDF. Not undoable.
   */
  public static class ExportPDFAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    private IDrawingProvider drawingProvider;
    private String defaultSuffix;

    public ExportPDFAction(
        IPlugInPort plugInPort, IDrawingProvider drawingProvider, String defaultSuffix) {
      super(plugInPort, "Export to PDF", Icon.PDF.icon());
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
        initialFile =
            new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".pdf");
      }

      final File file = DialogFactory.getInstance().showSaveDialog(
          FileFilterEnum.PDF.getFilter(),
          initialFile,
          FileFilterEnum.PDF.getExtensions()[0],
          null);
      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

              @Override
              public Void doInBackground() throws Exception {
                LOG.debug("Exporting to " + file.getAbsolutePath());
                DrawingExporter.getInstance()
                    .exportPDF(ExportPDFAction.this.drawingProvider, file);
                return null;
              }

              @Override
              public void complete(Void result) {}

              @Override
              public void failed(Exception e) {
                App.ui().error("Could not export to PDF.", e);
              }
            },
            true);
      }
    }
  }

  /**
     Export to PNG. Not undoable.
  */
  public static class ExportPNGAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    private IDrawingProvider drawingProvider;
    private String defaultSuffix;

    public ExportPNGAction(
        IPlugInPort plugInPort, IDrawingProvider drawingProvider, String defaultSuffix) {
      super(plugInPort, "Export to PNG", Icon.Image.icon());
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
        initialFile =
            new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".png");
      }

      final File file =
          DialogFactory.getInstance()
              .showSaveDialog(
                  FileFilterEnum.PNG.getFilter(),
                  initialFile,
                  FileFilterEnum.PNG.getExtensions()[0],
                  null);
      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

              @Override
              public Void doInBackground() throws Exception {
                LOG.debug("Exporting to " + file.getAbsolutePath());
                DrawingExporter.getInstance()
                    .exportPNG(ExportPNGAction.this.drawingProvider, file);
                return null;
              }

              @Override
              public void complete(Void result) {}

              @Override
              public void failed(Exception e) {
                App.ui().error("Could not export to PNG.", e);
              }
            },
            true);
      }
    }
  }

  /**
     Print. Not undoable.
  */
  public static class PrintAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    private IDrawingProvider drawingProvider;

    public PrintAction(IDrawingProvider drawingProvider, KeyStroke acceleratorKey) {
      super(null, "Print...", acceleratorKey, Icon.Print.icon());
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

  /**
     Export variants. Not undoable.
  */
  public static class ExportVariantsAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    private Map<String, ComponentType> typeMap =
        new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);

    public ExportVariantsAction(IPlugInPort plugInPort) {
      super(plugInPort, "Export Variants");

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
            (Map<String, List<Template>>) App.getObject(IPlugInPort.Key.TEMPLATES);
        if (variantMap == null || variantMap.isEmpty()) {
          App.ui().error(getMsg("no-variants"));
          return;
        }

        List<ComponentType> types = new ArrayList<ComponentType>();
        for (String className : variantMap.keySet()) {
          ComponentType type = typeMap.get(className);
          if (type != null) types.add(type);
          else LOG.warn("Could not find type for: " + className);
        }

        Collections.sort(
            types,
            new Comparator<ComponentType>() {

              @Override
              public int compare(ComponentType o1, ComponentType o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
              }
            });

        CheckBoxListDialog dialog =
            new CheckBoxListDialog(App.ui().getOwnerFrame(), "Export Variants", types.toArray());

        dialog.setVisible(true);

        if (dialog.getSelectedButtonCaption() != "OK") return;

        Object[] selected = dialog.getSelectedOptions();

        if (selected.length == 0) {
          App.ui().error(getMsg("no-variants-selected"));
          return;
        }

        selectedVariants = new HashMap<String, List<Template>>();
        for (Object key : selected) {
          ComponentType type = (ComponentType) key;
          String clazz = type.getInstanceClass().getCanonicalName();
          List<Template> variants = variantMap.get(clazz);
          if (variants != null) selectedVariants.put(clazz, variants);
        }
      } catch (Exception ex) {
        LOG.error("Error preparing variants for export", ex);
        App.ui().error(getMsg("export-variants"), getMsg("variant-export-failed"));
        return;
      }

      final VariantPackage variantPkg =
          new VariantPackage(selectedVariants, System.getProperty("user.name"));

      File initialFile =
          new File(
              variantPkg.getOwner() == null
                  ? "variants.xml"
                  : ("variants by " + variantPkg.getOwner().toLowerCase() + ".xml"));

      final File file = DialogFactory.getInstance().showSaveDialog(
          FileFilterEnum.XML.getFilter(),
          initialFile,
          FileFilterEnum.XML.getExtensions()[0],
          null);

      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

              @Override
              public Void doInBackground() throws Exception {
                LOG.debug("Exporting variants to {}", file.getAbsolutePath());

                try {
                  Serializer.toFile(file, variantPkg);

                  LOG.info("Exported variants successfully");
                } catch (IOException e) {
                  LOG.error("Could not export variants", e);
                }

                return null;
              }

              @Override
              public void complete(Void result) {
                App.ui().success(String.format(getMsg("variants-exported"), file.getName()));
              }

              @Override
              public void failed(Exception e) {
                App.ui().error(getMsg("variant-export-failed") + e.getMessage());
              }
            },
            true);
      }
    }
  }

  /**
     Import variants.
     TODO: should this be undoable?
   */
  public static class ImportVariantsAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public ImportVariantsAction(IPlugInPort plugInPort) {
      super(plugInPort, "Import Variants");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ImportVariantsAction triggered");

      final File file = DialogFactory.getInstance().showOpenDialog(
          FileFilterEnum.XML.getFilter(),
          FileFilterEnum.XML.getExtensions()[0],
          App.ui().getOwnerFrame());

      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Integer>() {

              @Override
              public Integer doInBackground() throws Exception {
                return plugInPort.importVariants(file.getAbsolutePath());
              }

              @Override
              public void complete(Integer result) {
                App.ui().success(
                    String.format(getMsg("variants-imported"), result, file.getName()));
              }

              @Override
              public void failed(Exception e) {
                App.ui().error(getMsg("variant-import-failed") + e.getMessage());
              }
            },
            true);
      }
    }
  }

  /**
     Export blocks. Not undoable.
   */
  public static class ExportBlocksAction extends AbstractAction {

    // private static final long serialVersionUID = 1L;

    public ExportBlocksAction() {
      super();
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
            (Map<String, List<IDIYComponent<?>>>) App.getObject(IPlugInPort.Key.BLOCKS);
        if (blocks == null || blocks.isEmpty()) {
          App.ui().error(getMsg("no-building-blocks"));
          return;
        }

        String[] options = blocks.keySet().toArray(new String[0]);

        Arrays.sort(
            options,
            new Comparator<String>() {

              @Override
              public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
              }
            });

        CheckBoxListDialog dialog =
            new CheckBoxListDialog(
                App.ui().getOwnerFrame(), getMsg("export-building-blocks"), options);

        dialog.setVisible(true);

        if (dialog.getSelectedButtonCaption() != "OK") return;

        Object[] selected = dialog.getSelectedOptions();

        if (selected.length == 0) {
          App.ui().error(getMsg("no-building-blocks-selected"));
          return;
        }

        selectedBlocks = new HashMap<String, List<IDIYComponent<?>>>();
        for (Object key : selected) {
          selectedBlocks.put(key.toString(), blocks.get(key));
        }
      } catch (Exception ex) {
        LOG.error("Error preparing building blocks for export", ex);
        App.ui().error(getMsg("export-building-blocks"), getMsg("building-block-export-failed"));
        return;
      }

      final BuildingBlockPackage variantPkg =
          new BuildingBlockPackage(selectedBlocks, System.getProperty("user.name"));

      File initialFile =
          new File(variantPkg.getOwner() == null
                   ? "building blocks.xml"
                   : ("building blocks by " + variantPkg.getOwner().toLowerCase() + ".xml"));

      final File file = DialogFactory.getInstance().showSaveDialog(
          FileFilterEnum.XML.getFilter(),
          initialFile,
          FileFilterEnum.XML.getExtensions()[0],
          null);

      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Void>() {

              @Override
              public Void doInBackground() throws Exception {
                LOG.debug("Exporting variants to " + file.getAbsolutePath());
                try {
                  Serializer.toFile(file, variantPkg);
                  LOG.info("Exported building blocks successfully");
                } catch (IOException e) {
                  LOG.error("Could not export building blocks", e);
                }

                return null;
              }

              @Override
              public void complete(Void result) {
                App.ui().success(
                    String.format(getMsg("building-blocks-exported"), file.getName()));
              }

              @Override
              public void failed(Exception e) {
                App.ui().error(getMsg("building-block-export-failed") + e.getMessage());
              }
            },
            true);
      }
    }
  }

  /**
     Import blocks.
     TODO: should this be undoable?
  */
  public static class ImportBlocksAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public ImportBlocksAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("import-building-blocks"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ImportBlocksAction triggered");

      final File file = DialogFactory.getInstance().showOpenDialog(
          FileFilterEnum.XML.getFilter(),
          FileFilterEnum.XML.getExtensions()[0]);

      if (file != null) {
        App.ui().executeBackgroundTask(
            new ITask<Integer>() {

              @Override
              public Integer doInBackground() throws Exception {
                return plugInPort.importBlocks(file.getAbsolutePath());
              }

              @Override
              public void complete(Integer result) {
                App.ui().success(
                    String.format(getMsg("building-blocks-imported"), result, file.getName()));
              }

              @Override
              public void failed(Exception e) {
                App.ui().error(getMsg("building-block-import-failed"), e);
              }
            },
            true);
      }
    }
  }

  /**
     Show template dialog. Not undoable.
  */
  public static class TemplateDialogAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public TemplateDialogAction() {
      super(null, getMsg("show-template-dialog"), "Template Dialog", Icon.Form.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      App.showTemplateDialog();
    }
  }

  /**
     Exit the program. Definitely not undoable.
  */
  public static class ExitAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    public ExitAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("exit"), "Quit", Icon.Exit.icon());
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

  /**
     Cut. Undoable.
  */
  public static class CutAction extends ActionFactoryAction {

    // private static final long serialVersionUID = 1L;

    private Clipboard clipboard;
    private ClipboardOwner clipboardOwner;

    public CutAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
      super(plugInPort, getMsg("cut"), "Cut", Icon.Cut.icon());
      this.clipboard = clipboard;
      this.clipboardOwner = clipboardOwner;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Cut triggered");
      clipboard.setContents(
          new ComponentTransferable(cloneComponents(
              plugInPort.getCurrentProject().getSelection())),
          clipboardOwner);
      plugInPort.deleteSelectedComponents();
    }
  }

  /**
     Paste. Undoable.
  */
  public static class PasteAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private Clipboard clipboard;

    public PasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
      super(plugInPort, getMsg("paste"), "Paste", Icon.Paste.icon());
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
        LOG.error("Could not paste.", ex);
      }
    }
  }

  /**
     Copy. Not undoable.
  */
  public static class CopyAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private Clipboard clipboard;
    private ClipboardOwner clipboardOwner;

    public CopyAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
      super(plugInPort, getMsg("copy"), "Copy", Icon.Copy.icon());
      this.clipboard = clipboard;
      this.clipboardOwner = clipboardOwner;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Copy triggered");
      clipboard.setContents(
          new ComponentTransferable(cloneComponents(
              plugInPort.getCurrentProject().getSelection())),
          clipboardOwner);
    }
  }

  /**
     Duplicate. Undoable.
  */
  public static class DuplicateAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public DuplicateAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("duplicate"), "Duplicate", Icon.DocumentsGear.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Duplicate triggered");
      try {
        // plugInPort.duplicateSelection();
        plugInPort.getCurrentProject().duplicateSelection();
      } catch (Exception ex) {
        LOG.error("Could not duplicate", ex);
        throw new RuntimeException(ex);
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

  /**
     Select all. Not undoable.
  */
  public static class SelectAllAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public SelectAllAction(IPlugInPort plugInPort) {
      super(plugInPort, "Select All", "Select All", Icon.Selection.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.trace("Select All triggered");
      plugInPort.selectAll(0);
    }
  }

  /**
     Group components. Undoable.
  */
  public static class GroupAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public GroupAction(IPlugInPort plugInPort) {
      super(plugInPort, "Group Selection", "Group", Icon.Group.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.trace("Group Selection triggered");
      plugInPort.getCurrentProject().groupSelection();
    }
  }

  /**
     Ungroup components. Undoable.
  */
  public static class UngroupAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public UngroupAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("ungroup-selection"), "Ungroup", Icon.Ungroup.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.trace("Ungroup Selection triggered");
      plugInPort.getCurrentProject().ungroupSelection();
    }
  }

  /**
     Edit project settings. Undoable.
  */
  public static class EditProjectAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public EditProjectAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("edit-project-settings"), Icon.DocumentEdit.icon());
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
          plugInPort.setDefaultPropertyValue(
              Project.class, property.getName(), property.getValue());
        }
      }
    }
  }

  /**
     Nudge selection. Undoable.
  */
  public static class NudgeAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public NudgeAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("nudge"), "Nudge", Icon.FitToSize.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Nudge triggered");
      Nudge n = new Nudge();
      boolean metric = App.getBoolean(IPlugInPort.Key.METRIC, true);
      if (metric) {
        n.setxOffset(new Size(0d, SizeUnit.mm));
        n.setyOffset(new Size(0d, SizeUnit.mm));
      } else {
        n.setxOffset(new Size(0d, SizeUnit.in));
        n.setyOffset(new Size(0d, SizeUnit.in));
      }
      List<PropertyWrapper> properties = plugInPort.getProperties(n);
      PropertyEditorDialog editor =
          DialogFactory.getInstance()
              .createPropertyEditorDialog(properties, "Nudge Selection", false);
      editor.setVisible(true);
      if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
        plugInPort.applyProperties(n, properties);
        plugInPort.nudgeSelection(n.getxOffset(), n.getyOffset(), n.getAffectStuckComponents());
      }
    }
  }

  /**
     Edit selection properties. Undoable.
  */
  public static class EditSelectionAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public EditSelectionAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("edit-selection"), "Edit Selection", Icon.EditComponent.icon());
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

  /**
     Delete selection. Undoable.
  */
  public static class DeleteSelectionAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public DeleteSelectionAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("delete-selection"), "Delete", Icon.Delete.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Delete Selection triggered");
      plugInPort.deleteSelectedComponents();
    }
  }

  /**
     Save as template. Not undoable.
  */
  public static class SaveAsTemplateAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public SaveAsTemplateAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("save-as-variant"), Icon.BriefcaseAdd.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Save as template triggered");
      String templateName =
          JOptionPane.showInputDialog(
              null, getMsg("variant-name"), getMsg("save-as-variant"), JOptionPane.PLAIN_MESSAGE);
      if (templateName != null && !templateName.trim().isEmpty()) {
        plugInPort.saveSelectedComponentAsVariant(templateName);
      }
    }
  }

  /**
     Save as block. Not undoable.
  */
  public static class SaveAsBlockAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public SaveAsBlockAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("save-as-building-block"), Icon.ComponentAdd.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Save as building block triggered");
      String templateName =
          JOptionPane.showInputDialog(
              null, "Block name:", getMsg("save-as-building-block"), JOptionPane.PLAIN_MESSAGE);
      if (templateName != null && !templateName.trim().isEmpty()) {
        plugInPort.saveSelectionAsBlock(templateName);
      }
    }
  }

  /**
     Expand selection.
     TODO: what does this do, should this be undoable?
   */
  public static class ExpandSelectionAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private ExpansionMode expansionMode;

    public ExpandSelectionAction(IPlugInPort plugInPort, ExpansionMode expansionMode) {
      super(
          plugInPort,
          (expansionMode == ExpansionMode.ALL
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
      LOG.trace("Expand Selection triggered, mode {}", expansionMode);
      plugInPort.expandSelection(expansionMode);
    }
  }

  /**
     Rotate selection. Undoable.
  */
  public static class RotateSelectionAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private int direction;

    public RotateSelectionAction(IPlugInPort plugInPort, int direction) {
      super(
          plugInPort,
          getMsg(direction > 0 ? "rotate-clockwise" : "rotate-counterclockwise"),
          direction > 0 ? "Rotate Clockwise" : "Rotate Counterclockwise",
          direction > 0 ? Icon.RotateCW.icon() : Icon.RotateCCW.icon());
      this.direction = direction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Rotate Selection triggered: " + direction);
      plugInPort.rotateSelection(direction);
    }
  }

  /**
     Mirror selection. Undoable.
  */
  public static class MirrorSelectionAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private int direction;

    public MirrorSelectionAction(IPlugInPort plugInPort, int direction) {
      super(plugInPort,
            getMsg(direction == IComponentTransformer.HORIZONTAL
                   ? "mirror-horizontally"
                   : "mirror-vertically"),
            direction == IComponentTransformer.HORIZONTAL
            ? "Mirror Horizontally"
            : "Mirror Vertically",
            direction == IComponentTransformer.HORIZONTAL
            ? Icon.FlipHorizontal.icon()
            : Icon.FlipVertical.icon());
      this.direction = direction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Mirror Selection triggered: " + direction);
      plugInPort.mirrorSelection(direction);
    }
  }

  /**
     Send to back. Undoable.
  */
  public static class SendToBackAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public SendToBackAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("send-to-back"), "Send to Back", Icon.Back.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Send to Back triggered");
      plugInPort.sendSelectionToBack();
    }
  }

  /**
     Bring to front. Undoable.
  */
  public static class BringToFrontAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public BringToFrontAction(IPlugInPort plugInPort) {
      super(plugInPort, getMsg("bring-to-front"), "Bring to Front", Icon.Front.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Bring to Front triggered");
      plugInPort.bringSelectionToFront();
    }
  }

  /**
     Configuration options settable via checkbox in UI. Not undoable.
   */
  public static class ConfigAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private String configKey;
    private String tipKey;

    public ConfigAction(
        IPlugInPort plugInPort,
        String title,
        String configKey,
        boolean defaultValue,
        String tipKey) {
      super(plugInPort, title);
      this.configKey = configKey;
      this.tipKey = tipKey;
      putValue(IView.CHECK_BOX_MENU_ITEM, true);
      putValue(AbstractAction.SELECTED_KEY, App.getBoolean(configKey, defaultValue));
    }

    public ConfigAction(
        IPlugInPort plugInPort, String title, String configKey, boolean defaultValue) {
      this(plugInPort, title, configKey, defaultValue, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      App.putValue(configKey, getValue(AbstractAction.SELECTED_KEY));
      if ((Boolean) getValue(AbstractAction.SELECTED_KEY)
          && tipKey != null
          && !App.getBoolean(tipKey + ".dismissed", false)) {
        DialogFactory.getInstance().createInfoDialog(tipKey).setVisible(true);
      }
      plugInPort.refresh();
    }
  }

  /**
     Select theme. Not undoable.
  */
  public static class ThemeAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private Theme theme;

    public ThemeAction(IPlugInPort plugInPort, Theme theme) {
      super(plugInPort, theme.getName());
      this.theme = theme;
      putValue(IView.RADIO_BUTTON_GROUP_KEY, "theme");
      putValue(
          AbstractAction.SELECTED_KEY,
          plugInPort.getSelectedTheme().getName().equals(theme.getName()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("{} triggered", getValue(AbstractAction.NAME));
      plugInPort.setSelectedTheme(theme);
    }
  }

  /**
     Use component browser. Not undoable.
   */
  public static class ComponentBrowserAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private String browserType;

    public ComponentBrowserAction(String browserType) {
      super(null, browserType);
      this.browserType = browserType;
      putValue(IView.RADIO_BUTTON_GROUP_KEY, "componentBrowser");
      putValue(
          AbstractAction.SELECTED_KEY,
          browserType.equals(
              App.getString(ConfigPlugin.COMPONENT_BROWSER, ConfigPlugin.SEARCHABLE_TREE)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("{} triggered", getValue(AbstractAction.NAME));
      App.putValue(ConfigPlugin.COMPONENT_BROWSER, browserType);
    }
  }

  /**
     Renumber selection. Undoable.
  */
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

  /**
     Generate netlist. Not undoable.
   */
  public static class GenerateNetlistAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    public GenerateNetlistAction(IPlugInPort plugInPort) {
      super(plugInPort, "Generate DIYLC Netlist", Icon.Web.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      App.ui().executeBackgroundTask(
          new ITask<List<Netlist>>() {

            @Override
            public List<Netlist> doInBackground() throws Exception {
              return plugInPort.extractNetlists(true);
            }

            @Override
            public void failed(Exception e) {
              App.ui().error("Failed to generate the netlist.", e);
            }

            @Override
            public void complete(List<Netlist> res) {
              if (res == null) {
                App.ui().info("The generated netlist is empty, nothing to show.");
                return;
              }
              StringBuilder sb = new StringBuilder("<html>");

              for (Netlist netlist : res) {
                sb.append(
                    "<p style=\"font-family: "
                    + new JLabel().getFont().getName()
                    + "; font-size: 9px\"><b>Switch configuration: ")
                    .append(netlist.getSwitchSetup())
                    .append("</b><br><br>Connected node groups:<br>");
                for (Group v : netlist.getSortedGroups()) {
                  sb.append("&nbsp;&nbsp;").append(v.getSortedNodes()).append("<br>");
                }
                sb.append("</p><br><hr>");
              }
              sb.append("</html>");
              new TextDialog(
                  App.ui().getOwnerFrame().getRootPane(),
                  sb.toString(),
                  "DIYLC Netlist",
                  new Dimension(600, 480))
                  .setVisible(true);
            }
          },
          true);
    }
  }

  /**
     Summarize netlist. Not undoable.
  */
  public static class SummarizeNetlistAction extends ActionFactoryAction {

    private static final long serialVersionUID = 1L;

    private INetlistAnalyzer summarizer;

    public SummarizeNetlistAction(IPlugInPort plugInPort, INetlistAnalyzer summarizer) {
      super(
          plugInPort,
          summarizer.getName(),
          Enum.valueOf(Icon.class, summarizer.getIconName()).icon());
      this.summarizer = summarizer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

      App.ui().executeBackgroundTask(
          new ITask<List<Summary>>() {

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
              App.ui().info(summarizer.getName(), e);
            }

            @Override
            public void complete(List<Summary> res) {
              if (res == null) {
                App.ui().info(summarizer.getName(), getMsg("empty-netlist-summary"));
                return;
              }
              StringBuilder sb = new StringBuilder("<html>");

              for (Summary summary : res) {
                sb.append("<p style=\"font-family: ")
                    .append(summarizer.getFontName())
                    .append("; font-size: 9px\">");

                if (res.size() > 1)
                  sb.append("<b>Switch configuration: ")
                      .append(summary.getNetlist().getSwitchSetup())
                      .append("</b><br><br>");

                sb.append(summary.getSummary());

                sb.append("</p><br>");

                if (res.size() > 1) sb.append("<hr>");
              }
              sb.append("</html>");
              new TextDialog(
                  App.ui().getOwnerFrame().getRootPane(),
                  sb.toString(),
                  summarizer.getName(),
                  new Dimension(600, 480)).setVisible(true);
            }
          },
          true);
    }
  }
}

/*
  DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by
  the individual authors.

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

package org.diylc.swing.plugins.config;

import java.io.File;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.Serializer;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.components.autocreate.SolderPadAutoCreator;
import org.diylc.core.Theme;
import org.diylc.images.Icon;
import org.diylc.swing.action.ActionFactory;

/**
 * Controls configuration menu.
 *
 * @author Branislav Stojkovic
 */
public class ConfigPlugin implements IPlugIn {

  private static final Logger LOG = LogManager.getLogger(ConfigPlugin.class);

  public static final String COMPONENT_BROWSER = "componentBrowser";
  public static final String SEARCHABLE_TREE = "Searchable Tree";
  public static final String TABBED_TOOLBAR = "Tabbed Toolbar";

  public ConfigPlugin() {
    super();
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    final ConfigActions actions = new ConfigActions();

    // TODO: get default values from config?
    actions.add("anti-aliasing", IPlugInPort.Key.ANTI_ALIASING, true);
    actions.add("auto-create-pads", SolderPadAutoCreator.AUTO_PADS_KEY, false);
    actions.add("auto-edit-mode", IPlugInPort.Key.AUTO_EDIT, true);
    actions.add("continuous-creation", IPlugInPort.Key.CONTINUOUS_CREATION, false);
    actions.add("export-grid", IPlugInPort.Key.EXPORT_GRID, false);
    actions.add("extra-working-area", IPlugInPort.Key.EXTRA_SPACE, true);
    actions.add("hardware-acceleration", IPlugInPort.Key.HARDWARE_ACCELERATION, false);
    actions.add("hi-quality-rendering", IPlugInPort.Key.HI_QUALITY_RENDER, false);
    actions.add("highlight-connected-areas", IPlugInPort.Key.HIGHLIGHT_CONTINUITY_AREA, false);
    actions.add("mouse-wheel-zoom", IPlugInPort.Key.WHEEL_ZOOM, false);
    actions.add("outline-mode", IPlugInPort.Key.OUTLINE, false);
    actions.add("show-rulers", IPlugInPort.Key.SHOW_RULERS, true);
    actions.add("show-grid", IPlugInPort.Key.SHOW_GRID, true);
    actions.add("snap-to-grid", IPlugInPort.Key.SNAP_TO_GRID, true);
    actions.add("sticky-points", IPlugInPort.Key.STICKY_POINTS, true);

    final String configMenu = App.getString("menu.config.title");

    actions.injectActions(plugInPort, configMenu);

    // Themes
    // TODO - get default themes from resources
    File themeDir = new File(Utils.getUserDataDirectory() + "themes");
    if (themeDir.exists()) {
      final String themeMenu = App.getString("menu.config.theme");
      App.ui().injectSubmenu(themeMenu, Icon.Pens, configMenu);
      for (File file : themeDir.listFiles()) {
        if (file.getName().toLowerCase().endsWith(".xml")) {
          try {
            Theme theme = (Theme) Serializer.fromFile(file);
            LOG.debug("Found theme: " + theme.getName());
            App.ui().injectMenuAction(
                ActionFactory.createThemeAction(plugInPort, theme),
                themeMenu);
          } catch (Exception e) {
            LOG.error("Could not load theme file " + file.getName(), e);
          }
        }
      }
    }

    // Toolbox
    final String componentBrowserMenu = App.getString("menu.config.component-browser");
    App.ui().injectSubmenu(componentBrowserMenu, Icon.Hammer, configMenu);
    App.ui().injectMenuAction(
        ActionFactory.createComponentBrowserAction(SEARCHABLE_TREE),
        componentBrowserMenu);
    App.ui().injectMenuAction(
        ActionFactory.createComponentBrowserAction(TABBED_TOOLBAR),
        componentBrowserMenu);

    // Developer Tools
    final ConfigActions developerActions = new ConfigActions();
    final String developerMenu = App.getString("menu.config.developer");
    App.ui().injectSubmenu(developerMenu, Icon.Screwdriver, configMenu);
    // TODO: get default values from Config - developer might want to always set these
    developerActions.add("debug-component-areas", IPlugInPort.Debug.COMPONENT_AREA, false);
    developerActions.add("debug-continuity-areas", IPlugInPort.Debug.CONTINUITY_AREA, false);
    developerActions.injectActions(plugInPort, developerMenu);
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.noneOf(EventType.class);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}
}

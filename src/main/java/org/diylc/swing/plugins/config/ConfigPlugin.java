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
import org.diylc.appframework.Serializer;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.Config;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
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
  private static final String configMenu = App.getString("menu.config.title");
  private static final ConfigActions actions = new ConfigActions();

  static {
    actions.add("anti-aliasing", Config.Flag.ANTI_ALIASING, App.antiAliasing());
    // TODO: get default value for AUTO_PADS from Config
    actions.add("auto-create-pads", Config.Flag.AUTO_PADS, false);
    actions.add("auto-edit-mode", Config.Flag.AUTO_EDIT, App.autoEdit());
    actions.add("continuous-creation", Config.Flag.CONTINUOUS_CREATION, App.continuousCreation());
    actions.add("export-grid", Config.Flag.EXPORT_GRID, App.exportGrid());
    actions.add("extra-working-area", Config.Flag.EXTRA_SPACE, App.extraSpace());
    actions.add(
        "hardware-acceleration", Config.Flag.HARDWARE_ACCELERATION, App.hardwareAcceleration());
    actions.add("hi-quality-rendering", Config.Flag.HI_QUALITY_RENDER, App.highQualityRendering());
    actions.add(
        "highlight-connected-areas",
        Config.Flag.HIGHLIGHT_CONTINUITY_AREA,
        App.highlightContinuityArea());
    actions.add("mouse-wheel-zoom", Config.Flag.WHEEL_ZOOM, App.wheelZoom());
    actions.add("outline-mode", Config.Flag.OUTLINE, App.outlineMode());
    actions.add("show-rulers", Config.Flag.SHOW_RULERS, App.showRulers());
    actions.add("show-grid", Config.Flag.SHOW_GRID, App.showGrid());
    actions.add("snap-to-grid", Config.Flag.SNAP_TO_GRID, App.snapToGrid());
    actions.add("sticky-points", Config.Flag.STICKY_POINTS, App.stickyPoints());
  }

  public static final String COMPONENT_BROWSER = "componentBrowser";
  public static final String SEARCHABLE_TREE = "Searchable Tree";
  public static final String TABBED_TOOLBAR = "Tabbed Toolbar";

  public ConfigPlugin() {
    super();
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    actions.injectActions(plugInPort, configMenu);

    final String themeMenu = App.getString("menu.config.theme");
    App.ui().injectSubmenu(themeMenu, Icon.Pens, configMenu);
    for (String themeName : Theme.getThemes().keySet()) {
      App.ui()
          .injectMenuAction(
              ActionFactory.createThemeAction(plugInPort, Theme.getTheme(themeName)), themeMenu);
    }

    // Toolbox
    final String componentBrowserMenu = App.getString("menu.config.component-browser");
    App.ui().injectSubmenu(componentBrowserMenu, Icon.Hammer, configMenu);
    App.ui()
        .injectMenuAction(
            ActionFactory.createComponentBrowserAction(SEARCHABLE_TREE), componentBrowserMenu);
    App.ui()
        .injectMenuAction(
            ActionFactory.createComponentBrowserAction(TABBED_TOOLBAR), componentBrowserMenu);

    // Developer Tools
    final String developerMenu = App.getString("menu.config.developer");
    final ConfigActions developerActions = new ConfigActions();
    App.ui().injectSubmenu(developerMenu, Icon.Screwdriver, configMenu);
    // TODO: get default values from Config - developer might want to always set these
    developerActions.add("debug-component-areas", Config.Flag.DEBUG_COMPONENT_AREA, false);
    developerActions.add("debug-continuity-areas", Config.Flag.DEBUG_CONTINUITY_AREA, false);
    developerActions.injectActions(plugInPort, developerMenu);

    App.ui().injectMenuAction(ActionFactory.createResetOptionsAction(this), configMenu);
  }

  public void resetOptionsToDefaults() {
    actions.reset();
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.noneOf(EventType.class);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    //
  }
}

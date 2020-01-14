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

  private static final String CONFIG_MENU = "Config";
  private static final String THEME_MENU = "Theme";
  private static final String COMPONENT_BROWSER_MENU = "Toolbox";
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

    actions.injectActions(plugInPort, CONFIG_MENU);

    File themeDir = new File("themes");
    if (themeDir.exists()) {
      App.ui().injectSubmenu(THEME_MENU, Icon.Pens, CONFIG_MENU);
      for (File file : themeDir.listFiles()) {
        if (file.getName().toLowerCase().endsWith(".xml")) {
          try {
            Theme theme = (Theme) Serializer.fromFile(file);
            LOG.debug("Found theme: " + theme.getName());
            App.ui().injectMenuAction(
                ActionFactory.createThemeAction(plugInPort, theme), THEME_MENU);
          } catch (Exception e) {
            LOG.error("Could not load theme file " + file.getName(), e);
          }
        }
      }
    }

    App.ui().injectSubmenu(COMPONENT_BROWSER_MENU, Icon.Hammer, CONFIG_MENU);
    App.ui().injectMenuAction(
        ActionFactory.createComponentBrowserAction(SEARCHABLE_TREE), COMPONENT_BROWSER_MENU);
    App.ui().injectMenuAction(
        ActionFactory.createComponentBrowserAction(TABBED_TOOLBAR), COMPONENT_BROWSER_MENU);
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.noneOf(EventType.class);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}
}

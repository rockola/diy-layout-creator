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
import org.diylc.DIYLC;
import org.diylc.appframework.Serializer;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.components.autocreate.SolderPadAutoCreator;
import org.diylc.core.Theme;
import org.diylc.images.Icon;
import org.diylc.swing.ActionFactory;

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

    actions.add("Anti-Aliasing", IPlugInPort.ANTI_ALIASING_KEY, true);
    actions.add("Auto-Create Pads", SolderPadAutoCreator.AUTO_PADS_KEY, false);
    actions.add("Auto-Edit Mode", IPlugInPort.AUTO_EDIT_KEY, true);
    actions.add("Continuous Creation", IPlugInPort.CONTINUOUS_CREATION_KEY, false);
    actions.add("Export Grid", IPlugInPort.EXPORT_GRID_KEY, false);
    actions.add("Extra Working Area", IPlugInPort.EXTRA_SPACE_KEY, true);
    actions.add("Hardware Acceleration", IPlugInPort.HARDWARE_ACCELERATION, false);
    actions.add("Hi-Quality Rendering", IPlugInPort.HI_QUALITY_RENDER_KEY, false);
    actions.add("Highlight Connected Areas", IPlugInPort.HIGHLIGHT_CONTINUITY_AREA, false);
    actions.add("Mouse Wheel Zoom", IPlugInPort.WHEEL_ZOOM_KEY, false);
    actions.add("Outline Mode", IPlugInPort.OUTLINE_KEY, false);
    actions.add("Show Rulers", IPlugInPort.SHOW_RULERS_KEY, true);
    actions.add("Show Grid", IPlugInPort.SHOW_GRID_KEY, true);
    actions.add("Snap to Grid", IPlugInPort.SNAP_TO_GRID_KEY, true);
    actions.add("Sticky Points", IPlugInPort.STICKY_POINTS_KEY, true);

    actions.injectActions(plugInPort, CONFIG_MENU);

    File themeDir = new File("themes");
    if (themeDir.exists()) {
      DIYLC.ui().injectSubmenu(THEME_MENU, Icon.Pens, CONFIG_MENU);
      for (File file : themeDir.listFiles()) {
        if (file.getName().toLowerCase().endsWith(".xml")) {
          try {
            Theme theme = (Theme) Serializer.fromFile(file);
            LOG.debug("Found theme: " + theme.getName());
            DIYLC
                .ui()
                .injectMenuAction(ActionFactory.createThemeAction(plugInPort, theme), THEME_MENU);
          } catch (Exception e) {
            LOG.error("Could not load theme file " + file.getName(), e);
          }
        }
      }
    }

    DIYLC.ui().injectSubmenu(COMPONENT_BROWSER_MENU, Icon.Hammer, CONFIG_MENU);

    DIYLC
        .ui()
        .injectMenuAction(
            ActionFactory.createComponentBrowserAction(SEARCHABLE_TREE), COMPONENT_BROWSER_MENU);
    DIYLC
        .ui()
        .injectMenuAction(
            ActionFactory.createComponentBrowserAction(TABBED_TOOLBAR), COMPONENT_BROWSER_MENU);
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.noneOf(EventType.class);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}
}

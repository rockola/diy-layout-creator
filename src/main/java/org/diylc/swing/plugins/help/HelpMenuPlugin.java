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

package org.diylc.swing.plugins.help;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import javax.swing.AbstractAction;
import org.apache.logging.log4j.LogManager;
import org.diylc.App;
import org.diylc.Splash;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.update.UpdateChecker;
import org.diylc.appframework.update.Version;
import org.diylc.common.Config;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.Icon;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swingframework.AboutDialog;
import org.diylc.swingframework.update.UpdateDialog;

/**
 * Entry point class for help-related utilities.
 *
 * @author Branislav Stojkovic
 */
public class HelpMenuPlugin implements IPlugIn {

  private static final String HELP_TITLE = Config.getString("menu.help.title");

  private AboutDialog aboutDialog;

  private void navigateUrl(String menuEntry, Icon icon, String key) {
    App.ui()
        .injectMenuAction(
            new NavigateUrlAction(
                Config.getString("menu.help." + menuEntry), icon, Config.getUrl(key).toString()),
            HELP_TITLE);
  }

  private void separator() {
    App.ui().injectMenuAction(null, HELP_TITLE);
  }

  public HelpMenuPlugin() {
    navigateUrl("user-manual", Icon.Manual, "manual");
    navigateUrl("faq", Icon.Faq, "faq");
    navigateUrl("component-api", Icon.CoffeebeanEdit, "component");
    // navigateUrl("plugin-api", Icon.ApplicationEdit, "plugin");
    navigateUrl("bug-report", Icon.Bug, "bug");
    separator();
    App.ui().injectMenuAction(new RecentUpdatesAction(), HELP_TITLE);
    separator();
    navigateUrl("donate", Icon.Donate, "donate");
    App.ui().injectMenuAction(new AboutAction(), HELP_TITLE);
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    // this.plugInPort = plugInPort;
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return null;
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}

  public static class AboutTextAction extends AbstractAction {
    private String textKey;
    private AboutDialog about;

    public AboutTextAction(AboutDialog about, String textKey) {
      super(textKey, null);
      this.about = about;
      this.textKey = textKey;
    }

    public void actionPerformed(ActionEvent e) {
      about.setEditorText(App.getHtml(this.textKey));
    }
  }

  private String defaultAboutText() {
    return App.getHtml("interactive-license");
  }

  private AboutDialog getAboutDialog() {
    if (aboutDialog == null) {
      aboutDialog =
          DialogFactory.getInstance()
              .createAboutDialog(
                  Config.getString("app.title"),
                  Icon.App.icon(),
                  Config.getString("app.version"),
                  Config.getString("app.author"),
                  Config.getUrl("website"),
                  defaultAboutText());

      aboutDialog.setSize(aboutDialog.getSize().width + 30, aboutDialog.getSize().height + 200);
      aboutDialog.addAction("W", "warranty", new AboutTextAction(aboutDialog, "warranty"));
      aboutDialog.addAction("C", "conditions", new AboutTextAction(aboutDialog, "conditions"));
    }
    return aboutDialog;
  }

  class AboutAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public AboutAction() {
      super();
      putValue(AbstractAction.NAME, "About");
      putValue(AbstractAction.SMALL_ICON, Icon.About.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // reset About dialog text when making visible
      getAboutDialog().setEditorText(defaultAboutText());
      getAboutDialog().setVisible(true);
    }
  }

  public static class RecentUpdatesAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public RecentUpdatesAction() {
      super();
      putValue(AbstractAction.NAME, "Recent Updates");
      putValue(AbstractAction.SMALL_ICON, Icon.ScrollInformation.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<Version> updates = Version.getRecentUpdates();
      if (updates == null) {
        App.ui().info("Version history is not available.");
      } else {
        String html = UpdateChecker.createUpdateHtml(updates);
        UpdateDialog updateDialog =
            new UpdateDialog(App.ui().getOwnerFrame().getRootPane(), html, (String) null);
        updateDialog.setVisible(true);
      }
    }
  }

  public static class NavigateUrlAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private String url;

    public NavigateUrlAction(String name, Icon icon, String url) {
      super();
      this.url = url;
      putValue(AbstractAction.NAME, name);
      putValue(AbstractAction.SMALL_ICON, icon.icon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        App.openUrl(new URL(url));
      } catch (Exception e1) {
        LogManager.getLogger(HelpMenuPlugin.class).error("Could not launch default browser", e1);
      }
    }
  }
}

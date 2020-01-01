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
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.update.UpdateChecker;
import org.diylc.appframework.update.Version;
import org.diylc.common.Config;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Message;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swingframework.AboutDialog;
import org.diylc.swingframework.update.UpdateDialog;

/**
   Entry point class for help-related utilities.
    
   @author Branislav Stojkovic
*/
public class HelpMenuPlugin implements IPlugIn {

    private static final String HELP_TITLE = Config.getString("menu.help.title");

    private IPlugInPort plugInPort;
    private AboutDialog aboutDialog;
    private ISwingUI swingUI;

    private void navigateURL(String menuEntry, Icon icon, String key) {
	this.swingUI.injectMenuAction(new NavigateURLAction(Config.getString("menu.help." + menuEntry),
							    icon,
							    Config.getURL(key).toString()),
				      HELP_TITLE);
    }

    private void separator() {
	this.swingUI.injectMenuAction(null, HELP_TITLE);
    }

    public HelpMenuPlugin(ISwingUI swingUI) {
	this.swingUI = swingUI;
	navigateURL("user-manual", IconLoader.Manual.getIcon(), "manual");
	navigateURL("faq", IconLoader.Faq.getIcon(), "faq");
	navigateURL("component-api", IconLoader.CoffeebeanEdit.getIcon(), "component");
	navigateURL("plugin-api", IconLoader.ApplicationEdit.getIcon(), "plugin");
	navigateURL("bug-report", IconLoader.Bug.getIcon(), "bug");
	separator();
	swingUI.injectMenuAction(new RecentUpdatesAction(), HELP_TITLE);
	separator();
	navigateURL("donate", IconLoader.Donate.getIcon(), "donate");
	swingUI.injectMenuAction(new AboutAction(), HELP_TITLE);
    }

    @Override
    public void connect(IPlugInPort plugInPort) {
	this.plugInPort = plugInPort;
    }

    @Override
    public EnumSet<EventType> getSubscribedEventTypes() {
	return null;
    }

    @Override
    public void processMessage(EventType eventType, Object... params) {}

    public class AboutTextAction extends AbstractAction {
	private String textKey;
	private AboutDialog about;

	public AboutTextAction(AboutDialog about, String textKey) {
	    super(textKey, null);
	    this.about = about;
	    this.textKey = textKey;
	}

	public void actionPerformed(ActionEvent e) {
	    about.setEditorText(Message.getHTML(this.textKey));
	}
    }

    private AboutDialog getAboutDialog() {
	if (aboutDialog == null) {
	    aboutDialog =
		DialogFactory.getInstance().createAboutDialog(Config.getString("app.title"),
							      IconLoader.IconLarge.getIcon(),
							      plugInPort.getCurrentVersionNumber().toString(),
							      Config.getString("app.author"),
							      Config.getURL("website"),
							      Message.getHTML("interactive-license"));
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
	    putValue(AbstractAction.SMALL_ICON, IconLoader.About.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    getAboutDialog().setVisible(true);
	}
    }
  
    class RecentUpdatesAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	public RecentUpdatesAction() {
	    super();
	    putValue(AbstractAction.NAME, "Recent Updates");
	    putValue(AbstractAction.SMALL_ICON, IconLoader.ScrollInformation.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    List<Version> updates = plugInPort.getRecentUpdates();
	    if (updates == null)
		swingUI.showMessage("Version history is not available.",
				    "Information",
				    IView.INFORMATION_MESSAGE);
	    else {
		String html = UpdateChecker.createUpdateHTML(updates);
		UpdateDialog updateDialog = new UpdateDialog(swingUI.getOwnerFrame().getRootPane(),
							     html,
							     (String) null);
		updateDialog.setVisible(true);
	    }
	}
    }

    class NavigateURLAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private String url;

	public NavigateURLAction(String name, Icon icon, String url) {
	    super();
	    this.url = url;
	    putValue(AbstractAction.NAME, name);
	    putValue(AbstractAction.SMALL_ICON, icon);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    try {
		Utils.openURL(url);
	    } catch (Exception e1) {
		LogManager.getLogger(HelpMenuPlugin.class).error("Could not launch default browser", e1);
	    }
	}
    }
}

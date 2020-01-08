package org.diylc.swing.plugins.config;

import java.util.ArrayList;
import java.util.List;

import org.diylc.DIYLC;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;

public class ConfigActions {
    private List<ConfigAction> actions;

    public ConfigActions() {
	actions = new ArrayList<ConfigAction>();
    }

    public void add(String name, String action, boolean defaultValue) {
	actions.add(new ConfigAction(name, action, defaultValue));
    }

    public void injectActions(IPlugInPort plugInPort, String menuName) {
	for (ConfigAction a : actions) {
	    DIYLC.ui().injectMenuAction(ActionFactory.createConfigAction(plugInPort,
									 a.getName(),
									 a.getAction(),
									 a.getDefault()),
					menuName);
	}
    }

    private class ConfigAction {
	private String name;
	private String action;
	private boolean defaultValue;

	public ConfigAction(String name, String action, boolean defaultValue) {
	    this.name = name;
	    this.action = action;
	    this.defaultValue = defaultValue;
	}

	public String getName() { return name; }
	public String getAction() { return action; }
	public boolean getDefault() { return defaultValue; }
    }
}

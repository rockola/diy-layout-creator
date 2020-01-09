/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2020 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.diylc.DIYLC;
import org.diylc.common.Config;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.images.Icon;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swing.ISimpleView;

public class UploadManagerFrame extends JFrame implements ISimpleView {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogManager.getLogger(UploadManagerFrame.class);

    private static final String TITLE = "Manage My Uploads";
    private ResultsScrollPanel resultsScrollPane;
    private IPlugInPort plugInPort;

    public UploadManagerFrame(IPlugInPort plugInPort) {
	super(TITLE);
	this.setIconImage(Icon.CloudGear.image());
	this.setPreferredSize(new Dimension(700, 640));
	this.plugInPort = plugInPort;

	setContentPane(getResultsScrollPane());
	this.pack();
	this.setLocationRelativeTo(DIYLC.ui().getOwnerFrame());
	this.setGlassPane(SimpleCloudGlassPane.GLASS_PANE);

	search();
    }

    private ResultsScrollPanel getResultsScrollPane() {
	if (resultsScrollPane == null) {
	    resultsScrollPane = new ResultsScrollPanel(this, plugInPort, null, true);
	}
	return resultsScrollPane;
    }

    private void search() {
	getResultsScrollPane().clearPrevious();
	executeBackgroundTask(new ITask<List<ProjectEntity>>() {

		@Override
		public List<ProjectEntity> doInBackground() throws Exception {
		    return CloudPresenter.Instance.fetchUserUploads(null);
		}

		@Override
		public void failed(Exception e) {
		    showMessage("Search failed! Detailed message is in the logs. Please report to the author.", "Search Failed",
				IView.ERROR_MESSAGE);
		}

		@Override
		public void complete(List<ProjectEntity> result) {
		    //        setTitle(TITLE + " - " + result.size() + " Uploads Found");
		    getResultsScrollPane().startSearch(result);
		}
	    });
    }

    @Override
    public <T extends Object> void executeBackgroundTask(final ITask<T> task) {
	getGlassPane().setVisible(true);
	SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {

		@Override
		protected T doInBackground() throws Exception {
		    return task.doInBackground();
		}

		@Override
		protected void done() {
		    getGlassPane().setVisible(false);
		    try {
			T result = get();
			task.complete(result);
		    } catch (ExecutionException e) {
			LOG.error("Background task execution failed", e);
			task.failed(e);
		    } catch (InterruptedException e) {
			LOG.error("Background task execution interrupted", e);
			task.failed(e);
		    }
		}
	    };
	worker.execute();
    }

    @Override
    public void showMessage(String message, String title, int messageType) {
	JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public void info(String title, String text) {
	showMessage(text, title, IView.INFORMATION_MESSAGE);
    }
    public void info(String text) {
	info(Config.getString("message.info"), text);
    }
    public void error(String title, String text) {
	showMessage(text, title, IView.ERROR_MESSAGE);
    }
    public void error(String text) {
	error(Config.getString("message.error"), text);
    }
    public void error(String text, Exception e) {
	error(Config.getString("message.error"),
	      String.format("%s %s", text, e.getMessage()));
    }
    public void warn(String title, String text) {
	showMessage(text, title, IView.WARNING_MESSAGE);
    }
    public void warn(String text) {
	warn(Config.getString("message.warn"), text);
    }

    @Override
    public int showConfirmDialog(String message, String title, int optionType, int messageType) {
	return JOptionPane.showConfirmDialog(this, message, title, optionType, messageType);
    }
  
    @Override
    public JFrame getOwnerFrame() {
	return this;
    }
}

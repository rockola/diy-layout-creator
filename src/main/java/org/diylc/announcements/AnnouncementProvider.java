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

package org.diylc.announcements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.diylc.App;

public class AnnouncementProvider {

  private static final Logger LOG = LogManager.getLogger(AnnouncementProvider.class);

  private String serviceUrl = App.getUrl("api.announcements").toString();
  private final String lastReadKey = "announcement.lastReadDate";
  private Date lastDate;
  private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private Parser parser;
  private HtmlRenderer renderer;
  private OkHttpClient httpClient = new OkHttpClient();

  public AnnouncementProvider() {
    parser = Parser.builder().build();
    renderer = HtmlRenderer.builder().build();
    String lastDateStr = App.getString(lastReadKey);
    try {
      lastDate = lastDateStr == null ? null : dateFormat.parse(lastDateStr);
      LOG.info("Announcement last read {}", lastDate);
    } catch (ParseException e) {
      LOG.error("Could not parse last read date {}", lastDateStr);
    }
  }

  public String getCurrentAnnouncements(boolean forceLast) throws IOException {
    Request request = new Request.Builder().url(serviceUrl).build();
    String announcements = null;
    try (Response response = httpClient.newCall(request).execute()) {
      if (response != null && response.body() != null) {
        Node document = parser.parse(response.body().string());
        // render replacing newlines with spaces
        // (JOptionPane wants the HTML in one line...)
        //
        // Can't use HtmlRenderer.builder().softbreak(" ") here
        // as that will not get rid of newlines between tags
        String txt = renderer.render(document).replace("\n", " ");

        // TODO: only show announcements newer than lastReadKey

        LOG.debug("Announcements [{}]", txt);

        // wrap in HTML tags for JOptionPane
        announcements = String.format("<html>%s</html>", txt);
      }
    }
    return announcements;
  }

  public void dismissed() {
    Date date = new Date();
    App.putValue(lastReadKey, dateFormat.format(date));
  }
}

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

package org.diylc.appframework.simplemq;

import java.util.EnumSet;
import javax.swing.SwingUtilities;

/**
 * Interface for message listener.
 *
 * @author Branislav Stojkovic
 * @param <E> enum that contains all available event types
 * @see MessageDispatcher
 */
public interface IMessageListener<E extends Enum<E>> {

  /**
   * Returns a set of event types to subscribe for. Listener will be
   * notified only if event type is contained in this set.
   *
   * @return
   */
  EnumSet<E> getSubscribedEventTypes();

  /**
   * Called from the background thread when event is received. Use
   * {@link SwingUtilities#invokeLater} if event processing needs to
   * take place in the EDT.
   *
   * @param eventType Received event.
   * @param params Message parameters.
   */
  void processMessage(E eventType, Object... params);
}

package org.diylc.appframework.simplemq;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Synchronous message distribution.
 *
 * @author Branislav Stojkovic
 * @param <E> enum that contains all available event types
 * @see IMessageListener
 */
public class MessageDispatcher<E extends Enum<E>> {

  private static final Logger LOG = LogManager.getLogger(MessageDispatcher.class);

  private Map<E, List<IMessageListener<E>>> subscriptions = new HashMap<>();

  public MessageDispatcher() {}

  public void subscribe(IMessageListener<E> listener, E eventType) {
    if (subscriptions.get(eventType) == null) {
      subscriptions.put(eventType, new ArrayList<IMessageListener<E>>());
    }

    List<IMessageListener<E>> subscribers = subscriptions.get(eventType);
    if (!subscribers.contains(listener)) {
      subscribers.add(listener);
    }
    LOG.debug(
        "subscribe({}, {}): {} subscribers",
        listener.getClass().getName(),
        eventType,
        subscribers.size());
    int i = 1;
    for (IMessageListener<E> subscriber : subscribers) {
      LOG.debug(
          "subscribe({}, {}): subscriber #{} is a {}",
          listener.getClass().getName(),
          eventType,
          i,
          subscriber.getClass().getName());
      i = i + 1;
    }
  }

  public void subscribe(IMessageListener<E> listener, EnumSet<E> eventTypes) {
    if (eventTypes != null) {
      for (E e : eventTypes) {
        subscribe(listener, e);
      }
    }
  }

  public void subscribe(IMessageListener<E> listener) {
    subscribe(listener, listener.getSubscribedEventTypes());
  }

  /**
   * Notifies all interested listeners.
   *
   * @param eventType
   * @param params
   */
  public void dispatchMessage(E eventType, Object... params) {
    List<IMessageListener<E>> listeners = subscriptions.get(eventType);
    if (listeners != null) {
      for (IMessageListener<E> listener : listeners) {
        try {
          listener.processMessage(eventType, params);
        } catch (Exception e) {
          LOG.error("Listener threw an exception", e);
          throw new RuntimeException(e);
        }
      }
    }
  }
}

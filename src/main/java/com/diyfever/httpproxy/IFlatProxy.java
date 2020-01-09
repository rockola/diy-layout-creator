package com.diyfever.httpproxy;

import java.io.InputStream;
import java.util.Map;

/**
 * Interface for web proxy utilities.
 *
 * @author Branislav Stojkovic
 */
public interface IFlatProxy {

  /**
   * Invokes the specified method on the specified url and returns the result as {@link InputStream}
   * .
   *
   * @param url host url, e.g. <code>yahoo.com</code>
   * @param methodName method name, this is actually a script on the server, e.g. <code>search
   *     </code>
   * @param params {@link Map} containing name-value pairs representing method parameters.
   * @return streamed server response
   */
  public InputStream invoke(String url, String methodName, Map<String, Object> params);

  /**
   * Invokes the specified method on the specified url and returns deserializes the result.
   *
   * @param url host url, e.g. <code>yahoo.com</code>
   * @param methodName method name, this is actually a script on the server, e.g. <code>search
   *     </code>
   * @param params {@link Map} containing name-value pairs representing method parameters.
   * @return deserialized server response
   */
  public Object invokeAndDeserialize(String url, String methodName, Map<String, Object> params);
}

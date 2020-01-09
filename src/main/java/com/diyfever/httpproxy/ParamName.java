package com.diyfever.httpproxy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation should be added to all method parameters in proxy interfaces.
 *
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamName {

  /**
   * @return parameter name, this will be used to put together server
   *         request and dynamically create code.
   */
  String value();
}

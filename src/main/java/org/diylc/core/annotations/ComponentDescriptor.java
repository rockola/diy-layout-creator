/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

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

package org.diylc.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.diylc.common.DefaultTransformer;
import org.diylc.common.IComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.parsing.XmlNode;

/**
 * Annotation to use for each {@link IDIYComponent} implementation. Describes component properties,
 * how the component should be represented, and how it interacts with the rest of the system.
 *
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ComponentDescriptor {

  /** @return component type name. */
  String name();

  /** @return component type description. */
  String description();

  /**
   * @return method that should be used to create a component. If <code>
   *      CreationMethod.POINT_BY_POINT</code> is used, user will have to select ending points
   *     before the component is created.
   */
  CreationMethod creationMethod() default CreationMethod.SINGLE_CLICK;

  /** @return component category, e.g. "Passive", "Semiconductors", etc. */
  String category();

  /** @return component author name. */
  String author();

  /**
   * @return prefix that will be used to generate component instance names, e.g. "R" for resistors
   *     or "Q" for transistors.
   */
  String instanceNamePrefix();

  /**
   * String specifyng XML tag from which this component can be unmarshalled. Syntax is either "tag"
   * or "tag:type", corresponding to tag name or tag name and value of 'type' attribute
   * respectively.
   *
   * @return tag string.
   */
  String xmlTag() default XmlNode.NO_TAG;

  /** @return Z-order of the component. */
  double zOrder();

  /** @return true if the component may go beyond it's predefined layer without warning the user. */
  boolean flexibleZOrder() default false;

  /** @return controls what should be shown in the Bill of Materials. */
  BomPolicy bomPolicy() default BomPolicy.SHOW_ALL_NAMES;

  /** @return true if component editor dialog should be shown in Auto-Edit mode, false otherwise. */
  boolean autoEdit() default true;

  /**
   * @return the transformer class that can rotate and/or mirror objects of this class, if one
   *     exists
   */
  Class<? extends IComponentTransformer> transformer() default DefaultTransformer.class;

  /**
   * Defines if and how a component should appear in auto-generated project keywords. See {@link
   * KeywordPolicy} for more info.
   *
   * @return keyword policy.
   */
  KeywordPolicy keywordPolicy() default KeywordPolicy.NEVER_SHOW;

  /**
   * Only used if {@link KeywordPolicy} is set to {@link KeywordPolicy#SHOW_TAG}.
   *
   * @return keyword tag.
   */
  String keywordTag() default "";
}

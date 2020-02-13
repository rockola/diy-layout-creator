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

package org.diylc.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import org.diylc.common.ComponentType;
import org.diylc.components.AbstractComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.KeywordPolicy;

public class KeywordExtractor {

  private static KeywordExtractor instance;

  public static KeywordExtractor getInstance() {
    if (instance == null) {
      instance = new KeywordExtractor();
    }
    return instance;
  }

  private KeywordExtractor() {
    //
  }

  public String extractKeywords(Project project) {
    Set<String> words = new HashSet<String>();
    for (AbstractComponent c : project.getComponents()) {
      ComponentType componentType = ComponentType.extractFrom(c);
      KeywordPolicy policy = componentType.getKeywordPolicy();
      if (policy == KeywordPolicy.SHOW_TYPE_NAME) {
        words.add(componentType.getName().toLowerCase());
      } else {
        if (policy.showValue()
            && c.getValueForDisplay() != null
            && c.getValueForDisplay().trim().length() > 0) {
          words.add(c.getValueForDisplay().trim().toLowerCase());
        }
        if (policy.showTag()
            && componentType.getKeywordTag() != null
            && componentType.getKeywordTag().length() > 0) {
          words.add(componentType.getKeywordTag().trim().toLowerCase());
        }
      }
    }
    StringJoiner sj = new StringJoiner(",");
    for (String word : words) {
      sj.add(word);
    }
    return sj.toString();
  }
}

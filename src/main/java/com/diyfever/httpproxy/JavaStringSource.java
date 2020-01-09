package com.diyfever.httpproxy;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class JavaStringSource extends SimpleJavaFileObject {

  final String code;

  JavaStringSource(String name, String code) {
    super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
    this.code = code;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }
}

package com.diyfever.httpproxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.codehaus.commons.compiler.ISimpleCompiler;
import org.codehaus.janino.CompilerFactory;

/**
 * Utility that dynamically creates web proxy objects.
 * 
 * @see IFlatProxy
 * @author Branislav Stojkovic
 */
public class ProxyFactory {

  private static final Logger LOG = LogManager.getLogger(ProxyFactory.class);

  private IFlatProxy flatProxy;

  /**
   * Creates a new instance of {@link ProxyFactory} for the specified {@link IFlatProxy}. All server
   * calls will be delegated to the specified {@link IFlatProxy}.
   * 
   * @param flatProxy
   */
  public ProxyFactory(IFlatProxy flatProxy) {
    super();
    this.flatProxy = flatProxy;
  }

  /**
   * Dynamically creates proxy class and instatiates it. All methods from the specified interface
   * <code>clazz</code> are implemented in the dynamically created class. Method parameters that are
   * annotated with {@link ParamName} annotation are named according to annotation. Parameters
   * without the annotation are named <code>paramN</code> where <code>N</code> is parameter index.
   * 
   * @param <T>
   * @param clazz
   * @param url
   * @return
   */
  @SuppressWarnings("unchecked")
  public <T> T createProxy(Class<? extends T> clazz, String url) {
    if (!clazz.isInterface()) {
      LOG.error("Specified class is not an interface: " + clazz.getName());
      return null;
    }
    String interfaceName = clazz.getName();
    String newClassName = interfaceName.substring(interfaceName.lastIndexOf(".") + 1) + "Impl";
    try {
      LOG.info("Creating temp class code");
      StringWriter writer = new StringWriter();
      PrintWriter out = new PrintWriter(writer);

      out.println("public class " + newClassName + " implements " + interfaceName + " {\n");
      out.println("  private " + IFlatProxy.class.getName() + " proxy;\n");
      // Generate a constructor
      out.println("  public " + newClassName + "(" + IFlatProxy.class.getName() + " proxy) { this.proxy = proxy; }\n");
      for (Method method : clazz.getDeclaredMethods()) {
        out.print("  public " + method.getReturnType().getName() + " " + method.getName() + "(");
        for (int i = 0; i < method.getParameterTypes().length; i++) {
          if (i > 0) {
            out.print(", ");
          }
          out.print(method.getParameterTypes()[i].getName() + " " + extractParameterName(method, i));
        }
        out.print(") {\n");

        // Put together the parameter map.
        out.println("    java.util.Map<String, Object> params = new java.util.HashMap<String, Object>();");
        for (int i = 0; i < method.getParameterTypes().length; i++) {
          String paramName = extractParameterName(method, i);          
          out.println("    params.put(\"" + paramName + "\", " + paramName + ");");
        }

        // Call the proxy
        String factoryMethodName;
        if (InputStream.class.equals(method.getReturnType())) {
          factoryMethodName = "invoke";
        } else {
          factoryMethodName = "invokeAndDeserialize";
        }
        out.println("    return (" + method.getReturnType().getName() + ") proxy." + factoryMethodName + "(\"" + url
            + "\", \"" + method.getName() + "\", params);");
        out.println("  }\n");
      }
      out.print("}\n");

      String code = writer.toString();

      ISimpleCompiler compiler = new CompilerFactory().newSimpleCompiler();
      compiler.cook(code);
      T instance = (T) compiler.getClassLoader().loadClass(newClassName).getConstructors()[0].newInstance(flatProxy);

      LOG.info("Successfully instantiated proxy");
      return instance;
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Could not create proxy: " + e.getMessage());
    }
    return null;
  }

  private String extractParameterName(Method method, int parameterIndex) {
    Annotation[] annotations = method.getParameterAnnotations()[parameterIndex];
    for (Annotation annotation : annotations) {
      if (annotation instanceof ParamName) {
        return ((ParamName) annotation).value();
      }
    }
    LOG.warn("@" + ParamName.class.getSimpleName() + " annotation not present for method " + method.getName()
        + ", at index " + parameterIndex);
    return "param" + parameterIndex;
  }

  public static File createTempDirectory() throws IOException {
    final File temp;

    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

    if (!(temp.delete())) {
      throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
    }

    if (!(temp.mkdir())) {
      throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
    }

    return (temp);
  }
}

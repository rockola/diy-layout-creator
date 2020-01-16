package com.diyfever.httpproxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.appframework.Serializer;

/**
 * {@link IFlatProxy} implementation for PHP. Method names are interpreted as PHP script names,
 * excluding the ".php" extension. Example below shows how native Java call is translated into PHP
 * call:<br>
 * <br>
 * <code>invoke("yahoo.com", "search", params)</code> <br>
 * <br>
 * This call will query the server using PHP script <code>yahoo.com/search.php</code> and will pass
 * the parameters using POST method.
 *
 * @author Branislav Stojkovic
 */
public class PhpFlatProxy implements IFlatProxy {

  private static final Logger LOG = LogManager.getLogger(PhpFlatProxy.class);
  //private final OkHttpClient httpClient = new OkHttpClient();

  @Override
  public InputStream invoke(String url, String methodName, Map<String, Object> params) {
    InputStream serverInput = null;
    try {
      URL phpUrl = new java.net.URL(createPhpFileName(url, methodName));
      // Call the server.
      /*
      MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        if (entry.getValue() != null) {
          final String key = entry.getKey();
          //if (entry.getValue is not a file
          if (entry.getValue() instanceof File) {
            File file = entry.getValue();
            builder = builder.addFormDataPart(
                key,
                file.getPath(),
                RequestBody.create(XXX, file));
          } else {
            // not a File
            final String value = entry.getValue().toString();
            LOG.trace("invoke({}, {}, params) adding {}={}", url, methodName, key, value);
            builder = builder.addFormDataPart(key, value);
          }
        }
      }
      RequestBody body = builder.build();
      LOG.debug("Connecting to {}", phpUrl);
      Request request = new Request.Builder().url(phpUrl).post(body).build();
      try (Response response = httpClient.newCall(request).execute()) {
        return response.body();
        }*/
      // Flatten params map into an array
      List<Object> paramList = new ArrayList<Object>();
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        if (entry.getValue() != null) {
          paramList.add(entry.getKey());
          paramList.add(entry.getValue());
        }
      }
      serverInput = ClientHttpRequest.post(phpUrl, paramList.toArray());
    } catch (IOException e) {
      LOG.error(e);
      throw new RuntimeException(e);
    }
    return serverInput;
  }

  @Override
  public Object invokeAndDeserialize(String url, String methodName, Map<String, Object> params) {
    InputStream stream = invoke(url, methodName, params);
    if (stream == null) {
      return null;
    }
    // Deserialize the stream
    try {
      Object o = Serializer.fromInputStream(stream);
      stream.close();
      return o;
    } catch (IOException e) {
      LOG.error(e);
    }
    return null;
  }

  private String createPhpFileName(String url, String methodName) {
    if (url.endsWith("/")) {
      return url + methodName + ".php";
    } else {
      return url + "/" + methodName + ".php";
    }
  }
}

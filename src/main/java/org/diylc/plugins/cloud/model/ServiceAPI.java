package org.diylc.plugins.cloud.model;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.App;

/**
   Instance of DIYLC service API proxy created with
   com.diyfever.httpproxy.ProxyFactory at runtime, saved here to avoid
   runtime compilation.
*/
public class ServiceAPI implements IServiceAPI {

  private com.diyfever.httpproxy.IFlatProxy proxy;
  private static final String apiUrl = App.getURL("api.base").toString();

  private Object sendRequest(String requestName, Map<String, Object> params) {
    return (Object) proxy.invokeAndDeserialize(apiUrl, requestName, params);
  }

  public ServiceAPI(com.diyfever.httpproxy.IFlatProxy proxy) {
    this.proxy = proxy;
  }

  // API
  public String createUser(
      String username,
      String password,
      String email,
      String website,
      String bio) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("password", password);
    params.put("email", email);
    params.put("website", website);
    params.put("bio", bio);
    return (String) sendRequest("createUser", params);
  }

  public String updateUserDetails(
      String username,
      String token,
      String machineId,
      String email,
      String website,
      String bio) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    params.put("email", email);
    params.put("website", website);
    params.put("bio", bio);
    return (String) sendRequest("updateUserDetails", params);
  }

  public String login(String username, String password, String machineId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("password", password);
    params.put("machineId", machineId);
    return (String) sendRequest("login", params);
  }

  public Object getUserDetails(String username, String token, String machineId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    return (Object) sendRequest("getUserDetails", params);
  }

  public String loginWithToken(String username, String token, String machineId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    return (String) sendRequest("loginWithToken", params);
  }

  public String updatePassword(String username, String oldPassword, String newPassword) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("oldPassword", oldPassword);
    params.put("newPassword", newPassword);
    return (String) sendRequest("updatePassword", params);
  }

  public Object search(
      String criteria,
      String category,
      Integer page,
      Integer itemsPerPage,
      String sort,
      String username,
      Integer projectId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("criteria", criteria);
    params.put("category", category);
    params.put("page", page);
    params.put("itemsPerPage", itemsPerPage);
    params.put("sort", sort);
    params.put("username", username);
    params.put("projectId", projectId);
    return (Object) sendRequest("search", params);
  }

  public String uploadProject(
      String username,
      String token,
      String machineId,
      String projectName,
      String category,
      String description,
      String diylcVersion,
      String keywords,
      File thumbnail,
      File project,
      Integer projectId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    params.put("projectName", projectName);
    params.put("category", category);
    params.put("description", description);
    params.put("diylcVersion", diylcVersion);
    params.put("keywords", keywords);
    params.put("thumbnail", thumbnail);
    params.put("project", project);
    params.put("projectId", projectId);
    return (String) sendRequest("uploadProject", params);
  }

  public String deleteProject(String username, String token, String machineId, Integer projectId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    params.put("projectId", projectId);
    return (String) sendRequest("deleteProject", params);
  }

  public String postComment(
      String username,
      String token,
      String machineId,
      Integer projectId,
      String comment) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    params.put("projectId", projectId);
    params.put("comment", comment);
    return (String) sendRequest("postComment", params);
  }

  public List<String> getCategories() {
    Map<String, Object> params = new HashMap<String, Object>();
    return (List<String>) sendRequest("getCategories", params);
  }

  public List<String> getSortings() {
    Map<String, Object> params = new HashMap<String, Object>();
    return (List<String>) sendRequest("getSortings", params);
  }

  public Object getComments(int projectId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("projectId", projectId);
    return (Object) sendRequest("getComments", params);
  }
}

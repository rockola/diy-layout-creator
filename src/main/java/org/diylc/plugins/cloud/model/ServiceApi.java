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

package org.diylc.plugins.cloud.model;

import com.diyfever.httpproxy.ParamName;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.diylc.App;

/**
 * Instance of DIYLC service API proxy created with
 * com.diyfever.httpproxy.ProxyFactory at runtime, saved here to avoid
 * runtime compilation.
 *
 * <p>Interface of DIYLC PHP server.
 *
 * <p>Original server located at <code>www.diy-fever.com/diylc/api/v1</code>
*/
public class ServiceApi {

  private com.diyfever.httpproxy.IFlatProxy proxy;
  private static final String apiUrl = App.getUrl("api.base").toString();

  private Object sendRequest(String requestName, Map<String, Object> params) {
    return (Object) proxy.invokeAndDeserialize(apiUrl, requestName, params);
  }

  public ServiceApi(com.diyfever.httpproxy.IFlatProxy proxy) {
    this.proxy = proxy;
  }

  /**
   * Creates a user with the specified details.
   *
   * @param username
   * @param password
   * @param email
   * @param website
   * @param bio
   * @return "Success" if the operation is successful or error message if it failed.
   */
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

  /**
   * * Updates user details using token to authenticate.
   *
   * @param username
   * @param token
   * @param machineId
   * @param email
   * @param website
   * @param bio
   * @return "Success" if the operation is successful or error message if it failed.
   */
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

  /**
   * * Tries to login with the provided credentials and machineId.
   *
   * @param username
   * @param password
   * @param machineId
   * @return login token if the login was successful or string literal "Error" if it failed.
   */
  public String login(String username, String password, String machineId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("password", password);
    params.put("machineId", machineId);
    return (String) sendRequest("login", params);
  }

  /**
   * * Retrieves current user's details.
   *
   * @param username
   * @param password
   * @param machineId
   * @return string with an error message if it fails, or an instance
   *     of {@link UserEntity} if it succeeds.
   */
  public Object getUserDetails(String username, String token, String machineId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    return (Object) sendRequest("getUserDetails", params);
  }

  /**
   * Tries to authenticate with the token that was previously created
   * by calling login() function.
   *
   * @param username
   * @param password
   * @param machineId
   * @return "Success" if the login is successful or error message if it failed.
   */
  public String loginWithToken(String username, String token, String machineId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    return (String) sendRequest("loginWithToken", params);
  }

  /**
   * * Updates password of the current user using token to authenticate.
   *
   * @param username
   * @param token
   * @param machineId
   * @param password
   * @return "Success" if the login is successful or error message if it failed.
   */
  public String updatePassword(String username, String oldPassword, String newPassword) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("oldPassword", oldPassword);
    params.put("newPassword", newPassword);
    return (String) sendRequest("updatePassword", params);
  }

  /**
   * * Searches for projects that meet the specified search criteria. Supports pagination.
   *
   * @param criteria
   * @param category
   * @param page
   * @param itemsPerPage
   * @param sort
   * @param username
   * @param projectId optional parameter used when we want to fetch a
   *        particular project
   * @return string with error message if the error occurred or a list
   *         of {@link ProjectEntity} objects.
   */
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

  /**
   * Uploads a project using token to authenticate. When projectId is
   * specified, it updates the existing project instead. In that case,
   * the project must belong to the user.
   *
   * @param username
   * @param token
   * @param machineId
   * @param projectName
   * @param category
   * @param description
   * @param diylcVersion
   * @param keywords
   * @param thumbnail
   * @param project
   * @param projectId
   * @return "Success" if the upload was successful or error message if it failed.
   */
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

  /**
   * Deletes an existing project. The project must belong to the user.
   *
   * @param username
   * @param token
   * @param machineId
   * @param projectId
   * @return
   */
  public String deleteProject(String username, String token, String machineId, Integer projectId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("username", username);
    params.put("token", token);
    params.put("machineId", machineId);
    params.put("projectId", projectId);
    return (String) sendRequest("deleteProject", params);
  }

  /**
   * Posts a comment to the specified project.
   *
   * @param username
   * @param token
   * @param machineId
   * @param projectId
   * @param comment
   * @return
   */
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

  /**
     @return a {@link List} of available categories.
  */
  public List<String> getCategories() {
    Map<String, Object> params = new HashMap<String, Object>();
    return (List<String>) sendRequest("getCategories", params);
  }

  /**
     @return a {@link List} of available sortings.
  */
  public List<String> getSortings() {
    Map<String, Object> params = new HashMap<String, Object>();
    return (List<String>) sendRequest("getSortings", params);
  }

  /**
   * Returns all available comments for the given projectId.
   *
   * @param projectId
   * @return string with error message if the error occurred or a list of {@link CommentEntity}
   *     objects.
   */
  public Object getComments(int projectId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("projectId", projectId);
    return (Object) sendRequest("getComments", params);
  }
}

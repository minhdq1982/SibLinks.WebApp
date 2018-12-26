<%--
  Created by IntelliJ IDEA.
  User: hieu
  Date: 9/9/15
  Time: 3:55 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/WEB-INF/struts-tags.tld"%>
<%@ taglib uri="http://ckeditor.com" prefix="ckeditor" %>

<%
  String endPointUrl=(String)application.getAttribute("endPointUrl");
  System.out.println("endPointUrl=============jspjsp==--=============--------=" + endPointUrl);
%>  
<script type="text/javascript">
  var endPointUrl = '<%=endPointUrl%>';
</script>    

<script id="list-article" type="text/x-handlebars-template">
    <thead>
    <tr>
        <th style="width: 5%; text-align: center;">arId</th>
        <th style="width: 33%; text-align: center;">Title</th>
        <th style="width: 50%; text-align: center;">Description</th>
        <th style="width: 4%; text-align: center;">Num Comments</th>
        <th style="width: 8%; text-align: center;">Action</th>
    </tr>
    </thead>
    <tbody>
    {{#each articles}}
      <tr>
        <td>{{arId}}</td>
        <td>{{title}}</td>
        <td>{{description}}</td>
        <td>{{numComments}}</td>
        <td>
		      <button  title="Edit Article" onclick="showEditForm(this)" class="editBtn ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" arId = {{arId}} contentEdit = '{{content}}' titleEdit = '{{title}}'>
            <span class="ui-button-icon-primary ui-icon ui-icon-pencil"></span>
          </button>
		      <button onclick="showDeleteForm(this)" class="deleteBtn ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" arId = {{arId}} title="Delete Article">
            <span class="ui-button-icon-primary ui-icon ui-icon-trash"></span>
          </button>
	       </td>
      </tr>
    {{/each}}
    </tbody>
</script>

<script type="text/javascript" src="js/articles.js"></script>

<div>
  <fieldset>
    <legend>Manage Article Admission</legend>
    <div style="">
      <div id="manageVideosTableWrapper">
        <button class="addBtn btnAdd" title="Add Article">Button</button>
        <table id="article-items" class="display">
          
        </table>
      </div>
    </div>
  </fieldset>
</div>

<div id="delete-article-dialog"
  title="Manage Article : Delete" class="displayNone">
  <div class="ui-widget-content" style="padding: 20px;">
  <p>Do you want to delete?</p>
  </div>
</div>

<div id="add-article-dialog" title="Article:" class="displayNone">
  <div class="dialogAlertWrapper" class="displayNone"></div>
  <div class="contentWrapper">
    <table cellpadding="15">
      <thead>
      </thead>
      <tbody>
        <tr>
          <td>Title:<span class="required">*</span></td>
          <td><input class="title" type="text" value="" style="width: 590px" /></td>
        </tr>
        <tr>
          <td colspan="2"> 
            <form id="upload-file-add-form" ng-hide="true">
              Image: <input id="upload-file-input" class="fileUpload" type="file" name="uploadfile" accept="*" />
            </form>
          </td>
        </tr>
        <tr>
          <td>Content:</td>
          <td>
            <textarea class="description" id="txtDescriptionAdd" value=""></textarea>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>

<div id="edit-article-dialog" title="Article Details:" class="displayNone">
  <div class="dialogAlertWrapper" class="displayNone"></div>
  <div class="contentWrapper">
    <table cellpadding="15">
      <thead>
      </thead>
      <tbody>
        <tr>
          <td>Title:<span class="required">*</span></td>
          <td><input class="title" type="text" value="" style="width: 590px" /></td>
        </tr>
        <tr>
          <td colspan="2">Image: 
            <form id="upload-file-form" ng-hide="true">
              <input id="upload-file-input" class="fileUpload" type="file" name="uploadfile" accept="*" />
            </form>
          </td>
        </tr>
        <tr>
          <td>Content:</td>
          <td>
            <textarea class="description" id="contentEdit" value=""></textarea>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
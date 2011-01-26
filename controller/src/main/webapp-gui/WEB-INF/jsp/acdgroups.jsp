<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>ACD Groups</title>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-acdgroups-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/lib/forms.css"/>">
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/acdgroups-min.css"/>">
    <meta name="body-class" content="application-acdgroups"/>
    <meta name="page-title" content="ACD Groups"/>
  </head>
  <body>
    <div class="help">
      ACD Groups are collections of system subscribers that are grouped together by a specific trait.  
      These groups are utilized to select the next available member of the group to handle an incoming call.
    </div>
    
    <form id="acd-groups-form">
	  <fieldset>
	    <div class="group-buttons">
          <button type="button" class="button-add" id="acdgroups-add-group">Add Group</button>
    	</div>
    	<div class="save-button-container" id="acd-groups-form-save-button-div">
      	  <button type="button" class="button-save" id="acdgroupsSave">Save</button>
    	</div>
	  </fieldset>
	</form>
    
    <div class="templates">
      <div class="clearfix" id="acdGroupTemplate">
        <fieldset>
        
          <label>
            Group Name
            <input type="text" class="groupName"/>
          </label>
          
          <label>
            <button type="button" class="acdGroupDelete button-delete">Delete Group</button>
          </label>
          
          <fieldset class="side-by-side">
            
            <label>
              Subscriber
              <select class="acdMemberSelect"></select>
            </label>
            
            <label>
              Administrator
              <input class="acdGroupMemberIsAdmin" type="checkbox"/>
            </label>
            
            <label>
              <button type="button" class="acdGroupAddMemberButton icon-add">
            </label>
          
          </fieldset>
        </fieldset>
        <ul class="acdGroupMemberList">
        </ul>
      </div>
      
      <ul>
        <li id="acdGroupMemberTemplate" class="acdGroupMember">
          <label>
            <button type="button" class="acdGroupMemberRemove icon-delete"></button>
          </label>
          <label class="groupMemberName"></label>
        </li>
      </ul>
    </div>
  </body>
</html>
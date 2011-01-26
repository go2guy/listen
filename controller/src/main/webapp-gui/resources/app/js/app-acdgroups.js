var interact = interact || {};
var ACDGroups;
$(document).ready(function() {

    $("#acdgroups-add-group").click(function() {
        ACDGroups.loadNewGroup();
    });
    
    $(".acdGroupMemberRemove").click(function (e) {
		$(e.target).parent().parent().remove();
	});
	
	$("#acdgroupsSave").click(function (e) {
		ACDGroups.saveGroups();
	});
	
	$(".acdGroupDelete").click(function (e) {
		$(e.target).parent().parent().parent().remove();
	});

    ACDGroups = function() {
    	var eligibleMembers = [];
    	
        return {
            Application: function() {
                this.load = function() {
                    interact.util.trace('Loading ACD Groups');
                    
                    $.ajax({
                        url: interact.listen.url('/ajax/getAcdConfiguration'),
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                        
                        	eligibleMembers = data.eligible;
                        	ACDGroups.addSelectOptions(eligibleMembers);
                        	console.log(data);

                            for(var i =0, group; group = data.groups[i]; i++) {
                            	ACDGroups.loadGroup(group);	
                            }
                        }
                    });
                };
            },

            loadNewGroup: function() {
                var clone = $("#acdGroupTemplate").clone(true);
                clone.removeAttr("id");
                clone.addClass("acdGroup");
                
                $(".acdGroupAddMemberButton", clone).click(function() {
                	ACDGroups.addNewMember(clone);
                });
                
                $('#acd-groups-form-save-button-div').before(clone);
            },
            
            loadGroup: function(group) {
            	var clone = $("#acdGroupTemplate").clone(true);
                clone.removeAttr("id");
                clone.addClass("acdGroup");
                
                $(".acdGroupAddMemberButton", clone).click(function() {
                	ACDGroups.addNewMember(clone);
                });
                
                for(var i = 0, member; member = group.members[i]; i++) {
                	ACDGroups.addMember(member, clone);
                }
                
                $(".groupName", clone).val(group.name);
                $('#acd-groups-form-save-button-div').before(clone);
            },
            
            addSelectOptions: function(eligibleMembers) {
            	var template = $("#acdGroupTemplate");
            	var templateSelect = $("select", template);
            	
            	var options = templateSelect.attr('options');
                $('option', templateSelect).remove();
            	
            	for(var i=0; i < eligibleMembers.length; i++) {
            		options[options.length] = new Option(eligibleMembers[i].name, eligibleMembers[i].id);
            	}
            },
            
            addNewMember: function(group) {
            	var clone = $("#acdGroupMemberTemplate").clone(true);
            	clone.removeAttr('id');
              
            	var selectedName = $(".acdMemberSelect option:selected", group).text();
            	$(".groupMemberName", clone).text(selectedName);
            	
            	if($(".acdGroupMemberIsAdmin", group).attr("checked")) {
            		clone.addClass("acdGroupAdmin");
            	}
            	
            	$(".acdGroupMemberList", group).append(clone);
            	
            	//Un-check the isAdmin checkbox
            	$(".acdGroupMemberIsAdmin", group).attr('checked', false);
            },
            
            addMember: function(member, group) {
            	var clone = $("#acdGroupMemberTemplate").clone(true);
            	clone.removeAttr('id');
            	
            	$(".groupMemberName", clone).text(ACDGroups.findMemberNameById(member.id));
            	
            	if(member.isAdministrator) {
            		clone.addClass("acdGroupAdmin");
            	}
            	
            	$(".acdGroupMemberList", group).append(clone);
            },
            
            findMemberIdByName: function(name) {
            	for(var i = 0; i < eligibleMembers.length; i++) {
            		if(name === eligibleMembers[i].name)
            		{
            			console.log("returning " + eligibleMembers[i].id);
            			return eligibleMembers[i].id;
            		}
            	}
            },
            
            findMemberNameById: function(id) {
            	for(var i = 0; i < eligibleMembers.length; i++) {
            		if(id === eligibleMembers[i].id)
            		{
            			return eligibleMembers[i].name;
            		}
            	}
            },
            
            buildObjectFromMarkup: function() {
                var groups = [];
                $('.acdGroup').each(function(i, it) {
                	var groupName = $(".groupName", it).val();
                    interact.util.trace('GROUP ' + groupName);
                    
                    var members = [];
                    
                    $(".acdGroupMember", it).each(function(j, member) {
                    	var entry = {
                            id: ACDGroups.findMemberIdByName($(".groupMemberName", member).text()),
                            isAdministrator: $(member).hasClass("acdGroupAdmin")
                        };
                        
                        members.push(entry);
                    });
                    
                    var group = {
                    	name: groupName,
                    	members: members
                    };
                    
                    groups.push(group);
                });
                return groups;
            },
            
            saveGroups: function() {
            	var saveButton = $('#acdgroupsSave');
                saveButton.attr('readonly', 'readonly').attr('disabled', 'disabled');
                
                var acdgroups = ACDGroups.buildObjectFromMarkup();
                console.log(JSON.stringify(acdgroups));
                Server.post({
                    url: interact.listen.url('/ajax/saveAcdConfiguration'),
                    properties: {
                        groups: JSON.stringify(acdgroups)
                    },
                    successCallback: function(data, textStatus, xhr) {
                        saveButton.removeAttr('readonly').removeAttr('disabled');
                        interact.listen.notifySuccess('ACD Groups configuration saved');
                    },
                    errorCallback: function(message) {
                        saveButton.removeAttr('readonly').removeAttr('disabled');
                        interact.listen.notifyError(message);
                    }
                });
            }
        }
    }();

    new ACDGroups.Application().load();
});
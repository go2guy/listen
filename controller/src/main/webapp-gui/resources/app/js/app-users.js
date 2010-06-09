$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('users', 'users-application', 'menu-users', new UsersApplication()));

    function UsersApplication() {
        var interval;
        var dynamicTable = new LISTEN.DynamicTable({
            tableId: 'users-table',
            templateId: 'user-row-template',
            retrieveList: function(data) {
                return data;
            },
            updateRowCallback: function(row, data) {
                var usernameCell = row.find('.user-cell-username');
                if(usernameCell.text() != data.username) {
                    usernameCell.text(data.username);
                }

                var subscriberCell = row.find('.user-cell-subscriber');
                if(subscriberCell.text() != data.subscriber) {
                    subscriberCell.text(data.subscriber);
                }

                var lastLoginCell = row.find('.user-cell-lastLogin');
                if(lastLoginCell.text() != data.lastLogin) {
                    lastLoginCell.text(data.lastLogin);
                }
            }
        });

        var pollAndSet = function() {
            $.ajax({
                url: '/ajax/getUserList',
                dataType: 'json',
                cache: 'false',
                success: function(data, textStatus, xhr) {
                    dynamicTable.update(data);
                }
            });
        };

        this.load = function() {
            LISTEN.log('Loading users');
            pollAndSet();
            interval = setInterval(function() {
                pollAndSet();
            }, 1000);
        };

        this.unload = function() {
            LISTEN.log('Unloading users');
            if(interval) {
                clearInterval(interval);
            }
        };
    }
});
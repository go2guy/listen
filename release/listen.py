#!/usr/bin/env python
import interface
import os

class listenAction(interface.Action):
    INCLUDED_PACKAGES = [
        interface.Package('all',
            [interface.Process('httpd',
                               '0',
                               statusCmd='/sbin/service httpd status',
                               statusSource=interface.Process.RETVAL_STATUS_SOURCE,
                               stopCmd='/sbin/service httpd stop',
                               startCmd='/sbin/service httpd start'),

             interface.Process('vipStart',
                               '/interact/program/iiMoap',
                               stopCmd='/sbin/service vipStart stop',
                               startCmd='/sbin/service vipStart start'),

             interface.Process('listen-controller',
                               '/interact/listen/lib/listen-controller.war',
                               stopCmd='/sbin/service listen-controller stop',
                               startCmd='/sbin/service listen-controller start'),

             interface.Process('collector',
                               '/interact/collector/bin/collector',
                               stopCmd='/sbin/service collector stop',
                               startCmd='/sbin/service collector start'),

             interface.Process('mysql',
                               '/usr/lib.*/mysqld ',
                               startCmd='/sbin/service mysqld start',
                               desiredState=interface.Process.RUNNING_STATE)])]

    def __init__(self, masterpkg, uiapkg):
        super(listenAction, self).__init__(masterpkg, uiapkg)


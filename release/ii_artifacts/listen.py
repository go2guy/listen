#!/usr/bin/env python
import interface

class listenAction(interface.Action):
    INCLUDED_PACKAGES = [
        interface.Package('all',
            [interface.Process('httpd',
                               '0',
                               statusCmd='/sbin/service httpd status',
                               statusSource=Process.RETURN_CODE_KEY,
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
                               '^[^\s]*/mysqld ',
                               stopCmd='/sbin/service mysqld stop',
                               startCmd='/sbin/service mysqld start',
                               statusState=interface.Process.STATE_RUNNING)])]

    def preinstall(cls, packages, test):
        if not os.path.isdir('/var/lib/mysql/listen2'):
            cls.run('mysql --user=root -e "create database listen2"')

        if not os.path.isdir('/var/lib/mysql/ip_pbx'):
            cls.run('mysql --user=root < /interact/apps/spotbuild/ippbx/sql/ippbx_schema.sql')
    preinstall = classmethod(preinstall)

